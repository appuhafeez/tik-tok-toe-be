package io.github.appuhafeez.tiktoktoe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

@SpringBootApplication
@EnableRedisRepositories
public class TicTokToeApplication {

	public static void main(String[] args) {
		SpringApplication.run(TicTokToeApplication.class, args);
	}

}
