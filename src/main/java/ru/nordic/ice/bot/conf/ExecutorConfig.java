package ru.nordic.ice.bot.conf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
public class ExecutorConfig {
    Logger logger = LoggerFactory.getLogger(ExecutorConfig.class);

    @Bean
    public Executor telegramTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setThreadNamePrefix("TelegramBot-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setCorePoolSize(6);

        // Custom exception handler
        executor.setTaskDecorator(runnable -> () -> {
            try {
                runnable.run();
            } catch (Exception e) {
                logger.error("Unhandled exception in Telegram executor: {}", e.getMessage(), e);
            }
        });

        executor.initialize();
        return executor;
    }
}