package com.digital5.crypto.xeddsa;

import com.digital5.crypto.exception.SignatureVerificationException;
import com.digital5.crypto.math.*;
import com.digital5.crypto.math.ed25519.Ed25519LittleEndianEncoding;

import com.digital5.crypto.math.ed25519.Ed25519ScalarOps;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * XEdDSA signature verifier for X25519 public keys.
 * <p>
 * Implements verification per the Signal XEdDSA specification:
 * <a href="https://signal.org/docs/specifications/xeddsa/xeddsa.pdf">...</a>
 * <p>
 * XEdDSA verify is equivalent to standard Ed25519 verify (RFC 8032 §5.1.7)
 * with the public key derived from the X25519 Montgomery u-coordinate via
 * the birational map: y = (u - 1) / (u + 1) mod p.
 */
@Component
public class XEdDsaVerifier {

    /** Field prime p = 2^255 - 19 */
    private static final BigInteger FIELD_PRIME = BigInteger.TWO.pow(255).subtract(BigInteger.valueOf(19));

    /** Curve order q = 2^252 + 27742317777372353535851937790883648493 */
    private static final BigInteger CURVE_ORDER = new BigInteger(
            "7237005577332262213973186563042994240857116359379907606001950938285454250989");

    private static final int KEY_LENGTH = 32;
    private static final int SIGNATURE_LENGTH = 64;

    /** Ed25519 curve parameters – initialized once, thread-safe (all fields are final). */
    private static final Field ED25519_FIELD = new Field(
            256,
            Utils.hexToBytes("edffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff7f"),
            new Ed25519LittleEndianEncoding());

    private static final Curve ED25519_CURVE = new Curve(
            ED25519_FIELD,
            Utils.hexToBytes("a3785913ca4deb75abd841414d0a700098e879777940c78c73fe6f2bee6c0352"), // d
            ED25519_FIELD.fromByteArray(Utils.hexToBytes("b0a00e4a271beec478e42fad0618432fa7d7fb3d99004d2b0bdfc14f8024832b"))); // I = sqrt(-1)

    /** Base point B with precomputed tables for scalar multiplication. */
    private static final GroupElement BASE_POINT = ED25519_CURVE.createPoint(
            Utils.hexToBytes("5866666666666666666666666666666666666666666666666666666666666666"),
            true);

    private static final ScalarOps SCALAR_OPS = new Ed25519ScalarOps();

    // Instance references (use static finals above)
    private static final Curve curve = ED25519_CURVE;
    private static final GroupElement basePoint = BASE_POINT;
    private static final ScalarOps scalarOps = SCALAR_OPS;


    /**
     * Verifies an XEdDSA signature against an X25519 public key.
     * <p>
     * Implements verification per the Signal XEdDSA specification:
     * <a href="https://signal.org/docs/specifications/xeddsa/xeddsa.pdf">XEdDSA</a>
     * <p>
     * Verification equation: [s]B = R + [h]A, equivalently: [s]B - [h]A = R
     *
     * @param x25519PublicKey 32-byte X25519 public key (Montgomery u-coordinate, little-endian)
     * @param message         the signed message (arbitrary length)
     * @param signature       64-byte XEdDSA signature (R || s, each 32 bytes little-endian)
     * @return true if the signature is valid
     * @throws SignatureVerificationException if inputs have invalid length or encoding
     */
    public boolean verify(byte[] x25519PublicKey, byte[] message, byte[] signature) throws SignatureVerificationException {
        validateInputs(x25519PublicKey, message, signature);

        // Reject u-coordinate >= p (non-canonical encoding)
        BigInteger uCoordinate = littleEndianToBigInteger(x25519PublicKey);
        if (uCoordinate.compareTo(FIELD_PRIME) >= 0) {
            return false;
        }

        // Reject scalar s >= q (prevents signature malleability)
        byte[] sBytes = Arrays.copyOfRange(signature, 32, 64);
        BigInteger scalar = littleEndianToBigInteger(sBytes);
        if (scalar.compareTo(CURVE_ORDER) >= 0) {
            return false;
        }

        try {
            // Convert Montgomery u-coordinate to Edwards y-coordinate
            byte[] edwardsPublicKey = convertMontgomeryToEdwards(x25519PublicKey);

            // Decode signature point R (first 32 bytes)
            byte[] encodedR = Arrays.copyOfRange(signature, 0, 32);

            // Decode points from their 32-byte Edwards encoding
            GroupElement A = curve.createPoint(edwardsPublicKey, true); // precompute for doubleScalarMultiply

            // Reject identity point as public key (allows trivial forgery)
            if (A.equals(curve.getZero(GroupElement.Representation.P3))) {
                return false;
            }

            // Reject small-order points: compute [8]A and check if it equals identity.
            // All 8 small-order points on Ed25519 satisfy [8]P = identity.
            if (hasSmallOrder(A)) {
                return false;
            }

            // Compute challenge hash: h = SHA-512(R || A || M) mod q
            byte[] hashInput = concatenate(encodedR, edwardsPublicKey, message);
            MessageDigest sha512 = MessageDigest.getInstance("SHA-512");
            byte[] hash64 = sha512.digest(hashInput);
            byte[] hBytes = scalarOps.reduce(hash64);

            // Verification: check [s]B - [h]A == R
            // doubleScalarMultiplyVariableTime(A, a, b) computes a*A + b*B
            // We negate A so that: h*(-A) + s*B = s*B - h*A
            GroupElement negA = A.negate();
            GroupElement Rcheck = basePoint.doubleScalarMultiplyVariableTime(negA, hBytes, sBytes);

            // Constant-time comparison of computed R with signature R
            return MessageDigest.isEqual(Rcheck.toByteArray(), encodedR);

        } catch (ArithmeticException e) {
            // Denominator zero in Montgomery-to-Edwards conversion (u = p-1)
            return false;
        } catch (IllegalArgumentException e) {
            // Invalid point encoding (not on curve) → signature invalid
            return false;
        } catch (NoSuchAlgorithmException e) {
            throw new SignatureVerificationException("SHA-512 not available"+ e);
        }
    }

    private void validateInputs(byte[] x25519PublicKey, byte[] message, byte[] signature) throws SignatureVerificationException {
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
    }

    private static byte[] concatenate(byte[]... arrays) {
        int totalLength = 0;
        for (byte[] arr : arrays) {
            totalLength += arr.length;
        }
        byte[] result = new byte[totalLength];
        int offset = 0;
        for (byte[] arr : arrays) {
            System.arraycopy(arr, 0, result, offset, arr.length);
            offset += arr.length;
        }
        return result;
    }

    /**
     * Converts an X25519 public key (Montgomery u-coordinate) to an Ed25519 public key encoding.
     * <p>
     * Birational map per RFC 7748 §4.1: y = (u - 1) / (u + 1) mod p
     * Output encoding: y-coordinate little-endian with sign bit = 0 (bit 255).
     *
     * @param x25519PublicKey 32-byte Montgomery u-coordinate (little-endian)
     * @return 32-byte Ed25519 public key encoding
     * @throws ArithmeticException if the denominator (u + 1) is zero mod p
     */
    byte[] convertMontgomeryToEdwards(byte[] x25519PublicKey) {
        BigInteger uCoordinate = littleEndianToBigInteger(x25519PublicKey);

        // Reduce u mod p to handle edge cases where u >= p after interpretation
        uCoordinate = uCoordinate.mod(FIELD_PRIME);

        // y = (u - 1) / (u + 1) mod p
        // Division in modular arithmetic: multiply by modular inverse
        BigInteger numerator = uCoordinate.subtract(BigInteger.ONE).mod(FIELD_PRIME);
        BigInteger denominator = uCoordinate.add(BigInteger.ONE).mod(FIELD_PRIME);

        // When u = p-1, denominator is zero and modInverse is undefined
        if (denominator.equals(BigInteger.ZERO)) {
            throw new ArithmeticException("Denominator is zero: u = p-1 maps to undefined point");
        }

        BigInteger denominatorInverse = denominator.modInverse(FIELD_PRIME);
        BigInteger yCoordinate = numerator.multiply(denominatorInverse).mod(FIELD_PRIME);

        // Encode y as 32-byte little-endian with sign bit = 0
        byte[] encoded = bigIntegerToLittleEndian(yCoordinate, KEY_LENGTH);
        encoded[31] &= 0x7F; // Ensure sign bit is cleared

        return encoded;
    }

    /**
     * Checks if a point has small order by computing [8]P and testing if the result is the identity.
     * The Ed25519 cofactor is 8, so all 8 small-order points satisfy [8]P = identity.
     *
     * @param point the Edwards point to check (must be in P3 representation)
     * @return true if the point has small order (must be rejected)
     */
    private boolean hasSmallOrder(GroupElement point) {
        GroupElement result = point.dbl().toP2().dbl().toP2().dbl().toP3(); // [8]P
        return result.equals(curve.getZero(GroupElement.Representation.P3));
    }

    /**
     * Interprets a little-endian byte array as an unsigned {@link BigInteger}.
     *
     * @param bytes the little-endian encoded value
     * @return the unsigned BigInteger representation
     */
    private static BigInteger littleEndianToBigInteger(byte[] bytes) {
        // Reverse to big-endian for BigInteger constructor
        byte[] reversed = new byte[bytes.length + 1]; // +1 to ensure unsigned (leading zero)
        for (int i = 0; i < bytes.length; i++) {
            reversed[bytes.length - i] = bytes[i];
        }
        // reversed[0] = 0 ensures positive interpretation
        return new BigInteger(reversed);
    }

    /**
     * Converts a non-negative {@link BigInteger} to a little-endian byte array of the specified length.
     *
     * @param value  the non-negative value to encode
     * @param length the desired output byte array length
     * @return the little-endian encoded byte array
     */
    private static byte[] bigIntegerToLittleEndian(BigInteger value, int length) {
        byte[] result = new byte[length];
        byte[] bigEndian = value.toByteArray(); // signed big-endian representation

        // Copy bytes in reverse, skipping the leading sign byte if present
        int sourceStart = (bigEndian[0] == 0 && bigEndian.length > 1) ? 1 : 0;
        int sourceLength = bigEndian.length - sourceStart;
        int copyLength = Math.min(sourceLength, length);

        for (int i = 0; i < copyLength; i++) {
            result[i] = bigEndian[bigEndian.length - 1 - i];
        }
        return result;
    }
}
