package cn.jason31416.authX.handler;

import cn.jason31416.authX.virtual.VirtualSession;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;

import javax.annotation.Nonnull;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class EventListener {
    @Subscribe
    public void onPreLogin(@Nonnull PreLoginEvent event) {
        if(event.getUniqueId() == null || UUID.nameUUIDFromBytes(("OfflinePlayer:" + event.getUsername()).getBytes(StandardCharsets.UTF_8)).equals(event.getUniqueId())) {
            event.setResult(PreLoginEvent.PreLoginComponentResult.forceOfflineMode());
        } else {
            event.setResult(PreLoginEvent.PreLoginComponentResult.forceOnlineMode());
        }
    }
    @Subscribe
    public void onServerPreConnect(@Nonnull ServerPreConnectEvent event) {
        if(!event.getPlayer().isOnlineMode()) {
            event.setResult(ServerPreConnectEvent.ServerResult.denied());
            VirtualSession.spawn(event.getPlayer());
        }
    }
}
