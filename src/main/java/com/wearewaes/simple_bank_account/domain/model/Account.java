package com.wearewaes.simple_bank_account.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "account", schema = "accounts")
public record Account(
        @Id @GeneratedValue(strategy = GenerationType.AUTO)
        UUID id,

        @ManyToOne
        @JoinColumn(name = "holder_id", nullable = false)
        AccountHolder holder,

        @Column(nullable = false)
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        int number,

        @Column(nullable = false, precision = 10, scale = 2)
        BigDecimal balance
) {}
