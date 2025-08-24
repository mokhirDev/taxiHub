package com.mokhir.dev.telegram.bot.taxi.hub.service;

import com.mokhir.dev.telegram.bot.taxi.hub.dto.PageDto;
import com.mokhir.dev.telegram.bot.taxi.hub.entity.UserState;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;

import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageBuilderService {
    private final LocalizationService localizationService;
    private final ButtonService buttonService;

    public SendMessage createNewMessage(UserState user, PageDto page) {
        Long chatId = user.getUserId();
        Locale locale = Locale.of(user.getLocale());

        String text = page.getMessage().stream()
                .map(m -> localizationService.getMessage(m, locale))
                .collect(Collectors.joining("\n"));

        SendMessage.SendMessageBuilder builder = SendMessage.builder()
                .chatId(chatId.toString())
                .text(text);

        InlineKeyboardMarkup inlineKeyboard = buttonService.buildInlineKeyboard(page.getButtons(), locale);
        ReplyKeyboardMarkup replyKeyboard = buttonService.buildReplyKeyboard(page.getButtons(), locale);

        if (inlineKeyboard != null && !inlineKeyboard.getKeyboard().isEmpty()) {
            builder.replyMarkup(inlineKeyboard);
        } else if (replyKeyboard != null && !replyKeyboard.getKeyboard().isEmpty()) {
            builder.replyMarkup(replyKeyboard);
        }

        return builder.build();
    }


    public EditMessageText editMessage(UserState userState, PageDto nextPage) {
        Locale locale = Locale.of(userState.getLocale());

        String text = nextPage.getMessage().stream()
                .map(m -> localizationService.getMessage(m, locale))
                .collect(Collectors.joining("\n"));

        Long chatId = userState.getUserId();

        InlineKeyboardMarkup inlineKeyboardMarkup = buttonService.buildInlineKeyboard(nextPage.getButtons(), locale);

        EditMessageText.EditMessageTextBuilder builder = EditMessageText.builder()
                .chatId(chatId.toString())
                .messageId(userState.getLastMessageId())
                .text(text);

        if (inlineKeyboardMarkup != null && !inlineKeyboardMarkup.getKeyboard().isEmpty()) {
            builder.replyMarkup(inlineKeyboardMarkup);
        }

        return builder.build();
    }

    public DeleteMessage deleteMessage(UserState userState) {
        Long chatId = userState.getUserId();
        Integer messageId = userState.getLastMessageId();

        return DeleteMessage.builder()
                .chatId(chatId.toString())
                .messageId(messageId)
                .build();
    }

    public Integer getLastMessageId(Update update) {
        if (update.hasCallbackQuery()) {
            return update.getCallbackQuery().getMessage().getMessageId();
        }
        return 0;
    }

}
