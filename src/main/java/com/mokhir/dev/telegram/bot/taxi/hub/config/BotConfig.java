package com.mokhir.dev.telegram.bot.taxi.hub.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "telegram.bots")
@Getter
@Setter
public class BotConfig {
    private String botUsername;
    private String botToken;
}
