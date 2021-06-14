package io.github.appuhafeez.tiktoktoe.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.github.appuhafeez.tiktoktoe.exception.RequestValidationException;
import io.github.appuhafeez.tiktoktoe.model.GameMoveRequest;
import io.github.appuhafeez.tiktoktoe.model.GameResponseDto;
import io.github.appuhafeez.tiktoktoe.service.GameService;
import io.github.appuhafeez.tiktoktoe.util.GameStatusCalculationUtil;
import lombok.extern.slf4j.Slf4j;

//@CrossOrigin(origins = "${allowed.origins}",allowedHeaders = "*")
@RestController
@RequestMapping("/game")
@Slf4j
public class GameController {
	
	@Autowired
	private GameService gameService;
	
	@GetMapping("/create/new")
	public int createNewGame(@AuthenticationPrincipal Authentication authentication) throws JsonProcessingException {
		return gameService.getGameCode(authentication);
	}
	
	@PostMapping("/continue/move")
	public ResponseEntity<Void> continueMove(@AuthenticationPrincipal Authentication authentication,@Valid @RequestBody GameMoveRequest gameMoveRequest,BindingResult bindingResult){
		if(bindingResult.hasErrors()) {
			throw new RequestValidationException(GameStatusCalculationUtil.getErrorMessage(bindingResult));
		}
		if(gameMoveRequest.getMovedBy()==null) {
			throw new RequestValidationException(GameStatusCalculationUtil.getErrorMessage("movedBy should not be null"));
		}
		if(!gameService.isGameExists(gameMoveRequest.getGameCode())) {
			return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
		}else {
			gameService.makeAMove(gameMoveRequest,authentication);
			return new ResponseEntity<Void>(HttpStatus.OK);
		}
	}
	
	@GetMapping("/data/{gameCode}")
	public ResponseEntity<GameResponseDto> getGameData(@PathVariable("gameCode") int gameCode){
		if(!gameService.isGameExists(gameCode)) {
			return new ResponseEntity<GameResponseDto>(HttpStatus.NO_CONTENT);
		}else {
			GameResponseDto gameResponseDto = gameService.getGameData(gameCode);
			return new ResponseEntity<GameResponseDto>(gameResponseDto,HttpStatus.OK);
		}
	}
	
	@PutMapping("/start/{gameCode}")
	public ResponseEntity<Void> startGame(@AuthenticationPrincipal Authentication authentication,@PathVariable("gameCode") int gameCode){
		log.info("Game code to start: {}",gameCode);
		if(gameCode<100000 || gameCode>999999) {
			throw new RequestValidationException(GameStatusCalculationUtil.getErrorMessage("Invalid gamecode"));
		}
		if(!gameService.isGameExists(gameCode)) {
			return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
		}else {
			if(gameService.startGame(gameCode,authentication)) {
				return new ResponseEntity<Void>(HttpStatus.OK);
			}else {
				return new ResponseEntity<Void>(HttpStatus.ALREADY_REPORTED);
			}
		}
	}
}
