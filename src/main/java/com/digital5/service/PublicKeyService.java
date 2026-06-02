package com.digital5.service;

import com.digital5.crypto.xeddsa.XEdDsaVerifier;
import com.digital5.data.models.RegisterModel;
import com.digital5.entity.PublicKeysEntity;
import com.digital5.repository.KeysRepository;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Service for managing public keys and verifying XEdDSA signatures.
 */
@Service
public class PublicKeyService {

    private final KeysRepository keysRepository;
    private final XEdDsaVerifier xEdDsaVerifier;

    public PublicKeyService(KeysRepository keysRepository, XEdDsaVerifier xEdDsaVerifier) {
        this.keysRepository = keysRepository;
        this.xEdDsaVerifier = xEdDsaVerifier;
    }

    public void registerPublicKeys(RegisterModel publishKeysModel) {
        // TODO: implement key bundle registration with signature validation
    }

    /**
     * Verifies an XEdDSA signature against a user's stored identity key.
     *
     * @param uuid      the user's UUID (used to look up the identity key)
     * @param data      the signed data (UTF-8 string)
     * @param signature the hex-encoded XEdDSA signature (64 bytes = 128 hex chars)
     * @return true if the signature is valid
     */
    public boolean verifySignature(String uuid, String data, String signature) {
        if (uuid == null || data == null || signature == null) {
            return false;
        }

        PublicKeysEntity keys = keysRepository.findById(uuid).orElse(null);
        if (keys == null) {
            return false;
        }

        byte[] publicKeyBytes = null;
        byte[] signatureBytes = null;
        try {
            publicKeyBytes = hexToBytes(keys.getIdentityKey());
            byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
            signatureBytes = hexToBytes(signature);

            return xEdDsaVerifier.verify(publicKeyBytes, dataBytes, signatureBytes); //todo too many layers of verifiers
        } catch (Exception e) {
            return false;
        } finally {
            // Zeroize key material after use
            if (publicKeyBytes != null) {
                Arrays.fill(publicKeyBytes, (byte) 0);
            }
            if (signatureBytes != null) {
                Arrays.fill(signatureBytes, (byte) 0);
            }
        }
    }

    private static byte[] hexToBytes(String hex) {
        if (hex == null || hex.length() % 2 != 0) {
            throw new IllegalArgumentException("Invalid hex string");
        }
        byte[] bytes = new byte[hex.length() / 2];
        for (int i = 0; i < hex.length(); i += 2) {
            bytes[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return bytes;
    }
}
