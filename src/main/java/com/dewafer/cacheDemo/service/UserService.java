package com.dewafer.cacheDemo.service;

import com.dewafer.cacheDemo.exception.UserAlreadyExistsException;
import com.dewafer.cacheDemo.exception.UserNotFoundException;
import com.dewafer.cacheDemo.model.User;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;

@CacheConfig(cacheNames = "users")
public interface UserService {

    @Cacheable
    User findUserByUsername(String username) throws UserNotFoundException;

    @CachePut(key = "#user.username")
    void updateUser(User user) throws UserNotFoundException;

    @CacheEvict
    void deleteUser(String username) throws UserNotFoundException;

    @CachePut(key = "#user.username")
    void addUser(User user) throws UserAlreadyExistsException;

}
