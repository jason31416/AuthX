package cn.jason31416.authX.handler;

import cn.jason31416.authX.AuthXPlugin;
import cn.jason31416.authX.authbackend.AbstractAuthenticator;
import cn.jason31416.authX.hook.FloodgateHandler;
import cn.jason31416.authX.message.Message;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import com.velocitypowered.api.event.player.GameProfileRequestEvent;
import com.velocitypowered.api.util.GameProfile;
import com.velocitypowered.api.util.UuidUtils;
import net.elytrium.limboapi.api.event.LoginLimboRegisterEvent;

import javax.annotation.Nonnull;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class EventListener {
    public static boolean checkUserYggdrasilStatusFromUUID(String username, UUID uuid){
        return !UUID.nameUUIDFromBytes(("OfflinePlayer:" + username).getBytes(StandardCharsets.UTF_8)).equals(uuid);
    }

    @Subscribe
    public void onPreLogin(@Nonnull PreLoginEvent event) {
        String username = event.getUsername();

        AbstractAuthenticator.UserStatus accountStatus;

        try{
            accountStatus = AbstractAuthenticator.getInstance().fetchStatus(username);
        }catch (Exception e){
            event.setResult(PreLoginEvent.PreLoginComponentResult.denied(Message.getMessage("auth.failed-to-login").toComponent()));
            throw e;
        }

        LoginSession session = new LoginSession(username, event.getUniqueId());
        LoginSession.getSessionMap().put(username, session);

        if(accountStatus == AbstractAuthenticator.UserStatus.IMPORTED){
            event.setResult(PreLoginEvent.PreLoginComponentResult.forceOnlineMode());
            session.setVerifyPassword(true);
            session.setEnforcePrimaryMethod(true);
        }else if(checkUserYggdrasilStatusFromUUID(username, event.getUniqueId())){
            event.setResult(PreLoginEvent.PreLoginComponentResult.forceOnlineMode());
            session.setVerifyPassword(false);
        }else {
            event.setResult(PreLoginEvent.PreLoginComponentResult.forceOfflineMode());
            session.setVerifyPassword(true);
        }
    }

    @Subscribe
    public void onPlayerLimboConnect(LoginLimboRegisterEvent event){
        LoginSession session = LoginSession.getSessionMap().get(event.getPlayer().getUsername());
        if(!session.isVerifyPassword()) return;
        if(AuthXPlugin.getInstance().getProxy().getPluginManager().isLoaded("floodgate") && FloodgateHandler.isFloodgatePlayer(event.getPlayer().getUniqueId())){
            return;
        }
        event.addOnJoinCallback(() -> {
            LimboHandler.spawnPlayer(event.getPlayer());
        });
    }

    @Subscribe
    public void onPlayerDisconnect(DisconnectEvent event){
        LoginSession.getSessionMap().remove(event.getPlayer().getUsername());
    }

    @Subscribe(order = PostOrder.FIRST)
    public void onGameProfileRequest(GameProfileRequestEvent event) {
        GameProfile profile = event.getOriginalProfile().withId(UuidUtils.generateOfflinePlayerUuid(event.getUsername()));
        event.setGameProfile(profile);
    }
}
