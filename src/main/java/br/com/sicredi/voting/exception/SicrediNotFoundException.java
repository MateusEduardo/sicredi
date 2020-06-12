package br.com.sicredi.voting.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_FOUND)
public class SicrediNotFoundException extends RuntimeException {

	public SicrediNotFoundException(String message) {
		super(message);
	}
	
}
