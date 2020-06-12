package br.com.sicredi.voting.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.CONFLICT)
public class SicrediAlreadyExistsException extends RuntimeException {

	private static final long serialVersionUID = 3092475485245615920L;
	
	public SicrediAlreadyExistsException(String message) {
		super(message);
	}
	
}
