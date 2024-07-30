package com.wearewaes.simple_bank_account;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class SimpleBankAccountApplicationTests {

	@Test
	void contextLoads() {
	}

}
