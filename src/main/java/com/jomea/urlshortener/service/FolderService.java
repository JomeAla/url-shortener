package com.jomea.urlshortener.service;

import com.jomea.urlshortener.entity.Folder;
import com.jomea.urlshortener.entity.User;
import com.jomea.urlshortener.repository.FolderRepository;
import com.jomea.urlshortener.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class FolderService {

    private final FolderRepository folderRepository;
    private final UserRepository userRepository;

    public FolderService(FolderRepository folderRepository, UserRepository userRepository) {
        this.folderRepository = folderRepository;
        this.userRepository = userRepository;
    }

    private Long getUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) return null;
        return userRepository.findByEmail(auth.getName()).map(User::getId).orElse(null);
    }

    public List<Folder> getUserFolders() {
        Long userId = getUserId();
        if (userId == null) return List.of();
        return folderRepository.findByUserIdOrderByCreatedAtAsc(userId);
    }

    public Folder createFolder(String name, Long parentFolderId, String color) {
        Long userId = getUserId();
        if (userId == null) throw new IllegalArgumentException("Authentication required");
        Folder folder = new Folder();
        folder.setName(name);
        folder.setUserId(userId);
        folder.setParentFolderId(parentFolderId);
        folder.setColor(color);
        folder.setCreatedAt(LocalDateTime.now());
        return folderRepository.save(folder);
    }

    public Folder updateFolder(Long id, String name, Long parentFolderId, String color) {
        Long userId = getUserId();
        if (userId == null) throw new IllegalArgumentException("Authentication required");
        Folder folder = folderRepository.findByIdAndUserId(id, userId)
            .orElseThrow(() -> new IllegalArgumentException("Folder not found"));
        if (name != null) folder.setName(name);
        if (parentFolderId != null) folder.setParentFolderId(parentFolderId);
        if (color != null) folder.setColor(color);
        return folderRepository.save(folder);
    }

    public void deleteFolder(Long id) {
        Long userId = getUserId();
        if (userId == null) throw new IllegalArgumentException("Authentication required");
        Folder folder = folderRepository.findByIdAndUserId(id, userId)
            .orElseThrow(() -> new IllegalArgumentException("Folder not found"));
        folderRepository.delete(folder);
    }
}
