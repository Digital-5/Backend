package com.digital5.crypto.xeddsa;

import org.bouncycastle.math.ec.rfc8032.Ed25519;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Arrays;

/**
 * Test helper that generates Ed25519 signatures verifiable via XEdDsaVerifier.
 * <p>
 * Strategy: Generate a standard Ed25519 key pair, sign with it, then compute
 * the corresponding X25519 public key (Montgomery u-coordinate) that will map
 * back to the same Edwards point. This lets us verify the full pipeline without
 * needing raw scalar multiplication.
 * <p>
 * The reverse birational map (Edwards y → Montgomery u): u = (1 + y) / (1 - y) mod p
 * <p>
 * ONLY FOR TESTING – not part of production code.
 */
class XEdDsaTestHelper {

    private static final BigInteger P = BigInteger.TWO.pow(255).subtract(BigInteger.valueOf(19));
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Generates a test key set: Ed25519 seed + Ed25519 public key + equivalent X25519 public key.
     * Retries until the Ed25519 public key has sign bit = 0 (so convert_mont will map correctly).
     *
     * @return [ed25519Seed (32), ed25519PublicKey (32), x25519PublicKey (32)]
     */
    static byte[][] generateCompatibleKeySet() {
        byte[] seed = new byte[32];
        byte[] edPublicKey = new byte[32];

        // Retry until sign bit = 0 (expected ~50% chance each attempt)
        for (int attempt = 0; attempt < 100; attempt++) {
            RANDOM.nextBytes(seed);
            Ed25519.generatePublicKey(seed, 0, edPublicKey, 0);

            // Check sign bit (bit 7 of byte 31)
            if ((edPublicKey[31] & 0x80) == 0) {
                // Compute X25519 public key via reverse birational map
                byte[] x25519PublicKey = edwardsToMontgomery(edPublicKey);
                return new byte[][]{seed, edPublicKey, x25519PublicKey};
            }
        }
        throw new IllegalStateException("Failed to generate key with sign bit = 0 after 100 attempts");
    }

    /**
     * Signs a message using standard Ed25519 (RFC 8032).
     * The resulting signature is verifiable via XEdDsaVerifier with the compatible X25519 public key.
     */
    static byte[] sign(byte[] ed25519Seed, byte[] message) {
        byte[] signature = new byte[64];
        Ed25519.sign(ed25519Seed, 0, message, 0, message.length, signature, 0);
        return signature;
    }

    /**
     * Reverse birational map: Edwards y-coordinate → Montgomery u-coordinate.
     * u = (1 + y) / (1 - y) mod p
     *
     * @param edPublicKey 32-byte Ed25519 encoding (y little-endian, sign bit must be 0)
     */
    private static byte[] edwardsToMontgomery(byte[] edPublicKey) {
        // Extract y (clear sign bit)
        byte[] yBytes = Arrays.copyOf(edPublicKey, 32);
        yBytes[31] &= 0x7F;
        BigInteger y = littleEndianToBigInteger(yBytes);

        // u = (1 + y) / (1 - y) mod p
        BigInteger numerator = BigInteger.ONE.add(y).mod(P);
        BigInteger denominator = BigInteger.ONE.subtract(y).mod(P);
        BigInteger denominatorInv = denominator.modInverse(P);
        BigInteger u = numerator.multiply(denominatorInv).mod(P);

        return bigIntegerToLittleEndian(u, 32);
    }

    private static BigInteger littleEndianToBigInteger(byte[] bytes) {
        byte[] reversed = new byte[bytes.length + 1];
        for (int i = 0; i < bytes.length; i++) {
            reversed[bytes.length - i] = bytes[i];
        }
        return new BigInteger(reversed);
    }

    private static byte[] bigIntegerToLittleEndian(BigInteger value, int length) {
        byte[] result = new byte[length];
        byte[] bigEndian = value.toByteArray();
        int srcStart = (bigEndian[0] == 0 && bigEndian.length > 1) ? 1 : 0;
        int copyLen = Math.min(bigEndian.length - srcStart, length);
        for (int i = 0; i < copyLen; i++) {
            result[i] = bigEndian[bigEndian.length - 1 - i];
        }
        return result;
    }
}

