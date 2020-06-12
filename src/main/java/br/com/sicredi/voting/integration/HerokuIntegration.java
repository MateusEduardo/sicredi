package br.com.sicredi.voting.integration;

import br.com.sicredi.voting.dto.AssociateValidation;
import org.springframework.web.reactive.function.client.WebClient;

public class HerokuIntegration {

    private static String URL_BASE = "https://user-info.herokuapp.com";

    private WebClient webClient;

    public HerokuIntegration() {
        webClient = WebClient.create(URL_BASE);
    }

    public HerokuIntegration(WebClient webClient) {
        this.webClient = webClient;
    }

    public HerokuIntegration(String baseUrl) {
        this.webClient = WebClient.create(baseUrl);
    }

    public AssociateValidation getAssociateValidation(String cpf) {
        return webClient.get()
                .uri("/users/{cpf}", cpf)
                .retrieve()
                .bodyToMono(AssociateValidation.class)
                .block();
    }
}
