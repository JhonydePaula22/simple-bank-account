package com.wearewaes.simple_bank_account.domain.model;

import com.wearewaes.model.CardTypeEnum;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "`card`", schema = "accounts")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CardEntity{

        @Id
        private Long number;

        @Column(nullable = false, length = 4)
        private String cvv;

        @Column(nullable = false)
        private CardTypeEnum type;

        @ManyToOne
        @JoinColumn(name = "account_id", nullable = false)
        private AccountEntity account;
}

