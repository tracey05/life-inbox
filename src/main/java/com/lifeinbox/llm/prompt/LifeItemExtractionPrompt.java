package com.lifeinbox.llm.prompt;

public final class LifeItemExtractionPrompt {
    private LifeItemExtractionPrompt() {
    }

    public static final String SYSTEM_PROMPT =
            "Extract candidate life items from raw user input.\n" +
            "Return only the agreed JSON schema.\n" +
            "Rules:\n" +
            "1. Do not add facts that are not present in the original input.\n" +
            "2. Return null for uncertain fields.\n" +
            "3. One input may produce multiple items.\n" +
            "4. Return an empty items array when there is no useful information to save.\n" +
            "5. Evidence must be short verbatim quotes from the input.\n" +
            "6. Resolve relative dates using the current date and the user's timezone.\n" +
            "7. If the year cannot be determined, do not guess it.";
}
