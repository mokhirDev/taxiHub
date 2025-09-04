package com.mokhir.dev.telegram.bot.taxi.hub.util;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;

public class TemplateUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @SneakyThrows
    public static String fillTemplate(String template, List<Object> values) {
        if (values == null || values.isEmpty()) {
            return template;
        }

        StringBuilder sb = new StringBuilder();
        for (Object obj : values) {

            // Если obj — это список, достаём первый элемент
            if (obj instanceof List<?> list && !list.isEmpty()) {
                obj = list.get(0);
            }

            Map<String, Object> valueMap;
            if (obj instanceof Map<?, ?> map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> casted = (Map<String, Object>) map;
                valueMap = casted;
            } else {
                valueMap = objectMapper.convertValue(obj, Map.class);
            }

            String result = template;
            for (Map.Entry<String, Object> entry : valueMap.entrySet()) {
                String placeholder = "{" + entry.getKey() + "}";
                result = result.replace(placeholder, entry.getValue() != null ? entry.getValue().toString() : "");
            }
            sb.append(result).append("\n\n"); // каждый результат с новой строки
        }
        return sb.toString().trim();
    }
}

