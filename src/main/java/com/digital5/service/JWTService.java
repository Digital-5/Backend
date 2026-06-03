package com.digital5.service;

import com.digital5.entity.AccountEntity;
import com.digital5.exception.DigitalException;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.sql.Date;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@AllArgsConstructor
public class JWTService {

    private static final long JWT_EXPIRY_TIME = 1;
    private static final ChronoUnit JWT_EXPIRY_UNIT = ChronoUnit.DAYS;

    private AccountService accountService;
    private PublicKeyService publicKeyService;

    public AccountEntity verifyJWT(String token) throws DigitalException {
        String[] splitToken = token.split("\\.");
        if (splitToken.length != 3) {
            return null;
        }
        if (validateHeader(splitToken[0])) {
            String uuid = validatePayload(splitToken[1]);
            AccountEntity account = accountService.getUserFromUUID(uuid);
            if (account == null) {
                return null;
            }
            boolean validSignature = validateSignature(account, splitToken[0] + "." + splitToken[1], splitToken[2]);
            if (validSignature) {
                return account;
            }

        }
        return null;
    }

    private boolean validateHeader(String header) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode root = mapper.readTree(header);
            String signingAlgorithm = root.get("alg").asString();
            assert signingAlgorithm.equals("XEdDSA");
            String type = root.get("typ").asString();
            assert type.equals("JWT");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private String validatePayload(String payload) throws DigitalException {
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode root = mapper.readTree(payload);
            assert root.has("sub");
            AccountEntity account = accountService.getUserFromUUID(root.get("sub").asString());
            assert root.has("iat");
            Date issuedAt = (Date) Date.from(Instant.ofEpochSecond(root.get("iat").asLong()));
            assert issuedAt.before(Date.from(Instant.now()));
            assert root.has("exp");
            Date expiresAt = (Date) Date.from(Instant.ofEpochSecond(root.get("exp").asLong()));
            assert expiresAt.after(Date.from(Instant.now().plus(JWT_EXPIRY_TIME, JWT_EXPIRY_UNIT)));
            return account.getUuid();
        } catch (JacksonException e) {
            throw new DigitalException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not parse JWT");
        } catch (AssertionError e) {
            throw new DigitalException(HttpStatus.BAD_REQUEST, "Invalid Authentication");
        }
    }

    private boolean validateSignature(AccountEntity account, String toSign, String signature) throws DigitalException {
        return publicKeyService.verifySignature(account, toSign, signature);
    }

}
