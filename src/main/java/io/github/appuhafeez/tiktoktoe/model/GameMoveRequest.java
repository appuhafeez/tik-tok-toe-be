package io.github.appuhafeez.tiktoktoe.model;

import org.hibernate.validator.constraints.Range;

import io.github.appuhafeez.tiktoktoe.constants.PlayerEnum;
import lombok.Data;

@Data
public class GameMoveRequest {
	
	private PlayerEnum movedBy;
	
	@Range(min = 0,max = 9,message = "Invalidate position")
	private int position;
	
	@Range(min = 100000,max = 999999, message = "Invalid Gamecode")
	private int gameCode;

}
