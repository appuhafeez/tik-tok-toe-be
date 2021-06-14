package io.github.appuhafeez.tiktoktoe.model;

import lombok.Data;

@Data
public class AddHistoryRequest {

	private String username;
	private String playedWith;
	private Boolean didUserWon;
	private Boolean isMatchTie;
	
}
