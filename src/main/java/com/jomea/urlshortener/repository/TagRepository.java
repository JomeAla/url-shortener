package com.jomea.urlshortener.repository;

import com.jomea.urlshortener.entity.Tag;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {

    List<Tag> findByUserIdOrderByCreatedAtAsc(Long userId);

    Optional<Tag> findByIdAndUserId(Long id, Long userId);

    Optional<Tag> findByUserIdAndNameIgnoreCase(Long userId, String name);
}
