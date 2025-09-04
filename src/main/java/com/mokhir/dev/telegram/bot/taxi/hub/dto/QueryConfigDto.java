package com.mokhir.dev.telegram.bot.taxi.hub.dto;

import lombok.Data;

import java.util.Map;

@Data
public class QueryConfigDto {
    private String name;
    private String sql;
    private Map<String, String> params;
    private AnswerDto answer;
}