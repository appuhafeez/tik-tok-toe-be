package io.github.appuhafeez.tiktoktoe.model;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.github.appuhafeez.tiktoktoe.constants.PlayerEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class RedisGamePojo implements Serializable{

	private static final long serialVersionUID = -8710721602155736614L;
	
	private int gameCode;

	private boolean isGameStarted;
	
	private List<String> spacesOccupiedPlayerIcons;
	
	private List<Integer> spacesOccupiedBy;
	
	private PlayerEnum lastMoveBy;
	
	private Map<String, String> players;
	
	@JsonIgnore
	private Long timeToLive;
	
}
