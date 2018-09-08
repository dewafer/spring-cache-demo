package com.dewafer.cacheDemo.exception;

public class UserAlreadyExistsException extends UserServiceException {

    public UserAlreadyExistsException(String username) {
        super(username, "User with name: " + username + " already exists.");
    }
}
