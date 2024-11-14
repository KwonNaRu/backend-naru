package com.naru.backend.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class LoginDTO {
    @NotEmpty
    private String email;

    @NotEmpty
    private String password;
}
