package ru.nordic.ice.bot.mapper;

import ru.nordic.ice.bot.dao.UserData;

import java.time.Instant;

public class UserDataMapper {

    public static String userDataToMessage(UserData userData) {
        String type = switch (userData.getType()) {
            case USER_CHILD -> "Для ребенка";
            case USER -> "Для Себя";
            case CALL_REQUEST -> "Заказ звонка";
            case ERROR -> "Ошибочная";
        };
        return
                """
                ❗❗НОВАЯ ЗАЯВКА❗❗
                ФИО - %s
                Заявка типа - %s
                Указанный возраст - %s
                Телефон - %s
                Время начала заполнения анкеты (по местоположению сервера!) - %s
                """.formatted(
                        userData.getName(),
                        type,
                        userData.getAge(),
                        userData.getNumber(),
                        Instant.ofEpochMilli(userData.getCreatedAt()).toString());
    }

}
