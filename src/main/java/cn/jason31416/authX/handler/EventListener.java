package cn.jason31416.authX.handler;

import cn.jason31416.authX.AuthXPlugin;
import cn.jason31416.authX.authbackend.AbstractAuthenticator;
import cn.jason31416.authX.hook.FloodgateHandler;
import cn.jason31416.authX.injection.ReflectionException;
import cn.jason31416.authX.message.Message;
import cn.jason31416.authX.util.Config;
import cn.jason31416.authX.util.Logger;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import com.velocitypowered.api.event.player.GameProfileRequestEvent;
import com.velocitypowered.api.util.GameProfile;
import com.velocitypowered.api.util.UuidUtils;
import com.velocitypowered.proxy.connection.MinecraftConnection;
import com.velocitypowered.proxy.connection.client.InitialInboundConnection;
import com.velocitypowered.proxy.connection.client.LoginInboundConnection;
import lombok.SneakyThrows;
import net.elytrium.limboapi.api.event.LoginLimboRegisterEvent;

import javax.annotation.Nonnull;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class EventListener {
    private static final MethodHandle DELEGATE_FIELD;
    private static final Set<String> pendingLogins = new HashSet<>();
    public static final Map<UUID, Long> loginPremiumFailedCache = new ConcurrentHashMap<>();

    static {
        try {
            DELEGATE_FIELD = MethodHandles.privateLookupIn(LoginInboundConnection.class, MethodHandles.lookup())
                    .findGetter(LoginInboundConnection.class, "delegate", InitialInboundConnection.class);
        } catch (Exception e) {
            throw new ReflectionException(e.getMessage(), e);
        }
    }

    private static boolean PCLUUIDFilter(UUID uuid){
        String uuidStr = uuid.toString().replace("-", "");
        return uuidStr.startsWith("0000000000") && uuidStr.charAt(12)=='3' && uuidStr.charAt(16)=='9';
    }

    public static boolean checkUserYggdrasilStatusFromUUID(String username, UUID uuid){
        if(PCLUUIDFilter(uuid)) return false;
        return !UUID.nameUUIDFromBytes(("OfflinePlayer:" + username).getBytes(StandardCharsets.UTF_8)).equals(uuid);
    }

    @SneakyThrows
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

        if(AuthXPlugin.getInstance().getProxy().getPluginManager().isLoaded("floodgate") && FloodgateHandler.isFloodgatePlayer(event.getUniqueId())){
            return;
        }

        String cs;

        if(accountStatus == AbstractAuthenticator.UserStatus.IMPORTED){
            event.setResult(PreLoginEvent.PreLoginComponentResult.forceOnlineMode());
            session.setVerifyPassword(true);
            session.setEnforcePrimaryMethod(true);
            cs = "imported";
        }else if(checkUserYggdrasilStatusFromUUID(username, event.getUniqueId()) && !loginPremiumFailedCache.containsKey(event.getUniqueId())){
            event.setResult(PreLoginEvent.PreLoginComponentResult.forceOnlineMode());
            session.setVerifyPassword(false);

            LoginInboundConnection inboundConnection = (LoginInboundConnection) event.getConnection();
            InitialInboundConnection initialInbound = (InitialInboundConnection) DELEGATE_FIELD.invokeExact(inboundConnection);
            MinecraftConnection connection = initialInbound.getConnection();
            if (!connection.isClosed()) {
                pendingLogins.add(username);
                connection.getChannel().closeFuture().addListener(future -> {
                    if(pendingLogins.remove(username)){
                        loginPremiumFailedCache.put(event.getUniqueId(), System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1));
                    }
                });
            }
            cs = "yggdrasil";
        }else {
            event.setResult(PreLoginEvent.PreLoginComponentResult.forceOfflineMode());
            session.setVerifyPassword(true);
            cs = "offline";
        }
        if(Config.getBoolean("log.pre-login"))
            Logger.info("Player "+event.getUsername()+" ("+event.getUniqueId()+") Joined the server! Detected as " + cs + " authentication.");
    }

    @Subscribe
    public void onPlayerLimboConnect(LoginLimboRegisterEvent event){
        LoginSession session = LoginSession.getSessionMap().get(event.getPlayer().getUsername());
        pendingLogins.remove(event.getPlayer().getUsername());
        if(!session.isVerifyPassword()) return;
        if(AuthXPlugin.getInstance().getProxy().getPluginManager().isLoaded("floodgate") && FloodgateHandler.isFloodgatePlayer(event.getPlayer().getUniqueId())){
            return;
        }
        event.addOnJoinCallback(() -> {
            if(Config.getBoolean("log.join-limbo"))
                Logger.info("Player "+event.getPlayer().getUsername()+" (Orig. UUID: "+event.getPlayer().getUniqueId()+") is authenticating via password.");
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
