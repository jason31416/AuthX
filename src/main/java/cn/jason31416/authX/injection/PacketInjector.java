package cn.jason31416.authX.injection;

import cn.jason31416.authX.injection.packet.XEncryptionResponse;
import com.velocitypowered.proxy.protocol.MinecraftPacket;
import com.velocitypowered.proxy.protocol.StateRegistry;
import com.velocitypowered.proxy.protocol.packet.EncryptionResponsePacket;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.function.Supplier;

public class PacketInjector {
    public static void inject(){
        try {
            StateRegistry.PacketRegistry serverbound = getServerboundPacketRegistry(StateRegistry.LOGIN);
            redirectInput(serverbound, EncryptionResponsePacket.class, XEncryptionResponse::new);
        } catch (Exception e) {
            throw new ReflectionException("Failed to inject packet injection", e);
        }
    }
    private static StateRegistry.PacketRegistry getServerboundPacketRegistry(StateRegistry stateRegistry) {
        return (StateRegistry.PacketRegistry) ReflectionUtil.fetchField(stateRegistry, "serverbound", StateRegistry.class);
    }

    private static Map<?, ?> getProtocolRegistriesMap(StateRegistry.PacketRegistry bound) {
        return (Map<?, ?>) ReflectionUtil.fetchField(bound, "versions", StateRegistry.PacketRegistry.class);
    }
    private static <T> void redirectInput(StateRegistry.PacketRegistry bound, Class<T> originalClass, Supplier<? extends T> supplierRedirect) throws NoSuchFieldException, InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        Field f$packetIdToSupplier = StateRegistry.PacketRegistry.ProtocolRegistry.class.getDeclaredField("packetIdToSupplier");
        f$packetIdToSupplier.setAccessible(true);

        Method map$entry$setValueMethod = Map.Entry.class.getMethod("setValue", Object.class);

        for (Object protocolRegistry : getProtocolRegistriesMap(bound).values()) {
            Map<?, ?> packetIdToSupplier = (Map<?, ?>) f$packetIdToSupplier.get(protocolRegistry); // IntObjectMap<Supplier<? extends MinecraftPacket>>
            for (Map.Entry<?, ?> e : packetIdToSupplier.entrySet()) {
                MinecraftPacket minecraftPacketObject = (MinecraftPacket) ((Supplier<?>) e.getValue()).get();
                // 类匹配则进行替换
                if (minecraftPacketObject.getClass().equals(originalClass)) {
                    map$entry$setValueMethod.invoke(e, supplierRedirect);
                }
            }
        }
    }
}
