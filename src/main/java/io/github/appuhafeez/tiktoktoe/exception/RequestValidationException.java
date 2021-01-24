package io.github.appuhafeez.tiktoktoe.exception;

import java.util.Date;
import java.util.List;

import io.github.appuhafeez.tiktoktoe.model.ErrorResponse;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestValidationException extends RuntimeException{

	/**
	 * 
	 */
	private static final long serialVersionUID = 2330567943684707067L;
	
	private final List<ErrorResponse> errors;
	
	private final Date timeStamp;
	
	public RequestValidationException(List<ErrorResponse> errors) {
		super();
		timeStamp = new Date();
		this.errors=errors;
	}

}
