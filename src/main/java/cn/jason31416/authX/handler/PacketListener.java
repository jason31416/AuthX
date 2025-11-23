package cn.jason31416.authX.handler;

import cn.jason31416.authX.util.DebugUtil;
import cn.jason31416.authX.virtual.VirtualSession;
import com.github.retrooper.packetevents.event.SimplePacketListenerAbstract;
import com.github.retrooper.packetevents.event.simple.*;
import com.github.retrooper.packetevents.protocol.nbt.NBT;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerJoinGame;
import com.google.gson.Gson;
import com.velocitypowered.api.proxy.Player;

import javax.annotation.Nonnull;
import java.util.Map;

public class PacketListener extends SimplePacketListenerAbstract {
//    @Override
//    public void onPacketLoginReceive(@Nonnull PacketLoginReceiveEvent event) {
//    }
//
//    public void onPacketHandshakeReceive(PacketHandshakeReceiveEvent event) {
//        DebugUtil.listenTo(event, 1);
//    }
//
//    public void onPacketStatusReceive(PacketStatusReceiveEvent event) {
//        DebugUtil.listenTo(event, 1);
//    }
//
//    public void onPacketStatusSend(PacketStatusSendEvent event) {
//        DebugUtil.listenTo(event, 2);
//    }
//
//    public void onPacketLoginSend(PacketLoginSendEvent event) {
//        DebugUtil.listenTo(event, 2);
//    }
//
//    public void onPacketConfigReceive(PacketConfigReceiveEvent event) {
//        DebugUtil.listenTo(event, 1);
//    }
//
//    public void onPacketConfigSend(PacketConfigSendEvent event) {
//
//    }
//
//    public void onPacketPlayReceive(PacketPlayReceiveEvent event) {
//        DebugUtil.listenTo(event, 1);
//    }
//
//    public void onPacketPlaySend(PacketPlaySendEvent event) {
//        if(event.getPacketType() == PacketType.Play.Server.JOIN_GAME) {
//            WrapperPlayServerJoinGame packet = new WrapperPlayServerJoinGame(event);
//            System.out.println(packet.getDimensionCodec());
//        }
//    }
}
