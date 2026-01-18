package com.example.demo.exception;

public class NotUniqueUserEmailException extends RuntimeException {

    public NotUniqueUserEmailException(String message) {
        super(message);
    }
}
