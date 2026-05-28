package com.jomea.urlshortener.repository;

import com.jomea.urlshortener.entity.CustomDomain;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CustomDomainRepository extends JpaRepository<CustomDomain, Long> {
    List<CustomDomain> findByUserIdOrderByCreatedAtDesc(Long userId);
    Optional<CustomDomain> findByDomain(String domain);
    Optional<CustomDomain> findByDomainAndVerifiedTrue(String domain);
    boolean existsByDomain(String domain);
    boolean existsByUserIdAndVerifiedTrue(Long userId);
}
