package cn.jason31416.authX.virtual;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.SimplePacketListenerAbstract;
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import com.github.retrooper.packetevents.event.simple.PacketPlaySendEvent;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.protocol.world.chunk.BaseChunk;
import com.github.retrooper.packetevents.protocol.world.chunk.Column;
import com.github.retrooper.packetevents.protocol.world.chunk.TileEntity;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerChunkData;
import com.velocitypowered.api.proxy.Player;

import javax.annotation.Nonnull;

public class VirtualSession extends SimplePacketListenerAbstract {
    private final Player player;

    private VirtualSession(@Nonnull Player player) {
        this.player = player;
    }

    public static @Nonnull VirtualSession spawn(@Nonnull Player player) {
        User user = PacketEvents.getAPI().getPlayerManager().getUser(player);
        WrapperPlayServerChunkData packet = new WrapperPlayServerChunkData(
                new Column(0, 0, false, new BaseChunk[0], new TileEntity[0])
        )
    }

    @Override
    public void onPacketPlayReceive(@Nonnull PacketPlayReceiveEvent event) {
        switch(event.getPacketType()) {
            case CHAT_MESSAGE -> {}
            case PLAYER_POSITION -> {}
            case PLAYER_POSITION_AND_ROTATION -> {}
            case PLAYER_ROTATION -> {}
        }
    }
}
