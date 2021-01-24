package io.github.appuhafeez.tiktoktoe.util;

import static io.github.appuhafeez.tiktoktoe.constants.GameConstants.gameWinningCount;
import static io.github.appuhafeez.tiktoktoe.constants.GameConstants.playerOImageUrl;
import static io.github.appuhafeez.tiktoktoe.constants.GameConstants.playerXImageUrl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;

import io.github.appuhafeez.tiktoktoe.model.ErrorResponse;
import io.github.appuhafeez.tiktoktoe.model.GameResponseDto;
import io.github.appuhafeez.tiktoktoe.model.RedisGamePojo;

public class GameStatusCalculationUtil {

	public static GameResponseDto calculateGameStatus(RedisGamePojo redisGamePojo) {
		List<Integer> gameSpaceOccupiedBy = redisGamePojo.getSpacesOccupiedBy();
		int[][] convertedGameData = getGridConvertedGameData(gameSpaceOccupiedBy);
		GameResponseDto gameResponseDto = applyGameIntelligence(convertedGameData);
		gameResponseDto.setGameData(redisGamePojo);
		return gameResponseDto;
	}

	private static GameResponseDto applyGameIntelligence(int[][] convertedGameData) {
		GameResponseDto responseDto = new GameResponseDto();
		responseDto.setGameCompleted(false);
		responseDto = checkPosibilityOneAndTwo(convertedGameData, responseDto, true);
		if(responseDto.isGameCompleted()) {
			return responseDto;
		}
		responseDto = checkPosibilityOneAndTwo(convertedGameData, responseDto, false);
		if(responseDto.isGameCompleted()) {
			return responseDto;
		}
		responseDto = checkPosibilityThree(convertedGameData,responseDto);
		if(responseDto.isGameCompleted()) {
			return responseDto;
		}
		responseDto = checkPosibilityFour(convertedGameData, responseDto);
		return responseDto;
	}
	
	private static GameResponseDto checkPosibilityThree(int[][] convertedGameData, GameResponseDto responseDto) {
		int count = 0;
		for(int i=0;i<convertedGameData.length;i++) {
			count = count + convertedGameData[i][i];
		}
		count = checkCountForGameWin(responseDto, count);
		return responseDto;
	}
	
	private static GameResponseDto checkPosibilityFour(int[][] convertedGameData, GameResponseDto responseDto) {
		int count = 0;
		for(int i=convertedGameData.length-1,columnCount=0;i>=0 || columnCount<convertedGameData.length;i--,columnCount++) {
			count=count+convertedGameData[columnCount][i];
		}
		count = checkCountForGameWin(responseDto, count);
		return responseDto;
	}

	private static GameResponseDto checkPosibilityOneAndTwo(int[][] convertedGameData, GameResponseDto responseDto,boolean isPossibilityOne) {
		int count=0;
		for(int i=0;i<convertedGameData.length;i++) {
			for(int j=0;j<convertedGameData.length;j++) {
				count=count + (isPossibilityOne?convertedGameData[i][j] : convertedGameData[j][i]);
			}
			count = checkCountForGameWin(responseDto, count);
			if(responseDto.isGameCompleted()) {
				return responseDto;
			}
		}
		return responseDto;
	}

	private static int checkCountForGameWin(GameResponseDto responseDto, int count) {
		if(count==0||count==gameWinningCount) {
			responseDto.setGameCompleted(true);
			if(count==0) {
				responseDto.setWhoWonGame(playerOImageUrl);
			}else if(count==gameWinningCount) {
				responseDto.setWhoWonGame(playerXImageUrl);
			}
		}
		count = 0;
		return count;
	}

	public static int[][] getGridConvertedGameData(List<Integer> gameSpaceOccupiedBy) {
		int sizeOfSquare = (int) Math.sqrt(gameSpaceOccupiedBy.size());
		int[][] convertedGameData = new int[sizeOfSquare][sizeOfSquare];
		for(int i=0;i<gameSpaceOccupiedBy.size();i++) {
			convertedGameData[(i)/sizeOfSquare][i%sizeOfSquare] = gameSpaceOccupiedBy.get(i);
		}
		return convertedGameData;
	}
	
	public static List<ErrorResponse> getErrorMessage(BindingResult bindingResult) {
		List<ErrorResponse> errorResponses = new ArrayList<>();
		for(ObjectError objectError: bindingResult.getAllErrors()) {
			errorResponses.add(new ErrorResponse(objectError.getDefaultMessage()));
		}
		return errorResponses;
	}
	public static List<ErrorResponse> getErrorMessage(String errorMessage) {
		List<ErrorResponse> errorResponses = new ArrayList<>();
		errorResponses.add(new ErrorResponse(errorMessage));
		return errorResponses;
	}
	
}
