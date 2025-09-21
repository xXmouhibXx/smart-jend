package com.projectjend.demo.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.time.LocalDate;

public record ReviewDTO(
    @NotBlank String clientEmail,
    @NotBlank String provider,
    Long serviceProposalId,
    LocalDate bookingStartDate,
    LocalDate bookingEndDate,
    @DecimalMin("0.0") @DecimalMax("5.0") BigDecimal rating,
    String comment
) {}