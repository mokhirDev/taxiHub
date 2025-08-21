package com.mokhir.dev.telegram.bot.taxi.hub.dto.request;

import com.mokhir.dev.telegram.bot.taxi.hub.entity.enums.RideStatusEnum;
import lombok.Builder;
import lombok.Data;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.time.LocalDateTime;

@Data
@Builder
public class RideRequest {
    private Long id;
    private Long passengerId;
    private String fromCity;
    private String toCity;
    private String phone;
    private RideStatusEnum status;
    private LocalDateTime createdAt;
    private InlineKeyboardMarkup inlineKeyboardMarkup;
}
