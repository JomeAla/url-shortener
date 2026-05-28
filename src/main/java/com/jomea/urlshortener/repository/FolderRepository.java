package com.jomea.urlshortener.repository;

import com.jomea.urlshortener.entity.Folder;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FolderRepository extends JpaRepository<Folder, Long> {

    List<Folder> findByUserIdOrderByCreatedAtAsc(Long userId);

    Optional<Folder> findByIdAndUserId(Long id, Long userId);

    List<Folder> findByUserIdAndParentFolderIdOrderByCreatedAtAsc(Long userId, Long parentFolderId);
}
