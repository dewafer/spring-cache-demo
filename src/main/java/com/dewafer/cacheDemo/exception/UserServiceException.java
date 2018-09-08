package com.dewafer.cacheDemo.exception;

import lombok.Getter;

@Getter
public class UserServiceException extends Exception {

    private String username;

    public UserServiceException(String username, String message) {
        super(message);
        this.username = username;
    }
}
