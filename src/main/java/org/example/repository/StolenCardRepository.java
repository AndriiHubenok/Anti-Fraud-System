package org.example.repository;

import org.example.model.StolenCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StolenCardRepository extends JpaRepository<StolenCard, Long> {
    boolean deleteByNumber(String number);
    List<StolenCard> findAllByOrderByIdAsc();
    Optional<StolenCard> findByNumber(String number);
}
