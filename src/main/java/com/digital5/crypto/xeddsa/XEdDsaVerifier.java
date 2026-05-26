package com.digital5.crypto.xeddsa;

import com.digital5.crypto.exception.SignatureVerificationException;
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;
import org.bouncycastle.crypto.signers.Ed25519Signer;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.Arrays;

/**
 * XEdDSA signature verifier for X25519 public keys.
 * <p>
 * Implements verification per the Signal XEdDSA specification:
 * https://signal.org/docs/specifications/xeddsa/xeddsa.pdf
 * <p>
 * XEdDSA verify is equivalent to standard Ed25519 verify (RFC 8032 §5.1.7)
 * with the public key derived from the X25519 Montgomery u-coordinate via
 * the birational map: y = (u - 1) / (u + 1) mod p.
 */
@Component
public class XEdDsaVerifier {

    /** Field prime p = 2^255 - 19 */
    private static final BigInteger P = BigInteger.TWO.pow(255).subtract(BigInteger.valueOf(19));

    /** Curve order q = 2^252 + 27742317777372353535851937790883648493 */
    private static final BigInteger Q = new BigInteger(
            "7237005577332262213973186563042994240857116359379907606001950938285454250989");

    private static final int KEY_LENGTH = 32;
    private static final int SIGNATURE_LENGTH = 64;

    /**
     * Verifies an XEdDSA signature against an X25519 public key.
     *
     * @param x25519PublicKey 32-byte X25519 public key (Montgomery u-coordinate, little-endian)
     * @param message         the signed message (arbitrary length)
     * @param signature       64-byte XEdDSA signature (R || s, each 32 bytes little-endian)
     * @return true if the signature is valid
     * @throws SignatureVerificationException if inputs have invalid length or encoding
     */
    public boolean verify(byte[] x25519PublicKey, byte[] message, byte[] signature) {
        if (x25519PublicKey == null || x25519PublicKey.length != KEY_LENGTH) {
            throw new SignatureVerificationException(
                    "X25519 public key must be exactly 32 bytes, got " +
                            (x25519PublicKey == null ? "null" : x25519PublicKey.length));
        }
        if (signature == null || signature.length != SIGNATURE_LENGTH) {
            throw new SignatureVerificationException(
                    "Signature must be exactly 64 bytes, got " +
                            (signature == null ? "null" : signature.length));
        }
        if (message == null) {
            throw new SignatureVerificationException("Message must not be null");
        }

        // Range checks per XEdDSA spec
        BigInteger u = littleEndianToBigInteger(x25519PublicKey);
        if (u.compareTo(P) >= 0) {
            return false;
        }

        byte[] sBytes = Arrays.copyOfRange(signature, 32, 64);
        BigInteger s = littleEndianToBigInteger(sBytes);
        if (s.compareTo(Q) >= 0) {
            return false;
        }

        // Convert Montgomery u-coordinate to Ed25519 public key encoding
        byte[] edPublicKey = convertMontgomeryToEdwards(x25519PublicKey);

        // Use Bouncy Castle Ed25519 verification (RFC 8032)
        // This computes: h = SHA-512(R || A || M) mod q, checks sB == R + hA
        try {
            Ed25519PublicKeyParameters pubKeyParams = new Ed25519PublicKeyParameters(edPublicKey, 0);
            Ed25519Signer verifier = new Ed25519Signer();
            verifier.init(false, pubKeyParams);
            verifier.update(message, 0, message.length);
            return verifier.verifySignature(signature);
        } catch (Exception e) {
            // Invalid point encoding or other crypto failure → signature invalid
            return false;
        }
    }

    /**
     * Converts an X25519 public key (Montgomery u-coordinate) to an Ed25519 public key encoding.
     * <p>
     * Birational map per RFC 7748 §4.1: y = (u - 1) / (u + 1) mod p
     * Output encoding: y-coordinate little-endian with sign bit = 0 (bit 255).
     *
     * @param x25519PublicKey 32-byte Montgomery u-coordinate (little-endian)
     * @return 32-byte Ed25519 public key encoding
     */
    byte[] convertMontgomeryToEdwards(byte[] x25519PublicKey) {
        BigInteger u = littleEndianToBigInteger(x25519PublicKey);

        // u mod p (handles edge cases where u >= p after interpretation)
        u = u.mod(P);

        // y = (u - 1) / (u + 1) mod p
        // Division in modular arithmetic: multiply by modular inverse
        BigInteger numerator = u.subtract(BigInteger.ONE).mod(P);
        BigInteger denominator = u.add(BigInteger.ONE).mod(P);
        BigInteger denominatorInv = denominator.modInverse(P);
        BigInteger y = numerator.multiply(denominatorInv).mod(P);

        // Encode y as 32-byte little-endian, sign bit = 0
        byte[] encoded = bigIntegerToLittleEndian(y, KEY_LENGTH);
        encoded[31] &= 0x7F; // Ensure sign bit is 0

        return encoded;
    }

    /**
     * Interprets a little-endian byte array as an unsigned BigInteger.
     */
    private static BigInteger littleEndianToBigInteger(byte[] bytes) {
        // Reverse to big-endian for BigInteger constructor
        byte[] reversed = new byte[bytes.length + 1]; // +1 for unsigned (leading zero)
        for (int i = 0; i < bytes.length; i++) {
            reversed[bytes.length - i] = bytes[i];
        }
        // reversed[0] = 0 → ensures positive interpretation
        return new BigInteger(reversed);
    }

    /**
     * Converts a non-negative BigInteger to a little-endian byte array of specified length.
     */
    private static byte[] bigIntegerToLittleEndian(BigInteger value, int length) {
        byte[] result = new byte[length];
        byte[] bigEndian = value.toByteArray(); // signed big-endian

        // Copy bytes in reverse, skipping leading sign byte if present
        int srcStart = (bigEndian[0] == 0 && bigEndian.length > 1) ? 1 : 0;
        int srcLen = bigEndian.length - srcStart;
        int copyLen = Math.min(srcLen, length);

        for (int i = 0; i < copyLen; i++) {
            result[i] = bigEndian[bigEndian.length - 1 - i];
        }
        return result;
    }
}

