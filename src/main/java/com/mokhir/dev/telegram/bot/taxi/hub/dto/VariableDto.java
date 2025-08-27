package com.mokhir.dev.telegram.bot.taxi.hub.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class VariableDto {
    String name;
    Integer value;
    Integer max;
    Integer min;
}
