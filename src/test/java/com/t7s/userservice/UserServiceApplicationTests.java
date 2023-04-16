package com.t7s.userservice;

import com.t7s.userservice.registration.RegistrationRequest;
import com.t7s.userservice.registration.RegistrationService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class UserServiceApplicationTests {

	@Autowired
	private RegistrationService registrationService;

	@Test
	void contextLoads() {
		Assertions.assertThat(registrationService).isNotNull();
	}

	@Test
	void registrationTest() {
		RegistrationRequest registrationRequest = new RegistrationRequest("username", "password", "test@email.com");

		String result = registrationService.register(registrationRequest);

		Assertions.assertThat(result).isEqualTo("Successss");
	}

}
