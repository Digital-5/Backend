package com.digital5.service;

import com.digital5.entity.AccountEntity;
import com.digital5.exception.DigitalException;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;

/**
 * Service for verifying XEdDSA-signed JWTs.
 * <p>
 * JWT format: base64url(header).base64url(payload).hex(signature)
 * The signature covers the ASCII bytes of "base64url(header).base64url(payload)".
 */
@Service
public class JWTService {

    private static final long JWT_MAX_AGE = 1;
    private static final ChronoUnit JWT_MAX_AGE_UNIT = ChronoUnit.DAYS;
    private static final String EXPECTED_ALGORITHM = "XEdDSA";
    private static final String EXPECTED_TYPE = "JWT";

    private final ObjectMapper objectMapper;
    private final AccountService accountService;
    private final PublicKeyService publicKeyService;

    public JWTService(@Lazy AccountService accountService, PublicKeyService publicKeyService) {
        this.objectMapper = new ObjectMapper();
        this.accountService = accountService;
        this.publicKeyService = publicKeyService;
    }

    private static String decodeBase64url(String base64url) {
        byte[] decoded = Base64.getUrlDecoder().decode(base64url);
        return new String(decoded, StandardCharsets.UTF_8);
    }

    /**
     * Verifies a JWT token: validates structure, header, payload timestamps,
     * and XEdDSA signature against the user's stored identity key.
     *
     * @param token the full JWT string (header.payload.signature)
     * @return the UUID of the authenticated user, or null if verification fails
     */
    public String verifyJWT(String token) throws DigitalException {
        try {
            if (token == null || token.isBlank()) {
                throw new DigitalException(HttpStatus.BAD_REQUEST, "JWT token is missing.");
            }

            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                throw new DigitalException(HttpStatus.BAD_REQUEST, "Invalid JWT token format.");
            }

            // 1. Validate header structure (cheap, no I/O)
            if (!validateHeader(parts[0])) {
                throw new DigitalException(HttpStatus.BAD_REQUEST, "Invalid JWT token format.");
            }

            // 2. Extract subject from payload without DB lookup
            String payloadJson = decodeBase64url(parts[1]);
            JsonNode payloadRoot = objectMapper.readTree(payloadJson);
            if (!payloadRoot.has("sub")) {
                throw new DigitalException(HttpStatus.BAD_REQUEST, "Invalid JWT token format.");
            }
            String uuid = payloadRoot.get("sub").asString();

            // 3. Verify signature before hitting the DB (prevents DoS via forged tokens)
            String signedData = parts[0] + "." + parts[1];
            if (uuid == null || !publicKeyService.verifySignature(uuid, signedData, parts[2])) {
                throw new DigitalException(HttpStatus.BAD_REQUEST, "Invalid JWT token format.");
            }

            // 4. Validate payload claims (DB lookup + timestamp checks)
            String validatedUuid = validatePayload(parts[1]);
            if (validatedUuid == null) {
                throw new DigitalException(HttpStatus.BAD_REQUEST, "Invalid JWT token format.");
            }

            return validatedUuid;
        } catch (DigitalException e) {
            throw e;
        } catch (IllegalArgumentException e) {
            throw new DigitalException(HttpStatus.BAD_REQUEST, "Invalid JWT token.");
        }
    }

    private boolean validateHeader(String headerBase64url) {
        try {
            String headerJson = decodeBase64url(headerBase64url);
            JsonNode root = objectMapper.readTree(headerJson);

            String alg = root.path("alg").asString();
            String typ = root.path("typ").asString();

            return EXPECTED_TYPE.equals(typ) && EXPECTED_ALGORITHM.equals(alg);

        } catch (Exception e) {
            return false;
        }
    }

    private String validatePayload(String payloadBase64url) throws DigitalException {
        try {
            String payloadJson = decodeBase64url(payloadBase64url);
            JsonNode root = objectMapper.readTree(payloadJson);

            if (!root.has("sub")) {
                return null;
            }
            String uuid = root.get("sub").asString();

            AccountEntity account = accountService.getUserFromUUID(uuid);
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
          //todo check if everything was catched
        } catch (JacksonException e) {
            throw new DigitalException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not parse JWT");
        } catch (AssertionError e) {
            throw new DigitalException(HttpStatus.BAD_REQUEST, "Invalid Authentication");
        }
    }

}
