package com.digital5.service;

import com.digital5.entity.AccountEntity;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.sql.Date;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Service for verifying XEdDSA-signed JWTs.
 * <p>
 * JWT format: base64url(header).base64url(payload).hex(signature)
 * The signature covers the ASCII bytes of "base64url(header).base64url(payload)".
 */
@Service
@AllArgsConstructor
public class JWTService {

    private static final long JWT_MAX_AGE = 1;
    private static final ChronoUnit JWT_MAX_AGE_UNIT = ChronoUnit.DAYS;
    private static final String EXPECTED_ALGORITHM = "XEdDSA";
    private static final String EXPECTED_TYPE = "JWT";

    private final ObjectMapper objectMapper;
    private final AccountService accountService;
    private final PublicKeyService publicKeyService;

    public JWTService(AccountService accountService, PublicKeyService publicKeyService) {
        this.objectMapper = new ObjectMapper();
        this.accountService = accountService;
        this.publicKeyService = publicKeyService;
    }

    /**
     * Verifies a JWT token: validates structure, header, payload timestamps,
     * and XEdDSA signature against the user's stored identity key.
     *
     * @param token the full JWT string (header.payload.signature)
     * @return the UUID of the authenticated user, or null if verification fails
     */
    public String verifyJWT(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                return null;
            }

            if (!validateHeader(parts[0])) {
                return null;
            }

            String uuid = validatePayload(parts[1]);
            if (uuid == null) {
                return null;
            }

            // Signature covers "header.payload" as raw ASCII bytes
            String signedData = parts[0] + "." + parts[1];
            if (!publicKeyService.verifySignature(uuid, signedData, parts[2])) {
                return null;
            }

            return uuid;
        } catch (Exception e) {
            return null;
        }
    }

    private boolean validateHeader(String headerBase64url) {
        try {
            String headerJson = decodeBase64url(headerBase64url);
            JsonNode root = objectMapper.readTree(headerJson);

            String alg = root.path("alg").asString();
            if (!EXPECTED_ALGORITHM.equals(alg)) {
                return false;
            }

            String typ = root.path("typ").asString();
            if (!EXPECTED_TYPE.equals(typ)) {
                return false;
            }

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private String validatePayload(String payloadBase64url) {
        try {
            String payloadJson = decodeBase64url(payloadBase64url);
            JsonNode root = objectMapper.readTree(payloadJson);

            if (!root.has("sub")) {
                return null;
            }
            String uuid = root.get("sub").asString();

            AccountEntity account = accountService.getUserByUUID(uuid);
            if (account == null) {
                return null;
            }

            Instant now = Instant.now();

            // Validate iat (issued at): must be present and in the past
            if (!root.has("iat")) {
                return null;
            }
            Instant issuedAt = Instant.ofEpochSecond(root.get("iat").asLong());
            if (issuedAt.isAfter(now)) {
                return null;
            }

            // Validate exp (expires at): must be present and in the future
            if (!root.has("exp")) {
                return null;
            }
            Instant expiresAt = Instant.ofEpochSecond(root.get("exp").asLong());
            if (expiresAt.isBefore(now)) {
                return null;
            }

            // Reject tokens with unreasonably long validity
            Instant maxExpiry = now.plus(JWT_MAX_AGE, JWT_MAX_AGE_UNIT);
            if (expiresAt.isAfter(maxExpiry)) {
                return null;
            }

            return account.getUuid();
        } catch (Exception e) {
            return null;
        }
    }

}
