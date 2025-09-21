package com.projectjend.demo.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record ServiceBookingDTO(
    @NotNull Long clientId,
    @NotNull Long serviceId,
    @NotNull LocalDateTime bookingStartDate,
    @NotNull LocalDateTime bookingEndDate,
    String status
) {}