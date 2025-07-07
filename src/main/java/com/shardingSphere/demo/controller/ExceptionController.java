package com.shardingSphere.demo.controller;

import com.shardingSphere.demo.exception.COSException;
import com.shardingSphere.demo.exception.FileException;
import com.shardingSphere.demo.exception.IllegalException;
import com.shardingSphere.demo.param.ExceptionResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;

@ControllerAdvice
public class ExceptionController {
    @ExceptionHandler(IllegalException.class)
    public ResponseEntity<ExceptionResponse> handleException(IllegalException e) {
        ExceptionResponse exceptionResponse = new ExceptionResponse(400, e.getMessage(), LocalDateTime.now());
        return new ResponseEntity<>(exceptionResponse, HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(FileException.class)
    public ResponseEntity<ExceptionResponse> handleException(FileException e) {
        ExceptionResponse exceptionResponse = new ExceptionResponse(505, e.getMessage(), LocalDateTime.now());
        return new ResponseEntity<>(exceptionResponse, HttpStatus.HTTP_VERSION_NOT_SUPPORTED);
    }
    @ExceptionHandler(COSException.class)
    public ResponseEntity<ExceptionResponse> handleException(COSException e) {
        ExceptionResponse exceptionResponse = new ExceptionResponse(506, e.getMessage(), LocalDateTime.now());
        return new ResponseEntity<>(exceptionResponse, HttpStatus.VARIANT_ALSO_NEGOTIATES);
    }
}
