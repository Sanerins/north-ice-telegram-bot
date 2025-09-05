package ru.nordic.ice.bot.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.abilitybots.api.db.DBContext;
import org.telegram.abilitybots.api.sender.MessageSender;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import ru.nordic.ice.bot.constant.ControlChatType;
import ru.nordic.ice.bot.constant.PersistentData;


@Component
public class SenderService {
    Logger LOGGER = LoggerFactory.getLogger(SenderService.class);

    private final MessageSender messageSender;
    private final DBContext db;

    public SenderService(MessageSender messageSender, DBContext db) {
        this.messageSender = messageSender;
        this.db = db;
    }

    public void sendMessage(long chatId, String text) {
        sendMessage(() -> {}, chatId, text);
    }

    public void sendMessage(long chatId, String text, ReplyKeyboard replyKeyboard) {
        sendMessage(() -> {}, chatId, text, replyKeyboard);
    }

    public void sendMessage(Runnable predicate, long chatId, String text) {
        SendMessage message = createMessage(chatId, text);
        sendMessage(predicate, message);
    }

    public void sendMessage(Runnable predicate, long chatId, String text, ReplyKeyboard replyKeyboard) {
        SendMessage message = createMessage(chatId, text);
        message.setReplyMarkup(replyKeyboard);
        sendMessage(predicate, message);
    }

    public void sendMessage(Runnable predicate, SendMessage message) {
        try {
            predicate.run();
            messageSender.execute(message);
        } catch (Exception e) {
            LOGGER.error("Exception occurred while sending message", e);
            trySendingControlMessage(String.format("Sending message to user failed with exception: %s", e.getMessage()), ControlChatType.SYSTEM);
        }
    }

    public SendMessage createMessage(long chatId, String message) {
        SendMessage sendMessage = new SendMessage();

        sendMessage.setChatId(chatId);
        sendMessage.setText(message);
        sendMessage.setParseMode(ParseMode.HTML);

        return sendMessage;
    }

    public void trySendingControlMessage(String message, ControlChatType type) {
        trySendingControlMessage(() -> {}, message, type);
    }

    public void trySendingControlMessage(Runnable predicate, String message, ControlChatType type) {
        try {
            Long chatId = (Long) db.getMap(PersistentData.CONTROL_CHATS).get(type);

            if (chatId == null) {
                switch (type) {
                    case REPORT -> trySendingControlMessage("Couldn't find chat id for sending user report", ControlChatType.SYSTEM);
                    case SYSTEM -> LOGGER.warn("Couldn't find chat id for system chat");
                }
                return;
            }

            predicate.run();

            SendMessage sendMessage = new SendMessage();

            sendMessage.setChatId(chatId);
            sendMessage.setText(message);
            sendMessage.setParseMode(ParseMode.HTML);

            messageSender.execute(sendMessage);
        } catch (Exception e) {
            switch (type) {
                case REPORT -> trySendingControlMessage(String.format("Sending report message %s failed with exception: %s", message, e.getMessage()), ControlChatType.SYSTEM);
                case SYSTEM -> LOGGER.error("Error sending system message - {}", message, e);
            }
        }
    }

}
