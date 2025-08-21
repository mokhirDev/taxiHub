package com.mokhir.dev.telegram.bot.taxi.hub.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class UserState {
    private Long userId;
    private String role;
    private boolean hasPhone;
    private boolean hasFromLocation;
    private boolean hasToLocation;
    private boolean hasDate;
    private boolean hasSeats;
    private boolean orderComplete;
    private String currentPageCode;
    private Map<String, Object> orderData;
}