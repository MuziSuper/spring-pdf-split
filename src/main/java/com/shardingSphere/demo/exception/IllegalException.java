package com.shardingSphere.demo.exception;

import org.springframework.stereotype.Service;

@Service
public class IllegalException extends RuntimeException{
    public IllegalException(String message) {
        super(message);
    }
    public IllegalException(String message, Throwable cause) {
        super(message, cause);
    }
    public IllegalException(){
        super();
    }

}
