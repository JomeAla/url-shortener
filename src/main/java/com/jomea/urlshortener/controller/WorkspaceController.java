package com.jomea.urlshortener.controller;

import com.jomea.urlshortener.service.WorkspaceService;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/workspaces")
public class WorkspaceController {

    private final WorkspaceService workspaceService;

    public WorkspaceController(WorkspaceService workspaceService) {
        this.workspaceService = workspaceService;
    }

    @GetMapping
    public ResponseEntity<?> list() {
        return ResponseEntity.ok(workspaceService.getUserWorkspaces());
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body) {
        try {
            String name = (String) body.get("name");
            if (name == null || name.isBlank())
                return ResponseEntity.badRequest().body(Map.of("error", "Name is required"));
            var ws = workspaceService.createWorkspace(name);
            return ResponseEntity.ok(Map.of("id", ws.getId(), "name", ws.getName(), "slug", ws.getSlug()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        try {
            String name = (String) body.get("name");
            var ws = workspaceService.updateWorkspace(id, name);
            return ResponseEntity.ok(Map.of("id", ws.getId(), "name", ws.getName(), "slug", ws.getSlug()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            workspaceService.deleteWorkspace(id);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}/members")
    public ResponseEntity<?> getMembers(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(workspaceService.getMembers(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/members")
    public ResponseEntity<?> addMember(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        try {
            String email = (String) body.get("email");
            if (email == null || email.isBlank())
                return ResponseEntity.badRequest().body(Map.of("error", "Email is required"));
            String role = (String) body.get("role");
            var member = workspaceService.addMember(id, email, role);
            return ResponseEntity.ok(Map.of("id", member.getId(), "userId", member.getUserId(), "role", member.getRole()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{workspaceId}/members/{userId}")
    public ResponseEntity<?> removeMember(@PathVariable Long workspaceId, @PathVariable Long userId) {
        try {
            workspaceService.removeMember(workspaceId, userId);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
