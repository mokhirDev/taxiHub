package com.mokhir.dev.telegram.bot.taxi.hub.dto;

import lombok.Data;

@Data
public class Condition {
    private String type;   // например: role, has_phone, has_from_location, order_complete
    private Object value;  // значение для проверки
}