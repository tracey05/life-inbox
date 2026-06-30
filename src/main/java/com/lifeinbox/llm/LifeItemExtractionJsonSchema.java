package com.lifeinbox.llm;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public final class LifeItemExtractionJsonSchema {
    private LifeItemExtractionJsonSchema() {
    }

    public static Map<String, Object> schema() {
        Map<String, Object> root = object();
        root.put("properties", properties(
                entry("containsUsefulInformation", booleanType()),
                entry("items", itemsArray())
        ));
        root.put("required", Arrays.asList("containsUsefulInformation", "items"));
        return root;
    }

    private static Map<String, Object> itemsArray() {
        Map<String, Object> array = new LinkedHashMap<String, Object>();
        array.put("type", "array");
        array.put("items", itemObject());
        return array;
    }

    private static Map<String, Object> itemObject() {
        Map<String, Object> item = object();
        item.put("properties", properties(
                entry("type", enumType("EVENT", "DEADLINE", "TASK", "REFERENCE")),
                entry("title", nullableString()),
                entry("summary", nullableString()),
                entry("startTime", nullableString()),
                entry("endTime", nullableString()),
                entry("deadline", nullableString()),
                entry("location", nullableString()),
                entry("actionRequired", booleanType()),
                entry("suggestedAction", nullableString()),
                entry("confidence", numberType()),
                entry("missingInformation", stringArray()),
                entry("evidence", stringArray())
        ));
        item.put("required", Arrays.asList(
                "type",
                "title",
                "summary",
                "startTime",
                "endTime",
                "deadline",
                "location",
                "actionRequired",
                "suggestedAction",
                "confidence",
                "missingInformation",
                "evidence"
        ));
        return item;
    }

    private static Map<String, Object> object() {
        Map<String, Object> object = new LinkedHashMap<String, Object>();
        object.put("type", "object");
        object.put("additionalProperties", false);
        return object;
    }

    private static Map<String, Object> nullableString() {
        Map<String, Object> type = new LinkedHashMap<String, Object>();
        type.put("type", Arrays.asList("string", "null"));
        return type;
    }

    private static Map<String, Object> booleanType() {
        Map<String, Object> type = new LinkedHashMap<String, Object>();
        type.put("type", "boolean");
        return type;
    }

    private static Map<String, Object> numberType() {
        Map<String, Object> type = new LinkedHashMap<String, Object>();
        type.put("type", "number");
        type.put("minimum", 0);
        type.put("maximum", 1);
        return type;
    }

    private static Map<String, Object> enumType(String... values) {
        Map<String, Object> type = new LinkedHashMap<String, Object>();
        type.put("type", "string");
        type.put("enum", Arrays.asList(values));
        return type;
    }

    private static Map<String, Object> stringArray() {
        Map<String, Object> array = new LinkedHashMap<String, Object>();
        array.put("type", "array");
        Map<String, Object> item = new LinkedHashMap<String, Object>();
        item.put("type", "string");
        array.put("items", item);
        return array;
    }

    @SafeVarargs
    private static Map<String, Object> properties(Map.Entry<String, Object>... entries) {
        Map<String, Object> properties = new LinkedHashMap<String, Object>();
        for (Map.Entry<String, Object> entry : entries) {
            properties.put(entry.getKey(), entry.getValue());
        }
        return properties;
    }

    private static Map.Entry<String, Object> entry(String key, Object value) {
        return new SimpleEntry(key, value);
    }

    private static class SimpleEntry implements Map.Entry<String, Object> {
        private final String key;
        private Object value;

        private SimpleEntry(String key, Object value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public String getKey() {
            return key;
        }

        @Override
        public Object getValue() {
            return value;
        }

        @Override
        public Object setValue(Object value) {
            Object old = this.value;
            this.value = value;
            return old;
        }
    }
}
