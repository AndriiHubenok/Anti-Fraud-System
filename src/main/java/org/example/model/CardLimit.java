package org.example.model;

import jakarta.persistence.*;

@Entity
@Table(name = "card_limits")
public class CardLimit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "allowed_limit")
    private Long allowedLimit;

    @Column(name = "manual_limit")
    private Long manualLimit;

    public CardLimit() {}

    public CardLimit(Long allowedLimit, Long manualLimit) {
        this.allowedLimit = allowedLimit;
        this.manualLimit = manualLimit;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getAllowedLimit() { return allowedLimit; }
    public void setAllowedLimit(Long allowedLimit) { this.allowedLimit = allowedLimit; }

    public Long getManualLimit() { return manualLimit; }
    public void setManualLimit(Long manualLimit) { this.manualLimit = manualLimit; }
}
