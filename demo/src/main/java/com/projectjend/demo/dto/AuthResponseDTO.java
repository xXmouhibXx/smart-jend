package com.projectjend.demo.dto;

public record AuthResponseDTO(
    String token,
    String email,
    String name,
    Long id
) {}