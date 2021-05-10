package io.github.appuhafeez.tiktoktoe.service;


import static io.github.appuhafeez.tiktoktoe.constants.GameConstants.blankPageImageUrl;
import static io.github.appuhafeez.tiktoktoe.constants.GameConstants.endingValue;
import static io.github.appuhafeez.tiktoktoe.constants.GameConstants.playerOImageUrl;
import static io.github.appuhafeez.tiktoktoe.constants.GameConstants.playerXImageUrl;
import static io.github.appuhafeez.tiktoktoe.constants.GameConstants.startingValue;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.github.appuhafeez.tiktoktoe.constants.PlayerEnum;
import io.github.appuhafeez.tiktoktoe.model.GameMoveRequest;
import io.github.appuhafeez.tiktoktoe.model.GameResponseDto;
import io.github.appuhafeez.tiktoktoe.model.RedisGamePojo;
import io.github.appuhafeez.tiktoktoe.repository.RedisRepository;
import io.github.appuhafeez.tiktoktoe.util.GameStatusCalculationUtil;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class GameService {
	
	@Autowired
	private RedisRepository redisRepo;

	@Autowired
	private SimpMessagingTemplate template;

	@Value("${game.inactive.alive.minutes}")
	private long gameTimeInMinutes;

	public boolean isGameExists(int gameCode) {
		return redisRepo.isGameExists(gameCode+"");
	}

	public int getGameCode() throws JsonProcessingException {
		List<Integer> gameSpaces = new ArrayList<>();
		List<String> gameOccupiedPlayerIcon = new ArrayList<>();
		for(int i=0;i<9;i++) {
			gameSpaces.add(100);
			gameOccupiedPlayerIcon.add(blankPageImageUrl);
		}
		int gameCode = getUniqueGameCode();
		RedisGamePojo redisGamePojo = RedisGamePojo.builder().gameCode(gameCode).timeToLive((long)gameTimeInMinutes).lastMoveBy(PlayerEnum.O)
				.isGameStarted(false).spacesOccupiedBy(gameSpaces).spacesOccupiedPlayerIcons(gameOccupiedPlayerIcon).isGameStarted(false).build();

		redisRepo.save(redisGamePojo,redisGamePojo.getGameCode()+"");

		return gameCode;
	}

	private int getUniqueGameCode() {
		int gameCode = 0;
		boolean gameExists = true;
		while(gameExists) {
			gameCode = RandomUtils.nextInt(startingValue, endingValue);
			if(!redisRepo.isGameExists(gameCode+"")) {
				gameExists = false;
			}
		}
		return gameCode;
	}

	public GameResponseDto getGameData(int gameCode) {
		RedisGamePojo redisGamePojo = redisRepo.getGameData(gameCode+"");
		GameResponseDto gameResponseDto = GameStatusCalculationUtil.calculateGameStatus(redisGamePojo);
		return gameResponseDto;
	}

	public void makeAMove(GameMoveRequest gameMoveRequest){
		RedisGamePojo redisGamePojo = redisRepo.getGameData(gameMoveRequest.getGameCode()+"");
		log.info("Game space icon: {} and game data: {}",redisGamePojo.getSpacesOccupiedPlayerIcons().get(gameMoveRequest.getPosition()),redisGamePojo.toString());
		if(redisGamePojo!=null && redisGamePojo.getSpacesOccupiedPlayerIcons().get(gameMoveRequest.getPosition()).equals(blankPageImageUrl)
				&& !redisGamePojo.getLastMoveBy().equals(gameMoveRequest.getMovedBy())) {
			log.info("Proceeding with game move: player {}",gameMoveRequest.getMovedBy());
			if(gameMoveRequest.getMovedBy().equals(PlayerEnum.O)) {
				redisGamePojo.getSpacesOccupiedBy().set(gameMoveRequest.getPosition(), 0);
				redisGamePojo.getSpacesOccupiedPlayerIcons().set(gameMoveRequest.getPosition(), playerOImageUrl);
			}else if(gameMoveRequest.getMovedBy().equals(PlayerEnum.X)) {
				redisGamePojo.getSpacesOccupiedBy().set(gameMoveRequest.getPosition(), 1);
				redisGamePojo.getSpacesOccupiedPlayerIcons().set(gameMoveRequest.getPosition(), playerXImageUrl);
			}
			redisGamePojo.setLastMoveBy(gameMoveRequest.getMovedBy());
			redisGamePojo.setTimeToLive(gameTimeInMinutes);
			redisRepo.save(redisGamePojo,gameMoveRequest.getGameCode()+"");
			GameResponseDto gameResponseDto = GameStatusCalculationUtil.calculateGameStatus(redisGamePojo);
			template.convertAndSend("/topic/"+gameMoveRequest.getGameCode(),gameResponseDto);
		}
	}
	
	public boolean startGame(int gameCode) {
		RedisGamePojo redisGamePojo = redisRepo.getGameData(gameCode+"");
		if(redisGamePojo.isGameStarted()) {
			log.info("Game already started : {}",gameCode);
			return false;
		}
		redisGamePojo.setGameStarted(true);
		redisRepo.save(redisGamePojo, gameCode+"");
		GameResponseDto gameResponseDto = GameStatusCalculationUtil.calculateGameStatus(redisGamePojo);
		template.convertAndSend("/topic/"+gameCode,gameResponseDto);
		return true;
	}
}
