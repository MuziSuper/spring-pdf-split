package com.shardingSphere.demo.exception;

import org.springframework.stereotype.Service;

@Service
public class IllegalFileException extends RuntimeException{
    public IllegalFileException(String message) {
        super(message);
    }
    public IllegalFileException(String message, Throwable cause) {
        super(message, cause);
    }
    public IllegalFileException(){
        super();
    }

}
