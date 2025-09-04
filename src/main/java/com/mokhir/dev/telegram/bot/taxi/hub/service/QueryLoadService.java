package com.mokhir.dev.telegram.bot.taxi.hub.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mokhir.dev.telegram.bot.taxi.hub.dto.QueryConfigDto;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QueryLoadService {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ResourceLoader resourceLoader;

    private List<QueryConfigDto> queriesConfig; // кеш для загруженных query

    @PostConstruct
    public void init() throws IOException {
        // Загружаем JSON один раз при старте приложения
        Resource resource = resourceLoader.getResource("classpath:load/queries.json");
        queriesConfig = objectMapper.readValue(
                resource.getInputStream(),
                new TypeReference<List<QueryConfigDto>>() {
                }
        );
    }

    // Метод для поиска конкретного query по имени
    public QueryConfigDto getQueryByName(String name) {
        return queriesConfig.stream()
                .filter(q -> q.getName().equals(name))
                .findFirst()
                .orElse(null);
    }
}
