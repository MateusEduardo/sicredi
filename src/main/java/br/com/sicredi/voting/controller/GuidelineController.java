package br.com.sicredi.voting.controller;

import br.com.sicredi.voting.dto.*;
import br.com.sicredi.voting.service.GuidelineService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;


@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/guideline")
public class GuidelineController {

    final GuidelineService guidelineService;

    final ObjectMapper objectMapper;


    private Logger logger = LoggerFactory.getLogger(GuidelineController.class);

    @PostMapping
    @ApiOperation(value = "Criar uma pauta",
            notes = "Cria uma pauta para votação.",
            response = GuidelineResponse.class)
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<GuidelineResponse> create(@Valid @RequestBody GuidelineRequest guidelineRequest) {
        return guidelineService.create(guidelineRequest).map(it -> objectMapper.convertValue(it, GuidelineResponse.class))
                .doOnSuccess(it -> logger.info("Pauta criada com sucesso: {}", it))
                .doOnError(it -> logger.error("Erro ao criar pauta: {}", it));
    }

    @GetMapping
    @ApiOperation(value = "Buscar pautas",
            notes = "Buscar lista de todas as pautas",
            response = GuidelineResponse[].class)
    public Flux<GuidelineResponse> findAll(Pageable pageable) {
        return guidelineService.findAll(pageable)
                .map(it -> objectMapper.convertValue(it, GuidelineResponse.class))
                .doOnComplete(() -> logger.info("Retornado lista de pautas com sucesso"));
    }

    @GetMapping(value = "stream", produces = MediaType.APPLICATION_STREAM_JSON_VALUE)
    @ApiOperation(value = "Buscar pautas",
            notes = "Buscar lista de todas as pautas",
            response = GuidelineResponse[].class)
    public Flux<GuidelineResponse> findAllStream() {
        return guidelineService.findAllStream()
                .map(it -> objectMapper.convertValue(it, GuidelineResponse.class))
                .doOnComplete(() -> logger.info("Retornado lista de pautas com sucesso"));
    }

    @GetMapping(value = "{id}")
    @ApiOperation(value = "Buscar pauta",
            notes = "Buscar pauta por id",
            response = GuidelineResponse.class)
    public Mono<GuidelineResponse> findById(@PathVariable(name = "id") String id) {
        return guidelineService.findById(id).map(it -> objectMapper.convertValue(it, GuidelineResponse.class))
                .doOnSuccess(response -> logger.info("Retornando pauta encontrada: {}", response))
                .doOnError(error -> logger.error("Erro ao buscar pauta com id: {}, {}", id, error.getMessage()));
    }

    @PutMapping(value = "{id}")
    @ApiOperation(value = "Atualizar pauta",
            notes = "Atualizar dados da pauta por id",
            response = GuidelineResponse.class)
    public Mono<GuidelineResponse> update(@PathVariable(name = "id") String id, @Valid @RequestBody GuidelineRequest guidelineRequest) {
        return guidelineService.update(id, guidelineRequest).map(it -> objectMapper.convertValue(it, GuidelineResponse.class))
                .doOnSuccess(response -> logger.info("Retornando pauta atualizada: {}", response))
                .doOnError(error -> logger.error("Erro ao atualizar pauta com id: {}, {}", id, error.getMessage()));
    }

    @PostMapping(value = "{id}/open")
    @ApiOperation(value = "Abrir sessão",
            notes = "Abrir uma sessão de votação",
            response = SessionResponse.class)
    public Mono<SessionResponse> openVotingSession(@PathVariable(name = "id") String id, @Valid @RequestBody SessionRequest sessionRequest) {
        return guidelineService.openSession(id, sessionRequest).map(it -> objectMapper.convertValue(it, SessionResponse.class))
                .doOnSuccess(response -> logger.info("Retornando sessão aberta: {}", response))
                .doOnError(error -> logger.error("Erro ao abrir sessão com pauta: {}, {}", id, error.getMessage()));
    }

    @GetMapping(value = "{id}/session")
    @ApiOperation(value = "Buscar dados da sessão",
            notes = "Apurar dados da sessão",
            response = SessionResponse.class)
    public Mono<SessionResponse> getSession(@PathVariable(name = "id") String id) {
        return guidelineService.findById(id).map(it -> objectMapper.convertValue(it, SessionResponse.class))
                .doOnSuccess(response -> logger.info("Retornando sessão aberta: {}", response))
                .doOnError(error -> logger.error("Erro ao buscar sessão com pauta: {}, {}", id, error.getMessage()));
    }

    @PostMapping(value = "{id}/vote")
    @ApiOperation(value = "Votar",
            notes = "Enviar uma votação à pauta")
    public Mono<Void> vote(@PathVariable(name = "id") String id, @Valid @RequestBody VoteRequest voteRequest) {
        return guidelineService.vote(id, voteRequest)
                .doOnSuccess(response -> logger.info("Votação recebida para pauta: {}", id))
                .doOnError(error -> logger.error("Erro ao enviar votação para pauta: {}, {}", id, error.getMessage()));
    }
}
