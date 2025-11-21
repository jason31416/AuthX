package cn.jason31416.authX.handler;

import cn.jason31416.authX.util.DebugUtil;
import cn.jason31416.authX.virtual.VirtualSession;
import com.github.retrooper.packetevents.event.SimplePacketListenerAbstract;
import com.github.retrooper.packetevents.event.simple.*;
import com.velocitypowered.api.proxy.Player;

import javax.annotation.Nonnull;

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
    public void onPacketStatusSend(PacketStatusSendEvent event) {
        DebugUtil.listenTo(event, 1);
    }
//
    public void onPacketLoginSend(PacketLoginSendEvent event) {
        DebugUtil.listenTo(event, 1);
    }
//
//    public void onPacketConfigReceive(PacketConfigReceiveEvent event) {
//        DebugUtil.listenTo(event, 1);
//    }
//
    public void onPacketConfigSend(PacketConfigSendEvent event) {
        DebugUtil.listenTo(event, 1);
    }
//
//    public void onPacketPlayReceive(PacketPlayReceiveEvent event) {
//        DebugUtil.listenTo(event, 1);
//    }
//
    public void onPacketPlaySend(PacketPlaySendEvent event) {
        DebugUtil.listenTo(event, 0);
    }
}
