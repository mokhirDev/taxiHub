package com.mokhir.dev.telegram.bot.taxi.hub.service;

import com.mokhir.dev.telegram.bot.taxi.hub.config.BotConfig;
import com.mokhir.dev.telegram.bot.taxi.hub.dto.PageDto;
import com.mokhir.dev.telegram.bot.taxi.hub.entity.UserState;
import com.mokhir.dev.telegram.bot.taxi.hub.entity.enums.ButtonTypeEnum;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.extensions.bots.commandbot.TelegramLongPollingCommandBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;

import java.util.Collection;

@Component
@RequiredArgsConstructor
public class TaxiHubService extends TelegramLongPollingCommandBot {
    private static final Logger log = LoggerFactory.getLogger(TaxiHubService.class);

    private final BotConfig botConfig;
    private final BotNavigationService navigationService;
    private final ClientService clientService;
    private final MessageBuilderService messageBuilder;
    private final BotPageService botPageService;

    @Override
    @Transactional
    public void processNonCommandUpdate(Update update) {
        UserState user = clientService.getOrCreate(update);
        checkRestart(update, user);
        PageDto nextPage = navigationService.getNextPage(user, update);
        PageDto currentPage = botPageService.getCurrentPage(user.getCurrentPageCode());
        handleRemoveButtons(update, currentPage);
        sendMessage(user, nextPage);

        clientService.setCurrentPage(user, nextPage.getPageCode());
    }

    private void sendMessage(UserState user, PageDto nextPage) {
        try {
            if (notExistLastMessage(user)) {
                sendNewMessage(messageBuilder.createNewMessage(user, nextPage), user);
            } else {
                deleteMessage(messageBuilder.deleteMessage(user));
                sendNewMessage(messageBuilder.createNewMessage(user, nextPage), user);
            }
        } catch (RuntimeException e) {
            sendNewMessage(messageBuilder.createNewMessage(user, nextPage), user);
        }
    }

    private Boolean notExistLastMessage(UserState user) {
        return user.getLastMessageId() == 0;
    }

    private void checkRestart(Update update, UserState user) {
        if (update.hasMessage() && "/start".equals(update.getMessage().getText())) {
            clientService.resetUserStatus(user);
        }
    }

    private void handleRemoveButtons(Update update, PageDto currentPage) {
        if (update.hasMessage()) {
            if (update.getMessage().hasContact()) {
                removeKeyboard(update.getMessage().getChatId(), "Спасибо! Контакт получен ✅");
            } else if (update.getMessage().hasLocation()) {
                removeKeyboard(update.getMessage().getChatId(), "Спасибо! Локация получена ✅");
            } else if (hasReplyButton(currentPage)) {
                removeKeyboard(update.getMessage().getChatId(), "Спасибо! Контакт получен ✅");
            }
        }
    }

    private boolean hasReplyButton(PageDto currentPage) {
        return currentPage.getButtons() != null &&
                currentPage.getButtons().stream()
                        .flatMap(Collection::stream)
                        .anyMatch(e -> e.getButtonType().equals(ButtonTypeEnum.ReplyKeyboardMarkup));
    }

    @Override
    public String getBotUsername() {
        return botConfig.getBotUsername();
    }

    @Override
    public String getBotToken() {
        return botConfig.getBotToken();
    }

    public void sendNewMessage(SendMessage newMessage, UserState user) {
        try {
            Message message = execute(newMessage);
            user.setLastMessageId(message.getMessageId());
            clientService.save(user);
        } catch (Exception e) {
            log.error("Ошибка при отправке SendMessage в чат {}: {}", newMessage.getChatId(), e.getMessage(), e);
            throw new RuntimeException("Не удалось отправить сообщение", e);
        }
    }

    public void editMessage(EditMessageText message) {
        try {
            execute(message);
        } catch (Exception e) {
            log.error("Ошибка при редактировании сообщения {}: {}", message.getChatId(), e.getMessage(), e);
            throw new RuntimeException("Не удалось отредактировать сообщение", e);
        }
    }

    public void deleteMessage(DeleteMessage message) {
        try {
            execute(message);
        } catch (Exception e) {
            log.error("Ошибка при удалении сообщения {}: {}", message.getChatId(), e.getMessage(), e);
            throw new RuntimeException("Не удалось удалить сообщение", e);
        }
    }

    public void deleteReplyButton(SendMessage message) {
        try {
            execute(message);
        } catch (Exception e) {
            log.error("Ошибка при удалении сообщения {}: {}", message.getChatId(), e.getMessage(), e);
            throw new RuntimeException("Не удалось удалить сообщение", e);
        }
    }

    public void removeKeyboard(Long chatId, String text) {
        SendMessage message = new SendMessage(chatId.toString(), text);
        message.setReplyMarkup(new ReplyKeyboardRemove(true));
        deleteReplyButton(message);
    }
}