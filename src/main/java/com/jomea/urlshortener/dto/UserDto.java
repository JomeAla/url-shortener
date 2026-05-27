package com.jomea.urlshortener.dto;

import java.time.LocalDateTime;

public record UserDto(Long id, String name, String email, String role, LocalDateTime createdAt) {
}
