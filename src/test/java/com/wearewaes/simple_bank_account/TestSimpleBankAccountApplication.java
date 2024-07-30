package com.wearewaes.simple_bank_account;

import org.springframework.boot.SpringApplication;

public class TestSimpleBankAccountApplication {

	public static void main(String[] args) {
		SpringApplication.from(SimpleBankAccountApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
