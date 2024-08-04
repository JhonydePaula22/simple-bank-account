package com.wearewaes.simple_bank_account.domain.model;

import com.wearewaes.model.CardTypeEnum;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "fee", schema = "accounts")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CardFeeEntity {

    @Id
    @Enumerated(EnumType.STRING)
    private CardTypeEnum type;

    @Column(name = "fee", precision = 5, scale = 2)
    private BigDecimal fee;
}
