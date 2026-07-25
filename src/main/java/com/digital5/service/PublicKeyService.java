package com.digital5.service;

import com.digital5.crypto.xeddsa.XEdDsaVerifier;
import com.digital5.data.models.RegisterModel;
import com.digital5.entity.PublicKeysEntity;
import com.digital5.exception.DigitalException;
import com.digital5.repository.KeysRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * Service for managing public keys and verifying XEdDSA signatures.
 */
@Service
@AllArgsConstructor
public class PublicKeyService {

    private static final Pattern HEX_PATTERN = Pattern.compile("[0-9a-fA-F]+");
    private static final int IDENTITY_KEY_BYTES = 32;
    private static final int PREKEY_BYTES = 32;
    private static final int KEM_KEY_BYTES = 1568;
    private static final int SIGNATURE_BYTES = 64;

    private final KeysRepository keysRepository;
    private final XEdDsaVerifier xEdDsaVerifier;

    @Transactional
    public void registerPublicKeys(RegisterModel registerModel, String uuid) throws DigitalException {
        validateKeyFormats(registerModel);
        verifyKeySignatures(registerModel);

        PublicKeysEntity publicKeys = new PublicKeysEntity(
                uuid,
                registerModel.getIdentityKey(),
                registerModel.getPreKey(),
                registerModel.getPreKeySignature(),
                registerModel.getKemKey(),
                registerModel.getKeyKemSignature()
        );
        keysRepository.save(publicKeys);
    }

    private void validateKeyFormats(RegisterModel model) throws DigitalException {
        validateHexField(model.getIdentityKey(), "identityKey", IDENTITY_KEY_BYTES);
        validateHexField(model.getPreKey(), "preKey", PREKEY_BYTES);
        validateHexField(model.getPreKeySignature(), "preKeySignature", SIGNATURE_BYTES);
        validateHexField(model.getKemKey(), "kemKey", KEM_KEY_BYTES);
        validateHexField(model.getKeyKemSignature(), "keyKemSignature", SIGNATURE_BYTES);
    }

    private void validateHexField(String value, String fieldName, int expectedBytes) throws DigitalException {
        if (value == null || value.isEmpty()) {
            throw new DigitalException(HttpStatus.BAD_REQUEST, fieldName + " must not be empty.");
        }
        if (!HEX_PATTERN.matcher(value).matches()) {
            throw new DigitalException(HttpStatus.BAD_REQUEST, fieldName + " must be valid hex.");
        }
        if (value.length() != expectedBytes * 2) {
            throw new DigitalException(HttpStatus.BAD_REQUEST,
                    fieldName + " must decode to exactly " + expectedBytes + " bytes.");
        }
    }

    private void verifyKeySignatures(RegisterModel model) throws DigitalException {
        byte[] identityKeyBytes = hexToBytes(model.getIdentityKey());
        byte[] preKeyBytes = hexToBytes(model.getPreKey());
        byte[] preKeySignatureBytes = hexToBytes(model.getPreKeySignature());
        byte[] kemKeyBytes = hexToBytes(model.getKemKey());
        byte[] kemKeySignatureBytes = hexToBytes(model.getKeyKemSignature());

        try {
            if (!xEdDsaVerifier.verify(identityKeyBytes, preKeyBytes, preKeySignatureBytes)) {
                throw new DigitalException(HttpStatus.BAD_REQUEST, "PreKey signature verification failed.");
            }
            if (!xEdDsaVerifier.verify(identityKeyBytes, kemKeyBytes, kemKeySignatureBytes)) {
                throw new DigitalException(HttpStatus.BAD_REQUEST, "KEM key signature verification failed.");
            }
        } catch (DigitalException e) {
            throw e;
        } catch (Exception e) {
            throw new DigitalException(HttpStatus.BAD_REQUEST, "Key signature verification failed.");
        }
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

            return xEdDsaVerifier.verify(publicKeyBytes, dataBytes, signatureBytes);
        } catch (Exception e) { //catches signature exceptions and hex conversion errors
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
