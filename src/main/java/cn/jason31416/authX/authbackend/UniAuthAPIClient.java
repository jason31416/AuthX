package cn.jason31416.authX.authbackend;

import cn.jason31416.authX.AuthXPlugin;
import cn.jason31416.authX.util.Config;
import cn.jason31416.authX.util.Logger;
import cn.jason31416.authX.util.MapTree;
import cn.jason31416.authX.util.RSAUtil;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.velocitypowered.api.scheduler.Scheduler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class UniAuthAPIClient {
    public static Map<String, String> userOnlineUUIDCache = new HashMap<>();

    public enum AuthResult {
        SUCCESS,
        INVALID_PASSWORD,
        NOT_REGISTERED,
        EMAIL_NOT_VERIFIED,
        ALREADY_REGISTERED,
        UNKNOWN_ERROR,
        FAILED,
        EMAIL_EXISTS
    }
    public static class ParamBuilder{
        private Map<String, String> data = new HashMap<>();
        public ParamBuilder put(String key, String value){
            data.put(key, value);
            return this;
        }
        public Map<String, String> build(){
            return data;
        }
    }

    public static String getAPIUrl(){
        return Config.getString("authentication.password.uniauth.url");
    }

    public static String fetchPublicKey(boolean forceReload){
        File file = new File(AuthXPlugin.instance.getDataDirectory(), "publickey.txt");
        if(file.exists() && !forceReload){
            try (FileInputStream fis = new FileInputStream(file)) {
                return new String(fis.readAllBytes(), StandardCharsets.UTF_8);
            }catch (IOException e){
                throw new RuntimeException(e);
            }
        }
        try {
            String i = HttpClient.newHttpClient().send(HttpRequest.newBuilder()
                    .uri(URI.create(getAPIUrl() + "publickey"))
                    .GET()
                    .build(), HttpResponse.BodyHandlers.ofString()).body();
            try(FileOutputStream fos = new FileOutputStream(file)){
                fos.write(i.getBytes(StandardCharsets.UTF_8));
            }
            return i;
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static String hashWithFormat(String password, String format) {
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance(format);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        byte[] encodedhash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
        return frombytes(encodedhash);
    }

    private static String frombytes(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private static MapTree request(String endpoint, Map<String, String> data){
//        Logger.info(endpoint);
        HttpClient client = HttpClient.newHttpClient();
        try {
            Map<String, Object> cont = new HashMap<>();
            cont.put("data", data);
            cont.put("apikey", Config.getString("authentication.password.uniauth.api-key"));
            cont.put("timestamp", System.currentTimeMillis());
            String pkey = fetchPublicKey(false);
            MapTree res;
            try {
                var ret = client.send(HttpRequest.newBuilder()
                        .uri(URI.create(getAPIUrl() + endpoint))
                        .POST(HttpRequest.BodyPublishers.ofString(RSAUtil.encryptByPublicKey(new Gson().toJson(cont), pkey)))
                        .build(), HttpResponse.BodyHandlers.ofString());

                String header_hashsum = ret.headers().firstValue("X-Checksum").orElse("");
                String header_timestamp = ret.headers().firstValue("X-Timestamp").orElse("");
                String hashsum = hashWithFormat(ret.body(), "SHA-256");
                if (header_hashsum.isEmpty() || header_timestamp.isEmpty())
                    throw new RuntimeException("Missing header!");
                hashsum += "$" + header_timestamp;
                if (Long.parseLong(header_timestamp) + 1000 * 60 * 5 < System.currentTimeMillis())
                    throw new RuntimeException("Expired timestamp!");
                if (!hashsum.equals(RSAUtil.decryptByPublicKey(header_hashsum, pkey))) {
                    throw new RuntimeException("Hashsum not match!");
                }

                res = new MapTree(new Gson().fromJson(ret.body(), new TypeToken<Map<String, Object>>() {
                }.getType()));
            }catch (Exception e){
                String pk = fetchPublicKey(true);
                if(!pk.equals(pkey)){
                    Logger.error("DETECTED CHANGED PUBLIC KEY! THIS MIGHT BE CAUSED BY AN ATTACK!");
                    Logger.error("Please contact your Uniauth provider IMMEDIATELY to confirm if the change was intentionally made!");
                    try(FileOutputStream fos = new FileOutputStream(new File(AuthXPlugin.instance.getDataDirectory(), "security-alert-"+new Date()+".txt"))){
                        String message = "Detected changed public key! This might be caused by an attack!\nPlease contact your Uniauth provider IMMEDIATELY to confirm if the change was intentionally made!\n\nOld public key:\n"+pkey+"\n\nNew public key:\n"+pk+"\n\nTime: "+new Date()+"\n";
                        fos.write(message.getBytes(StandardCharsets.UTF_8));
                    }catch (IOException ex){
                        ex.printStackTrace();
                    }
                }
                throw e;
            }

            if(res.getInt("code", 500)==491){
                throw new JsonParseException("Invalid API key");
            }

            return res;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public static MapTree fetchUserInfo(String username){
        return request("playerInfo", new ParamBuilder().put("username", username).build());
    }
    public static void setAuthType(String username, String authType){
        request("setAuthType", new ParamBuilder().put("username", username).put("authType", authType).build());
    }
    public static AuthResult login(String username, String password){
        MapTree response = request("login", new ParamBuilder().put("username", username).put("password", password).build());
        int code = response.getInt("code", 500);
        return switch (code){
            case 200 -> AuthResult.SUCCESS;
            case 401 -> AuthResult.INVALID_PASSWORD;
            case 402 -> AuthResult.NOT_REGISTERED;
            case 403 -> AuthResult.EMAIL_NOT_VERIFIED;
            default -> {
                Logger.error("Unknown auth result: "+response.data);
                yield AuthResult.UNKNOWN_ERROR;
            }
        };
    }
    public static AuthResult registerWithoutEmail(String username, String password){
        MapTree response = request("register_without_email", new ParamBuilder().put("username", username).put("password", password).build());
        int code = response.getInt("code", 500);
        return switch (code){
            case 200 -> AuthResult.SUCCESS;
            case 501 -> AuthResult.FAILED;
            case 402 -> AuthResult.ALREADY_REGISTERED;
            default -> {
                Logger.error("Unknown auth result: "+response.data);
                yield AuthResult.UNKNOWN_ERROR;
            }
        };
    }
    public static AuthResult register(String username, String email){
        MapTree response = request("register", new ParamBuilder().put("username", username).put("email", email).build());
        int code = response.getInt("code", 500);
        return switch (code){
            case 200 -> AuthResult.SUCCESS;
            case 401 -> AuthResult.ALREADY_REGISTERED;
            case 402 -> AuthResult.FAILED;
            default -> {
                Logger.error("Unknown auth result: "+response.data);
                yield AuthResult.UNKNOWN_ERROR;
            }
        };
    }
    public static AuthResult resetPasswordEmail(String username, String email){
        MapTree response = request("authviaemail", new ParamBuilder().put("username", username).put("email", email).put("operation", "changepassword").build());
        int code2 = response.getInt("code", 500);
        return switch (code2){
            case 200 -> AuthResult.SUCCESS;
            case 401 -> AuthResult.FAILED;
            default -> {
                Logger.error("Unknown auth result: "+response.data);
                yield AuthResult.UNKNOWN_ERROR;
            }
        };
    }
    public static String forceResetPassword(String username, String newPassword){
        MapTree response = request("accountoperation", new ParamBuilder().put("operation", "changepassword")
                .put("authentication", "registered_server")
                .put("username", username)
                .put("newPassword", newPassword).build());
        return response.getBoolean("success")? "" : response.getString("message");
    }
    public static boolean resetPassword(String username, String oldPassword, String newPassword) {
        MapTree response = request("accountoperation", new ParamBuilder().put("operation", "changepassword")
                .put("authentication", "password")
                .put("username", username)
                .put("password", oldPassword)
                .put("newPassword", newPassword).build());
        return response.getBoolean("success");
    }
    public static AuthResult verifyEmail(String username, String email, String password, String code){
        MapTree response = request("verify", new ParamBuilder().put("username", username).put("email", email).put("password", password).put("verificationCode", code).build());
        int code2 = response.getInt("code", 500);
        return switch (code2){
            case 200 -> AuthResult.SUCCESS;
            case 401 -> AuthResult.ALREADY_REGISTERED;
            case 402 -> AuthResult.FAILED;
            default -> {
                Logger.error("Unknown auth result: "+response.data);
                yield AuthResult.UNKNOWN_ERROR;
            }
        };
    }
    public static AuthResult linkEmail(String username, String password, String email){
        MapTree response = request("linkemail", new ParamBuilder().put("username", username).put("password", password).put("email", email).build());
        int code = response.getInt("code", 500);
        return switch (code) {
            case 200 -> AuthResult.SUCCESS;
            case 401 -> AuthResult.EMAIL_EXISTS;
            case 403 -> AuthResult.INVALID_PASSWORD;
            default -> {
                Logger.error("Unknown auth result: "+response.data);
                yield AuthResult.UNKNOWN_ERROR;
            }
        };
    }
    public static AuthResult verifyLink(String username, String email, String code){
        MapTree response = request("verifylink", new ParamBuilder().put("username", username).put("email", email).put("verificationCode", code).build());
        int code2 = response.getInt("code", 500);
        return switch (code2){
            case 200 -> AuthResult.SUCCESS;
            case 402 -> AuthResult.FAILED;
            default -> {
                Logger.error("Unknown auth result: "+response.data);
                yield AuthResult.UNKNOWN_ERROR;
            }
        };
    }
}
