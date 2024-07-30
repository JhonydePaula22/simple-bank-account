package com.wearewaes.simple_bank_account.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "card", schema = "accounts")
public record Card(
        @Id
        long number,

        @Column(nullable = false, length = 4)
        String cvv,

        @Column(nullable = false)
        String type,

        @ManyToOne
        @JoinColumn(name = "account_id", nullable = false)
        Account account
) {}
