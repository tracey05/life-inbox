package com.lifeinbox.evaluation;

import com.lifeinbox.domain.LifeItemCandidate;
import com.lifeinbox.domain.LifeItemType;

import java.util.ArrayList;
import java.util.List;

public class EvaluationResult {
    private final EvaluationCase evaluationCase;
    private final List<LifeItemCandidate> candidates;

    public EvaluationResult(EvaluationCase evaluationCase, List<LifeItemCandidate> candidates) {
        this.evaluationCase = evaluationCase;
        this.candidates = candidates;
    }

    public boolean typeSequenceMatches() {
        List<LifeItemType> actualTypes = actualTypes();
        return actualTypes.equals(evaluationCase.getExpectedTypes());
    }

    public List<LifeItemType> actualTypes() {
        List<LifeItemType> types = new ArrayList<LifeItemType>();
        for (LifeItemCandidate candidate : candidates) {
            types.add(candidate.getType());
        }
        return types;
    }

    public EvaluationCase getEvaluationCase() {
        return evaluationCase;
    }

    public List<LifeItemCandidate> getCandidates() {
        return candidates;
    }
}
