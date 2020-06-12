package br.com.sicredi.voting.controller;

import br.com.sicredi.voting.document.Guideline;
import br.com.sicredi.voting.dto.GuidelineRequest;
import br.com.sicredi.voting.dto.GuidelineResponse;
import br.com.sicredi.voting.dto.SessionRequest;
import br.com.sicredi.voting.dto.VoteRequest;
import br.com.sicredi.voting.integration.HerokuIntegration;
import br.com.sicredi.voting.repository.GuidelineReactiveRepository;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureWebTestClient
@DirtiesContext
public class GuidelineControllerTest {

    @Autowired
    WebTestClient webTestClient;

    @Autowired
    GuidelineReactiveRepository guidelineReactiveRepository;

    @Mock
    private WebClient webClientMock;
    @Mock
    private WebClient.RequestHeadersSpec requestHeadersMock;
    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriMock;
    @Mock
    private WebClient.RequestBodySpec requestBodyMock;
    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriMock;
    @Mock
    private WebClient.ResponseSpec responseMock;

    private HerokuIntegration herokuIntegration;


    private Logger logger = LoggerFactory.getLogger(GuidelineControllerTest.class);

    private static final String ENDPOINT_GUIDELINE_V1 = "/v1/guideline";

    private List<Guideline> data() {
        return Arrays.asList(
                new Guideline("123", "votacao 1", "primeira pauta"),
                new Guideline("456", "votacao 2", "segunda pauta"),
                new Guideline("789", "votacao 3", "terceira pauta")
        );
    }

    @Before
    public void setUp() {
        guidelineReactiveRepository.deleteAll()
                .thenMany(Flux.fromIterable(data()))
                .flatMap(guidelineReactiveRepository::save)
                .doOnNext((item -> {
                    logger.info("Item inserido: {}", item);
                }))
                .blockLast();
        herokuIntegration = new HerokuIntegration(webClientMock);
    }

    @Test
    public void testCreateGuideline() {
        GuidelineRequest guidelineRequest = GuidelineRequest.builder()
                .name("nome da pauta 1")
                .description("descricao da pauta 1")
                .build();
        webTestClient.post().uri(ENDPOINT_GUIDELINE_V1)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(guidelineRequest), GuidelineRequest.class)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isNotEmpty()
                .jsonPath("$.name").isEqualTo("nome da pauta 1")
                .jsonPath("$.description").isEqualTo("descricao da pauta 1");
    }

    @Test
    public void testDuplicatedGuideline() {
        GuidelineRequest guidelineRequest = GuidelineRequest.builder()
                .name("nome da pauta 2")
                .description("descricao da pauta 2")
                .build();

        webTestClient.post().uri(ENDPOINT_GUIDELINE_V1)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(guidelineRequest), GuidelineRequest.class)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isNotEmpty()
                .jsonPath("$.name").isEqualTo("nome da pauta 2")
                .jsonPath("$.description").isEqualTo("descricao da pauta 2");

        webTestClient.post().uri(ENDPOINT_GUIDELINE_V1)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(guidelineRequest), GuidelineRequest.class)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    public void testCreateGuidelineBadRequest() {
        webTestClient.post().uri(ENDPOINT_GUIDELINE_V1)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(new GuidelineRequest()), GuidelineRequest.class)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.BAD_REQUEST);
    }


    @Test
    public void testFindAllGuidelines() {
        webTestClient.get().uri(ENDPOINT_GUIDELINE_V1)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(GuidelineResponse.class)
                .hasSize(data().size());
    }

    @Test
    public void testFindAllGuidelines_Pageable() {
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(ENDPOINT_GUIDELINE_V1)
                        .queryParam("page", "1")
                        .queryParam("size", "1")
                        .build()
                )
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(GuidelineResponse.class)
                .hasSize(1);
    }

    @Test
    public void testFindAllStream() {
        Flux<GuidelineResponse> responseBody = webTestClient.get().uri(ENDPOINT_GUIDELINE_V1.concat("/stream"))
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_STREAM_JSON)
                .returnResult(GuidelineResponse.class)
                .getResponseBody();

        StepVerifier.create(responseBody)
                .expectNextCount(data().size())
                .verifyComplete();
    }

    @Test
    public void testFindById() {
        Guideline firstElemntFromData = data().get(0);
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(ENDPOINT_GUIDELINE_V1 + "/{id}").build(firstElemntFromData.getId()))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(firstElemntFromData.getId())
                .jsonPath("$.name").isEqualTo(firstElemntFromData.getName())
                .jsonPath("$.description").isEqualTo(firstElemntFromData.getDescription());
    }

    @Test
    public void testFindById_InvalidId() {
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(ENDPOINT_GUIDELINE_V1 + "/{id}").build("IDNOTFOUND"))
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    public void testUpdate() {
        String newDescription = "descricao da pauta atualizada";
        String newName = "nome da pauta atualizada";
        GuidelineRequest request = GuidelineRequest.builder()
                .description(newDescription)
                .name(newName)
                .build();
        String idToUpdate = data().get(0).getId();
        webTestClient.put().uri(uriBuilder -> uriBuilder
                .path(ENDPOINT_GUIDELINE_V1 + "/{id}").build(idToUpdate))
                .body(Mono.just(request), GuidelineRequest.class)
                .exchange()
                .expectStatus().isOk();

        webTestClient.get().uri(uriBuilder -> uriBuilder
                .path(ENDPOINT_GUIDELINE_V1 + "/{id}").build(idToUpdate))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.name").isEqualTo(newName)
                .jsonPath("$.description").isEqualTo(newDescription);
    }

    @Test
    public void testUpdate_InvalidId() {
        GuidelineRequest request = GuidelineRequest.builder()
                .description("descricao")
                .name("nome")
                .build();
        webTestClient.put().uri(uriBuilder -> uriBuilder
                .path(ENDPOINT_GUIDELINE_V1 + "/{id}").build("IDNOTFOUND"))
                .body(Mono.just(request), GuidelineRequest.class)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    public void testOpenSession() {
        String idToOpen = data().get(0).getId();

        webTestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path(ENDPOINT_GUIDELINE_V1 + "/{id}/open")
                        .build(idToOpen)
                )
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(new SessionRequest()), SessionRequest.class)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.votingStart").isNotEmpty()
                .jsonPath("$.votingEnd").isNotEmpty();
    }

    @Test
    public void testOpenSession_AlreadyOpen() {
        String idToOpen = data().get(1).getId();

        webTestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path(ENDPOINT_GUIDELINE_V1 + "/{id}/open")
                        .build(idToOpen)
                )
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(new SessionRequest()), SessionRequest.class)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.votingStart").isNotEmpty()
                .jsonPath("$.votingEnd").isNotEmpty();

        webTestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path(ENDPOINT_GUIDELINE_V1 + "/{id}/open")
                        .build(idToOpen)
                )
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(new SessionRequest()), SessionRequest.class)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    public void testOpenSession_InvalidId() {
        webTestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path(ENDPOINT_GUIDELINE_V1 + "/{id}/open")
                        .build("IDNOTFOUND")
                )
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(new SessionRequest()), SessionRequest.class)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    public void testGetSessionData() {
        String idToOpen = data().get(2).getId();

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(ENDPOINT_GUIDELINE_V1 + "/{id}/session")
                        .build(idToOpen))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.votingStart").isEmpty()
                .jsonPath("$.votingEnd").isEmpty()
                .jsonPath("$.positive").isEmpty()
                .jsonPath("$.negative").isEmpty();

        webTestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path(ENDPOINT_GUIDELINE_V1 + "/{id}/open")
                        .build(idToOpen)
                )
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(new SessionRequest()), SessionRequest.class)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.votingStart").isNotEmpty()
                .jsonPath("$.votingEnd").isNotEmpty()
                .jsonPath("$.positive").isEqualTo(0L)
                .jsonPath("$.negative").isEqualTo(0L);

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(ENDPOINT_GUIDELINE_V1 + "/{id}/session")
                        .build(idToOpen))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.votingStart").isNotEmpty()
                .jsonPath("$.votingEnd").isNotEmpty()
                .jsonPath("$.positive").isEqualTo(0L)
                .jsonPath("$.negative").isEqualTo(0L);
    }

    @Test
    public void testGetSessionData_InvalidId() {
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(ENDPOINT_GUIDELINE_V1 + "/{id}/session")
                        .build("IDNOTFOUND"))
                .exchange()
                .expectStatus().isNotFound();
    }

    @Ignore
    @Test
    public void testVote() {
        String idToOpen = data().get(1).getId();
        String cpfMock = "12345678910";
        VoteRequest voteRequest = new VoteRequest(true, cpfMock);

        webTestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path(ENDPOINT_GUIDELINE_V1 + "/{id}/open")
                        .build(idToOpen)
                )
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(new SessionRequest()), SessionRequest.class)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.votingStart").isNotEmpty()
                .jsonPath("$.votingEnd").isNotEmpty()
                .jsonPath("$.positive").isEqualTo(0L)
                .jsonPath("$.negative").isEqualTo(0L);

        webTestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path(ENDPOINT_GUIDELINE_V1 + "/{id}/vote")
                        .build(idToOpen)
                )
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(voteRequest), VoteRequest.class)
                .exchange()
                .expectStatus().isOk();

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(ENDPOINT_GUIDELINE_V1 + "/{id}/session")
                        .build(idToOpen))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.positive").isEqualTo(1L)
                .jsonPath("$.associates").isArray();
    }

    @Test
    public void testVote_ClosedSession() {
        String id = data().get(0).getId();
        VoteRequest voteRequest = new VoteRequest(true, "12345678910");

        webTestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path(ENDPOINT_GUIDELINE_V1 + "/{id}/vote")
                        .build(id)
                )
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(voteRequest), VoteRequest.class)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    public void testVote_InvalidId() {
        VoteRequest voteRequest = new VoteRequest(true, "12345678910");
        webTestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path(ENDPOINT_GUIDELINE_V1 + "/{id}/vote")
                        .build("IDNOTFOUND")
                )
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(voteRequest), VoteRequest.class)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Ignore
    @Test
    public void testVote_Duplicated() {
        String idToOpen = data().get(1).getId();
        VoteRequest voteRequest = new VoteRequest(true, "12345678910");

        webTestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path(ENDPOINT_GUIDELINE_V1 + "/{id}/open")
                        .build(idToOpen)
                )
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(new SessionRequest()), SessionRequest.class)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.votingStart").isNotEmpty()
                .jsonPath("$.votingEnd").isNotEmpty()
                .jsonPath("$.positive").isEqualTo(0L)
                .jsonPath("$.negative").isEqualTo(0L);

        webTestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path(ENDPOINT_GUIDELINE_V1 + "/{id}/vote")
                        .build(idToOpen)
                )
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(voteRequest), VoteRequest.class)
                .exchange()
                .expectStatus().isOk();

        webTestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path(ENDPOINT_GUIDELINE_V1 + "/{id}/vote")
                        .build(idToOpen)
                )
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(voteRequest), VoteRequest.class)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT);
    }
}
