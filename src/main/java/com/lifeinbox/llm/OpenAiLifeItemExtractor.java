package com.lifeinbox.llm;

import com.lifeinbox.infrastructure.json.JsonUtil;
import com.lifeinbox.llm.prompt.LifeItemExtractionPrompt;
import com.lifeinbox.llm.schema.LifeItemExtractionResponse;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Map;

public class OpenAiLifeItemExtractor implements LifeItemExtractor {
    private final LlmProvider llmProvider;
    private final Clock clock;
    private final OpenAiConfig config;

    public OpenAiLifeItemExtractor(LlmProvider llmProvider, Clock clock, OpenAiConfig config) {
        this.llmProvider = llmProvider;
        this.clock = clock;
        this.config = config;
    }

    @Override
    public LifeItemExtractionResponse extract(String rawContent) {
        String modelJson = llmProvider.complete(buildPrompt(rawContent));
        Map<String, Object> modelOutput = JsonUtil.parseObject(modelJson);
        return LifeItemCandidateMapper.fromModelOutput(modelOutput, rawContent);
    }

    private String buildPrompt(String rawContent) {
        ZonedDateTime now = ZonedDateTime.now(clock.withZone(config.getUserZone()));
        LocalDate today = now.toLocalDate();

        StringBuilder prompt = new StringBuilder();
        prompt.append(LifeItemExtractionPrompt.SYSTEM_PROMPT).append("\n\n");
        prompt.append("Current date: ").append(today).append("\n");
        prompt.append("Current timezone: ").append(config.getUserZone().getId()).append("\n\n");
        prompt.append("Output contract:\n");
        prompt.append("- Return JSON only.\n");
        prompt.append("- Date-time values must use ISO local date-time format, for example 2026-07-18T15:00:00.\n");
        prompt.append("- Use null for unknown date-time fields.\n");
        prompt.append("- If a date has month/day but no year in the input, keep the date-time field null and add \"Year\" to missingInformation.\n");
        prompt.append("- Evidence strings must be exact substrings from the original input.\n\n");
        prompt.append("Original input:\n");
        prompt.append(rawContent == null ? "" : rawContent);
        return prompt.toString();
    }
}
