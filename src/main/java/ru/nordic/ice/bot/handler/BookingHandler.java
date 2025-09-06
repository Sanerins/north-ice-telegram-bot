package ru.nordic.ice.bot.handler;

import org.springframework.stereotype.Component;
import org.telegram.abilitybots.api.db.DBContext;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import ru.nordic.ice.bot.constant.ControlChatType;
import ru.nordic.ice.bot.constant.message.BookingMessage;
import ru.nordic.ice.bot.constant.PersistentData;
import ru.nordic.ice.bot.constant.message.ReportMessage;
import ru.nordic.ice.bot.constant.message.SystemMessage;
import ru.nordic.ice.bot.dao.UserData;
import ru.nordic.ice.bot.keyboard.KeyboardFactory;
import ru.nordic.ice.bot.mapper.UserDataMapper;
import ru.nordic.ice.bot.service.SenderService;

import java.util.Map;

@Component
public class BookingHandler {
    private final SenderService senderService;

    private final Map<Long, UserData> userData;
    private final Map<String, Long> controlChats;

    public BookingHandler(DBContext db, SenderService senderService) {
        this.senderService = senderService;

        userData = db.getMap(PersistentData.CHAT_STATES);
        controlChats = db.getMap(PersistentData.CONTROL_CHATS);
    }

    public void replyToStart(long chatId) {
        senderService.sendMessage(chatId, BookingMessage.START, new ReplyKeyboardRemove(true));

        senderService.sendMessage(
                () -> userData.put(chatId, new UserData(chatId, UserData.BookingState.QUESTION_WHO_FOR)),
                chatId, BookingMessage.QUESTION_FOR_WHOM, KeyboardFactory.getSurveyTakerKeyboard());
    }

    public void finishTheConversation(long chatId) {
        senderService.trySendingControlMessage(() -> userData.remove(chatId),
                UserDataMapper.userDataToMessage(userData.get(chatId)), ControlChatType.REPORT);
    }

    public void replyToReportHook(long chatId) {
        controlChats.put(ControlChatType.REPORT.toString(), chatId);
        senderService.trySendingControlMessage(ReportMessage.INIT, ControlChatType.REPORT);
    }

    public void replyToSystemHook(long chatId) {
        controlChats.put(ControlChatType.SYSTEM.toString(), chatId);
        senderService.trySendingControlMessage(SystemMessage.INIT, ControlChatType.SYSTEM);
    }

    public void replyToButtons(long chatId, Message message) {
        if (message.getText() == null || message.getText().isEmpty()) {
            sendErrorMessage(chatId);
            return;
        }
        if (message.getText().equalsIgnoreCase("/start")) {
            replyToStart(chatId);
            return;
        }

        UserData data = userData.get(chatId);

        switch (data.getState()) {
            case QUESTION_WHO_FOR -> {
                if (UserData.BookingType.fromString(message.getText()) == UserData.BookingType.ERROR) {
                    sendErrorMessage(chatId);
                    return;
                }
                senderService.sendMessage(
                        () -> changeUserData(data
                                .updateState(UserData.BookingState.PRIVACY_AGREEMENT)
                                .updateType(UserData.BookingType.fromString(message.getText()))
                        ),
                        chatId, BookingMessage.PRIVACY_NOTICE, KeyboardFactory.getAgreementKeyboard());
            }
            case PRIVACY_AGREEMENT -> {
                if (message.getText().equals("Я согласен на обработку данных")) {
                    senderService.sendMessage(
                            () -> userData.remove(chatId),
                            chatId, BookingMessage.PRIVACY_NOTICE_REJECT, new ReplyKeyboardRemove(true));
                    return;
                }
                switch (data.getType()) {
                    case USER_CHILD -> {
                        senderService.sendMessage(
                                () -> changeUserData(data
                                        .updateState(UserData.BookingState.QUESTION_CHILD_AGE)
                                ),
                                chatId, BookingMessage.QUESTION_CHILD_AGE, KeyboardFactory.getChildAgeKeyboard());
                    }
                    case USER -> {
                        senderService.sendMessage(
                                () -> changeUserData(data
                                        .updateState(UserData.BookingState.QUESTION_SELF_AGE)
                                ),
                                chatId, BookingMessage.QUESTION_SELF_AGE, KeyboardFactory.YesNoKeyboard());
                    }
                    case CALL_REQUEST -> {
                        senderService.sendMessage(
                                () -> changeUserData(data
                                        .updateState(UserData.BookingState.QUESTION_SELF_NUMBER)
                                        .updateAge("Не указан - заказ звонка напрямую")
                                ),
                                chatId, BookingMessage.QUESTION_SELF_NUMBER, new ReplyKeyboardRemove(true));
                    }
                    default -> sendErrorMessage(chatId);
                }
            }
            case QUESTION_CHILD_AGE -> {
                senderService.sendMessage(
                        () -> changeUserData(data
                                .updateState(UserData.BookingState.QUESTION_CHILD_NUMBER)
                                .updateAge(message.getText())
                        ),
                        chatId, BookingMessage.QUESTION_CHILD_NUMBER, new ReplyKeyboardRemove(true));
            }
            case QUESTION_CHILD_NUMBER -> {
                senderService.sendMessage(
                        () -> changeUserData(data
                                .updateState(UserData.BookingState.QUESTION_CHILD_NAME)
                                .updateNumber(message.getText())
                        ),
                        chatId, BookingMessage.QUESTION_CHILD_NAME, new ReplyKeyboardRemove(true));
            }
            case QUESTION_SELF_AGE -> {
                String age = message.getText().equals("Да") ? "Совершеннолетний" : "Несовершеннолетний";
                senderService.sendMessage(
                        () -> changeUserData(data
                                .updateState(UserData.BookingState.QUESTION_SELF_NUMBER)
                                .updateAge(age)
                        ),
                        chatId, BookingMessage.QUESTION_SELF_NUMBER, new ReplyKeyboardRemove(true));
            }
            case QUESTION_SELF_NUMBER -> {
                senderService.sendMessage(
                        () -> changeUserData(data
                                .updateState(UserData.BookingState.QUESTION_SELF_NAME)
                                .updateNumber(message.getText())
                        ),
                        chatId, BookingMessage.QUESTION_SELF_NAME, new ReplyKeyboardRemove(true));
            }
            case QUESTION_CHILD_NAME, QUESTION_SELF_NAME -> {
                senderService.sendMessage(
                        () -> changeUserData(data
                                .updateName(message.getText())
                        ),
                        chatId, BookingMessage.CALLBACK_CONFIRMATION, new ReplyKeyboardRemove(true));
                finishTheConversation(chatId);
            }
        }
    }

    public boolean userIsActive(Long chatId) {
        return userData.containsKey(chatId);
    }

    public void sendErrorMessage(long chatId) {
        senderService.sendMessage(chatId, BookingMessage.ERROR);
    }

    public void changeUserData(UserData newData) {
        if (!userData.containsKey(newData.getChatId())) {
            senderService.trySendingControlMessage(
                    String.format("ERROR - Tried to update data of an non-esistent user chat %s, data %s", newData.getChatId(), newData),
                    ControlChatType.SYSTEM);
            return;
        }
        userData.put(newData.getChatId(), newData);
    }
}