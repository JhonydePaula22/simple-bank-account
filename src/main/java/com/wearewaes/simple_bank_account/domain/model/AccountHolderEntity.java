package com.wearewaes.simple_bank_account.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "holder", schema = "accounts")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountHolderEntity {

        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        private UUID id;

        @Column(name = "provided_id", nullable = false)
        private String identification;

        @Column(name = "first_name", nullable = false)
        private String firstName;

        @Column(name = "last_name", nullable = false)
        private String lastName;

        @Column(nullable = false)
        private String email;

        @Column(nullable = false)
        private String phone;

        @Column(nullable = false)
        private String address;
}
