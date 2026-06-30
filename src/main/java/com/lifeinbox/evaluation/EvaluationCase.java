package com.lifeinbox.evaluation;

import com.lifeinbox.domain.LifeItemType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EvaluationCase {
    private final String id;
    private final String input;
    private final List<LifeItemType> expectedTypes;

    public EvaluationCase(String id, String input, LifeItemType... expectedTypes) {
        this.id = id;
        this.input = input;
        this.expectedTypes = new ArrayList<LifeItemType>(Arrays.asList(expectedTypes));
    }

    public String getId() {
        return id;
    }

    public String getInput() {
        return input;
    }

    public List<LifeItemType> getExpectedTypes() {
        return expectedTypes;
    }
}
