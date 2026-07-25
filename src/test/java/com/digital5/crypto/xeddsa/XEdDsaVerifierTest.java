package com.digital5.crypto.xeddsa;

import com.digital5.crypto.exception.SignatureVerificationException;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.charset.StandardCharsets;
import java.security.Security;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link XEdDsaVerifier}.
 */
@SpringBootTest
class XEdDsaVerifierTest {


    private final XEdDsaVerifier verifier;

    @Autowired
    public XEdDsaVerifierTest(XEdDsaVerifier verifier) {
        this.verifier = verifier;
    }

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

    // --- Cross-component test vectors (TypeScript → Java) ---

    @Test
    void tsSignaturesVerifyInJava() throws Exception {
        // Cross-component test: TypeScript-produced XEdDSA signatures verified by Java XEdDsaVerifier.
        // Each vector: (x25519PublicKey, message, signature) from TypeScript with deterministic Z=0.
        // Generated by gen-java-vectors.mjs using SignalProtocolReactLib/dist with fixed nonce Z=0x00*64.

        // Vector 1: message="Hello Signal Protocol!"
        {
            byte[] pubKey1 = hexToBytes("a4e09292b651c278b9772c569f5fa9bb13d906b46ab68c9df9dc2b4409f8a209");
            byte[] message1 = hexToBytes("48656c6c6f205369676e616c2050726f746f636f6c21");
            byte[] signature1 = hexToBytes("3283541657e90a4c84dd41794be2abcd680b228f238759b3034ca2bd379bd60233bac3754c53e101fc473d0d04f43c995e734b6d0d1a1bcc7cce52282faebf0d");
            assertTrue(verifier.verify(pubKey1, message1, signature1),
                    "Vector 1 failed: message=\"Hello Signal Protocol!\"");
        }

        // Vector 2: message="Cross-component test vector 2"
        {
            byte[] pubKey2 = hexToBytes("ce8d3ad1ccb633ec7b70c17814a5c76ecd029685050d344745ba05870e587d59");
            byte[] message2 = hexToBytes("43726f73732d636f6d706f6e656e74207465737420766563746f722032");
            byte[] signature2 = hexToBytes("e916055c695915b3b794116739a5882f8bfab08337643d64e6fa65faf25b2534f4730db87c239825e2ccd7082c2f4e034c840854102e2c0460151f61a297b30c");
            assertTrue(verifier.verify(pubKey2, message2, signature2),
                    "Vector 2 failed: message=\"Cross-component test vector 2\"");
        }

        // Vector 3: message="Java verifies TypeScript"
        {
            byte[] pubKey3 = hexToBytes("132c442be010fbd57e72603328aa76e71fccc1503aae219327d14d9c9993f472");
            byte[] message3 = hexToBytes("4a6176612076657269666965732054797065536372697074");
            byte[] signature3 = hexToBytes("f70b5d7b2953ae101e647385f7d00d3eb7078abda000f1fe572f1d1c72f756c29ba6ae11bfd80603a85a20f72424f0813ef71665e4b69b8e6236404a739ddc08");
            assertTrue(verifier.verify(pubKey3, message3, signature3),
                    "Vector 3 failed: message=\"Java verifies TypeScript\"");
        }

        // Vector 4: message=(empty)
        {
            byte[] pubKey4 = hexToBytes("07a37cbc142093c8b755dc1b10e86cb426374ad16aa853ed0bdfc0b2b86d1c7c");
            byte[] message4 = hexToBytes("");
            byte[] signature4 = hexToBytes("edf45ecf17312282d97b3588b4c98d4471c4acb182ae217e36cd5044bc71ca2a50ae48b8367f46e964dfd72ac62d18b86181a4a0588deacad05604a35d1c2701");
            assertTrue(verifier.verify(pubKey4, message4, signature4),
                    "Vector 4 failed: message=(empty)");
        }

        // Vector 5: message="The quick brown fox jumps over the lazy dog"
        {
            byte[] pubKey5 = hexToBytes("3ebcb692149344dc54e58160cf90bed9eea1dd14e81c8e91de557af7d7afd915");
            byte[] message5 = hexToBytes("54686520717569636b2062726f776e20666f78206a756d7073206f76657220746865206c617a7920646f67");
            byte[] signature5 = hexToBytes("7e4a6682fe07673e322bd06cd3dbf2f175ab1b6f2be8862312e7f41f5de036c14ab345e779f3775f1ae6ceaf31f309d306144141e23aa0bd3877d84a7d5f9401");
            assertTrue(verifier.verify(pubKey5, message5, signature5),
                    "Vector 5 failed: message=\"The quick brown fox jumps over the lazy dog\"");
        }
    }

    private static byte[] hexToBytes(String hex) {
        if (hex == null || hex.isEmpty()) return new byte[0];
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }
}
