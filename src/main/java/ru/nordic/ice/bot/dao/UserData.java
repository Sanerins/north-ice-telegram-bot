package ru.nordic.ice.bot.dao;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;
import java.sql.Timestamp;
import java.time.Instant;

public class UserData implements Serializable {
    private final Long chatId;
    private final long createdAt;
    private BookingState state;
    private BookingType type;
    private String age;
    private String name;
    private String number;

    public UserData(Long chatId, BookingState state) {
        this.chatId = chatId;
        this.createdAt = System.currentTimeMillis();
        this.state = state;
    }

    public Long getChatId() {
        return chatId;
    }

    public BookingState getState() {
        return state;
    }

    public UserData updateState(BookingState state) {
        this.state = state;
        return this;
    }

    public BookingType getType() {
        return type;
    }

    public UserData updateType(BookingType type) {
        this.type = type;
        return this;
    }

    public String getAge() {
        return age;
    }

    public UserData updateAge(String age) {
        this.age = age;
        return this;
    }

    public String getName() {
        return name;
    }

    public UserData updateName(String name) {
        this.name = name;
        return this;
    }

    public String getNumber() {
        return number;
    }

    public UserData updateNumber(String number) {
        this.number = number;
        return this;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    @Override
    public String toString() {
        return "UserData{" +
                "chatId=" + chatId +
                ", state=" + state +
                ", type=" + type +
                ", age='" + age + '\'' +
                ", name='" + name + '\'' +
                ", number='" + number + '\'' +
                ", createdAt='" + Instant.ofEpochMilli(createdAt).toString() + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        UserData userData = (UserData) o;

        return new EqualsBuilder()
                .append(chatId, userData.chatId)
                .append(state, userData.state)
                .append(type, userData.type)
                .append(age, userData.age)
                .append(name, userData.name)
                .append(number, userData.number)
                .append(createdAt, userData.createdAt)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(chatId)
                .append(state)
                .append(type)
                .append(age)
                .append(name)
                .append(number)
                .append(createdAt)
                .toHashCode();
    }

    public enum BookingState {
        QUESTION_WHO_FOR, PRIVACY_AGREEMENT,
        QUESTION_CHILD_AGE, QUESTION_CHILD_NUMBER, QUESTION_CHILD_NAME,
        QUESTION_SELF_AGE, QUESTION_SELF_NUMBER, QUESTION_SELF_NAME,
    }

    public enum BookingType {
        USER_CHILD, USER, CALL_REQUEST, ERROR;

        public static BookingType fromString(String type) {
            switch (type) {
                case "Ребенка" -> {
                    return USER_CHILD;
                }
                case "Себя" -> {
                    return USER;
                }
                case "Заказать звонок" -> {
                    return CALL_REQUEST;
                }
                default -> {
                    return ERROR;
                }
            }
        }
    }
}
