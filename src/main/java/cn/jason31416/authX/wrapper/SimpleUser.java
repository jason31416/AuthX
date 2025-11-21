package cn.jason31416.authX.wrapper;
import cn.jason31416.authX.message.Message;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.player.User;
import com.velocitypowered.api.proxy.Player;
import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.annotation.Nonnull;
import java.util.UUID;

@AllArgsConstructor
public class SimpleUser {
    @Getter
    private final Player player;

    public @Nonnull UUID getUUID() {
        return player.getUniqueId();
    }

    public @Nonnull String getName() {
        return player.getUsername();
    }

    public void sendMessage(@Nonnull Message message) {
        player.sendMessage(message.toComponent());
    }

    public static SimpleUser of(Player player) {
        return new SimpleUser(player);
    }
    public @Nonnull User getPacketeventsUser() {
        return PacketEvents.getAPI().getPlayerManager().getUser(getPlayer());
    }
}
