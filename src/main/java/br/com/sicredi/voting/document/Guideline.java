package br.com.sicredi.voting.document;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@ToString
@Document(collection = "guidelines")
public class Guideline {

    @Id
    private String id;

    private String name;

    private String description;

    @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime votingStart;

    @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime votingEnd;

    private Long positive;

    private Long negative;

    private List<Associate> associates = new ArrayList<>();

    public Guideline(String id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }


}
