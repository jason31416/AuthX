package cn.jason31416.authX.virtual;

import cn.jason31416.authX.AuthXPlugin;
import cn.jason31416.authX.util.Logger;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.SimplePacketListenerAbstract;
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import com.github.retrooper.packetevents.protocol.nbt.NBTCompound;
import com.github.retrooper.packetevents.protocol.player.GameMode;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.protocol.world.Difficulty;
import com.github.retrooper.packetevents.protocol.world.Dimension;
import com.github.retrooper.packetevents.protocol.world.WorldBlockPosition;
import com.github.retrooper.packetevents.protocol.world.chunk.BaseChunk;
import com.github.retrooper.packetevents.protocol.world.chunk.Column;
import com.github.retrooper.packetevents.protocol.world.chunk.LightData;
import com.github.retrooper.packetevents.protocol.world.chunk.TileEntity;
import com.github.retrooper.packetevents.protocol.world.dimension.DimensionType;
import com.github.retrooper.packetevents.protocol.world.dimension.DimensionTypes;
import com.github.retrooper.packetevents.resources.ResourceLocation;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.play.server.*;
import com.velocitypowered.api.proxy.Player;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.List;

public class VirtualSession extends SimplePacketListenerAbstract {
    @Getter
    private final Player player;

    private VirtualSession(@Nonnull Player player) {
        this.player = player;
    }

    public static @Nonnull VirtualSession spawn(@Nonnull Player player) {
        User user = PacketEvents.getAPI().getPlayerManager().getUser(player);
        VirtualSession session = new VirtualSession(player);
        PacketEvents.getAPI().getEventManager().registerListener(session);
        WrapperPlayServerJoinGame packet1 = new WrapperPlayServerJoinGame(
                0,
                true,
                GameMode.ADVENTURE,
                null,
                List.of("minecraft:overworld"),
                new NBTCompound(),
                DimensionTypes.OVERWORLD,
                Difficulty.PEACEFUL,
                "minecraft:overworld",
                1,
                0,
                2,
                5,
                false,
                false,
                false,
                false,
                new WorldBlockPosition(DimensionTypes.OVERWORLD, new Vector3i(0, 0, 0)),
                null
        );
        WrapperPlayServerPlayerAbilities packet2 = new WrapperPlayServerPlayerAbilities(false, false, false, false, 0, 0);
        WrapperPlayServerSpawnPosition packet3 = new WrapperPlayServerSpawnPosition(new ResourceLocation("overworld"), new Vector3i(0, 0, 0), 0, 0);
        WrapperPlayServerUpdateViewPosition packet4 = new WrapperPlayServerUpdateViewPosition(0, 0);
        WrapperPlayServerPlayerPositionAndLook packet5 = new WrapperPlayServerPlayerPositionAndLook(0, 0, 0, 0, 0, (byte) 0, 0, false);
        user.sendPacket(packet1);
        user.sendPacket(packet2);
        user.sendPacket(packet3);
        user.sendPacket(packet4);
        user.sendPacket(packet5);
        return session;
        // TODO:在会话结束unregister这个session!!!
        // TODO:否则会内溢存出!!!
    }

    @Override
    public void onPacketPlayReceive(@Nonnull PacketPlayReceiveEvent event) {
        switch(event.getPacketType()) {
            case CHAT_MESSAGE -> {
                player.createConnectionRequest(AuthXPlugin.getInstance().getProxy().getAllServers().stream().findFirst().orElseThrow());
            }
            case PLAYER_POSITION -> {}
            case PLAYER_POSITION_AND_ROTATION -> {}
            case PLAYER_ROTATION -> {}
        }
    }
}
