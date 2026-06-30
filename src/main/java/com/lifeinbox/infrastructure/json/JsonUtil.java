package com.lifeinbox.infrastructure.json;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public final class JsonUtil {
    private JsonUtil() {
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> parseObject(String json) {
        if (json == null || json.trim().isEmpty()) {
            return new LinkedHashMap<String, Object>();
        }

        ScriptEngine engine = new ScriptEngineManager().getEngineByName("javascript");
        if (engine == null) {
            throw new IllegalStateException("No JavaScript engine available for JSON parsing");
        }
        try {
            Object parsed = engine.eval("Java.asJSONCompatible(JSON.parse(" + quote(json) + "))");
            if (!(parsed instanceof Map)) {
                throw new IllegalArgumentException("Expected JSON object");
            }
            return (Map<String, Object>) parsed;
        } catch (ScriptException e) {
            throw new IllegalArgumentException("Invalid JSON request body", e);
        }
    }

    public static String stringify(Object value) {
        StringBuilder builder = new StringBuilder();
        appendJson(builder, value);
        return builder.toString();
    }

    @SuppressWarnings("rawtypes")
    private static void appendJson(StringBuilder builder, Object value) {
        if (value == null) {
            builder.append("null");
        } else if (value instanceof String || value instanceof UUID || value instanceof Instant || value instanceof LocalDateTime || value instanceof Enum) {
            builder.append(quote(String.valueOf(value)));
        } else if (value instanceof Number || value instanceof Boolean) {
            builder.append(String.valueOf(value));
        } else if (value instanceof Map) {
            builder.append('{');
            Iterator iterator = ((Map) value).entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry entry = (Map.Entry) iterator.next();
                builder.append(quote(String.valueOf(entry.getKey())));
                builder.append(':');
                appendJson(builder, entry.getValue());
                if (iterator.hasNext()) {
                    builder.append(',');
                }
            }
            builder.append('}');
        } else if (value instanceof Collection) {
            builder.append('[');
            Iterator iterator = ((Collection) value).iterator();
            while (iterator.hasNext()) {
                appendJson(builder, iterator.next());
                if (iterator.hasNext()) {
                    builder.append(',');
                }
            }
            builder.append(']');
        } else {
            builder.append(quote(String.valueOf(value)));
        }
    }

    public static String quote(String text) {
        StringBuilder builder = new StringBuilder();
        builder.append('"');
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            switch (c) {
                case '"':
                    builder.append("\\\"");
                    break;
                case '\\':
                    builder.append("\\\\");
                    break;
                case '\b':
                    builder.append("\\b");
                    break;
                case '\f':
                    builder.append("\\f");
                    break;
                case '\n':
                    builder.append("\\n");
                    break;
                case '\r':
                    builder.append("\\r");
                    break;
                case '\t':
                    builder.append("\\t");
                    break;
                default:
                    if (c < 0x20) {
                        builder.append(String.format("\\u%04x", (int) c));
                    } else {
                        builder.append(c);
                    }
            }
        }
        builder.append('"');
        return builder.toString();
    }
}
