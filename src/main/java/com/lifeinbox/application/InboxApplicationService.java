package com.lifeinbox.application;

import com.lifeinbox.domain.InboxEntry;
import com.lifeinbox.domain.LifeItemCandidate;
import com.lifeinbox.domain.ParseStatus;
import com.lifeinbox.infrastructure.repository.InboxEntryRepository;
import com.lifeinbox.infrastructure.repository.LifeItemCandidateRepository;
import com.lifeinbox.llm.LifeItemExtractor;
import com.lifeinbox.llm.schema.LifeItemExtractionResponse;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class InboxApplicationService {
    private final InboxEntryRepository inboxEntryRepository;
    private final LifeItemCandidateRepository candidateRepository;
    private final LifeItemExtractor extractor;

    public InboxApplicationService(InboxEntryRepository inboxEntryRepository,
                                   LifeItemCandidateRepository candidateRepository,
                                   LifeItemExtractor extractor) {
        this.inboxEntryRepository = inboxEntryRepository;
        this.candidateRepository = candidateRepository;
        this.extractor = extractor;
    }

    public SubmitEntryResult submitManualText(String content) {
        return submit("MANUAL_TEXT", content);
    }

    public SubmitEntryResult submit(String sourceType, String content) {
        UUID entryId = UUID.randomUUID();
        InboxEntry entry = new InboxEntry(entryId, sourceType, content, Instant.now(), ParseStatus.PENDING);
        inboxEntryRepository.save(entry);

        LifeItemExtractionResponse extractionResponse = extractor.extract(content);
        List<LifeItemCandidate> candidates = extractionResponse.getItems();
        for (LifeItemCandidate candidate : candidates) {
            candidate.setId(UUID.randomUUID());
            candidate.setInboxEntryId(entryId);
        }

        candidateRepository.saveAll(candidates);
        entry.setParseStatus(ParseStatus.PARSED);
        inboxEntryRepository.save(entry);

        return new SubmitEntryResult(entryId, candidates);
    }
}
