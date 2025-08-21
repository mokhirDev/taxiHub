package com.mokhir.dev.telegram.bot.taxi.hub.service;

import com.mokhir.dev.telegram.bot.taxi.hub.dto.Condition;
import com.mokhir.dev.telegram.bot.taxi.hub.dto.PageDto;
import com.mokhir.dev.telegram.bot.taxi.hub.entity.UserState;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BotNavigationService {

    private final BotPageService botPageService;

    public PageDto getNextPage(UserState userState, Update update) {
        String callBack = getReplyButtonCallBack(update);
        PageDto nextPage = null;
        if (callBack != null) {
            nextPage = botPageService.getPage(callBack);
        } else {
            nextPage = botPageService.getNextPage(userState.getCurrentPageCode());
        }
        return nextPage;
    }

    private String getReplyButtonCallBack(Update update) {
        if (update.hasCallbackQuery()) return update.getCallbackQuery().getData();
        return null;
    }

    private boolean checkConditions(UserState user, List<Condition> conditions) {
        if (conditions == null || conditions.isEmpty()) return true;
        for (Condition condition : conditions) {
            switch (condition.getType()) {
                case "role":
                    if (!user.getRole().equals(condition.getValue())) return false;
                    break;
                case "has_phone":
                    if (user.isHasPhone() != (Boolean) condition.getValue()) return false;
                    break;
                case "has_from_location":
                    if (user.isHasFromLocation() != (Boolean) condition.getValue()) return false;
                    break;
                case "has_to_location":
                    if (user.isHasToLocation() != (Boolean) condition.getValue()) return false;
                    break;
                case "has_date":
                    if (user.isHasDate() != (Boolean) condition.getValue()) return false;
                    break;
                case "has_seats":
                    if (user.isHasSeats() != (Boolean) condition.getValue()) return false;
                    break;
                case "order_complete":
                    if (user.isOrderComplete() != (Boolean) condition.getValue()) return false;
                    break;
                default:
                    break;
            }
        }
        return true;
    }
}
