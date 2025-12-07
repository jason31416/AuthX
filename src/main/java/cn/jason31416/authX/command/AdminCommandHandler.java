package cn.jason31416.authX.command;

import cn.jason31416.authX.AuthXPlugin;
import cn.jason31416.authX.authbackend.AbstractAuthenticator;
import cn.jason31416.authX.message.Message;
import cn.jason31416.authX.util.Config;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.List;

public class AdminCommandHandler implements SimpleCommand {
    public static final AdminCommandHandler INSTANCE = new AdminCommandHandler();

    @Override
    public void execute(final @NotNull SimpleCommand.Invocation invocation) {
        String subCommand;
        if(invocation.arguments().length>0){
            subCommand = invocation.arguments()[0];
        }else{
            subCommand = "";
        }
        switch (subCommand){
            case "changepass" -> {
                if(invocation.arguments().length<3){
                    invocation.source().sendMessage(Message.getMessage("command.force-change-password.invalid-format").toComponent());
                    return;
                }
                String newPassword = invocation.arguments()[2], username = invocation.arguments()[1];
                if(AbstractAuthenticator.getInstance().fetchStatus(username) == AbstractAuthenticator.UserStatus.NOT_EXIST){
                    invocation.source().sendMessage(Message.getMessage("command.player-not-exists").add("player", username).toComponent());
                    return;
                }
                AbstractAuthenticator.getInstance().changePassword(username, newPassword);
                invocation.source().sendMessage(Message.getMessage("command.force-change-password.success").add("player", username).toComponent());
            }
            case "unregister" -> {
                if(invocation.arguments().length<2){
                    invocation.source().sendMessage(Message.getMessage("command.force-unregister.invalid-format").toComponent());
                    return;
                }
                String username = invocation.arguments()[1];
                if(AbstractAuthenticator.getInstance().fetchStatus(username) == AbstractAuthenticator.UserStatus.NOT_EXIST){
                    invocation.source().sendMessage(Message.getMessage("command.player-not-exists").add("player", username).toComponent());
                    return;
                }
                AbstractAuthenticator.getInstance().unregister(username);
                invocation.source().sendMessage(Message.getMessage("command.change-password.success").toComponent());
            }
            case "reload" -> {
                AuthXPlugin.instance.init();
                invocation.source().sendMessage(Message.getMessage("command.reload.success").toComponent());
            }
            case "" -> {
                invocation.source().sendMessage(new Message("&a&lRunning &b&lAuthX v &a&l ").toComponent());
            }
            default -> {
                invocation.source().sendMessage(Message.getMessage("command.default").toComponent());
            }
        }
    }
    @Override
    public List<String> suggest(final @Nonnull Invocation invocation) {
        if(invocation.arguments().length<=1)
            return List.of("changepass", "unregister", "reload");
        else if(invocation.arguments().length == 2){
            return switch (invocation.arguments()[0]){
                case "changepass" -> List.of(Message.getMessage("tab-complete.force-change-password.player").toString());
                case "unregister" -> List.of(Message.getMessage("command.force-change-password.new").toString());
                default -> List.of();
            };
        }else if(invocation.arguments().length == 3){
            return switch (invocation.arguments()[0]){
                case "changepass" -> List.of(Message.getMessage("command.force-change-password.new").toString());
                default -> List.of();
            };
        }
        return List.of();
    }
}