package com.example.m3almacenamiento.excepciones;

public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
