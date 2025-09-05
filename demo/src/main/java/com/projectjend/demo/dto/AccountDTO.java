package com.projectjend.demo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AccountDTO(
    @NotBlank String name,
    @Email @NotBlank String email,
    @NotBlank String password
) {}
