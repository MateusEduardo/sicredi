package br.com.sicredi.voting.dto;

import lombok.*;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GuidelineRequest{

    @NotNull
    private String name;

    private String description;

}
