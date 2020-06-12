package br.com.sicredi.voting.repository;

import br.com.sicredi.voting.document.Guideline;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface GuidelineReactiveRepository extends ReactiveMongoRepository<Guideline, String> {

    @Query("{ id: { $exists: true }}")
    Flux<Guideline> retrieveAllGuidelinesPaged(final Pageable page);

    Mono<Guideline> findByName(String name);
}
