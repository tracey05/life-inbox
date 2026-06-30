package com.lifeinbox.llm;

import com.lifeinbox.llm.schema.LifeItemExtractionResponse;

public interface LifeItemExtractor {
    LifeItemExtractionResponse extract(String rawContent);
}
