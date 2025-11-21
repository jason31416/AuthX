package cn.jason31416.authX.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.yaml.snakeyaml.Yaml;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("unchecked")
public class MapTree {
    public Map<String, Object> data;
    public MapTree(Map<String, Object> data){
        this.data = data;
    }
    public MapTree(){
        this.data = new ConcurrentHashMap<>();
    }

    public MapTree put(String key, Object val){
        try {
            if (key.contains(".")) {
                String[] keys = key.split("\\.");
                if(!data.containsKey(keys[0])) data.put(keys[0], new ConcurrentHashMap<>());
                Map<String, Object> value = (Map<String, Object>) data.get(keys[0]);
                for (int i = 1; i < keys.length-1; i++) {
                    if(!data.containsKey(keys[0])) value.put(keys[0], new ConcurrentHashMap<>());
                    value = (Map<String, Object>) value.get(keys[0]);
                }
                value.put(keys[keys.length - 1], val);
            }else{
                data.put(key, val);
            }
            return this;
        } catch (Exception e) {
            e.printStackTrace();
            return this;
        }
    }

    public Object get(String key) {
        try {
            if (key.contains(".")) {
                String[] keys = key.split("\\.");
                Object value = data.get(keys[0]);
                for (int i = 1; i < keys.length; i++) {
                    value = ((Map<String, Object>) value).get(keys[i]);
                }
                return value;
            } else {
                return data.get(key);
            }
        } catch (Exception e) {
            return null;
        }
    }

    public MapTree getSection(String key){
        Object value = get(key);
        if(value instanceof Map){
            return new MapTree((Map<String, Object>) value);
        }
        return new MapTree(new ConcurrentHashMap<>());
    }

    public boolean getBoolean(String key, boolean defaultValue){
        Object value = get(key);
        if(value instanceof Boolean){
            return (Boolean) value;
        }
        return defaultValue;
    }
    public boolean getBoolean(String key){
        return getBoolean(key, false);
    }
    public boolean contains(String key){
        return get(key)!= null;
    }
    public int getInt(String key, int defaultValue){
        Object value = get(key);
        if(value instanceof Integer){
            return (Integer) value;
        }else if(value instanceof Double) {
            return ((Double) value).intValue();
        }else if(value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }
    public int getInt(String key){
        return getInt(key, 0);
    }
    public double getDouble(String key, double defaultValue){
        Object value = get(key);
        if(value instanceof Double){
            return (Double) value;
        }else if(value instanceof Integer) {
            return ((Integer) value).doubleValue();
        }else if(value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }
    public double getDouble(String key){
        return getDouble(key, 0.0);
    }
    public String getString(String key, String defaultValue){
        Object value = get(key);
        if(value instanceof String){
            return (String) value;
        }
        return defaultValue;
    }
    public String getString(String key){
        return getString(key, "");
    }
    public List<String> getStringList(String key){
        Object value = get(key);
        if(value instanceof List){
            return (List<String>) value;
        }
        return new ArrayList<>();
    }
    public Set<String> getKeys(){
        return data.keySet();
    }

    public String toJson(){
        return new Gson().toJson(data);
    }
    public String toYaml(){
        return new Yaml().dump(data);
    }
    public static MapTree fromJson(String json){
        return new MapTree(new Gson().fromJson(json, new TypeToken<Map<String, Object>>(){}.getType()));
    }
    public static MapTree fromYaml(String yaml){
        return new MapTree(new Yaml().load(yaml));
    }
}
