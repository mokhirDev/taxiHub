package com.mokhir.dev.telegram.bot.taxi.hub.entity.enums;

import com.mokhir.dev.telegram.bot.taxi.hub.dto.response.ResponseToUserDto;
import lombok.Getter;

@Getter
public enum RideStatusEnum {
    START("Начать"),
    LANGUAGE("Выберите язык"),
    ROLE("Выберите роль"),
    PASSENGER_MENU("Главное меню"),
    LEAVE_FROM("Откуда"),
    GO_TO("Куда"),
    LEAVING_DATE("Когда"),
    COUNT_PASSENGER("Кол-во мест"),
    PASSENGER_REQUEST_DONE("Заявка создана!"),
    DRIVER_MENU("Главное меню");
//    TAKEN,
//    CONFIRMED,
//    CANCELLED_BY_PASSENGER,
//    CANCELLED_BY_DRIVER,
//    EXPIRED;

    private final String definition;

    RideStatusEnum(String definition) {
        this.definition = definition;
    }

    public void getCurrentStatus(ResponseToUserDto response) {
        switch (this) {
            case START:
                response.setText(RideStatusEnum.START.getDefinition());
            case LANGUAGE:
                response.setText(RideStatusEnum.LANGUAGE.getDefinition());
            case ROLE:
                response.setText(RideStatusEnum.ROLE.getDefinition());
            case PASSENGER_MENU:
                response.setText(RideStatusEnum.PASSENGER_MENU.getDefinition());
            case DRIVER_MENU:
                response.setText(RideStatusEnum.DRIVER_MENU.getDefinition());
        }
    }
}
