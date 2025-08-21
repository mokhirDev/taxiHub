package com.mokhir.dev.telegram.bot.taxi.hub.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageDto {

    private String pageCode;
    private List<String> message;
    private String previousPageCode;
    private List<List<ButtonDto>> buttons;
}
