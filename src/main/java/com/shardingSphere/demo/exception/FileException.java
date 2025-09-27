package com.shardingSphere.demo.exception;

import org.springframework.stereotype.Service;

@Service
public class FileException extends RuntimeException{
    public FileException(String message) {
        super(message);
    }
    public FileException(String message, Throwable cause) {
        super(message, cause);
    }
    public FileException(){
        super();
    }

}
