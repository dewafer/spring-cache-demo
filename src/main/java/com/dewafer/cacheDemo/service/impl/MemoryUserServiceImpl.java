package com.dewafer.cacheDemo.service.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import com.dewafer.cacheDemo.exception.UserAlreadyExistsException;
import com.dewafer.cacheDemo.exception.UserNotFoundException;
import com.dewafer.cacheDemo.model.Gender;
import com.dewafer.cacheDemo.model.User;
import com.dewafer.cacheDemo.service.UserService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class MemoryUserServiceImpl implements UserService, InitializingBean {

    private Map<String, User> memoryStore = new HashMap<>();

    @Override
    public User findUserByUsername(String username) throws UserNotFoundException {
        log.debug("get user: {}", username);
        return Optional.ofNullable(this.memoryStore.get(username))
                .orElseThrow(() -> new UserNotFoundException(username));
    }

    @Override
    public User updateUser(User user) throws UserNotFoundException {
        log.debug("update user: {}", user);
        if (this.memoryStore.containsKey(user.getUsername())) {
            this.memoryStore.put(user.getUsername(), user);
        } else {
            throw new UserNotFoundException(user.getUsername());
        }
        // return the updated object for caching
        return user;
    }

    @Override
    public void deleteUser(String username) throws UserNotFoundException {
        log.debug("delete user: {}", username);
        if (this.memoryStore.containsKey(username)) {
            this.memoryStore.remove(username);
        } else {
            throw new UserNotFoundException(username);
        }
    }

    @Override
    public User addUser(User user) throws UserAlreadyExistsException {
        log.debug("add user: {}", user);
        if (this.memoryStore.containsKey(user.getUsername())) {
            throw new UserAlreadyExistsException(user.getUsername());
        }

        this.memoryStore.put(user.getUsername(), user);
        // return the added object for caching
        return user;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.memoryStore.putAll(Stream.of(
                User.of("john", "John Doe", null, Gender.UNKNOWN, null),
                User.of("james", "James", 10, Gender.MALE, "Some place on earth."),
                User.of("emma", "Emma", 22, Gender.FEMALE, "UNKNOWN"),
                User.of("david", "David", 29, Gender.MALE, "emmmm, I dunno"),
                User.of("alfred", "Alfred", null, Gender.UNKNOWN, null)
        ).collect(Collectors.toMap(User::getUsername, Function.identity())));
    }
}
