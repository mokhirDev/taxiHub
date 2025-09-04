package com.mokhir.dev.telegram.bot.taxi.hub.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mokhir.dev.telegram.bot.taxi.hub.dto.Expression;
import com.mokhir.dev.telegram.bot.taxi.hub.dto.PageDto;
import com.mokhir.dev.telegram.bot.taxi.hub.dto.CallBackVariablesDto;
import com.mokhir.dev.telegram.bot.taxi.hub.dto.VariableDto;
import com.mokhir.dev.telegram.bot.taxi.hub.entity.UserState;
import com.mokhir.dev.telegram.bot.taxi.hub.entity.enums.AnswerTypeEnum;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
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
    @Getter
    private CallBackVariablesDto callBackVariablesDto;

    private final ResourceLoader resourceLoader;
    private final ObjectMapper objectMapper;


    @PostConstruct
    public void loadPages() throws IOException {
        Resource resource = resourceLoader.getResource("classpath:load/dynamic-pages.json");
        pages = objectMapper.readValue(resource.getInputStream(), new TypeReference<>() {
        });
    }

    @PostConstruct
    public void loadVariables() throws IOException {
        Resource resource = resourceLoader.getResource("classpath:load/variables.json");
        callBackVariablesDto = objectMapper.readValue(resource.getInputStream(), new TypeReference<>() {
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
        if (answerType == null || !currentPage.getAnswerType().contains(answerType)) {
            return currentPage;
        }

        Optional<PageDto> nextPage = nextPageCodes
                .stream()
                .map(this::getCurrentPage)
                .filter(pageDto -> {
                    if (answerType.equals(AnswerTypeEnum.CallBack)) {
                        return pageDto.getExpectedCallBacks().contains("*") || pageDto.getExpectedCallBacks().toString().contains(update.getCallbackQuery().getData());
                    }
                    return true;
                })
                .findFirst();
        return nextPage.orElse(currentPage);
    }

    public AnswerTypeEnum getAnswerType(Update update) {
        if (update.hasCallbackQuery()) {
            if (callBackIsExpression(update.getCallbackQuery().getData())) {
                return AnswerTypeEnum.CallBackOfExpression;
            } else if (callBackIsVariable(update.getCallbackQuery().getData())) {
                return AnswerTypeEnum.CallBackOfVariable;
            }
            return AnswerTypeEnum.CallBack;
        } else if (update.hasMessage() && update.getMessage().hasText()) {
            return AnswerTypeEnum.Text;
        } else if (update.hasMessage() && update.getMessage().hasContact()) {
            return AnswerTypeEnum.Contact;
        }
        return null;
    }

    public boolean callBackIsExpression(String callBack) {
        Optional<Expression> isExpression = callBackVariablesDto.getExpressionDto().stream().filter(expression -> {
            return expression.getName().equals(callBack);
        }).findFirst();
        return isExpression.isPresent();
    }

    public boolean callBackIsVariable(String callBack) {
        Optional<VariableDto> isVariable = callBackVariablesDto.getVariableDto().stream().filter(variableDto -> {
            return variableDto.getName().equals(callBack);
        }).findFirst();
        return isVariable.isPresent();
    }

    public VariableDto getVariableDto(String callBack) {
        return callBackVariablesDto
                .getVariableDto()
                .stream()
                .filter(v -> v.getName().equals(callBack))
                .findFirst()
                .get();
    }

    public String getUpdateText(Update update) {
        AnswerTypeEnum answerType = getAnswerType(update);
        if (answerType == null) {
            return null;
        }
        return switch (answerType) {
            case CallBack -> update.getCallbackQuery().getData();
            case Text -> update.getMessage().getText();
            case Contact -> update.getMessage().getContact().toString();
            default -> null;
        };
    }
}
