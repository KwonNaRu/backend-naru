package com.naru.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.naru.backend.dto.LoginDto;
import com.naru.backend.dto.UserDto;
import com.naru.backend.model.User;
import com.naru.backend.service.UserService;

import org.springframework.security.core.userdetails.UsernameNotFoundException;

@RestController
@RequestMapping("/auth")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public User registerUser(@RequestBody UserDto userDto) {
        return userService.registerUser(userDto);
    }

    @PostMapping("/login")
    public String loginUser(@RequestBody LoginDto loginDto) {
        try {
            return userService.authenticateUser(loginDto);
        } catch (UsernameNotFoundException e) {
            throw new RuntimeException("Invalid username or password");
        }
    }
}
