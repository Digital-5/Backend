package com.digital5.service;

import com.digital5.entity.AccountEntity;
import com.digital5.entity.PublicKeysEntity;
import com.digital5.repository.KeysRepository;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.sql.Date;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class JWTService {

    private static final long JWT_EXPIRY_TIME = 1;
    private static final ChronoUnit JWT_EXPIRY_UNIT = ChronoUnit.DAYS;

    private AccountService accountService;
    private PublicKeyService publicKeyService;
    /*
    public boolean verifyJWT(String token) {
        try {
            String[] splitToken = token.split("\\.");
            if (splitToken.length != 3) {
                return false;
            }
            if (validateHeader(splitToken[0]) &&
            String uuid = validatePayload(splitToken[1], splitToken[2]);
            if (uuid == null) {
                return false;
            }
            publicKeyService.verifySignature(uuid, splitToken[0]+"."+splitToken[1], splitToken[2]);

            return true;
        } catch (Exception e) {
            return false;
        }
    }
    */

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

    private String validatePayload(String payload, String signature) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode root = mapper.readTree(payload);
            assert root.has("sub");
            AccountEntity account = accountService.getUserByUUID(root.get("sub").asString());
            assert root.has("iat");
            Date issuedAt = (Date) Date.from(Instant.ofEpochSecond(root.get("iat").asLong()));
            assert issuedAt.before(Date.from(Instant.now()));
            assert root.has("exp");
            Date expiresAt = (Date) Date.from(Instant.ofEpochSecond(root.get("exp").asLong()));
            assert expiresAt.after(Date.from(Instant.now().plus(JWT_EXPIRY_TIME, JWT_EXPIRY_UNIT)));
            return account.getUuid();
        } catch (JacksonException e) {
            return null;
        }
    }

}
