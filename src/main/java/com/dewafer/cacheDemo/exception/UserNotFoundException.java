package com.dewafer.cacheDemo.exception;

public class UserNotFoundException extends UserServiceException {

    public UserNotFoundException(String username) {
        super(username, "User with name: " + username + " not found.");
    }

}
