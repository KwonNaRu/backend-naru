package com.naru.backend.model;

import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Entity
@Data
public class Login {
    @NotEmpty
    private String email;

    @NotEmpty
    private String password;
}
