package io.github.appuhafeez.tiktoktoe.repository;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RedisRepository {
	
	@Value("${redis.namespace:tiktoktoe-game-data}")
	private String namespace;
	
	@Value("${game.inactive.alive.minutes}")
	private long timeToLive;
	
	@Autowired
	private RedisTemplate<String, Object> redisTemplate;
	
	public boolean isGameExists(String key) {
		return redisTemplate.hasKey(generateRedisKey(key));
	}
	
	public void save(Object redisGamePojo,String key) {
		String redisKey =  generateRedisKey(key);
		log.info("creating or updating game: {}",redisKey);
		redisTemplate.opsForValue().set(redisKey,redisGamePojo, timeToLive,TimeUnit.MINUTES);
	}
	
	public String generateRedisKey(String gameCode) {
		return namespace+":"+gameCode;
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getGameData(String gameCode){
		String redisKey = generateRedisKey(gameCode);
		return (T) redisTemplate.opsForValue().get(redisKey);
	}
}
