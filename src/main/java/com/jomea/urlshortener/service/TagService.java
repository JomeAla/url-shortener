package com.jomea.urlshortener.service;

import com.jomea.urlshortener.entity.Tag;
import com.jomea.urlshortener.entity.User;
import com.jomea.urlshortener.repository.TagRepository;
import com.jomea.urlshortener.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class TagService {

    private final TagRepository tagRepository;
    private final UserRepository userRepository;

    public TagService(TagRepository tagRepository, UserRepository userRepository) {
        this.tagRepository = tagRepository;
        this.userRepository = userRepository;
    }

    private Long getUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) return null;
        return userRepository.findByEmail(auth.getName()).map(User::getId).orElse(null);
    }

    public List<Tag> getUserTags() {
        Long userId = getUserId();
        if (userId == null) return List.of();
        return tagRepository.findByUserIdOrderByCreatedAtAsc(userId);
    }

    public Tag createTag(String name, String color) {
        Long userId = getUserId();
        if (userId == null) throw new IllegalArgumentException("Authentication required");
        if (tagRepository.findByUserIdAndNameIgnoreCase(userId, name).isPresent()) {
            throw new IllegalArgumentException("Tag already exists");
        }
        Tag tag = new Tag();
        tag.setName(name);
        tag.setUserId(userId);
        tag.setColor(color);
        tag.setCreatedAt(LocalDateTime.now());
        return tagRepository.save(tag);
    }

    public Tag updateTag(Long id, String name, String color) {
        Long userId = getUserId();
        if (userId == null) throw new IllegalArgumentException("Authentication required");
        Tag tag = tagRepository.findByIdAndUserId(id, userId)
            .orElseThrow(() -> new IllegalArgumentException("Tag not found"));
        if (name != null && !name.equals(tag.getName())) {
            if (tagRepository.findByUserIdAndNameIgnoreCase(userId, name).isPresent()) {
                throw new IllegalArgumentException("Tag already exists");
            }
            tag.setName(name);
        }
        if (color != null) tag.setColor(color);
        return tagRepository.save(tag);
    }

    public void deleteTag(Long id) {
        Long userId = getUserId();
        if (userId == null) throw new IllegalArgumentException("Authentication required");
        Tag tag = tagRepository.findByIdAndUserId(id, userId)
            .orElseThrow(() -> new IllegalArgumentException("Tag not found"));
        tagRepository.delete(tag);
    }
}
