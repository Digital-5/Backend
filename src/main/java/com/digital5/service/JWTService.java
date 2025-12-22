package com.digital5.service;

import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Service
public class JWTService {

    public boolean verifyJWT(String token) {
        try {
            String[] splitToken = token.split("\\.");
            if (splitToken.length != 3) {
                return false;
            }
            assert validateHeader(splitToken[0]);
            assert validatePayload(splitToken[1]);
            // TODO: validate signature with public key from db
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean validateHeader(String json) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode root = mapper.readTree(json);
            String signingAlgorithm = root.get("alg").asString();
            assert signingAlgorithm.equals("XEdDSA");
            String type = root.get("typ").asString();
            assert type.equals("JWT");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean validatePayload(String json) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode root = mapper.readTree(json);
            assert root.has("sub");
            // Check for a username, lookup public key in db to verify signature
            assert root.has("iat");
            // iat can't be in the future
            assert root.has("exp");
            // exp must be in the future, difference between iat and exp must be ~15m?
            return true;
        } catch (JacksonException e) {
            return false;
        }
    }

}
