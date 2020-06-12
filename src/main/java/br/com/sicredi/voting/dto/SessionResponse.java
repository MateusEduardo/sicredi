package br.com.sicredi.voting.dto;


import br.com.sicredi.voting.document.Associate;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;


@NoArgsConstructor
@Getter
@ToString
public class SessionResponse {

    @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime votingStart;

    @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime votingEnd;

    private String description;

    private String name;

    private Long positive;

    private Long negative;

    private List<Associate> associates;
}
