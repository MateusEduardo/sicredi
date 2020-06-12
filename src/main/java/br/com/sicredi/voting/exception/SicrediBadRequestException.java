package br.com.sicredi.voting.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST)
public class SicrediBadRequestException extends RuntimeException {

    public SicrediBadRequestException(String message) {
        super(message);
    }
}
