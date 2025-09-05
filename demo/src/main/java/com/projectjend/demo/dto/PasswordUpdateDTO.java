package com.projectjend.demo.dto;

import jakarta.validation.constraints.NotBlank;

public record PasswordUpdateDTO(
    @NotBlank String oldPassword,
    @NotBlank String newPassword
) {}
