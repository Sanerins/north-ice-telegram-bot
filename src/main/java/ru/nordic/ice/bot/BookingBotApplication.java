package ru.nordic.ice.bot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.telegram.abilitybots.api.objects.Ability;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.BotSession;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
@EnableScheduling
public class BookingBotApplication {
    private static final Logger LOG = LoggerFactory.getLogger(BookingBotApplication.class);

    private static BotSession botSession;
    private static ConfigurableApplicationContext applicationContext;

    public static void main(String[] args) {
        applicationContext = SpringApplication.run(BookingBotApplication.class, args);
        startBot();
    }

    private static void startBot() {
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            BookingBot bot = applicationContext.getBean("bookingBot", BookingBot.class);
            botSession = botsApi.registerBot(bot);

            LOG.info("Bot started successfully");

            // Add shutdown hook for graceful shutdown
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                if (botSession != null) {
                    botSession.stop();
                }
            }));

        } catch (TelegramApiException e) {
            LOG.error("Failed to register bot", e);
            throw new RuntimeException(e);
        }
    }

    @Scheduled(initialDelay = 30, fixedRate = 30, timeUnit = TimeUnit.SECONDS)
    public void monitorBotSession() {
        if (applicationContext == null) {
            LOG.info("Application context is null - bot doesn't exist yet");
            return;
        }
        if (botSession != null && botSession.isRunning()) {
            LOG.info("Bot session is running ok");
            return;
        }
        LOG.warn("Bot session is not running. Attempting to restart...");
        restartBot(applicationContext);
    }

    public synchronized void restartBot(ConfigurableApplicationContext ctx) {
        try {
            BotSession oldSession = botSession;
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            BookingBot bot = ctx.getBean("bookingBot", BookingBot.class);
            botSession = botsApi.registerBot(bot);
            LOG.info("Bot successfully restarted, stopping old session");
            if (oldSession != null) {
                oldSession.stop();
            }
            LOG.info("Old session stopped");
        } catch (Exception e) {
            LOG.error("Failed to restart bot, will reattempt in 30s", e);
        }
    }
}
