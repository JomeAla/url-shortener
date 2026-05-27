package com.jomea.urlshortener.repository;

import com.jomea.urlshortener.entity.ClickEvent;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClickEventRepository extends JpaRepository<ClickEvent, Long> {

    List<ClickEvent> findByShortCodeOrderByTimestampDesc(String shortCode);

    List<ClickEvent> findByShortCodeAndTimestampBetweenOrderByTimestampAsc(String shortCode, LocalDateTime from, LocalDateTime to);

    long countByShortCode(String shortCode);
}
