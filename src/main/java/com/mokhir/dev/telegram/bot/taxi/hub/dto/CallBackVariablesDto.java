package com.mokhir.dev.telegram.bot.taxi.hub.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class CallBackVariablesDto {
    Set<VariableDto> variableDto;
    Set<Expression> expressionDto;
}
