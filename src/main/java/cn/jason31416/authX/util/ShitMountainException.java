package cn.jason31416.authX.util;

public class ShitMountainException extends RuntimeException {
    public ShitMountainException(String message, Exception e) {
        super("An unexpected error has occurred, you should probably contact the developer providing complete log of the server. Error message: " + message, e);
    }
    public ShitMountainException(String message) {
        super("An unexpected error has occurred, you should probably contact the developer providing complete log of the server. Error message: " + message);
    }
}
