package com.mokhir.dev.telegram.bot.taxi.hub.entity.enums;

import lombok.Getter;

import java.util.Set;

@Getter
public enum AnswerTypeEnum {
    Text,
    CallBack,
    CallBackOfExpression,
    CallBackOfVariable,
    Date,
    Contact;
}
