package io.github.appuhafeez.tiktoktoe.exception;

import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import io.github.appuhafeez.tiktoktoe.model.ErrorResponse;
import lombok.extern.slf4j.Slf4j;

@ControllerAdvice
@Slf4j
public class ControllerExceptionHandler {
	
	@ExceptionHandler(RequestValidationException.class)
	public ResponseEntity<ErrorResponse> handleRequestValidationException(RequestValidationException exception,HttpServletResponse httpServletResponse){
		log.error("Exception occured while serving request: {}",exception.getMessage());
		return new ResponseEntity<ErrorResponse>(new ErrorResponse(exception.getErrors().toString()),HttpStatus.BAD_REQUEST);
	}

}
