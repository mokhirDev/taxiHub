package com.mokhir.dev.telegram.bot.taxi.hub.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mokhir.dev.telegram.bot.taxi.hub.dto.PageDto;
import jakarta.annotation.PostConstruct;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.hibernate.query.Page;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class BotPageService {
    private List<PageDto> pages;

    private final ResourceLoader resourceLoader;
    private final ObjectMapper objectMapper; // Jackson


    @PostConstruct
    public void loadPages() throws IOException {
        Resource resource = resourceLoader.getResource("classpath:pages/pages-dynamic.json");
        pages = objectMapper.readValue(resource.getInputStream(), new TypeReference<>() {
        });
    }

    public PageDto getPage(String pageCode) {
        return pages.stream()
                .filter(p -> p.getPageCode().equals(pageCode))
                .findFirst()
                .orElse(null);
    }

    public PageDto getNextPage(String currentPageCode) {
        return pages.stream()
                .filter(p->p.getPreviousPageCode()!=null && p.getPreviousPageCode().equals(currentPageCode))
                .findFirst()
                .orElse(null);
    }

}
