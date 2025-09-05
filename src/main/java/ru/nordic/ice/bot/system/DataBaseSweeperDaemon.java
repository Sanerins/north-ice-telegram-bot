package ru.nordic.ice.bot.system;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.abilitybots.api.db.DBContext;
import ru.nordic.ice.bot.constant.ControlChatType;
import ru.nordic.ice.bot.constant.PersistentData;
import ru.nordic.ice.bot.constant.message.BookingMessage;
import ru.nordic.ice.bot.constant.message.SystemMessage;
import ru.nordic.ice.bot.dao.UserData;
import ru.nordic.ice.bot.service.SenderService;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class DataBaseSweeperDaemon {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataBaseSweeperDaemon.class);

    private final SenderService senderService;
    private final DBContext dbContext;

    public DataBaseSweeperDaemon(DBContext db, SenderService senderService) {
        this.senderService = senderService;
        this.dbContext = db;
    }

    @Scheduled(fixedRate = 5, timeUnit = TimeUnit.MINUTES)
    public void sweep() {
        if (!dbContext.getMap(PersistentData.CONTROL_CHATS).containsKey(ControlChatType.SYSTEM)) {
            LOGGER.warn("Can't sweep because system chat is not defined");
            return;
        }

        senderService.trySendingControlMessage(SystemMessage.SWEEP_START, ControlChatType.SYSTEM);

        long now = System.currentTimeMillis();

        Map<Long, UserData> dataMap = dbContext.getMap(PersistentData.CHAT_STATES);

        AtomicInteger amount = new AtomicInteger();

        dataMap.forEach((chatId, userData) -> {
            if (now - userData.getCreatedAt() < TimeUnit.MINUTES.toMillis(10)) {
                return;
            }
            senderService.sendMessage(() -> dataMap.remove(chatId), chatId, BookingMessage.TIMEOUT);
            amount.getAndIncrement();
        });

        if (amount.get() > 0) {
            senderService.trySendingControlMessage(SystemMessage.SWEEP_RESULT.formatted(amount.get()), ControlChatType.SYSTEM);
        }
    }

}
