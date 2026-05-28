package com.jomea.urlshortener.repository;

import com.jomea.urlshortener.entity.Url;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface UrlRepository extends JpaRepository<Url, Long> {

    Optional<Url> findByShortCode(String shortCode);

    Optional<Url> findByCustomCode(String customCode);

    List<Url> findAllByOrderByCreatedAtDesc();

    List<Url> findByShortCodeContainingOrLongUrlContainingAllIgnoreCase(String shortCode, String longUrl, Sort sort);

    List<Url> findByCreatedAtBetween(LocalDateTime from, LocalDateTime to, Sort sort);

    @Modifying
    @Transactional
    @Query("UPDATE Url u SET u.clickCount = u.clickCount + 1 WHERE u.shortCode = :code")
    void incrementClickCount(@Param("code") String shortCode);

    @Query("SELECT COALESCE(SUM(u.clickCount), 0) FROM Url u")
    long sumClickCount();

    long countByUserId(Long userId);

    List<Url> findByUserIdOrderByCreatedAtDesc(Long userId);

    @Query("SELECT COALESCE(SUM(u.clickCount), 0) FROM Url u WHERE u.userId = :userId")
    long sumClickCountByUserId(@Param("userId") Long userId);

    List<Url> findByTagsContainingIgnoreCase(String tag);

    @Query("SELECT u FROM Url u WHERE u.userId = :userId AND u.deletedAt IS NOT NULL ORDER BY u.deletedAt DESC")
    List<Url> findTrashedByUserId(@Param("userId") Long userId);

    @Query("SELECT u FROM Url u WHERE u.deletedAt IS NOT NULL AND u.deletedAt < :before")
    List<Url> findDeletedBefore(@Param("before") LocalDateTime before);

    List<Url> findByUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(Long userId);

    List<Url> findByUserIdOrderByCreatedAtDesc(Long userId, org.springframework.data.domain.Pageable pageable);
}
