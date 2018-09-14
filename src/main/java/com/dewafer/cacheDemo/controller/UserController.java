package com.dewafer.cacheDemo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dewafer.cacheDemo.exception.UserAlreadyExistsException;
import com.dewafer.cacheDemo.exception.UserNotFoundException;
import com.dewafer.cacheDemo.exception.UserServiceException;
import com.dewafer.cacheDemo.model.User;
import com.dewafer.cacheDemo.service.UserService;

@RequestMapping("/user")
@RestController
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/{username}")
    public User getUser(@PathVariable String username) throws Exception {
        return this.userService.findUserByUsername(username);
    }

    @PostMapping("/")
    public String createUser(@RequestBody User user) throws Exception {
        this.userService.addUser(user);
        return "User " + user.getUsername() + " created!";
    }

    @DeleteMapping("/{username}")
    public String removeUser(@PathVariable String username) throws Exception {
        this.userService.deleteUser(username);
        return "User " + username + " removed!";
    }

    @PutMapping("/{username}")
    private String updateUser(@PathVariable String username, @RequestBody User user) throws Exception {
        this.userService.updateUser(
                User.of(username, user.getDisplayName(), user.getAge(), user.getGender(), user.getAddress()));
        return "User " + username + " updated!";
    }

    @ExceptionHandler(UserServiceException.class)
    public ResponseEntity<String> handleUserServiceException(UserServiceException e) {
        if (e instanceof UserNotFoundException) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User " + e.getUsername() + " not found");
        }

        if (e instanceof UserAlreadyExistsException) {
            return ResponseEntity.badRequest().body("User " + e.getUsername() + " already exists.");
        }

        throw new IllegalStateException("should not happen", e);
    }

}
