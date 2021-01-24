package io.github.appuhafeez.tiktoktoe.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponse {
	
	private String errorMessage;

	@Override
	public String toString() {
		return errorMessage;
	}

	
	
}
