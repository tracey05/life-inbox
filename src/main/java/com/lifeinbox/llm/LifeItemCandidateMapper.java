package com.lifeinbox.llm;

import com.lifeinbox.domain.LifeItemCandidate;
import com.lifeinbox.domain.LifeItemType;
import com.lifeinbox.llm.schema.LifeItemExtractionResponse;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class LifeItemCandidateMapper {
    private LifeItemCandidateMapper() {
    }

    @SuppressWarnings("unchecked")
    public static LifeItemExtractionResponse fromModelOutput(Map<String, Object> modelOutput, String rawContent) {
        Object rawItems = modelOutput.get("items");
        List<LifeItemCandidate> candidates = new ArrayList<LifeItemCandidate>();
        if (rawItems instanceof List) {
            for (Object rawItem : (List<Object>) rawItems) {
                if (rawItem instanceof Map) {
                    candidates.add(candidateFromMap((Map<String, Object>) rawItem, rawContent));
                }
            }
        }

        Object containsUsefulInformation = modelOutput.get("containsUsefulInformation");
        boolean contains = containsUsefulInformation instanceof Boolean
                ? (Boolean) containsUsefulInformation
                : !candidates.isEmpty();
        return new LifeItemExtractionResponse(contains && !candidates.isEmpty(), candidates);
    }

    private static LifeItemCandidate candidateFromMap(Map<String, Object> raw, String rawContent) {
        LifeItemCandidate candidate = new LifeItemCandidate();
        candidate.setType(LifeItemType.valueOf(requiredString(raw, "type")));
        candidate.setTitle(nullableString(raw.get("title")));
        candidate.setSummary(nullableString(raw.get("summary")));
        candidate.setStartTime(nullableDateTime(raw.get("startTime")));
        candidate.setEndTime(nullableDateTime(raw.get("endTime")));
        candidate.setDeadline(nullableDateTime(raw.get("deadline")));
        candidate.setLocation(nullableString(raw.get("location")));
        candidate.setActionRequired(booleanValue(raw.get("actionRequired")));
        candidate.setSuggestedAction(nullableString(raw.get("suggestedAction")));
        candidate.setConfidence(numberValue(raw.get("confidence")));
        candidate.setMissingInformation(stringList(raw.get("missingInformation")));
        candidate.setEvidence(validatedEvidence(stringList(raw.get("evidence")), rawContent, candidate));
        return candidate;
    }

    private static List<String> validatedEvidence(List<String> evidence, String rawContent, LifeItemCandidate candidate) {
        List<String> valid = new ArrayList<String>();
        String source = rawContent == null ? "" : rawContent;
        for (String quote : evidence) {
            if (quote != null && !quote.trim().isEmpty() && source.contains(quote)) {
                valid.add(quote);
            }
        }
        if (valid.size() != evidence.size()) {
            addMissing(candidate, "Verbatim evidence from source");
            candidate.setConfidence(Math.min(candidate.getConfidence(), 0.50));
        }
        return valid;
    }

    private static String requiredString(Map<String, Object> raw, String field) {
        Object value = raw.get(field);
        if (value == null) {
            throw new IllegalArgumentException("Model output missing required field: " + field);
        }
        return String.valueOf(value).toUpperCase(Locale.US);
    }

    private static String nullableString(Object value) {
        if (value == null) {
            return null;
        }
        String text = String.valueOf(value);
        return text.trim().isEmpty() ? null : text;
    }

    private static LocalDateTime nullableDateTime(Object value) {
        String text = nullableString(value);
        if (text == null) {
            return null;
        }
        return LocalDateTime.parse(text);
    }

    private static boolean booleanValue(Object value) {
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return Boolean.parseBoolean(String.valueOf(value));
    }

    private static double numberValue(Object value) {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        if (value == null) {
            return 0.0;
        }
        return Double.parseDouble(String.valueOf(value));
    }

    @SuppressWarnings("unchecked")
    private static List<String> stringList(Object value) {
        List<String> result = new ArrayList<String>();
        if (!(value instanceof List)) {
            return result;
        }
        for (Object item : (List<Object>) value) {
            if (item != null) {
                result.add(String.valueOf(item));
            }
        }
        return result;
    }

    private static void addMissing(LifeItemCandidate candidate, String field) {
        if (!candidate.getMissingInformation().contains(field)) {
            candidate.getMissingInformation().add(field);
        }
    }
}
