package ru.nordic.ice.bot.constant.message;

public class SystemMessage {

    public static final String INIT = """
            🟢 Успешно подключена отсылка системных сообщений мониторинга работы в этот чат!
            """;

    public static final String SWEEP_START = """
            🟢 Бот здоров и продолжает работу ❤️
            """;

    public static final String SWEEP_RESULT = """
            🟡 Бот успешно удалил %s анкет, которые были не полностью заполнены
            """;
}
