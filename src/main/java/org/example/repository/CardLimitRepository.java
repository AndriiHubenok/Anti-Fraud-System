package org.example.repository;

import org.example.model.CardLimit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CardLimitRepository extends JpaRepository<CardLimit, Long> {
}
