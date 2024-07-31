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
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transaction", schema = "accounts")
public record TransactionEntity(
        @Id @GeneratedValue(strategy = GenerationType.AUTO)
        UUID id,

        @Column(nullable = false)
        LocalDateTime timestamp,

        @Column(nullable = false)
        String type,

        @Column(nullable = false, precision = 10, scale = 2)
        BigDecimal amount,

        @Column(name = "credit_card_fee_amount", precision = 10, scale = 2)
        BigDecimal creditCardFeeAmount,

        @Column(name = "credit_card_fee", precision = 5, scale = 2)
        BigDecimal creditCardFee,

        @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
        BigDecimal totalAmount,

        @ManyToOne
        @JoinColumn(name = "account_id", nullable = false)
        AccountEntity accountEntity,

        @Column(name = "account_balance", nullable = false, precision = 10, scale = 2)
        BigDecimal accountBalance,

        @ManyToOne
        @JoinColumn(name = "card_id", nullable = false)
        CardEntity cardEntity,

        @Column(name = "ref_transaction", nullable = false)
        UUID refTransaction
) {}
