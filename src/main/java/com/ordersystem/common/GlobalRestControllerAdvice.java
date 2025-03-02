package com.ordersystem.common;

import com.ordersystem.common.exception.ApplicationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalRestControllerAdvice {

	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ExceptionHandler({Throwable.class})
	protected ResponseEntity<String> handle(Throwable throwable) {
		log.error("[UnknownException] Occur exception.", throwable);
		return ResponseEntity.ok(throwable.getMessage());
	}

	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler({ApplicationException.class})
	protected ResponseEntity<String> handle(ApplicationException applicationException) {
		log.error("[PointApplicationException] exception.", applicationException);
		return ResponseEntity.ok(applicationException.getMessage());
	}
}
