package cn.jason31416.authX.util;

import com.github.retrooper.packetevents.event.ProtocolPacketEvent;
import com.velocitypowered.api.proxy.Player;
import lombok.SneakyThrows;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Objects;

public class DebugUtil {
    @SneakyThrows
    public static void printObject(Object obj, String prefix, int depth) {
        for (Field field : obj.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            if (depth - 1 <= 0) {
                String cont = Objects.toString(field.get(obj));
                if (cont.length() > 50) {
                    cont = cont.substring(0, 30) + "..." + cont.substring(cont.length() - 20);
                }
                System.out.println(prefix+"- " + field.getName() + " : " + cont);
            } else {
                System.out.println(prefix+"- " + field.getName() + " :");
                printObject(field.get(obj), prefix + "  ", depth - 1);
            }
        }
    }

    public static void listenTo(ProtocolPacketEvent event, int depth) {
        try {
            com.github.retrooper.packetevents.wrapper.PacketWrapper<?> packet = null;
            for(Constructor<?> i : event.getPacketType().getWrapperClass().getConstructors()){
                if(i.getParameterCount()==1 && i.getParameterTypes()[0].isAssignableFrom(event.getClass())){
                    packet = (com.github.retrooper.packetevents.wrapper.PacketWrapper<?>) i.newInstance(event);
                    break;
                }
            }
            System.out.println(event.getClass().getSimpleName() + ": " + event.getPacketType());
            if(packet != null && depth > 0) printObject(packet, "  ", depth);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
