package com.lifeinbox.llm.schema;

import com.lifeinbox.domain.LifeItemCandidate;

import java.util.ArrayList;
import java.util.List;

public class LifeItemExtractionResponse {
    private boolean containsUsefulInformation;
    private List<LifeItemCandidate> items = new ArrayList<LifeItemCandidate>();

    public LifeItemExtractionResponse(boolean containsUsefulInformation, List<LifeItemCandidate> items) {
        this.containsUsefulInformation = containsUsefulInformation;
        this.items = items;
    }

    public boolean isContainsUsefulInformation() {
        return containsUsefulInformation;
    }

    public List<LifeItemCandidate> getItems() {
        return items;
    }
}
