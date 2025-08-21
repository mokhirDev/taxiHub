package com.mokhir.dev.telegram.bot.taxi.hub.config;

import com.mokhir.dev.telegram.bot.taxi.hub.service.TaxiHubService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Configuration
public class BotInitializer {

    private final TaxiHubService taxiHubService;

    public BotInitializer(TaxiHubService taxiHubService) {
        this.taxiHubService = taxiHubService;
    }

    @Bean
    public TelegramBotsApi telegramBotsApi() throws TelegramApiException {
        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        botsApi.registerBot(taxiHubService);
        return botsApi;
    }
}
