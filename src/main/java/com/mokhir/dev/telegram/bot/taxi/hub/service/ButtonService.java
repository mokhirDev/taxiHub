package com.mokhir.dev.telegram.bot.taxi.hub.service;

import java.util.Locale;
import java.util.regex.Pattern;

import com.mokhir.dev.telegram.bot.taxi.hub.dto.ButtonDto;
import com.mokhir.dev.telegram.bot.taxi.hub.dto.VariableDto;
import com.mokhir.dev.telegram.bot.taxi.hub.entity.UserState;
import com.mokhir.dev.telegram.bot.taxi.hub.entity.Variable;
import com.mokhir.dev.telegram.bot.taxi.hub.entity.enums.ButtonTypeEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.*;


@Service
@RequiredArgsConstructor
public class ButtonService {

    private static final Pattern PATTERN = Pattern.compile("\\{([^}]+)\\}([+-]\\d+)");

    private final LocalizationService localizationService;
    private final VariableService variableService;
    private final BotPageService botPageService;

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
        replyKeyboard.setOneTimeKeyboard(false);
        replyKeyboard.setSelective(true);
        return replyKeyboard;
    }

    /**
     * Построить InlineKeyboardMarkup (inline-кнопки).
     */
    public InlineKeyboardMarkup buildInlineKeyboard(UserState userState, List<List<ButtonDto>> buttons, Locale locale) {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        for (List<ButtonDto> row : buttons) {
            List<InlineKeyboardButton> inlineRow = new ArrayList<>();
            for (ButtonDto btn : row) {
                if (btn.getButtonType() == ButtonTypeEnum.InlineKeyboardMarkup || btn.getButtonType() == ButtonTypeEnum.Expression) {
                    inlineRow.add(toInlineButton(btn, locale));
                } else if (btn.getButtonType() == ButtonTypeEnum.Variable) {
                    inlineRow.add(toVariableInlineButton(userState, btn));
                }
            }
            if (!inlineRow.isEmpty()) {
                keyboard.add(inlineRow);
            }
        }

        return keyboard.isEmpty() ? null : new InlineKeyboardMarkup(keyboard);
    }

    public InlineKeyboardMarkup buildDateInlineKeyboard(List<List<ButtonDto>> buttons, Locale locale) {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        for (List<ButtonDto> row : buttons) {
            List<InlineKeyboardButton> inlineRow = new ArrayList<>();
            for (ButtonDto btn : row) {
                if (btn.getButtonType() == ButtonTypeEnum.Date) {
                    inlineRow.add(toInlineDateButton(btn, locale));
                }
            }
            if (!inlineRow.isEmpty()) {
                keyboard.add(inlineRow);
            }
        }

        return keyboard.isEmpty() ? null : new InlineKeyboardMarkup(keyboard);
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

        if (dto.getUrl() != null) {
            button.setUrl(dto.getUrl());
        } else if (dto.getCallBack() != null) {
            button.setCallbackData(dto.getCallBack());
        }
        button.setText(localizationService.getMessage(dto.getTextCode(), locale));
        return button;
    }


    private InlineKeyboardButton toVariableInlineButton(UserState user, ButtonDto dto) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        VariableDto variableDto = botPageService.getVariableDto(dto.getCallBack());
        Variable variable = variableService.getOrCreateVariable(user, variableDto);
        button.setText(variable.getVariableValue().toString());
        button.setCallbackData(variable.getVariableName());
        return button;
    }

    private InlineKeyboardButton toInlineDateButton(ButtonDto dto, Locale locale) {
        locale = locale.getLanguage().equals("uz") ? new Locale.Builder().setLanguage("uz").setScript("Cyrl").build() : locale;

        InlineKeyboardButton button = new InlineKeyboardButton();
        String textCodeFormat = DateShiftFormatter.textCodeFormat(dto.getTextCode(), locale);
        String callBackFormat = DateShiftFormatter.callBackFormat(dto.getCallBack(), locale);
        button.setText(DateShiftFormatter.capitalizeWords(textCodeFormat));
        button.setCallbackData(DateShiftFormatter.capitalizeWords(callBackFormat));
        if (dto.getUrl() != null) {
            button.setUrl(dto.getUrl());
        }
        return button;
    }

}
