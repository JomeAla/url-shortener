package com.jomea.urlshortener.service;

import com.jomea.urlshortener.entity.User;
import com.jomea.urlshortener.entity.Workspace;
import com.jomea.urlshortener.entity.WorkspaceMember;
import com.jomea.urlshortener.repository.UserRepository;
import com.jomea.urlshortener.repository.WorkspaceMemberRepository;
import com.jomea.urlshortener.repository.WorkspaceRepository;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class WorkspaceService {

    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository memberRepository;
    private final UserRepository userRepository;

    public WorkspaceService(WorkspaceRepository workspaceRepository,
                             WorkspaceMemberRepository memberRepository,
                             UserRepository userRepository) {
        this.workspaceRepository = workspaceRepository;
        this.memberRepository = memberRepository;
        this.userRepository = userRepository;
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) return null;
        return userRepository.findByEmail(auth.getName()).orElse(null);
    }

    private String slugify(String name) {
        return name.toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", "");
    }

    public List<Map<String, Object>> getUserWorkspaces() {
        User user = getCurrentUser();
        if (user == null) return List.of();
        List<Workspace> owned = workspaceRepository.findByOwnerIdOrderByCreatedAtAsc(user.getId());
        List<WorkspaceMember> memberOf = memberRepository.findByUserId(user.getId());
        List<Long> memberWorkspaceIds = memberOf.stream().map(WorkspaceMember::getWorkspaceId).toList();
        List<Workspace> all = workspaceRepository.findAllById(memberWorkspaceIds);
        all.addAll(owned);
        return all.stream().distinct().map(w -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", w.getId());
            m.put("name", w.getName());
            m.put("slug", w.getSlug());
            m.put("ownerId", w.getOwnerId());
            m.put("isOwner", w.getOwnerId().equals(user.getId()));
            m.put("createdAt", w.getCreatedAt().toString());
            return m;
        }).toList();
    }

    public Workspace createWorkspace(String name) {
        User user = getCurrentUser();
        if (user == null) throw new IllegalArgumentException("Authentication required");
        String slug = slugify(name);
        if (slug.isBlank()) throw new IllegalArgumentException("Invalid workspace name");
        if (workspaceRepository.findBySlug(slug).isPresent()) {
            slug = slug + "-" + System.currentTimeMillis();
        }
        Workspace ws = new Workspace();
        ws.setName(name);
        ws.setSlug(slug);
        ws.setOwnerId(user.getId());
        ws.setCreatedAt(LocalDateTime.now());
        workspaceRepository.save(ws);
        return ws;
    }

    public Workspace updateWorkspace(Long id, String name) {
        User user = getCurrentUser();
        if (user == null) throw new IllegalArgumentException("Authentication required");
        Workspace ws = workspaceRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Workspace not found"));
        if (!ws.getOwnerId().equals(user.getId()))
            throw new IllegalArgumentException("Only the owner can update the workspace");
        if (name != null && !name.isBlank()) {
            ws.setName(name);
            ws.setSlug(slugify(name));
        }
        ws.setUpdatedAt(LocalDateTime.now());
        return workspaceRepository.save(ws);
    }

    public void deleteWorkspace(Long id) {
        User user = getCurrentUser();
        if (user == null) throw new IllegalArgumentException("Authentication required");
        Workspace ws = workspaceRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Workspace not found"));
        if (!ws.getOwnerId().equals(user.getId()))
            throw new IllegalArgumentException("Only the owner can delete the workspace");
        memberRepository.findByWorkspaceId(id).forEach(m -> memberRepository.delete(m));
        workspaceRepository.delete(ws);
    }

    public List<Map<String, Object>> getMembers(Long workspaceId) {
        Workspace ws = workspaceRepository.findById(workspaceId)
            .orElseThrow(() -> new IllegalArgumentException("Workspace not found"));
        User user = getCurrentUser();
        if (user == null) throw new IllegalArgumentException("Authentication required");
        boolean isOwner = ws.getOwnerId().equals(user.getId());
        boolean isMember = memberRepository.findByWorkspaceIdAndUserId(workspaceId, user.getId()).isPresent();
        if (!isOwner && !isMember) throw new IllegalArgumentException("Access denied");

        return memberRepository.findByWorkspaceId(workspaceId).stream().map(m -> {
            var u = userRepository.findById(m.getUserId()).orElse(null);
            Map<String, Object> map = new HashMap<>();
            map.put("id", m.getId());
            map.put("userId", m.getUserId());
            map.put("email", u != null ? u.getEmail() : "unknown");
            map.put("name", u != null ? u.getName() : "Unknown");
            map.put("role", m.getRole());
            map.put("joinedAt", m.getJoinedAt().toString());
            return map;
        }).toList();
    }

    public WorkspaceMember addMember(Long workspaceId, String email, String role) {
        User user = getCurrentUser();
        if (user == null) throw new IllegalArgumentException("Authentication required");
        Workspace ws = workspaceRepository.findById(workspaceId)
            .orElseThrow(() -> new IllegalArgumentException("Workspace not found"));
        if (!ws.getOwnerId().equals(user.getId()))
            throw new IllegalArgumentException("Only the owner can add members");

        User invited = userRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (memberRepository.findByWorkspaceIdAndUserId(workspaceId, invited.getId()).isPresent())
            throw new IllegalArgumentException("User is already a member");

        WorkspaceMember m = new WorkspaceMember();
        m.setWorkspaceId(workspaceId);
        m.setUserId(invited.getId());
        m.setRole(role != null ? role : "member");
        m.setJoinedAt(LocalDateTime.now());
        return memberRepository.save(m);
    }

    public void removeMember(Long workspaceId, Long userId) {
        User user = getCurrentUser();
        if (user == null) throw new IllegalArgumentException("Authentication required");
        Workspace ws = workspaceRepository.findById(workspaceId)
            .orElseThrow(() -> new IllegalArgumentException("Workspace not found"));
        if (!ws.getOwnerId().equals(user.getId()))
            throw new IllegalArgumentException("Only the owner can remove members");
        memberRepository.deleteByWorkspaceIdAndUserId(workspaceId, userId);
    }
}
