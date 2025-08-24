package com.mokhir.dev.telegram.bot.taxi.hub.entity.enums;

import lombok.Getter;

import java.util.Set;

@Getter
public enum AnswerTypeEnum {
    Text,
    CallBack,
    Contact;

    public static boolean answerTypeExist(AnswerTypeEnum answerType) {
        AnswerTypeEnum[] values = values();
        for (AnswerTypeEnum value : values) {
            if (answerType.equals(value)) {
                return true;
            }
        }
        return false;
    }
}
