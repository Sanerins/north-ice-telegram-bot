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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class DataBaseSweeperDaemon {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataBaseSweeperDaemon.class);

    private final SenderService senderService;
    private final Map<Long, UserData> userDataMap;
    private final Map<String, Long> controlChats;

    public DataBaseSweeperDaemon(DBContext db, SenderService senderService) {
        this.senderService = senderService;
        this.userDataMap = db.getMap(PersistentData.CHAT_STATES);
        this.controlChats = db.getMap(PersistentData.CONTROL_CHATS);
    }

    @Scheduled(initialDelay = 1, fixedRate = 5, timeUnit = TimeUnit.MINUTES)
    public void sweep() {
        if (!controlChats.containsKey(ControlChatType.SYSTEM.toString())) {
            LOGGER.warn("Can't sweep because system chat is not defined");
            return;
        }

        senderService.trySendingControlMessage(SystemMessage.SWEEP_START, ControlChatType.SYSTEM);

        long now = System.currentTimeMillis();

        List<UserData> outdated = new ArrayList<>();
        userDataMap.forEach((chatId, userData) -> {
            if (now - userData.getCreatedAt() < TimeUnit.MINUTES.toMillis(10)) {
                return;
            }
            outdated.add(userData);
        });

        if (!outdated.isEmpty()) {
            senderService.trySendingControlMessage(SystemMessage.SWEEP_RESULT.formatted(outdated.size()), ControlChatType.SYSTEM);
        }

        outdated.forEach(userData -> {
            senderService.sendMessage(() -> userDataMap.remove(userData.getChatId()), userData.getChatId(), BookingMessage.TIMEOUT);
        });
    }

}
