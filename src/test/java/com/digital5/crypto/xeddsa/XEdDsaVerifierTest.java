package com.digital5.crypto.xeddsa;

import com.digital5.crypto.exception.SignatureVerificationException;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.Security;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link XEdDsaVerifier}.
 */
class XEdDsaVerifierTest {

    private final XEdDsaVerifier verifier = new XEdDsaVerifier();

    @BeforeAll
    static void setup() {
        Security.addProvider(new BouncyCastleProvider());
    }

    // --- Montgomery → Edwards conversion tests ---

    @Test
    void convertMontgomeryToEdwards_basepoint9() {
        // Known vector: Montgomery basepoint u=9 → Ed25519 basepoint y
        // Expected: "5866666666666666666666666666666666666666666666666666666666666666"
        byte[] u = new byte[32];
        u[0] = 9;

        byte[] edKey = verifier.convertMontgomeryToEdwards(u);

        assertEquals((byte) 0x58, edKey[0]);
        for (int i = 1; i < 32; i++) {
            assertEquals((byte) 0x66, edKey[i], "Byte " + i + " mismatch");
        }
    }

    @Test
    void convertMontgomeryToEdwards_u_equals_zero() {
        // u=0 → y = (0-1)/(0+1) = -1 mod p = p-1
        byte[] u = new byte[32];
        byte[] edKey = verifier.convertMontgomeryToEdwards(u);

        // p-1 in LE: 0xEC, 0xFF*30, 0x7F
        assertEquals((byte) 0xEC, edKey[0]);
        for (int i = 1; i < 31; i++) {
            assertEquals((byte) 0xFF, edKey[i], "Byte " + i);
        }
        assertEquals((byte) 0x7F, edKey[31]);
    }

    // --- End-to-End XEdDSA verify tests ---

    @Test
    void verify_ed25519SignatureWithCompatibleX25519Key_returnsTrue() throws SignatureVerificationException {
        byte[][] keySet = XEdDsaTestHelper.generateCompatibleKeySet();
        byte[] seed = keySet[0];
        byte[] x25519PublicKey = keySet[2];

        byte[] message = "Hello Signal Protocol!".getBytes(StandardCharsets.UTF_8);
        byte[] signature = XEdDsaTestHelper.sign(seed, message);

        assertTrue(verifier.verify(x25519PublicKey, message, signature));
    }

    @Test
    void verify_multipleMessages_allValid() throws SignatureVerificationException {
        byte[][] keySet = XEdDsaTestHelper.generateCompatibleKeySet();
        byte[] seed = keySet[0];
        byte[] x25519PublicKey = keySet[2];

        for (int i = 0; i < 10; i++) {
            byte[] message = ("Message #" + i).getBytes(StandardCharsets.UTF_8);
            byte[] signature = XEdDsaTestHelper.sign(seed, message);
            assertTrue(verifier.verify(x25519PublicKey, message, signature),
                    "Round " + i + " failed");
        }
    }

    @Test
    void verify_tamperedMessage_returnsFalse() throws SignatureVerificationException {
        byte[][] keySet = XEdDsaTestHelper.generateCompatibleKeySet();
        byte[] seed = keySet[0];
        byte[] x25519PublicKey = keySet[2];

        byte[] message = "Original".getBytes(StandardCharsets.UTF_8);
        byte[] signature = XEdDsaTestHelper.sign(seed, message);

        byte[] tampered = "Tampered".getBytes(StandardCharsets.UTF_8);
        assertFalse(verifier.verify(x25519PublicKey, tampered, signature));
    }

    @Test
    void verify_wrongPublicKey_returnsFalse() throws SignatureVerificationException {
        byte[][] keySet1 = XEdDsaTestHelper.generateCompatibleKeySet();
        byte[][] keySet2 = XEdDsaTestHelper.generateCompatibleKeySet();

        byte[] message = "Test".getBytes(StandardCharsets.UTF_8);
        byte[] signature = XEdDsaTestHelper.sign(keySet1[0], message);

        assertFalse(verifier.verify(keySet2[2], message, signature));
    }

    @Test
    void verify_tamperedSignature_returnsFalse() throws SignatureVerificationException {
        byte[][] keySet = XEdDsaTestHelper.generateCompatibleKeySet();
        byte[] seed = keySet[0];
        byte[] x25519PublicKey = keySet[2];

        byte[] message = "Test".getBytes(StandardCharsets.UTF_8);
        byte[] signature = XEdDsaTestHelper.sign(seed, message);

        signature[10] ^= 1;
        assertFalse(verifier.verify(x25519PublicKey, message, signature));
    }

    @Test
    void verify_emptyMessage_works() throws SignatureVerificationException {
        byte[][] keySet = XEdDsaTestHelper.generateCompatibleKeySet();
        byte[] seed = keySet[0];
        byte[] x25519PublicKey = keySet[2];

        byte[] message = new byte[0];
        byte[] signature = XEdDsaTestHelper.sign(seed, message);

        assertTrue(verifier.verify(x25519PublicKey, message, signature));
    }

    // --- Input validation tests ---

    @Test
    void verify_nullPublicKey_throwsException() {
        byte[] message = "test".getBytes();
        byte[] signature = new byte[64];
        assertThrows(SignatureVerificationException.class,
                () -> verifier.verify(null, message, signature));
    }

    @Test
    void verify_wrongKeyLength_throwsException() {
        byte[] key = new byte[16];
        byte[] message = "test".getBytes();
        byte[] signature = new byte[64];
        assertThrows(SignatureVerificationException.class,
                () -> verifier.verify(key, message, signature));
    }

    @Test
    void verify_wrongSignatureLength_throwsException() {
        byte[] key = new byte[32];
        byte[] message = "test".getBytes();
        byte[] signature = new byte[48];
        assertThrows(SignatureVerificationException.class,
                () -> verifier.verify(key, message, signature));
    }

    @Test
    void verify_nullMessage_throwsException() {
        byte[] key = new byte[32];
        byte[] signature = new byte[64];
        assertThrows(SignatureVerificationException.class,
                () -> verifier.verify(key, null, signature));
    }

    // --- Boundary cases ---

    @Test
    void verify_scalarS_exceedsOrder_returnsFalse() throws SignatureVerificationException {
        byte[] key = new byte[32];
        key[0] = 9;
        byte[] msg = "test".getBytes(StandardCharsets.UTF_8);
        byte[] sig = new byte[64];
        for (int i = 32; i < 64; i++) {
            sig[i] = (byte) 0xFF;
        }
        assertFalse(verifier.verify(key, msg, sig));
    }

    @Test
    void verify_allZeroSignature_returnsFalse() throws SignatureVerificationException {
        byte[] key = new byte[32];
        key[0] = 9;
        byte[] msg = "test".getBytes(StandardCharsets.UTF_8);
        byte[] sig = new byte[64];
        assertFalse(verifier.verify(key, msg, sig));
    }
}
