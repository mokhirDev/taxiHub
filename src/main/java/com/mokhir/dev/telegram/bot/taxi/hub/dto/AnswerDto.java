package com.mokhir.dev.telegram.bot.taxi.hub.dto;

import lombok.Data;

import java.util.List;

@Data
public class AnswerDto {
    private List<String> type;
    private List<String> expectedAnswers;
}
