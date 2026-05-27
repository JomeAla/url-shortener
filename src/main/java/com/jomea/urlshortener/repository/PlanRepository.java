package com.jomea.urlshortener.repository;

import com.jomea.urlshortener.entity.Plan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlanRepository extends JpaRepository<Plan, Long> {

    Optional<Plan> findBySlug(String slug);

    List<Plan> findByActiveTrueOrderBySortOrderAsc();

    boolean existsBySlug(String slug);
}
