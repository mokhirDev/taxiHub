package com.mokhir.dev.telegram.bot.taxi.hub.service;

import com.mokhir.dev.telegram.bot.taxi.hub.config.BotConfig;
import com.mokhir.dev.telegram.bot.taxi.hub.dto.PageDto;
import com.mokhir.dev.telegram.bot.taxi.hub.entity.UserState;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.extensions.bots.commandbot.TelegramLongPollingCommandBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class TaxiHubService extends TelegramLongPollingCommandBot {
    private static final Logger log = LoggerFactory.getLogger(TaxiHubService.class);

    private final BotConfig botConfig;
    private final BotNavigationService navigationService;
    private final BotPageService botPageService;
    private final ButtonService buttonService;
    private final ClientService clientService;
    private final LocalizationService localizationService;

    @Override
    public void processNonCommandUpdate(Update update) {
        Long userId = update.getMessage().getFrom().getId();
        UserState user = clientService.getOrCreate(userId);
        if (update.getMessage().getText().equals("/start")) {
            clientService.resetUserStatus(user);
        }
        PageDto nextPage = navigationService.getNextPage(user, update);
        sendMessage(user, nextPage);
        clientService.setCurrentPage(user, nextPage.getPageCode());
        clientService.save(user);
    }

    private void sendMessage(UserState userState, PageDto page) {
        Locale locale = Locale.of(userState.getLocale());
        StringBuilder message = new StringBuilder();
        page.getMessage().forEach(mDto -> {
            String localizedMessage = localizationService.getMessage(mDto, locale);
            message.append(localizedMessage);
            message.append("\n");
        });

        // Формируем клавиатуру (только одну)
        InlineKeyboardMarkup inlineKeyboard = buttonService.buildInlineKeyboard(page.getButtons(), locale);
        ReplyKeyboardMarkup replyKeyboard = buttonService.buildReplyKeyboard(page.getButtons(), locale);

        // Выбираем, какую клавиатуру отправлять
        if (inlineKeyboard != null) {
            SendMessage sendMessage = SendMessage.builder()
                    .chatId(userState.getUserId().toString())
                    .text(message.toString())
                    .replyMarkup(inlineKeyboard)
                    .build();
            executeMessage(sendMessage);

        } else if (replyKeyboard != null) {
            replyKeyboard.setOneTimeKeyboard(true);
            SendMessage sendMessage = SendMessage.builder()
                    .chatId(userState.getUserId().toString())
                    .replyMarkup(replyKeyboard)
                    .build();
            executeMessage(sendMessage);
        }
    }

    @Override
    public String getBotUsername() {
        return botConfig.getBotUsername();
    }

    @Override
    public String getBotToken() {
        return botConfig.getBotToken();
    }

    public void executeMessage(SendMessage message) {
        try {
            execute(message);
        } catch (Exception e) {
            log.error("Ошибка при отправке сообщения в чат {}: {}", message.getChatId(), e.getMessage());
        }
    }
}