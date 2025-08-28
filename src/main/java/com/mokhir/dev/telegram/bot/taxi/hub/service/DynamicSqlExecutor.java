package com.mokhir.dev.telegram.bot.taxi.hub.service;

import com.mokhir.dev.telegram.bot.taxi.hub.dto.QueryConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DynamicSqlExecutor {

    private final JdbcTemplate jdbcTemplate;

    public void executeQuery(QueryConfig queryConfig, Update update) {
        Map<String, Object> localVars = new HashMap<>();

        queryConfig.getParams().forEach((key, template) -> {
            if (template.startsWith("{") && template.endsWith("}")) {
                String path = template.substring(1, template.length() - 1);
                localVars.put(key, extractFromUpdate(update, path));
            } else {
                localVars.put(key, template);
            }
        });

        NamedParameterJdbcTemplate namedTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
        namedTemplate.update(queryConfig.getSql(), localVars);
    }

    private Object extractFromUpdate(Update update, String path) {
        String[] parts = path.split("\\.");
        Object current = update;

        for (String part : parts) {
            if (current == null) break;

            current = switch (part) {
                case "update" -> current;
                case "callback" -> ((Update) current).getCallbackQuery();
                case "message" -> {
                    if (current instanceof Update u) yield u.getMessage();
                    else if (current instanceof CallbackQuery c) yield c.getMessage();
                    else yield null;
                }
                case "chat" -> {
                    if (current instanceof Message m) yield m.getChat();
                    else yield null;
                }
                case "id" -> {
                    if (current instanceof Chat c) yield c.getId();
                    else yield null;
                }
                case "data" -> {
                    if (current instanceof CallbackQuery c) yield c.getData();
                    else yield null;
                }
                case "text" -> {
                    if (current instanceof Message m) yield m.getText();
                    else yield null;
                }
                case "contact" -> {
                    if (current instanceof Message m) yield m.getContact();
                    else yield null;
                }
                case "phoneNumber" -> {
                    if (current instanceof Contact c) yield c.getPhoneNumber();
                    else yield null;
                }
                default -> {
                    // для динамических полей в тексте сообщения (например fromCity: "fromCity:Ташкент")
                    if (current instanceof String text) {
                        Map<String, String> map = Arrays.stream(text.split(","))
                                .map(s -> s.split(":"))
                                .filter(a -> a.length == 2)
                                .collect(Collectors.toMap(a -> a[0], a -> a[1]));
                        yield map.get(part);
                    }
                    yield null;
                }
            };
        }

        return current;
    }
}
