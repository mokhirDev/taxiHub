package com.mokhir.dev.telegram.bot.taxi.hub.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mokhir.dev.telegram.bot.taxi.hub.dto.PageDto;
import com.mokhir.dev.telegram.bot.taxi.hub.entity.UserState;
import com.mokhir.dev.telegram.bot.taxi.hub.entity.enums.AnswerTypeEnum;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class BotPageService {
    private List<PageDto> pages;

    private final ResourceLoader resourceLoader;
    private final ObjectMapper objectMapper;


    @PostConstruct
    public void loadPages() throws IOException {
        Resource resource = resourceLoader.getResource("classpath:pages/dynamic-pages.json");
        pages = objectMapper.readValue(resource.getInputStream(), new TypeReference<>() {
        });
    }

    public PageDto getCurrentPage(String pageCode) {
        return pages.stream()
                .filter(p -> p.getPageCode().equals(pageCode))
                .findFirst()
                .orElse(null);
    }

    public PageDto getNextPage(UserState userState, Update update) {
        PageDto currentPage = getCurrentPage(userState.getCurrentPageCode());
        Set<String> nextPageCodes = currentPage.getNextPage();
        AnswerTypeEnum answerType = getAnswerType(update);
        if (answerType == null) {
            return currentPage;
        }

        Optional<PageDto> nextPage = nextPageCodes
                .stream()
                .map(this::getCurrentPage)
                .filter(pageDto -> pageDto.getAnswerType().contains(answerType))
                .filter(pageDto -> {
                    if (answerType.equals(AnswerTypeEnum.CallBack)) {
                        return pageDto.getCallBackCondition().contains(update.getCallbackQuery().getData());
                    }
                    return true;
                })
                .findFirst();
        return nextPage.orElse(currentPage);
    }

    private AnswerTypeEnum getAnswerType(Update update) {
        if (update.hasCallbackQuery()) {
            return AnswerTypeEnum.CallBack;
        } else if (update.hasMessage() && update.getMessage().hasText()) {
            return AnswerTypeEnum.Text;
        } else if (update.hasMessage() && update.getMessage().hasContact()) {
            return AnswerTypeEnum.Contact;
        }
        return null;
    }
}
