package com.naru.backend.dto;

import com.naru.backend.model.User;

import lombok.Data;

@Data
public class UserDto {
    private Long userId;
    private String username;

    private String email;

    private String role;

    public UserDto(User user) {
        this.userId = user.getUserId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        // 권한에서 역할을 추출하여 설정하기
        if (user.getAuthorities() != null && user.getAuthorities().contains("OWNER")) {
            this.role = "OWNER";
        } else {
            this.role = "GUEST";
        }
    }

    public UserDto(Long userId, String username, String email) {
        this.userId = userId;
        this.username = username;
        this.email = email;
    }
}
