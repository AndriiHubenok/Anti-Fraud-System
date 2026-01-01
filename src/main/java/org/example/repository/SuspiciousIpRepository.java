package org.example.repository;

import org.example.model.SuspiciousIp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SuspiciousIpRepository extends JpaRepository<SuspiciousIp, Long> {
    List<SuspiciousIp> findAllByOrderByIdAsc();
    boolean deleteByIp(String ip);
    Optional<SuspiciousIp> findByIp(String ip);
}
