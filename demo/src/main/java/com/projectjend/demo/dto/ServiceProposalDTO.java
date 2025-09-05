package com.projectjend.demo.dto;

import jakarta.validation.constraints.NotBlank;

public record ServiceProposalDTO(
    @NotBlank String name,
    @NotBlank String description,
    /**
     * "lat,lon" string, e.g. "36.81,10.17"
     */
    @NotBlank String location
) {}
