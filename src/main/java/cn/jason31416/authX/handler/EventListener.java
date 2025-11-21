package cn.jason31416.authX.handler;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PreLoginEvent;

import javax.annotation.Nonnull;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class EventListener {
    @Subscribe
    public void onPreLogin(@Nonnull PreLoginEvent event) {
        if(event.getUniqueId() == null || UUID.nameUUIDFromBytes(("OfflinePlayer:" + event.getUsername()).getBytes(StandardCharsets.UTF_8)).equals(event.getUniqueId())) {
            event.setResult(PreLoginEvent.PreLoginComponentResult.forceOfflineMode());
            // TODO:进virtual
            return;
        } else {
            event.setResult(PreLoginEvent.PreLoginComponentResult.forceOnlineMode());
        }
    }
}
