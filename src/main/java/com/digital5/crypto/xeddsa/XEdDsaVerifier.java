package com.digital5.crypto.xeddsa;

import com.digital5.crypto.exception.SignatureVerificationException;
import com.digital5.service.ConversionService;

import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.security.MessageDigest;
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

    /** Ed25519 base point x-coordinate */
    private static final BigInteger BASE_POINT_X = new BigInteger(
            "15112221349535400772501151409588531511454012693041857206046113283949847762202");

    /** Ed25519 base point y-coordinate */
    private static final BigInteger BASE_POINT_Y = new BigInteger(
            "46316835694926478169428394003475163141307993866256225615783033603165251855960");

    private static final int KEY_LENGTH = 32;
    private static final int SIGNATURE_LENGTH = 64;

    private final ConversionService conversionService;

    @Autowired
    public XEdDsaVerifier(ConversionService conversionService) {
        this.conversionService = conversionService;
    }

    /**
     * Verifies an XEdDSA signature against an X25519 public key.
     *
     * @param x25519PublicKey 32-byte X25519 public key (Montgomery u-coordinate, little-endian)
     * @param message         the signed message (arbitrary length)
     * @param signature       64-byte XEdDSA signature (R || s, each 32 bytes little-endian)
     * @return true if the signature is valid
     * @throws SignatureVerificationException if inputs have invalid length or encoding
     */
    public boolean verify(byte[] x25519PublicKey, byte[] message, byte[] signature) throws SignatureVerificationException {
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
        BigInteger uCoordinate = littleEndianToBigInteger(x25519PublicKey);
        if (uCoordinate.compareTo(FIELD_PRIME) >= 0) {
            return false;
        }

        byte[] scalarBytes = Arrays.copyOfRange(signature, 32, 64);
        BigInteger scalar = littleEndianToBigInteger(scalarBytes);

        if (scalar.compareTo(CURVE_ORDER) >= 0) {
            return false;
        }

        BigInteger encodedR = littleEndianToBigInteger(signature);
        BigInteger signBit = encodedR.shiftRight(255);
        BigInteger rYCoordinate = encodedR.and(BigInteger.ONE.shiftLeft(255).subtract(BigInteger.ONE));
        if (signBit.equals(BigInteger.ONE) || rYCoordinate.compareTo(FIELD_PRIME) >= 0) {
            return false;
        }

        // Verify that point u is on the curve and perform signature check
        try {
            byte[] edwardsPublicKey = convertMontgomeryToEdwards(x25519PublicKey);

            byte[] encodedRBytes = Arrays.copyOfRange(signature, 0, 32);
            byte[] rWithoutSignBit = Arrays.copyOf(encodedRBytes, encodedRBytes.length);
            rWithoutSignBit[31] &= 0x7F; // Clear sign bit

            byte[] hashInput = conversionService.concatenateByteArrays(new byte[][]{encodedRBytes, edwardsPublicKey, message});
            MessageDigest hasher = MessageDigest.getInstance("SHA-512");
            byte[] hashBytes = hasher.digest(hashInput);
            BigInteger challengeHash = littleEndianToBigInteger(hashBytes);
            challengeHash = challengeHash.mod(CURVE_ORDER);

            // Point arithmetic using Bouncy Castle
            Ed25519PublicKeyParameters rPoint = new Ed25519PublicKeyParameters(encodedRBytes, 0);
            Ed25519PublicKeyParameters aPoint = new Ed25519PublicKeyParameters(edwardsPublicKey, 0);
            //todo write point conversion myself bc there is no public version
            // and then verify S * Base Point == R + h * Public Key
            return false;

        } catch (Exception e) { //todo better exception catching with logging and divide problems
            // Invalid point encoding, degenerate key, or other crypto failure → signature invalid
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
        BigInteger uCoordinate = littleEndianToBigInteger(x25519PublicKey);

        // Reduce u mod p to handle edge cases where u >= p after interpretation
        uCoordinate = uCoordinate.mod(FIELD_PRIME);

        // y = (u - 1) / (u + 1) mod p
        // Division in modular arithmetic: multiply by modular inverse
        BigInteger numerator = uCoordinate.subtract(BigInteger.ONE).mod(FIELD_PRIME);
        BigInteger denominator = uCoordinate.add(BigInteger.ONE).mod(FIELD_PRIME);
        BigInteger denominatorInverse = denominator.modInverse(FIELD_PRIME);
        BigInteger yCoordinate = numerator.multiply(denominatorInverse).mod(FIELD_PRIME);

        // Encode y as 32-byte little-endian with sign bit = 0
        byte[] encoded = bigIntegerToLittleEndian(yCoordinate, KEY_LENGTH);
        encoded[31] &= 0x7F; // Ensure sign bit is cleared

        return encoded;
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
