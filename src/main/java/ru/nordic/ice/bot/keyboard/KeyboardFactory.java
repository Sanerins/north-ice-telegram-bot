package ru.nordic.ice.bot.keyboard;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.List;

public class KeyboardFactory {
    public static ReplyKeyboard getSurveyTakerKeyboard() {
        return new ReplyKeyboardMarkup(
                List.of(
                        new KeyboardRow(List.of(new KeyboardButton("Ребенка"))),
                        new KeyboardRow(List.of(new KeyboardButton("Себя"))),
                        new KeyboardRow(List.of(new KeyboardButton("Заказать звонок")))
                )
        );
    }

    public static ReplyKeyboard getChildAgeKeyboard() {
        return new ReplyKeyboardMarkup(
                List.of(
                        new KeyboardRow(List.of(new KeyboardButton("3-6"))),
                        new KeyboardRow(List.of(new KeyboardButton("7-10"))),
                        new KeyboardRow(List.of(new KeyboardButton("11-14"))),
                        new KeyboardRow(List.of(new KeyboardButton("15-18")))
                )
        );
    }

    public static ReplyKeyboard YesNoKeyboard() {
        return new ReplyKeyboardMarkup(
                List.of(
                        new KeyboardRow(List.of(new KeyboardButton("Да"))),
                        new KeyboardRow(List.of(new KeyboardButton("Нет")))
                )
        );
    }
}
