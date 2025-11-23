package cn.jason31416.authX.virtual;

import cn.jason31416.authX.AuthXPlugin;
import cn.jason31416.authX.util.DimensionUtil;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.SimplePacketListenerAbstract;
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.player.GameMode;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.protocol.world.Difficulty;
import com.github.retrooper.packetevents.protocol.world.WorldBlockPosition;
import com.github.retrooper.packetevents.protocol.world.chunk.BaseChunk;
import com.github.retrooper.packetevents.protocol.world.chunk.Column;
import com.github.retrooper.packetevents.protocol.world.chunk.LightData;
import com.github.retrooper.packetevents.protocol.world.chunk.TileEntity;
import com.github.retrooper.packetevents.protocol.world.dimension.DimensionTypes;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.play.server.*;
import com.velocitypowered.api.proxy.Player;
import lombok.Getter;

import javax.annotation.Nonnull;
import java.util.BitSet;
import java.util.List;
import java.util.Random;

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
        WrapperPlayServerJoinGame p0 = new WrapperPlayServerJoinGame(
                0,
                false,
                GameMode.ADVENTURE,
                null,
                List.of("minecraft:overworld"),
                DimensionUtil.getDimensionCodec(user.getClientVersion()),
                DimensionTypes.OVERWORLD,
                Difficulty.PEACEFUL,
                "minecraft:overworld",
                0,
                1,
                2,
                5,
                false,
                true,
                false,
                false,
                new WorldBlockPosition(DimensionTypes.OVERWORLD, new Vector3i(0, 0, 0)),
                null
        );
        WrapperPlayServerPlayerAbilities p1 = new WrapperPlayServerPlayerAbilities(
                false,
                false,
                false,
                false,
                0.0F,
                0.1F
        );
        int teleportId = new Random().nextInt();
        WrapperPlayServerPlayerPositionAndLook p2 = new WrapperPlayServerPlayerPositionAndLook(
                0,
                64,
                0,
                0,
                0,
                (byte) 0x02,
                teleportId,
                false
        );
        WrapperPlayServerSpawnPosition p3 = new WrapperPlayServerSpawnPosition(
                new Vector3i(
                        0,
                        64,
                        0
                )
        );
        WrapperPlayServerPlayerInfo p4 = new WrapperPlayServerPlayerInfo(
                WrapperPlayServerPlayerInfo.Action.UPDATE_DISPLAY_NAME
        );
        WrapperPlayServerDeclareCommands p5 = new WrapperPlayServerDeclareCommands(
                List.of(),
                0
        );
        WrapperPlayServerChunkData p6 = new WrapperPlayServerChunkData(
                new Column(0, 0, false, new BaseChunk[0], new TileEntity[0]),
                new LightData(false, new BitSet(), new BitSet(), new BitSet(), new BitSet(), 0, 0, new byte[0][0], new byte[0][0])
        );
        user.sendPacket(p0);
        user.sendPacket(p1);
        user.sendPacket(p2);
        if(user.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_19_3))
            user.sendPacket(p3);
        if(user.getClientVersion().equals(ClientVersion.V_1_16_4))
            user.sendPacket(p4);
        if(user.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_13))
            user.sendPacket(p5);
        user.sendPacket(p6);

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
