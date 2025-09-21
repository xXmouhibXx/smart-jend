package com.projectjend.demo.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

public record ServiceProposalDTO(
    @NotBlank String name,
    @NotBlank String description,
    @NotBlank String location,
    String ownerEmail,
    LocalDate endDate,
    String reservationLink,
    String delegation,
    String sector,
    String provider,
    String institution,
    String category
) {}