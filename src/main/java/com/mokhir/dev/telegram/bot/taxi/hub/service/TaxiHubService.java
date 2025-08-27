package com.mokhir.dev.telegram.bot.taxi.hub.service;

import com.mokhir.dev.telegram.bot.taxi.hub.config.BotConfig;
import com.mokhir.dev.telegram.bot.taxi.hub.dto.PageDto;
import com.mokhir.dev.telegram.bot.taxi.hub.dto.QueryConfig;
import com.mokhir.dev.telegram.bot.taxi.hub.entity.UserState;
import com.mokhir.dev.telegram.bot.taxi.hub.entity.enums.AnswerTypeEnum;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.extensions.bots.commandbot.TelegramLongPollingCommandBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.*;


@Component
@RequiredArgsConstructor
public class TaxiHubService extends TelegramLongPollingCommandBot {
    private final QueryLoadService queryLoadService;
    private final DynamicSqlExecutor executor;
    private static final Logger log = LoggerFactory.getLogger(TaxiHubService.class);

    private final BotConfig botConfig;
    private final ClientService clientService;
    private final MessageBuilderService messageBuilder;
    private final BotPageService botPageService;

    @Override
    @Transactional
    public void processNonCommandUpdate(Update update) {
        UserState user = clientService.getOrCreate(update);
        checkRestart(update, user);
        if (!isExpiredCallBack(update, user)) {
            PageDto currentPage = botPageService.getCurrentPage(user.getCurrentPageCode());
            handleUpdate(update, currentPage.getQuery());
            user = clientService.getOrCreate(update);
            PageDto nextPage = botPageService.getNextPage(user, update);
            sendMessage(user, nextPage, update);
            clientService.setCurrentPage(user, nextPage.getPageCode());
        }

    }

    private void sendMessage(UserState user, PageDto nextPage, Update update) {
        try {
            if (notExistLastMessage(user, update)) {
                sendNewMessage(messageBuilder.createNewMessage(user, nextPage), user);
            } else {
                if (botPageService.getAnswerType(update).equals(AnswerTypeEnum.CallBackOfExpression)) {
                    editMessage(
                            messageBuilder
                                    .calculateExpression(
                                            user,
                                            update,
                                            nextPage
                                    )
                    );
                } else if (botPageService.getAnswerType(update).equals(AnswerTypeEnum.CallBack) &&
                        nextPage.getAnswerType().contains(AnswerTypeEnum.CallBack)) {
                    editMessage(messageBuilder.editMessage(user, nextPage));
                } else if (botPageService.getAnswerType(update).equals(AnswerTypeEnum.CallBackOfVariable)) {
                    return;
                } else {
                    deleteMessage(messageBuilder.deleteMessage(user));
                    sendNewMessage(messageBuilder.createNewMessage(user, nextPage), user);
                }
            }
        } catch (RuntimeException e) {
            deleteMessage(messageBuilder.deleteMessage(user));
            sendNewMessage(messageBuilder.createNewMessage(user, nextPage), user);
        }
    }

    private Boolean notExistLastMessage(UserState user, Update update) {
        return user.getLastMessageId() == 0;
    }

    private void checkRestart(Update update, UserState user) {
        if (update.hasMessage() && "/start".equals(update.getMessage().getText())) {
            clientService.resetUserStatus(user);
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
            if (message == null) return;
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

    private boolean isExpiredCallBack(Update update, UserState userState) {
        if (update.hasCallbackQuery()) {
            Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
            return userState.getLastMessageId() > messageId;
        }
        return false;
    }

    @SneakyThrows
    public void handleUpdate(Update update, String queryName) {
        QueryConfig queryConfig = queryLoadService.getQueryByName(queryName);
        if (queryConfig != null) {
            executor.executeQuery(queryConfig, update);
        }
    }

}