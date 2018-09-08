package com.dewafer.cacheDemo.service.impl;

import com.dewafer.cacheDemo.exception.UserAlreadyExistsException;
import com.dewafer.cacheDemo.exception.UserNotFoundException;
import com.dewafer.cacheDemo.model.Gender;
import com.dewafer.cacheDemo.model.User;
import com.dewafer.cacheDemo.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class MemoryUserServiceImpl implements UserService, InitializingBean {

    private Map<String, User> memoryStore = new HashMap<>();

    @Override
    public User findUserByUsername(String username) throws UserNotFoundException {
        log.debug("get user: {}", username);
        return Optional.ofNullable(memoryStore.get(username))
                .orElseThrow(() -> new UserNotFoundException(username));
    }

    @Override
    public void updateUser(User user) throws UserNotFoundException {
        log.debug("update user: {}", user);
        if (memoryStore.containsKey(user.getUsername())) {
            memoryStore.put(user.getUsername(), user);
        } else {
            throw new UserNotFoundException(user.getUsername());
        }
    }

    @Override
    public void deleteUser(String username) throws UserNotFoundException {
        log.debug("delete user: {}", username);
        if (memoryStore.containsKey(username)) {
            memoryStore.remove(username);
        } else {
            throw new UserNotFoundException(username);
        }
    }

    @Override
    public void addUser(User user) throws UserAlreadyExistsException {
        log.debug("add user: {}", user);
        if (memoryStore.containsKey(user.getUsername())) {
            throw new UserAlreadyExistsException(user.getUsername());
        }

        memoryStore.put(user.getUsername(), user);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        memoryStore.putAll(Stream.of(
                User.of("john", "John Doe", null, Gender.UNKNOWN, null),
                User.of("james", "James", 10, Gender.MALE, "Some place on earth."),
                User.of("emma", "Emma", 22, Gender.FEMALE, "UNKNOWN"),
                User.of("david", "David", 29, Gender.MALE, "emmmm, I dunno"),
                User.of("Alfred", "Alfred", null, Gender.UNKNOWN, null)
        ).collect(Collectors.toMap(User::getUsername, Function.identity())));
    }
}
