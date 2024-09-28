package com.naru.backend.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDto {
    @NotEmpty
    private String username;

    @NotEmpty
    private String password;

    @NotEmpty
    private String email;
}
