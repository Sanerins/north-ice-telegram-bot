package ru.nordic.ice.bot.conf;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.telegram.abilitybots.api.db.DBContext;
import org.telegram.abilitybots.api.sender.MessageSender;
import ru.nordic.ice.bot.BookingBot;

@Configuration
@EnableScheduling
public class BotConf {

    @Bean
    public MessageSender getMessageSender(BookingBot bot) {
        return bot.sender();
    }

    @Bean
    public DBContext getDataBase(BookingBot bot) {
        return bot.db();
    }

}
