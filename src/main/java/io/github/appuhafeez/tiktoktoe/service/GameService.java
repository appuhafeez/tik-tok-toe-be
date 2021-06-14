package io.github.appuhafeez.tiktoktoe.service;


import static io.github.appuhafeez.tiktoktoe.constants.GameConstants.blankPageImageUrl;
import static io.github.appuhafeez.tiktoktoe.constants.GameConstants.endingValue;
import static io.github.appuhafeez.tiktoktoe.constants.GameConstants.playerOImageUrl;
import static io.github.appuhafeez.tiktoktoe.constants.GameConstants.playerXImageUrl;
import static io.github.appuhafeez.tiktoktoe.constants.GameConstants.startingValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.github.appuhafeez.tiktoktoe.constants.GameConstants;
import io.github.appuhafeez.tiktoktoe.constants.PlayerEnum;
import io.github.appuhafeez.tiktoktoe.model.AddHistoryRequest;
import io.github.appuhafeez.tiktoktoe.model.GameMoveRequest;
import io.github.appuhafeez.tiktoktoe.model.GameResponseDto;
import io.github.appuhafeez.tiktoktoe.model.RedisGamePojo;
import io.github.appuhafeez.tiktoktoe.proxy.HistoryMaintainerServiceProxy;
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

	@Autowired
	private HistoryMaintainerServiceProxy historyMaintainerServiceProxy;

	@Value("${game.inactive.alive.minutes}")
	private long gameTimeInMinutes;

	public boolean isGameExists(int gameCode) {
		return redisRepo.isGameExists(gameCode+"");
	}

	public int getGameCode(Authentication authentication) throws JsonProcessingException {
		List<Integer> gameSpaces = new ArrayList<>();
		List<String> gameOccupiedPlayerIcon = new ArrayList<>();
		for(int i=0;i<9;i++) {
			gameSpaces.add(100);
			gameOccupiedPlayerIcon.add(blankPageImageUrl);
		}
		int gameCode = getUniqueGameCode();
		Map<String, String> players = new HashMap<>();
		players = addPlayer(authentication, players,PlayerEnum.O.name());
		RedisGamePojo redisGamePojo = RedisGamePojo.builder().gameCode(gameCode).timeToLive((long)gameTimeInMinutes).lastMoveBy(PlayerEnum.O).players(players)
				.isGameStarted(false).spacesOccupiedBy(gameSpaces).spacesOccupiedPlayerIcons(gameOccupiedPlayerIcon).isGameStarted(false).build();
		log.info("inserting data to cache :: {}",redisGamePojo);
		redisRepo.save(redisGamePojo,redisGamePojo.getGameCode()+"");

		return gameCode;
	}

	private  Map<String, String> addPlayer(Authentication authentication, Map<String, String> players, String playerIcon) {
		log.info("Authentication object :: {}",authentication);
		if(players==null) {
			players = new HashMap<>();
		}
		if(authentication!=null && players.get(playerIcon)==null) {
			players.put(playerIcon, authentication.getName());
		}
		return players;
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

	public void makeAMove(GameMoveRequest gameMoveRequest,Authentication authentication){
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
			Map<String, String> players = redisGamePojo.getPlayers();
			addPlayer(authentication, players, gameMoveRequest.getMovedBy().name());
			redisGamePojo.setPlayers(players);
			redisGamePojo.setLastMoveBy(gameMoveRequest.getMovedBy());
			redisGamePojo.setTimeToLive(gameTimeInMinutes);
			redisRepo.save(redisGamePojo,gameMoveRequest.getGameCode()+"");
			GameResponseDto gameResponseDto = GameStatusCalculationUtil.calculateGameStatus(redisGamePojo);

			if(gameResponseDto.isGameCompleted()) {
				addHistory(gameResponseDto);
			}

			template.convertAndSend("/topic/"+gameMoveRequest.getGameCode(),gameResponseDto);
		}
	}

	private void addHistory(GameResponseDto gameResponseDto) {
		AddHistoryRequest addHistoryRequest = new AddHistoryRequest();
		RedisGamePojo redisGamePojo = gameResponseDto.getGameData();
		Map<String, String> players = redisGamePojo.getPlayers();
		if(!players.isEmpty()) {
			log.info("player :: {}",players);
			if(gameResponseDto.getWhoWonGame()!=null && gameResponseDto.getWhoWonGame().equals(GameConstants.playerOImageUrl)) {
				setHistoryFields(addHistoryRequest, players,PlayerEnum.O.name(), PlayerEnum.X.name());
			}else if(gameResponseDto.getWhoWonGame()!=null && gameResponseDto.getWhoWonGame().equals(GameConstants.playerXImageUrl)) {
				setHistoryFields(addHistoryRequest, players, PlayerEnum.X.name(), PlayerEnum.O.name());
			}else if(gameResponseDto.isTie()){
				if(players.get(PlayerEnum.O.name())!=null) {
					addHistoryRequest.setUsername(players.get(PlayerEnum.O.name()));
					addHistoryRequest.setPlayedWith(players.get(PlayerEnum.X.name()));
				}else {
					addHistoryRequest.setUsername(players.get(PlayerEnum.X.name()));
				}
				addHistoryRequest.setIsMatchTie(Boolean.TRUE);
			}
			historyMaintainerServiceProxy.addHistory(addHistoryRequest);
		}
	}

	private void setHistoryFields(AddHistoryRequest addHistoryRequest, Map<String, String> players,String wonPlayer, String lostPlayer) {
		if(players.get(wonPlayer)!=null) {
			addHistoryRequest.setUsername(players.get(wonPlayer));
			addHistoryRequest.setDidUserWon(true);
			addHistoryRequest.setPlayedWith(players.get(lostPlayer));
		}else {
			addHistoryRequest.setUsername(players.get(lostPlayer));
			addHistoryRequest.setDidUserWon(false);
			addHistoryRequest.setPlayedWith(players.get(wonPlayer));
		}
	}


	public boolean startGame(int gameCode,Authentication authentication) {
		RedisGamePojo redisGamePojo = redisRepo.getGameData(gameCode+"");
		if(redisGamePojo.isGameStarted()) {
			log.info("Game already started : {}",gameCode);
			return false;
		}
		Map<String, String> players = redisGamePojo.getPlayers();
		players = addPlayer(authentication, players, PlayerEnum.X.name());
		redisGamePojo.setPlayers(players);
		redisGamePojo.setGameStarted(true);
		redisRepo.save(redisGamePojo, gameCode+"");
		GameResponseDto gameResponseDto = GameStatusCalculationUtil.calculateGameStatus(redisGamePojo);
		template.convertAndSend("/topic/"+gameCode,gameResponseDto);
		return true;
	}
}
