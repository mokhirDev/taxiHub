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


    public UserState getOrCreate(Update update) {
        Long userId = getUserId(update);
        String userName = getUserName(update);
        Optional<UserState> byId = userStateRepository.findByUserId(userId);
        if (byId.isPresent()) {
            return byId.get();
        } else {
            UserState newUserState = UserState
                    .builder()
                    .userId(userId)
                    .currentPageCode("start")
                    .userName(userName)
                    .locale("uz")
                    .orders(new ArrayList<>())
                    .build();
            userStateRepository.save(newUserState);
            return newUserState;
        }
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
//
//    public void updateField(Long userId, String fieldName, Object value) {
//        UserState user = getOrCreate(userId);
//        switch (fieldName) {
//            case "role":
//                user.setRole(ClientTypeEnum.valueOf((String) value));
//                break;
//            case "hasPhone":
//                user.setHasPhone((Boolean) value);
//                break;
//            case "hasFromLocation":
//                user.setHasFromLocation((Boolean) value);
//                break;
//            case "hasToLocation":
//                user.setHasToLocation((Boolean) value);
//                break;
//            case "hasDate":
//                user.setHasDate((Boolean) value);
//                break;
//            case "hasSeats":
//                user.setHasSeats((Boolean) value);
//                break;
//            case "orderComplete":
//                user.setOrderComplete((Boolean) value);
//                break;
//            default:
//                user().put(fieldName, value); // динамические поля заказа
//        }
//        save(user);
//    }

    public void setCurrentPage(UserState userState, String pageCode) {
        userState.setCurrentPageCode(pageCode);
        save(userState);
    }

    public void resetUserStatus(UserState user) {
        user.setCurrentPageCode("start");
        save(user);
    }

//    public String getCurrentPage(Long userId) {
//        return getOrCreate(userId).getCurrentPageCode();
//    }
//
//    public boolean checkConditions(Long userId, List<Condition> conditions) {
//        UserState user = getOrCreate(userId);
//        if (conditions == null || conditions.isEmpty()) return true;
//        for (Condition condition : conditions) {
//            switch (condition.getType()) {
//                case "role":
//                    if (!user.getRole().equals(condition.getValue())) return false;
//                    break;
//                case "has_phone":
//                    if (user.isHasPhone() != (Boolean) condition.getValue()) return false;
//                    break;
//                case "has_from_location":
//                    if (user.isHasFromLocation() != (Boolean) condition.getValue()) return false;
//                    break;
//                case "has_to_location":
//                    if (user.isHasToLocation() != (Boolean) condition.getValue()) return false;
//                    break;
//                case "has_date":
//                    if (user.isHasDate() != (Boolean) condition.getValue()) return false;
//                    break;
//                case "has_seats":
//                    if (user.isHasSeats() != (Boolean) condition.getValue()) return false;
//                    break;
//                case "order_complete":
//                    if (user.isOrderComplete() != (Boolean) condition.getValue()) return false;
//                    break;
//                default:
//                    Object val = user.getOrderData().get(condition.getType());
//                    if (!condition.getValue().equals(val)) return false;
//            }
//        }
//        return true;
//    }
//
//    public void resetState(Long userId) {
//        userStates.remove(userId);
//    }
}
