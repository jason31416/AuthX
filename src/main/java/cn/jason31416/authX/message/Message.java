package cn.jason31416.authX.message;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class Message {
    public static MiniMessage miniMessage;
    String content;
    public Message(String content) {
        this.content = content
                .replace("§", "&")
                .replace("&a", "<green>")
                .replace("&b", "<aqua>")
                .replace("&c", "<red>")
                .replace("&d", "<light_purple>")
                .replace("&e", "<yellow>")
                .replace("&f", "<white>")
                .replace("&1", "<dark_blue>")
                .replace("&2", "<dark_green>")
                .replace("&3", "<dark_aqua>")
                .replace("&4", "<dark_red>")
                .replace("&5", "<dark_purple>")
                .replace("&6", "<gold>")
                .replace("&7", "<gray>")
                .replace("&8", "<dark_gray>")
                .replace("&9", "<blue>")
                .replace("&l", "<bold>")
                .replace("&r", "<reset>")
                .replace("&o", "<italic>");
    }
    public Message add(String placeholder, Object value){
        content = content.replace("%"+placeholder+"%", (value instanceof String)?(String)value:value.toString());
        return this;
    }
    public String toString(){
        return LegacyComponentSerializer.legacySection().serialize(MiniMessage.miniMessage().deserialize(content));
    }
    public Component toComponent(){
        return MiniMessage.miniMessage().deserialize(content);
    }
    public String toFormatted(){
        return MiniMessage.miniMessage().serialize(MiniMessage.miniMessage().deserialize(content));
    }
    public boolean equals(Object obj){
        if(obj instanceof Message){
            return ((Message)obj).content.equals(content);
        }
        return false;
    }

    public static Message getMessage(String path){
        return MessageLoader.getMessage(path);
    }
}
