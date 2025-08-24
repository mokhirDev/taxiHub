package com.mokhir.dev.telegram.bot.taxi.hub.service;

import com.mokhir.dev.telegram.bot.taxi.hub.dto.PageDto;
import com.mokhir.dev.telegram.bot.taxi.hub.entity.UserState;
import com.mokhir.dev.telegram.bot.taxi.hub.entity.enums.AnswerTypeEnum;
import com.mokhir.dev.telegram.bot.taxi.hub.entity.enums.ButtonTypeEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Collection;


@Service
@RequiredArgsConstructor
public class BotNavigationService {

    private final BotPageService botPageService;

    public PageDto getNextPage(UserState userState, Update update) {
        PageDto nextPage;
        nextPage = botPageService.getNextPage(userState, update);
        return nextPage;
    }

}
