package at.naurandir.discord.clem.bot.service.command;

import at.naurandir.discord.clem.bot.model.channel.InterestingChannel;
import at.naurandir.discord.clem.bot.model.enums.PushType;
import at.naurandir.discord.clem.bot.service.InterestingChannelService;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import discord4j.rest.util.Permission;
import discord4j.rest.util.PermissionSet;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 *
 * @author Naurandir
 */
@Slf4j
@Component
public class InterestingChannelCommand implements Command {
    
    @Autowired
    private InterestingChannelService interestingChannelService;

    @Override
    public String getCommandWord() {
        return "channel";
    }

    @Override
    public String getDescription() {
        return "Register or Unregister a Channel that Clem should use for Push Events.\n"
                + "Usage: *<bot-prefix> channel {register|unregister} <type>*.\n"
                + "Existing Types: " + Arrays.stream(PushType.values())
                        .map(type -> type.getValue())
                        .collect(Collectors.toList());
    }

    @Override
    public Mono<Void> handle(MessageCreateEvent event) {
        
        Member member = event.getMember().get().asFullMember().block();
        if (!isAllowedForCommand(member)) {
            return handleWrongPermissions(event);
        }
        
        CommandData data = getCommandData(event);
        if (data.getCommandType() == CommandType.UNKNOWN || data.getPushType() == null) {
            return handleWrongUsage(event);
        }
        
        if (data.getCommandType() == CommandType.REGISTER) {
            return handleRegister(event, data.getPushType(), member);
        } else {
            return handleUnregister(event, data.getPushType());
        }
    }
    
    private boolean isAllowedForCommand(Member member) {
        PermissionSet permissions = member.getBasePermissions().block();
        boolean isAdminOrModerator = 
                permissions.contains(Permission.ADMINISTRATOR) || permissions.contains(Permission.MANAGE_CHANNELS);
        log.debug("isAllowedForCommand: user is allowed [{}], full member permissions are {} ", isAdminOrModerator, permissions);
        
        return isAdminOrModerator;
    }

    private CommandData getCommandData(MessageCreateEvent event) {
        String[] splitted = event.getMessage().getContent().split(" "); // 'prefix command <register|unregister> <pushType>'
        
        CommandType commandType;
        try {
            commandType = CommandType.getByValue(splitted[2]);
        } catch (IllegalArgumentException | NullPointerException ex) {
            commandType = CommandType.UNKNOWN;
        }
        
        PushType pushType;
        try {
            pushType = PushType.getByValue(splitted[3]);
        } catch (IllegalArgumentException | NullPointerException ex) {
            pushType = null;
        }
        
        
        CommandData data = new CommandData();
        data.setCommandType(commandType);
        data.setPushType(pushType);
        
        return data;
    }
    
    private Mono<Void> handleWrongPermissions(MessageCreateEvent event) {
        EmbedCreateSpec embed = EmbedCreateSpec.builder()
                .color(Color.RED)
                .title("Register / Unregister")
                .description("Registered failed, as the user does not have Admin or Moderator Permissions.")
                .thumbnail("https://i.imgur.com/Jkq5UIF.png")
                .timestamp(Instant.now())
                .build();
        
        return event.getMessage().getChannel()
                .flatMap(ch -> ch.createMessage(embed))
                .then();
    }
    
    private Mono<Void> handleWrongUsage(MessageCreateEvent event) {
        EmbedCreateSpec embed = EmbedCreateSpec.builder()
                .color(Color.RED)
                .title("Register / Unregister")
                .description("Registered failed, please use the command as described: `prefix command <register|unregister> <pushType>`")
                .thumbnail("https://i.imgur.com/Jkq5UIF.png")
                .timestamp(Instant.now())
                .build();
        
        return event.getMessage().getChannel()
                .flatMap(ch -> ch.createMessage(embed))
                .then();
    }

    private Mono<Void> handleRegister(MessageCreateEvent event, PushType pushType, Member member) {
        InterestingChannel channel = interestingChannelService.createOrUpdateChannel(
                event.getMessage().getChannelId().asLong(), 
                pushType, 
                member.getDisplayName());
        log.info("handleRegister: created or updated channel [{}]", channel);
        
        EmbedCreateSpec embed = EmbedCreateSpec.builder()
                .color(Color.GREEN)
                .title("Register")
                .description("Registered {type} successfully added.".replace("{type}", pushType.toString()))
                .thumbnail("https://i.imgur.com/Jkq5UIF.png")
                .timestamp(Instant.now())
                .build();
        
        return event.getMessage().getChannel()
                .flatMap(ch -> ch.createMessage(embed))
                .then();
    }

    private Mono<Void> handleUnregister(MessageCreateEvent event, PushType pushType) {
        Optional<InterestingChannel> channelOpt = interestingChannelService.getChannel(
                event.getMessage().getChannelId().asLong(), 
                pushType);
        
        if (channelOpt.isEmpty()) {
            log.info("handleUnregister: no channel found for [{}] with push type [{}], ignoring request.");
            return Flux.empty().then();
        }
        
        InterestingChannel channel = channelOpt.get();
        interestingChannelService.deleteChannel(channel);
        log.info("handleUnregister: channel [{}] deleted", channel);
        
        EmbedCreateSpec embed = EmbedCreateSpec.builder()
                .color(Color.MEDIUM_SEA_GREEN)
                .title("Unregister")
                .description("Unregisterd {type} successfully for this channel.".replace("{type}", pushType.toString()))
                .thumbnail("https://i.imgur.com/Jkq5UIF.png")
                .timestamp(Instant.now())
                .build();
        
        return event.getMessage().getChannel()
                .flatMap(ch -> ch.createMessage(embed))
                .then();
    }
    
    @Getter
    @Setter
    private class CommandData {
        private CommandType commandType;
        private PushType pushType;
    }
    
    private enum CommandType {
        REGISTER("register"), UNREGISTER("unregister"), UNKNOWN("unknown");
        
        private final String value;
        
        private CommandType(String value) {
            this.value = value;
        }
        
        static CommandType getByValue(String value) {
            for (CommandType type : values()) {
                if (type.value.equals(value)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("no valid value found for " + value);
        }
    }
}
