package com.naru.backend.dto;

import java.util.List;

import com.naru.backend.model.User;

import lombok.Data;

@Data
public class UserResponseDTO {
    private Long userId;
    private String username;
    private String email;
    private List<String> role;

    public UserResponseDTO(User user) {
        this.userId = user.getUserId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.role = user.getAuthorities();
    }
}
