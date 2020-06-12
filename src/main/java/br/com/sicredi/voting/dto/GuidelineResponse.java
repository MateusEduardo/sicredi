package br.com.sicredi.voting.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor
@ToString
public class GuidelineResponse {

    private String id;

    private String name;

    private String description;

}
