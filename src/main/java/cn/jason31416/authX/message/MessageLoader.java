package cn.jason31416.authX.message;

import cn.jason31416.uniAuthReloaded.common.UniAuthAPIClient;
import cn.jason31416.uniAuthReloaded.velocity.UniAuthReloadedVelocity;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.Map;
import java.util.Objects;

public class MessageLoader {
    public static MessageLoader instance;
    public Map<String, Object> messageConfig;

    public static void initialize(){
        File lang = new File(UniAuthAPIClient.dataDirectory, "lang.yml");

        if(!lang.exists()){
            try (InputStream inputStream = UniAuthReloadedVelocity.class.getClassLoader().getResourceAsStream("lang.yml"); OutputStream outputStream = new FileOutputStream(lang)) {
                outputStream.write(Objects.requireNonNull(inputStream).readAllBytes());
            }catch (Exception e){
                throw new RuntimeException("Cannot save language file: "+e);
            }
        }
        new MessageLoader(lang);
    }

    public MessageLoader(File filePath) {
        try (FileInputStream is = new FileInputStream(filePath)){
            this.messageConfig = new Yaml().load(is);
        }catch (Exception ignored){
            throw new RuntimeException("Failed to load message config file!");
        }
        instance = this;
    }
    public static String get(String key, String def) {
        try {
            if (key.contains(".")) {
                String[] keys = key.split("\\.");
                Object value = instance.messageConfig.get(keys[0]);
                for (int i = 1; i < keys.length; i++) {
                    value = ((Map<String, Object>) value).get(keys[i]);
                }
                return value.toString();
            } else {
                return instance.messageConfig.get(key).toString();
            }
        } catch (Exception e) {
            return def;
        }
    }
    public static Message getMessage(String key) {
        return new Message(get(key, "<red>Error: message "+key+" not found, please contact admin!"));
    }
    public static Message getMessage(String key, String defaultMessage) {
        return new Message(get(key, defaultMessage));
    }
}
