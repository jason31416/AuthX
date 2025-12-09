package cn.jason31416.authX.authbackend;

import lombok.Getter;

public abstract class AbstractAuthenticator {
    @Getter
    public static AbstractAuthenticator instance;

    public enum RequestResult {
        SUCCESS(true),
        UNKNOWN_ERROR(false),

        INVALID_PASSWORD(false), // Login-specific error code
        USER_DOESNT_EXIST(false),
        EMAIL_NOT_LINKED(true),

        USER_ALREADY_EXISTS(false); // Register-specific error code

        public final boolean success;
        RequestResult(boolean suc){
            success = suc;
        }
    }

    public enum UserStatus {
        REGISTERED,
        IMPORTED,
        NOT_EXIST
    }

    public void initialize(){}

    public abstract UserStatus fetchStatus(String username);

    public abstract RequestResult requestRegister(String username, String email);
    public abstract RequestResult verifyEmail(String username, String email, String password, String code);

    public abstract RequestResult forceRegister(String username, String password);

    public abstract RequestResult authenticate(String username, String password);

    public abstract RequestResult unregister(String username);

    public abstract RequestResult changePassword(String username, String newPassword);

    public RequestResult changePasswordWithOld(String username, String oldPassword, String newPassword){
        return changePassword(username, newPassword);
    }
}
