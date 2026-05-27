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
}
