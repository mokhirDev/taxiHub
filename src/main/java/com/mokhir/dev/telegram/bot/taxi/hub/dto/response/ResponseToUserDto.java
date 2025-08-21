package com.mokhir.dev.telegram.bot.taxi.hub.dto.response;

import com.mokhir.dev.telegram.bot.taxi.hub.entity.enums.RideStatusEnum;
import lombok.Builder;
import lombok.Data;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;


@Builder
@Data
public class ResponseToUserDto {
    private RideStatusEnum status;
    private String text;
    private Long chatId;
    private InlineKeyboardMarkup inlineMarkup;
    private ReplyKeyboardMarkup buttonMarkup;
}
