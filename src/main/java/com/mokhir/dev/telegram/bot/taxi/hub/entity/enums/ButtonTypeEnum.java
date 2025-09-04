package com.mokhir.dev.telegram.bot.taxi.hub.entity.enums;

import lombok.Getter;

@Getter
public enum ButtonTypeEnum {
    InlineKeyboardMarkup("inline_keyboard"),
    ReplyKeyboardMarkup("reply_keyboard"),
    Date("inline_keyboard"),
    Expression("inline_keyboard"),
    Variable("inline_keyboard"),
    Location("reply_keyboard"),
    Contact("reply_keyboard");

    private final String group;

    ButtonTypeEnum(String group) {
        this.group = group;
    }

}

