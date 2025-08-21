package com.mokhir.dev.telegram.bot.taxi.hub.dto;

import com.mokhir.dev.telegram.bot.taxi.hub.entity.enums.ButtonTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ButtonDto {
    private String textCode;
    private String callBack;
    private ButtonTypeEnum buttonType;
    private String url;
    private List<Condition> conditions;
}
