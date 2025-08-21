package com.mokhir.dev.telegram.bot.taxi.hub.service;

import com.mokhir.dev.telegram.bot.taxi.hub.dto.ButtonDto;
import com.mokhir.dev.telegram.bot.taxi.hub.entity.enums.ButtonTypeEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendLocation;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class ButtonService {
    private final LocalizationService localizationService;

    public InlineKeyboardMarkup getLangButtons() {
        InlineKeyboardButton uzbek = InlineKeyboardButton.builder()
                .text("\uD83C\uDDFA\uD83C\uDDFF")
                .callbackData("uzbek")
                .build();

        InlineKeyboardButton russian = InlineKeyboardButton.builder()
                .text("\uD83C\uDDF7\uD83C\uDDFA")
                .callbackData("ru")
                .build();

        InlineKeyboardButton english = InlineKeyboardButton.builder()
                .text("\uD83C\uDDEC\uD83C\uDDE7")
                .callbackData("en")
                .build();

        return InlineKeyboardMarkup.builder()
                .keyboard(List.of(List.of(uzbek, russian, english)))
                .build();
    }

    public InlineKeyboardMarkup getPassengerMenuButtons() {
        InlineKeyboardButton leaveRequest = InlineKeyboardButton.builder()
                .text("\uD83D\uDCDD Оставить заявку")
                .callbackData("leave_request")
                .build();

        InlineKeyboardButton myTrips = InlineKeyboardButton.builder()
                .text("\uD83D\uDCC5 Мои поездки")
                .callbackData("my_trips")
                .build();

        InlineKeyboardButton backToMenu = InlineKeyboardButton.builder()
                .text("⬅\uFE0F Назад в меню")
                .callbackData("back_to_menu")
                .build();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        List<InlineKeyboardButton> row2 = new ArrayList<>();
        List<InlineKeyboardButton> row3 = new ArrayList<>();
        row1.add(leaveRequest);
        row2.add(myTrips);
        row3.add(backToMenu);
        return InlineKeyboardMarkup.builder()
                .keyboard(List.of(row1, row2, row3))
                .build();
    }

    public InlineKeyboardMarkup getDriverMenuButtons() {
        InlineKeyboardButton leaveRequest = InlineKeyboardButton.builder()
                .text("\uD83D\uDD0D Смотреть заявки ")
                .callbackData("show_tickets")
                .build();

        InlineKeyboardButton myTrips = InlineKeyboardButton.builder()
                .text("\uD83D\uDCC5 Мои клиенты")
                .callbackData("my_clients")
                .build();

        InlineKeyboardButton backToMenu = InlineKeyboardButton.builder()
                .text("⬅\uFE0F Назад в меню")
                .callbackData("back_to_menu")
                .build();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        List<InlineKeyboardButton> row2 = new ArrayList<>();
        List<InlineKeyboardButton> row3 = new ArrayList<>();
        row1.add(leaveRequest);
        row2.add(myTrips);
        row3.add(backToMenu);
        return InlineKeyboardMarkup.builder()
                .keyboard(List.of(row1, row2, row3))
                .build();
    }

    public InlineKeyboardMarkup getRoleMenuButtons() {
        InlineKeyboardButton passenger = InlineKeyboardButton.builder()
                .text("\uD83E\uDDCD Я пассажир")
                .callbackData("passenger")
                .build();

        InlineKeyboardButton driver = InlineKeyboardButton.builder()
                .text("\uD83D\uDE97 Я водитель")
                .callbackData("driver")
                .build();

        return InlineKeyboardMarkup.builder()
                .keyboard(List.of(List.of(passenger, driver)))
                .build();
    }

    public ReplyKeyboardMarkup getLeavingRequestButton() {
        KeyboardButton locationButton = new KeyboardButton("📍 Отправить местоположение");
        locationButton.setRequestLocation(true);
        KeyboardRow row = new KeyboardRow();
        row.add(locationButton);
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(true);
        replyKeyboardMarkup.setKeyboard(List.of(row));

        return replyKeyboardMarkup;
    }

    public ReplyKeyboardMarkup getWelcomeButton() {
        KeyboardButton startBtn = new KeyboardButton("/start");
        KeyboardRow row = new KeyboardRow();
        row.add(startBtn);
        return ReplyKeyboardMarkup.builder()
                .resizeKeyboard(true)
                .oneTimeKeyboard(false)
                .keyboardRow(row)
                .build();
    }

    public SendLocation getLocationToGroup(Double latitude, Double longitude) {
        SendLocation sendLocation = new SendLocation();
        sendLocation.setChatId("-1001234567890"); // ID группы (с минусом для супергрупп)
        sendLocation.setLatitude(latitude);
        sendLocation.setLongitude(longitude);
        return sendLocation;
    }

    // Генерация InlineKeyboardMarkup из списка кнопок
    public InlineKeyboardMarkup buildInlineKeyboard(List<List<ButtonDto>> buttons, Locale locale) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        for (List<ButtonDto> row : buttons) {
            List<InlineKeyboardButton> keyboardRow = new ArrayList<>();
            for (ButtonDto btn : row) {
                if (ButtonTypeEnum.InlineKeyboardMarkup.equals(btn.getButtonType())) {
                    InlineKeyboardButton button = new InlineKeyboardButton();
                    button.setText(localizationService.getMessage(btn.getTextCode(), locale));
                    button.setCallbackData(
                            btn.getCallBack() != null ? btn.getCallBack() : btn.getTextCode()
                    );
                    keyboardRow.add(button);
                }
            }
            if (!keyboardRow.isEmpty()) {
                rows.add(keyboardRow);
            }
        }

        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        keyboard.setKeyboard(rows);
        return keyboard;
    }

    // Генерация ReplyKeyboardMarkup
    public ReplyKeyboardMarkup buildReplyKeyboard(List<List<ButtonDto>> buttons, Locale locale) {
        List<KeyboardRow> keyboardRows = new ArrayList<>();

        for (List<ButtonDto> row : buttons) {
            KeyboardRow keyboardRow = new KeyboardRow();
            for (ButtonDto btn : row) {
                if (ButtonTypeEnum.ReplyKeyboardMarkup.equals(btn.getButtonType())) {
                    KeyboardButton kb = new KeyboardButton();
                    kb.setText(localizationService.getMessage(btn.getTextCode(), locale));
                    keyboardRow.add(kb);
                }
            }
            if (!keyboardRow.isEmpty()) {
                keyboardRows.add(keyboardRow);
            }
        }

        ReplyKeyboardMarkup replyKeyboard = new ReplyKeyboardMarkup();
        replyKeyboard.setKeyboard(keyboardRows);
        replyKeyboard.setResizeKeyboard(true);
        replyKeyboard.setOneTimeKeyboard(false);
        return replyKeyboard;
    }


}
