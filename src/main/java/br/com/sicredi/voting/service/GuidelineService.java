package br.com.sicredi.voting.service;

import br.com.sicredi.voting.document.Associate;
import br.com.sicredi.voting.document.Guideline;
import br.com.sicredi.voting.dto.AssociateValidation;
import br.com.sicredi.voting.dto.GuidelineRequest;
import br.com.sicredi.voting.dto.SessionRequest;
import br.com.sicredi.voting.dto.VoteRequest;
import br.com.sicredi.voting.enumeration.AssociateStatusEnum;
import br.com.sicredi.voting.exception.SicrediAlreadyExistsException;
import br.com.sicredi.voting.exception.SicrediBadRequestException;
import br.com.sicredi.voting.exception.SicrediNotFoundException;
import br.com.sicredi.voting.integration.HerokuIntegration;
import br.com.sicredi.voting.repository.GuidelineReactiveRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

@RequiredArgsConstructor
@Service
@Transactional
public class GuidelineService {

    final GuidelineReactiveRepository guidelineReactiveRepository;

    final ObjectMapper objectMapper;

    HerokuIntegration herokuIntegration = new HerokuIntegration();

    @Value("${session.duration.default}")
    private Long sessionDurationDefault;

    public Mono<Guideline> create(GuidelineRequest guidelineRequest) {
        return guidelineReactiveRepository.findByName(guidelineRequest.getName())
                .flatMap(__ -> Mono.error(new SicrediAlreadyExistsException("Pauta já registrada")))
                .switchIfEmpty(Mono.defer(() -> {
                    Guideline guideline = objectMapper.convertValue(guidelineRequest, Guideline.class);
                    return guidelineReactiveRepository.save(guideline);
                })).cast(Guideline.class);
    }

    public Flux<Guideline> findAll(Pageable pageable) {
        return guidelineReactiveRepository.retrieveAllGuidelinesPaged(pageable);
    }

    public Flux<Guideline> findAllStream() {
        return guidelineReactiveRepository.findAll();
    }


    public Mono<Guideline> findById(String id) {
        return guidelineReactiveRepository.findById(id)
                .switchIfEmpty(Mono.defer(() -> Mono.error(new SicrediNotFoundException("Id não encontrado"))));
    }

    public Mono<Guideline> update(String id, GuidelineRequest guidelineRequest) {
        return findById(id)
                .flatMap(existingGuideline -> {
                    existingGuideline.setDescription(guidelineRequest.getDescription());
                    existingGuideline.setName(guidelineRequest.getName());
                    return guidelineReactiveRepository.save(existingGuideline);
                });
    }

    public Mono<Guideline> openSession(String id, SessionRequest sessionRequest) {
        Long duration = Objects.isNull(sessionRequest.getDuration()) ? sessionDurationDefault : sessionRequest.getDuration();
        return findById(id)
                .flatMap(existingGuideline -> {
                    if (!Objects.isNull(existingGuideline.getVotingStart())) {
                        return Mono.error(new SicrediAlreadyExistsException("Sessão já iniciada"));
                    }
                    existingGuideline.setNegative(0L);
                    existingGuideline.setPositive(0L);
                    existingGuideline.setVotingStart(LocalDateTime.now());
                    existingGuideline.setVotingEnd(existingGuideline.getVotingStart().plusMinutes(duration));

                    return guidelineReactiveRepository.save(existingGuideline);
                });
    }

    public Mono<Void> vote(String id, VoteRequest voteRequest) {
        return findById(id).flatMap(existingGuideline -> {
            Optional<Associate> associateVote = existingGuideline.getAssociates().stream()
                    .filter(it -> voteRequest.getCpf().equals(it.getCpf())).findFirst();
            if (associateVote.isPresent()) {
                return Mono.error(new SicrediAlreadyExistsException("Associado já votou nessa pauta"));
            } else if (Objects.isNull(existingGuideline.getVotingStart()) || LocalDateTime.now().isAfter(existingGuideline.getVotingEnd())) {
                return Mono.error(new SicrediBadRequestException("A sessão está inativa e não pode receber votos"));
            } else if(!validAssociate(voteRequest.getCpf())) {
                return Mono.error(new SicrediBadRequestException("Associado impedido de votar"));
            }
            if (voteRequest.getDecision().booleanValue()) {
                existingGuideline.setPositive(existingGuideline.getPositive() + 1);
            } else {
                existingGuideline.setNegative(existingGuideline.getNegative() + 1);
            }
            existingGuideline.getAssociates().add(new Associate(voteRequest.getCpf()));
            return guidelineReactiveRepository.save(existingGuideline);
        }).then();
    }

    private boolean validAssociate(String cpf) {
        try {
            AssociateValidation responseValidation = herokuIntegration.getAssociateValidation(cpf);
            if (responseValidation.getStatus().equalsIgnoreCase(AssociateStatusEnum.ABLE_TO_VOTE.name())) {
                return true;
            } else return false;
        } catch (Exception ex) {
            return false;
        }
    }
}
