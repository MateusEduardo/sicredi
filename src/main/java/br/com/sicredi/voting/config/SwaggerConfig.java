package br.com.sicredi.voting.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2WebFlux;

import java.util.Collections;

@Configuration
@EnableSwagger2WebFlux
public class SwaggerConfig {

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(PathSelectors.any())
                .build()
                .apiInfo(buildApiInfo());
    }

    private ApiInfo buildApiInfo() {
        return new ApiInfo(
                "Sicredi vote API",
                "Sicredi vote API.",
                "v1",
                "Not specified",
                new Contact("Mateus Almeida", "https://www.linkedin.com/in/mateus-eduardo-de-almeida-8b5a65ba/", "mateusdarosadealmeida@gmail.com"),
                "MIT", "Not specified", Collections.emptyList());
    }
}
