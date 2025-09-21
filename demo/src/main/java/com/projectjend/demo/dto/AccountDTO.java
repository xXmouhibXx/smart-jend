package com.projectjend.demo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

public record AccountDTO(
    @NotBlank String name,
    @Email @NotBlank String email,
    @NotBlank String password,
    String phone,
    LocalDate birthDate,
    String gender,
    String delegation,
    String sector
) {}