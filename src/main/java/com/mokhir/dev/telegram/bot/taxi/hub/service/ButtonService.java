package com.mokhir.dev.telegram.bot.taxi.hub.service;

import com.mokhir.dev.telegram.bot.taxi.hub.dto.ButtonDto;
import com.mokhir.dev.telegram.bot.taxi.hub.entity.enums.ButtonTypeEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.*;

@Service
@RequiredArgsConstructor
public class ButtonService {

    private final LocalizationService localizationService;

    /**
     * Построить ReplyKeyboardMarkup (обычные кнопки).
     */
    public ReplyKeyboardMarkup buildReplyKeyboard(List<List<ButtonDto>> buttons, Locale locale) {
        List<KeyboardRow> keyboardRows = new ArrayList<>();

        for (List<ButtonDto> row : buttons) {
            KeyboardRow keyboardRow = new KeyboardRow();
            for (ButtonDto btn : row) {
                if (btn.getButtonType() == ButtonTypeEnum.ReplyKeyboardMarkup) {
                    keyboardRow.add(toReplyButton(btn, locale));
                }
            }
            if (!keyboardRow.isEmpty()) {
                keyboardRows.add(keyboardRow);
            }
        }

        if (keyboardRows.isEmpty()) {
            return null; // клавиатуры нет
        }

        ReplyKeyboardMarkup replyKeyboard = new ReplyKeyboardMarkup();
        replyKeyboard.setKeyboard(keyboardRows);
        replyKeyboard.setResizeKeyboard(true);
        replyKeyboard.setOneTimeKeyboard(true);
        return replyKeyboard;
    }

    /**
     * Построить InlineKeyboardMarkup (inline-кнопки).
     */
    public InlineKeyboardMarkup buildInlineKeyboard(List<List<ButtonDto>> buttons, Locale locale) {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        for (List<ButtonDto> row : buttons) {
            List<InlineKeyboardButton> inlineRow = new ArrayList<>();
            for (ButtonDto btn : row) {
                if (btn.getButtonType() == ButtonTypeEnum.InlineKeyboardMarkup) {
                    inlineRow.add(toInlineButton(btn, locale));
                }
            }
            if (!inlineRow.isEmpty()) {
                keyboard.add(inlineRow);
            }
        }

        return keyboard.isEmpty() ? null : new InlineKeyboardMarkup(keyboard);
    }

    /**
     * Проверить, есть ли reply-кнопки.
     */
    public boolean hasReplyButtons(List<List<ButtonDto>> buttons) {
        return buttons.stream()
                .flatMap(List::stream)
                .anyMatch(btn -> btn.getButtonType() == ButtonTypeEnum.ReplyKeyboardMarkup);
    }

    /**
     * Удалить клавиатуру (Reply).
     */
    public ReplyKeyboardRemove removeKeyboard() {
        return new ReplyKeyboardRemove(true);
    }

    // ================== Приватные методы ==================

    private KeyboardButton toReplyButton(ButtonDto dto, Locale locale) {
        KeyboardButton button = new KeyboardButton();
        button.setText(localizationService.getMessage(dto.getTextCode(), locale));
        if (Boolean.TRUE.equals(dto.getContact())) {
            button.setRequestContact(true);
        } else if (Boolean.TRUE.equals(dto.getLocation())) {
            button.setRequestLocation(true);
        }
        return button;
    }

    private InlineKeyboardButton toInlineButton(ButtonDto dto, Locale locale) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(localizationService.getMessage(dto.getTextCode(), locale));
        if (dto.getUrl() != null) {
            button.setUrl(dto.getUrl());
        } else if (dto.getCallBack() != null) {
            button.setCallbackData(dto.getCallBack());
        }
        return button;
    }
}
