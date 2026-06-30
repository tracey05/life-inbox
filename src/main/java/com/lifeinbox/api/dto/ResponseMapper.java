package com.lifeinbox.api.dto;

import com.lifeinbox.application.SubmitEntryResult;
import com.lifeinbox.domain.LifeItem;
import com.lifeinbox.domain.LifeItemCandidate;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ResponseMapper {
    private ResponseMapper() {
    }

    public static Map<String, Object> submitEntryResponse(SubmitEntryResult result) {
        Map<String, Object> response = new LinkedHashMap<String, Object>();
        response.put("entryId", result.getEntryId());
        response.put("candidates", candidates(result.getCandidates()));
        return response;
    }

    public static Map<String, Object> confirmResponse(LifeItem item) {
        Map<String, Object> response = new LinkedHashMap<String, Object>();
        response.put("item", item(item));
        return response;
    }

    public static Map<String, Object> ignoreResponse(LifeItemCandidate candidate) {
        Map<String, Object> response = new LinkedHashMap<String, Object>();
        response.put("candidateId", candidate.getId());
        response.put("status", candidate.getStatus());
        return response;
    }

    public static List<Map<String, Object>> candidates(List<LifeItemCandidate> candidates) {
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        for (LifeItemCandidate candidate : candidates) {
            result.add(candidate(candidate));
        }
        return result;
    }

    public static Map<String, Object> candidate(LifeItemCandidate candidate) {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("candidateId", candidate.getId());
        map.put("type", candidate.getType());
        map.put("title", candidate.getTitle());
        map.put("summary", candidate.getSummary());
        map.put("startTime", candidate.getStartTime());
        map.put("endTime", candidate.getEndTime());
        map.put("deadline", candidate.getDeadline());
        map.put("location", candidate.getLocation());
        map.put("suggestedAction", candidate.getSuggestedAction());
        map.put("actionRequired", candidate.isActionRequired());
        map.put("confidence", candidate.getConfidence());
        map.put("missingInformation", candidate.getMissingInformation());
        map.put("evidence", candidate.getEvidence());
        map.put("status", candidate.getStatus());
        return map;
    }

    public static Map<String, Object> item(LifeItem item) {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("itemId", item.getId());
        map.put("entryId", item.getInboxEntryId());
        map.put("type", item.getType());
        map.put("title", item.getTitle());
        map.put("summary", item.getSummary());
        map.put("startTime", item.getStartTime());
        map.put("endTime", item.getEndTime());
        map.put("deadline", item.getDeadline());
        map.put("location", item.getLocation());
        map.put("status", item.getStatus());
        map.put("confirmedAt", item.getConfirmedAt());
        return map;
    }
}
