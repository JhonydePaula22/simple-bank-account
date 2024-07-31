package com.wearewaes.simple_bank_account.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "account", schema = "accounts")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountEntity{

        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        private UUID id;

        @ManyToOne
        @JoinColumn(name = "holder_id", nullable = false)
        private AccountHolderEntity holder;

        @Column(nullable = false)
        private String number;

        @Column(nullable = false, precision = 10, scale = 2)
        private BigDecimal balance = BigDecimal.ZERO;
}

