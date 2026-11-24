package com.example.m3almacenamiento.controladores;


import com.example.m3almacenamiento.excepciones.BusinessException;
import com.example.m3almacenamiento.excepciones.IllegalInputValues;
import com.example.m3almacenamiento.excepciones.ResourceNotFoundException;
import com.example.m3almacenamiento.modelo.DTO.response.ApiErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;

@RestControllerAdvice
public class ExceptionController {

    @ExceptionHandler(IllegalInputValues.class)
    public ResponseEntity<ApiErrorResponse> resolveIllegalValues (IllegalInputValues ex, WebRequest request){
        ApiErrorResponse error = ApiErrorResponse.builder()
                .codigoError(HttpStatus.BAD_REQUEST.value())
                .estadoError(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .mensajeError(ex.getMessage())
                .fechaError(LocalDateTime.now())
                .build();
        return new ResponseEntity<>(error,HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiErrorResponse> resolveBadCredentials (BadCredentialsException ex, WebRequest request){
        ApiErrorResponse error = ApiErrorResponse.builder()
                .codigoError(HttpStatus.UNAUTHORIZED.value())
                .estadoError(HttpStatus.UNAUTHORIZED.getReasonPhrase())
                .mensajeError(ex.getMessage())
                .fechaError(LocalDateTime.now())
                .build();
        return new ResponseEntity<>(error,HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiErrorResponse> resolveBusiness(BusinessException ex, WebRequest request){
        ApiErrorResponse error = ApiErrorResponse.builder()
                .codigoError(HttpStatus.CONFLICT.value())
                .estadoError(HttpStatus.CONFLICT.getReasonPhrase())
                .mensajeError(ex.getMessage())
                .fechaError(LocalDateTime.now())
                .build();
        return new ResponseEntity<>(error,HttpStatus.CONFLICT);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> resolveNotFound (ResourceNotFoundException ex, WebRequest request){
        ApiErrorResponse error = ApiErrorResponse.builder()
                .codigoError(HttpStatus.NOT_FOUND.value())
                .estadoError(HttpStatus.NOT_FOUND.getReasonPhrase())
                .mensajeError(ex.getMessage())
                .fechaError(LocalDateTime.now())
                .build();
        return new ResponseEntity<>(error,HttpStatus.NOT_FOUND);
    }
}
