package com.jomea.urlshortener.controller;

import com.jomea.urlshortener.dto.UserDto;
import com.jomea.urlshortener.entity.Url;
import com.jomea.urlshortener.repository.UrlRepository;
import com.jomea.urlshortener.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UrlRepository urlRepository;
    private final UserRepository userRepository;

    public AdminController(UrlRepository urlRepository, UserRepository userRepository) {
        this.urlRepository = urlRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/urls")
    public ResponseEntity<List<Url>> listUrls() {
        return ResponseEntity.ok(urlRepository.findAllByOrderByCreatedAtDesc());
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserDto>> listUsers() {
        List<UserDto> dtos = userRepository.findAllByOrderByCreatedAtDesc()
            .stream()
            .map(u -> new UserDto(u.getId(), u.getName(), u.getEmail(), u.getRole(), u.getCreatedAt()))
            .toList();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> stats() {
        return ResponseEntity.ok(Map.of(
            "totalUrls", urlRepository.count(),
            "totalClicks", urlRepository.sumClickCount(),
            "totalUsers", userRepository.count()
        ));
    }
}
