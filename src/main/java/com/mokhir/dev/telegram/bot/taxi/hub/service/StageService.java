package com.mokhir.dev.telegram.bot.taxi.hub.service;

import com.mokhir.dev.telegram.bot.taxi.hub.dto.response.ResponseToUserDto;
import com.mokhir.dev.telegram.bot.taxi.hub.entity.enums.RideStatusEnum;
import org.telegram.telegrambots.meta.api.objects.Location;
import org.telegram.telegrambots.meta.api.objects.Update;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StageService {
    private final RedisService redisService;
    private final ButtonService buttonService;

    public void passengerOrDriver(ResponseToUserDto response, String text) {
        if (text.equalsIgnoreCase("passenger")) {
            passengerMenu(response);
        } else if (text.equalsIgnoreCase("driver")) {
            driverMenu(response);
        } else {
            response.setInlineMarkup(buttonService.getRoleMenuButtons());
        }
    }

    public void chooseRole(ResponseToUserDto response, String text) {
        if (text.equals("ru")) {
            response.setInlineMarkup(buttonService.getRoleMenuButtons());
            response.setText(RideStatusEnum.ROLE.getDefinition());
            redisService.setRideStage(response.getChatId(), RideStatusEnum.ROLE.toString());
        } else {
            setCurrentStage(response);
        }
    }

    public void chooseLanguage(ResponseToUserDto response, String text) {
        if (text.equals("/start")) {
            response.setInlineMarkup(buttonService.getLangButtons());
            response.setText(RideStatusEnum.LANGUAGE.getDefinition());
            redisService.setRideStage(response.getChatId(), RideStatusEnum.LANGUAGE.toString());
        } else {
            setCurrentStage(response);
        }
    }

    public void passengerMenu(ResponseToUserDto response) {
        response.setText(RideStatusEnum.PASSENGER_MENU.getDefinition());
        response.setInlineMarkup(buttonService.getPassengerMenuButtons());
        redisService.setRideStage(response.getChatId(), RideStatusEnum.PASSENGER_MENU.toString());
    }

    public void driverMenu(ResponseToUserDto response) {
        response.setText(RideStatusEnum.DRIVER_MENU.getDefinition());
        response.setInlineMarkup(buttonService.getDriverMenuButtons());
        redisService.setRideStage(response.getChatId(), RideStatusEnum.DRIVER_MENU.toString());
    }

    public void sendWelcomeMessage(ResponseToUserDto responseToUserDto) {
        responseToUserDto.setButtonMarkup(buttonService.getWelcomeButton());
        responseToUserDto.setText(RideStatusEnum.START.getDefinition());
        redisService.setRideStage(responseToUserDto.getChatId(), RideStatusEnum.START.toString());
    }

    public void leaveRequest(ResponseToUserDto response, Location location) {
        response.setText(RideStatusEnum.LEAVE_FROM.getDefinition());
        response.setButtonMarkup(buttonService.getLeavingRequestButton());
        redisService.setRideStage(response.getChatId(), RideStatusEnum.LEAVE_FROM.toString());
    }

    public void goTo(ResponseToUserDto response) {
        response.setText(RideStatusEnum.GO_TO.getDefinition());
        response.setButtonMarkup(buttonService.getLeavingRequestButton());
        redisService.setRideStage(response.getChatId(), RideStatusEnum.GO_TO.toString());
    }

    public void leavingDate(ResponseToUserDto response) {
        response.setText(RideStatusEnum.LEAVING_DATE.getDefinition());
        redisService.setRideStage(response.getChatId(), RideStatusEnum.LEAVING_DATE.toString());
    }

    public void countPassengers(ResponseToUserDto response, String text) {
        response.setText(RideStatusEnum.COUNT_PASSENGER.getDefinition());
        redisService.setRideStage(response.getChatId(), RideStatusEnum.COUNT_PASSENGER.toString());
        redisService.countPassengers(response.getChatId(), text);
    }

    public void passengerRequestDate(ResponseToUserDto response, String text) {
        // Сохраняем дату
        redisService.leavingDate(response.getChatId(), text);

        // Получаем все введённые ранее данные из stageService
        String date = redisService.getLeavingDate(response.getChatId());
        String seats = redisService.getPassengers(response.getChatId());

        // Формируем сводную информацию
        String summary = String.format(
                "📅 Дата: %s\n" +
                        "👥 Мест: %s\n\n" +
                        "✅ Заявка создана!",
                date, seats
        );

        response.setText(summary);
        redisService.setRideStage(response.getChatId(), RideStatusEnum.PASSENGER_REQUEST_DONE.toString());
    }


    public void passengerDirection(ResponseToUserDto response, Update update) {
        if (update.hasCallbackQuery() && update.hasMessage()) {
            String text = update.getMessage().getText();
            switch (text) {
                case "leave_request":
                    leaveRequest(response, update.getMessage().getLocation());
                    break;
                case "my_trips":
                    ;
                case "back_to_menu":
                    response.setInlineMarkup(buttonService.getRoleMenuButtons());
                    redisService.setRideStage(response.getChatId(), RideStatusEnum.ROLE.toString());
                    break;
                default:
                    setCurrentStage(response);
            }
        }


    }

    public void driverDirection(ResponseToUserDto response, String text) {
        switch (text) {
            case "show_tickets":
                ;
            case "my_clients":
                ;
            case "back_to_menu":
                response.setInlineMarkup(buttonService.getRoleMenuButtons());
                redisService.setRideStage(response.getChatId(), RideStatusEnum.ROLE.toString());
                break;
            default:
                setCurrentStage(response);
        }
    }

    public void setCurrentStage(ResponseToUserDto response) {
        String rideStage = redisService.getRideStage(response.getChatId());
        RideStatusEnum status = RideStatusEnum.valueOf(rideStage);
        switch (status) {
            case START -> {
                RideStatusEnum.START.getCurrentStatus(response);
                response.setButtonMarkup(buttonService.getWelcomeButton());
            }
            case LANGUAGE -> {
                RideStatusEnum.LANGUAGE.getCurrentStatus(response);
                response.setInlineMarkup(buttonService.getLangButtons());
            }
            case ROLE -> {
                RideStatusEnum.ROLE.getCurrentStatus(response);
                response.setInlineMarkup(buttonService.getRoleMenuButtons());
            }
            case DRIVER_MENU -> {
                RideStatusEnum.DRIVER_MENU.getCurrentStatus(response);
                response.setInlineMarkup(buttonService.getDriverMenuButtons());
            }
            case PASSENGER_MENU -> {
                RideStatusEnum.PASSENGER_MENU.getCurrentStatus(response);
                response.setInlineMarkup(buttonService.getPassengerMenuButtons());
            }
        }
    }


}
