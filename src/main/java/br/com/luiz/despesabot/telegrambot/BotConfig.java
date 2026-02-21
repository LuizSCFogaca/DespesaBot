package br.com.luiz.despesabot.telegrambot;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Configuration
public class BotConfig {

    @Bean
    public TelegramBotsApi telegramBotsApi(DespesaTelegramBot despesaTelegramBot) throws TelegramApiException {
        // Inicializa a API do Telegram usando a sessão de Long Polling padrão
        TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
        
        // Registra o bot que construímos
        api.registerBot(despesaTelegramBot);
        
        return api;
    }
}