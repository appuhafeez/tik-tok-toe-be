package io.github.appuhafeez.tiktoktoe.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GameResponseDto{
	
	private boolean isGameCompleted;
	
	private RedisGamePojo gameData;
	
	private String wonMessage;
	
	private String whoWonGame;
	
	private boolean isTie;

}
