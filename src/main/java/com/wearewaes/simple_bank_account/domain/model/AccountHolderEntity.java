package com.wearewaes.simple_bank_account.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "holder", schema = "accounts")
public record AccountHolderEntity(
        @Id @GeneratedValue(strategy = GenerationType.AUTO)
        UUID id,

        @Column(name = "first_name", nullable = false)
        String firstName,

        @Column(name = "last_name", nullable = false)
        String lastName,

        @Column(nullable = false)
        String email,

        @Column(nullable = false)
        String phone,

        @Column(nullable = false)
        String address
) {}
