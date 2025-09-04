package com.mokhir.dev.telegram.bot.taxi.hub.dto;

import com.mokhir.dev.telegram.bot.taxi.hub.entity.enums.AnswerTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageDto {
    private String pageCode;
    private List<String> query;
    private Set<String> nextPage;
    private Set<String> expectedCallBacks;
    private Set<AnswerTypeEnum> answerType;
    private Set<String> message;
    private List<List<ButtonDto>> buttons;
}
