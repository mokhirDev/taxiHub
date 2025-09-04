package com.mokhir.dev.telegram.bot.taxi.hub.dto;

import lombok.Data;

import java.util.List;

@Data
public class QueriesConfigDto {
    private List<QueryConfigDto> queries;
}