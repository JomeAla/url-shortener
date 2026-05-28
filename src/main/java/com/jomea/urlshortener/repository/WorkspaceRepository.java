package com.jomea.urlshortener.repository;

import com.jomea.urlshortener.entity.Workspace;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkspaceRepository extends JpaRepository<Workspace, Long> {
    Optional<Workspace> findBySlug(String slug);
    List<Workspace> findByOwnerIdOrderByCreatedAtAsc(Long ownerId);
}
