package com.concurent_MoneyTransfert.MoneyTransfert;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MoneyTransfertApplication {

	public static void main(String[] args) {
		SpringApplication.run(MoneyTransfertApplication.class, args);
	}

}
