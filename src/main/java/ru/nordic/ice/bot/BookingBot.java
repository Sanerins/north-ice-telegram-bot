package ru.nordic.ice.bot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.bot.BaseAbilityBot;
import org.telegram.abilitybots.api.db.MapDBContext;
import org.telegram.abilitybots.api.objects.Ability;
import org.telegram.abilitybots.api.objects.Flag;
import org.telegram.abilitybots.api.objects.Locality;
import org.telegram.abilitybots.api.objects.Privacy;
import org.telegram.abilitybots.api.objects.Reply;
import org.telegram.abilitybots.api.util.AbilityUtils;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.nordic.ice.bot.constant.CommandDescription;
import ru.nordic.ice.bot.handler.BookingHandler;

import java.util.function.BiConsumer;

@Component
@SuppressWarnings({"UnusedReturnValue", "WeakerAccess", "unused", "ConstantConditions"})
public class BookingBot extends AbilityBot {
    private final long creatorId;

    private BookingHandler bookingHandler;

    protected BookingBot(@Value("${spring.bot.token}") String token,
                         @Value("${spring.bot.username}") String username,
                         @Value("${spring.bot.creatorId}") long creatorId,
                         @Value("${spring.bot.db.data}") String dataPath) {
        super(token, username, MapDBContext.onlineInstance(dataPath));
        this.creatorId = creatorId;
    }

    @Autowired
    public void setBookingHandler(BookingHandler bookingHandler) {
        this.bookingHandler = bookingHandler;
    }

    @Override
    public long creatorId() {
        return creatorId;
    }

    public Ability startBot() {
        return Ability
                .builder()
                .name("start")
                .info(CommandDescription.START)
                .locality(Locality.USER)
                .privacy(Privacy.PUBLIC)
                .action(ctx -> bookingHandler.replyToStart(ctx.chatId()))
                .build();
    }

    public Ability initReportStream() {
        return Ability
                .builder()
                .name("report_hook")
                .info(CommandDescription.REPORT_HOOK)
                .locality(Locality.ALL)
                .privacy(Privacy.CREATOR)
                .action(ctx -> bookingHandler.replyToReportHook(ctx.chatId()))
                .build();
    }

    public Ability initSystemDataStream() {
        return Ability
                .builder()
                .name("system_hook")
                .info(CommandDescription.SYSTEM_HOOK)
                .locality(Locality.ALL)
                .privacy(Privacy.ADMIN)
                .action(ctx -> bookingHandler.replyToSystemHook(ctx.chatId()))
                .build();
    }

    public Reply replyToButtons() {
        BiConsumer<BaseAbilityBot, Update> action = (abilityBot, upd) -> bookingHandler.replyToButtons(AbilityUtils.getChatId(upd), upd.getMessage());
        return Reply.of(action, Flag.TEXT, upd -> bookingHandler.userIsActive(AbilityUtils.getChatId(upd)));
    }
}
