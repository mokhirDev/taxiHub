package com.mokhir.dev.telegram.bot.taxi.hub.service;

import com.mokhir.dev.telegram.bot.taxi.hub.entity.enums.RideStatusEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class RedisService {
    private final StringRedisTemplate redisTemplate;

    private String getRideStageKey(Long chatId) {
        return "taxihub:user:" + chatId + ":stage";
    }

    private String getPassengersKey(Long chatId) {
        return "taxihub:user:" + chatId + ":passengers";
    }

    private String getLeavingDateKey(Long chatId) {
        return "taxihub:user:" + chatId + ":leaving_date";
    }

    public void setRideStage(Long chatId, String stage) {
        redisTemplate.opsForValue()
                .set(getRideStageKey(chatId), stage);
    }

    public String getRideStage(Long chatId) {
        return redisTemplate.opsForValue()
                .get(getRideStageKey(chatId));
    }

    public void clearRideStage(Long chatId) {
        redisTemplate.delete(getRideStageKey(chatId));
    }

    public boolean checkRideStage(String stage) {
        return Arrays.stream(RideStatusEnum.values())
                .anyMatch(e -> e.name().equalsIgnoreCase(stage));
    }

    public void countPassengers(Long chatId, String text) {
        redisTemplate.opsForValue().set(getPassengersKey(chatId), text);
    }

    public String getPassengers(Long chatId) {
        return redisTemplate.opsForValue().get(getPassengersKey(chatId));
    }

    public void leavingDate(Long chatId, String text) {
        redisTemplate.opsForValue().set(getLeavingDateKey(chatId), text);
    }

    public String getLeavingDate(Long chatId) {
        return redisTemplate.opsForValue().get(getLeavingDateKey(chatId));
    }

    public void clearUserData(Long chatId) {
        redisTemplate.delete(Arrays.asList(
                getRideStageKey(chatId),
                getPassengersKey(chatId),
                getLeavingDateKey(chatId)
        ));
    }
}