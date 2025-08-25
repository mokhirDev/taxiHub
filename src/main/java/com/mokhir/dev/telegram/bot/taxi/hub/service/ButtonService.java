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

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;


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
        button.setText(localizationService.getMessage(dto.getTextCode(), locale));
        if (dto.getUrl() != null) {
            button.setUrl(dto.getUrl());
        } else if (dto.getCallBack() != null) {
            button.setCallbackData(dto.getCallBack());
        }
        return button;
    }

    private InlineKeyboardButton toInlineDateButton(ButtonDto dto, Locale locale) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        String textCode = dto.getTextCode();
        Integer dayNumber = getDayNumber(textCode);
        button.setText(getFullDay(textCode, locale));
        button.setCallbackData(dayNumber.toString());
        if (dto.getUrl() != null) {
            button.setUrl(dto.getUrl());
        }
        return button;
    }

    public static Map<String, Integer> getCurrentWeekDays() {
        Map<String, Integer> daysMap = new LinkedHashMap<>();
        Locale locale = Locale.of("en");
        LocalDate today = LocalDate.now();
        LocalDate monday = today.with(DayOfWeek.MONDAY);

        for (int i = 0; i < 7; i++) {
            LocalDate day = monday.plusDays(i);

            String dayShort = day.getDayOfWeek()
                    .getDisplayName(TextStyle.SHORT, locale)
                    .toLowerCase(locale);

            daysMap.put(dayShort, day.getDayOfMonth());
        }

        return daysMap;
    }

    public static String getCurrentMonth() {
        LocalDate today = LocalDate.now();
        Locale locale = Locale.of("en");
        return today.getMonth().getDisplayName(TextStyle.SHORT, locale);
    }

    public String getFullDay(String day, Locale locale) {
        Integer dayNumber = getDayNumber(day);
        String nameDayOfWeek = localizationService.getMessage(day, locale);
        String currentMonth = getCurrentMonth();
        String nameOfMonth = localizationService.getMessage(currentMonth.toLowerCase(), locale);
        return nameDayOfWeek + ": " + " " + dayNumber + " " + nameOfMonth;
    }

    public Integer getDayNumber(String day) {
        return getCurrentWeekDays().get(day);
    }

}
