package com.jomea.urlshortener.dto;

import java.time.LocalDateTime;

public record UserDto(Long id, String name, String email, String role, String tier, LocalDateTime createdAt) {
}
