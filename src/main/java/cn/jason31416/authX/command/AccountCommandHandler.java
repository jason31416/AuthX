package cn.jason31416.authX.command;

import cn.jason31416.authX.authbackend.AbstractAuthenticator;
import cn.jason31416.authX.message.Message;
import cn.jason31416.authX.util.Config;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import org.aopalliance.intercept.Invocation;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.List;

public class AccountCommandHandler implements SimpleCommand {
    public static final AccountCommandHandler INSTANCE = new AccountCommandHandler();

    @Override
    public void execute(final @NotNull SimpleCommand.Invocation invocation) {
        CommandSource source = invocation.source();
        if(!(source instanceof Player pl)) {
            invocation.source().sendMessage(Message.getMessage("command.player-command").toComponent());
            return;
        }
        String subCommand;
        if(invocation.arguments().length>0){
            subCommand = invocation.arguments()[0];
        }else{
            subCommand = "";
        }
        String username = pl.getUsername();
        switch (subCommand){
            case "changepass" -> {
                String newPassword;
                if(Config.getBoolean("command.change-password.need-old-password")){
                    if(invocation.arguments().length<3){
                        invocation.source().sendMessage(Message.getMessage("command.change-password.invalid-format-need-old-password").toComponent());
                        return;
                    }
                    if(!AbstractAuthenticator.getInstance().authenticate(username, invocation.arguments()[1]).success){
                        invocation.source().sendMessage(Message.getMessage("command.change-password.invalid-password").toComponent());
                        return;
                    }
                    newPassword = invocation.arguments()[2];
                }else{
                    if(invocation.arguments().length<2){
                        invocation.source().sendMessage(Message.getMessage("command.change-password.invalid-format-no-old-password").toComponent());
                        return;
                    }
                    newPassword = invocation.arguments()[1];
                }
                if(!newPassword.matches(Config.getString("regex.password-regex"))){
                    invocation.source().sendMessage(Message.getMessage("command.change-password.invalid-password-format").toComponent());
                    return;
                }
                AbstractAuthenticator.getInstance().changePassword(username, newPassword);
                invocation.source().sendMessage(Message.getMessage("command.change-password.success").toComponent());
            }
            default -> {
                invocation.source().sendMessage(Message.getMessage("command.default").toComponent());
            }
        }
    }
    @Override
    public List<String> suggest(final @Nonnull Invocation invocation) {
        if(invocation.arguments().length<=1)
            return List.of("changepass");
        else if(invocation.arguments().length == 2){
            return switch (invocation.arguments()[0]){
                case "changepass" -> List.of(Config.getBoolean("command.change-password.need-old-password")?Message.getMessage("tab-complete.change-password.old").toString():Message.getMessage("tab-complete.change-password.new").toString());
                default -> List.of();
            };
        }else if(invocation.arguments().length == 3){
            return switch (invocation.arguments()[0]){
                case "changepass" -> Config.getBoolean("command.change-password.need-old-password")?List.of(Message.getMessage("tab-complete.change-password.new").toString()):List.of();
                default -> List.of();
            };
        }
        return List.of();
    }
}