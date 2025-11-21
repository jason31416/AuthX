package cn.jason31416.authX.util;

import cn.jason31416.authX.AuthXPlugin;

public class Logger {
    public static void info(String message){
        AuthXPlugin.getInstance().info(message);
    }
    public static void warn(String message){
        AuthXPlugin.getInstance().warning(message);
    }
    public static void error(String message) {
        AuthXPlugin.getInstance().error(message);
    }
}
