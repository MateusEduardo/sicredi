package br.com.sicredi.voting.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VoteRequest {

    @NotNull
    private Boolean decision;

    @NotNull
    @Pattern(regexp = "[0-9]{3}[0-9]{3}[0-9]{3}[0-9]{2}")
    private String cpf;
}
