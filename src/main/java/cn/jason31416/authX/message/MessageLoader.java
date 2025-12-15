package cn.jason31416.authX.message;

import cn.jason31416.authX.AuthXPlugin;
import cn.jason31416.authX.util.Config;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MessageLoader {
    public static MessageLoader instance;
    public Map<String, Object> messageConfig;

    public static void initialize(){
        File langfolder = new File(AuthXPlugin.getInstance().getDataDirectory(), "lang");
        if(!langfolder.exists()) {
            langfolder.mkdir();
            List<String> supported = List.of("en-us", "zh-cn");
            supported.forEach(lang -> {
                File langfile = new File(AuthXPlugin.getInstance().getDataDirectory(), "lang/"+ lang+".yml");
                try (InputStream inputStream = AuthXPlugin.class.getClassLoader().getResourceAsStream("lang/"+ lang+".yml"); OutputStream outputStream = new FileOutputStream(langfile)) {
                    outputStream.write(Objects.requireNonNull(inputStream).readAllBytes());
                }catch (Exception e){
                    throw new RuntimeException("Cannot save language file: "+e);
                }
            });
        }
        File lang = new File(AuthXPlugin.getInstance().getDataDirectory(), "lang/"+ Config.getString("lang")+".yml");

        if(!lang.exists()){
            throw new RuntimeException("Language file doesn't exist!");
        }

        new MessageLoader(lang);
    }

    public MessageLoader(File filePath) {
        try (FileInputStream is = new FileInputStream(filePath)){
            this.messageConfig = new Yaml().load(is);
        }catch (Exception ignored){
            throw new RuntimeException("Failed to load message config file!" + ignored.getMessage());
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
