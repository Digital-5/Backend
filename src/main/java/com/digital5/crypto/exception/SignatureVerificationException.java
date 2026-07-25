package com.digital5.crypto.exception;

import com.digital5.exception.DigitalException;
import org.springframework.http.HttpStatus;

/**
 * Thrown when a cryptographic signature verification fails or inputs are invalid.
 */
public class SignatureVerificationException extends DigitalException {

    public SignatureVerificationException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}

