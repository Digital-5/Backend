package com.digital5.crypto.exception;

/**
 * Thrown when a cryptographic signature verification fails or inputs are invalid.
 */
public class SignatureVerificationException extends RuntimeException {

    public SignatureVerificationException(String message) {
        super(message);
    }

    public SignatureVerificationException(String message, Throwable cause) {
        super(message, cause);
    }
}

