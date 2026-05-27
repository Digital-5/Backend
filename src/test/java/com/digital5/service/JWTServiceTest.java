package com.digital5.service;

import com.digital5.entity.AccountEntity;
import com.digital5.exception.DigitalException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JWTServiceTest {

    @Mock
    private AccountService accountService;

    @Mock
    private PublicKeyService publicKeyService;

    private JWTService jwtService;

    private static final String TEST_UUID = "550e8400-e29b-41d4-a716-446655440000";

    @BeforeEach
    void setUp() {
        jwtService = new JWTService(accountService, publicKeyService);
    }

    // --- Helper methods ---

    private String buildJwt(String headerJson, String payloadJson, String signature) {
        String header = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(headerJson.getBytes(StandardCharsets.UTF_8));
        String payload = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(payloadJson.getBytes(StandardCharsets.UTF_8));
        return header + "." + payload + "." + signature;
    }

    private String validHeader() {
        return "{\"alg\":\"XEdDSA\",\"typ\":\"JWT\"}";
    }

    private String validPayload() {
        long now = Instant.now().getEpochSecond();
        long exp = Instant.now().plusSeconds(3600).getEpochSecond();
        return "{\"sub\":\"" + TEST_UUID + "\",\"iat\":" + now + ",\"exp\":" + exp + "}";
    }

    private void mockAccountExists() {
        AccountEntity account = new AccountEntity();
        account.setUuid(TEST_UUID);
        account.setUsername("testuser");
        when(accountService.getUserFromUUID(TEST_UUID)).thenReturn(account);
    }

    private void mockSignatureValid() {
        when(publicKeyService.verifySignature(eq(TEST_UUID), anyString(), anyString()))
                .thenReturn(true);
    }

    // --- Tests ---

    @Test
    void verifyJWT_validToken_returnsUuid() throws Exception {
        mockAccountExists();
        mockSignatureValid();

        String token = buildJwt(validHeader(), validPayload(), "aabbccdd");
        String result = jwtService.verifyJWT(token);

        assertEquals(TEST_UUID, result);
    }

    @Test
    void verifyJWT_null_throwsException() {
        assertThrows(DigitalException.class, () -> jwtService.verifyJWT(null));
    }

    @Test
    void verifyJWT_empty_throwsException() {
        assertThrows(DigitalException.class, () -> jwtService.verifyJWT(""));
    }

    @Test
    void verifyJWT_wrongPartCount_throwsException() {
        assertThrows(DigitalException.class, () -> jwtService.verifyJWT("only.two"));
        assertThrows(DigitalException.class, () -> jwtService.verifyJWT("a.b.c.d"));
    }

    @Test
    void verifyJWT_wrongAlgorithm_throwsException() {
        String header = "{\"alg\":\"RS256\",\"typ\":\"JWT\"}";
        String token = buildJwt(header, validPayload(), "sig");
        assertThrows(DigitalException.class, () -> jwtService.verifyJWT(token));
    }

    @Test
    void verifyJWT_wrongType_throwsException() {
        String header = "{\"alg\":\"XEdDSA\",\"typ\":\"JWS\"}";
        String token = buildJwt(header, validPayload(), "sig");
        assertThrows(DigitalException.class, () -> jwtService.verifyJWT(token));
    }

    @Test
    void verifyJWT_missingSubject_throwsException() {
        long now = Instant.now().getEpochSecond();
        String payload = "{\"iat\":" + now + ",\"exp\":" + (now + 3600) + "}";
        String token = buildJwt(validHeader(), payload, "sig");
        assertThrows(DigitalException.class, () -> jwtService.verifyJWT(token));
    }

    @Test
    void verifyJWT_unknownUser_throwsException() {
        when(accountService.getUserFromUUID(TEST_UUID)).thenReturn(null);

        String token = buildJwt(validHeader(), validPayload(), "sig");
        assertThrows(DigitalException.class, () -> jwtService.verifyJWT(token));
    }

    @Test
    void verifyJWT_expiredToken_throwsException() {
        mockAccountExists();

        long past = Instant.now().minusSeconds(7200).getEpochSecond();
        long expired = Instant.now().minusSeconds(3600).getEpochSecond();
        String payload = "{\"sub\":\"" + TEST_UUID + "\",\"iat\":" + past + ",\"exp\":" + expired + "}";

        String token = buildJwt(validHeader(), payload, "sig");
        assertThrows(DigitalException.class, () -> jwtService.verifyJWT(token));
    }

    @Test
    void verifyJWT_futureIssuedAt_throwsException() {
        mockAccountExists();

        long future = Instant.now().plusSeconds(3600).getEpochSecond();
        long exp = Instant.now().plusSeconds(7200).getEpochSecond();
        String payload = "{\"sub\":\"" + TEST_UUID + "\",\"iat\":" + future + ",\"exp\":" + exp + "}";

        String token = buildJwt(validHeader(), payload, "sig");
        assertThrows(DigitalException.class, () -> jwtService.verifyJWT(token));
    }

    @Test
    void verifyJWT_expiryTooFarInFuture_throwsException() {
        mockAccountExists();

        long now = Instant.now().getEpochSecond();
        // Expires in 2 days (exceeds 1 day max)
        long exp = Instant.now().plusSeconds(2 * 86400).getEpochSecond();
        String payload = "{\"sub\":\"" + TEST_UUID + "\",\"iat\":" + now + ",\"exp\":" + exp + "}";

        String token = buildJwt(validHeader(), payload, "sig");
        assertThrows(DigitalException.class, () -> jwtService.verifyJWT(token));
    }

    @Test
    void verifyJWT_invalidSignature_throwsException() {
        mockAccountExists();
        when(publicKeyService.verifySignature(eq(TEST_UUID), anyString(), anyString()))
                .thenReturn(false);

        String token = buildJwt(validHeader(), validPayload(), "badsig");
        assertThrows(DigitalException.class, () -> jwtService.verifyJWT(token));
    }

    @Test
    void verifyJWT_headerNotBase64_throwsException() {
        String token = "not-base64!." +
                Base64.getUrlEncoder().withoutPadding()
                        .encodeToString(validPayload().getBytes(StandardCharsets.UTF_8)) +
                ".sig";
        assertThrows(DigitalException.class, () -> jwtService.verifyJWT(token));
    }

    @Test
    void verifyJWT_missingIat_throwsException() {
        mockAccountExists();

        long exp = Instant.now().plusSeconds(3600).getEpochSecond();
        String payload = "{\"sub\":\"" + TEST_UUID + "\",\"exp\":" + exp + "}";

        String token = buildJwt(validHeader(), payload, "sig");
        assertThrows(DigitalException.class, () -> jwtService.verifyJWT(token));
    }

    @Test
    void verifyJWT_missingExp_throwsException() {
        mockAccountExists();

        long now = Instant.now().getEpochSecond();
        String payload = "{\"sub\":\"" + TEST_UUID + "\",\"iat\":" + now + "}";

        String token = buildJwt(validHeader(), payload, "sig");
        assertThrows(DigitalException.class, () -> jwtService.verifyJWT(token));
    }
}

