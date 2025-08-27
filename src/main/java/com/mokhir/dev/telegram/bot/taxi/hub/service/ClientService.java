package com.mokhir.dev.telegram.bot.taxi.hub.service;

import com.mokhir.dev.telegram.bot.taxi.hub.entity.UserState;
import com.mokhir.dev.telegram.bot.taxi.hub.repository.UserStateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.ArrayList;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ClientService {
    private final UserStateRepository userStateRepository;
    private final MessageBuilderService messageService;


    public UserState getOrCreate(Update update) {
        UserState userState;
        Long userId = getUserId(update);
        String userName = getUserName(update);
        Optional<UserState> byId = userStateRepository.findByUserId(userId);
        if (byId.isPresent()) {
            userState = byId.get();
            if (update.hasCallbackQuery()) {
                messageService.updateLastMessageId(update, userState);
            }
        } else {
            userState = UserState
                    .builder()
                    .userId(userId)
                    .currentPageCode("start")
                    .userName(userName)
                    .orders(new ArrayList<>())
                    .lastMessageId(messageService.setLastMessageId(update))
                    .build();
        }
        userStateRepository.save(userState);
        return userState;
    }

    private String getUserName(Update update) {
        return update.hasMessage() ?
                update.getMessage().getFrom().getUserName() : update.getCallbackQuery().getFrom().getUserName();
    }

    private Long getUserId(Update update) {
        return update.hasMessage() ?
                update.getMessage().getFrom().getId() : update.getCallbackQuery().getFrom().getId();
    }

    public void save(UserState userState) {
        userStateRepository.save(userState);
    }

    public void setCurrentPage(UserState userState, String pageCode) {
        userState.setCurrentPageCode(pageCode);
        save(userState);
    }

    public void resetUserStatus(UserState user) {
        user.setCurrentPageCode("start");
        user.setLastMessageId(0);
        user.setLocale(null);
        save(user);
    }
}
