package cn.jason31416.authX.handler;

import cn.jason31416.uniAuthReloaded.common.PlayerAccount;
import cn.jason31416.uniAuthReloaded.common.PlayerProfile;
import cn.jason31416.uniAuthReloaded.common.UniAuthAPIClient;
import cn.jason31416.uniAuthReloaded.common.data.DatabaseHandler;
import cn.jason31416.uniAuthReloaded.common.util.Config;
import com.google.gson.Gson;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class YggdrasilAuthenticator {
    public static CompletableFuture<PlayerProfile> authenticateVia(String username, String authMethod, String url){
        CompletableFuture<PlayerProfile> future = new CompletableFuture<>();
        var res = HttpClient.newHttpClient().sendAsync(HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build(), HttpResponse.BodyHandlers.ofString());
        res.thenAccept(response -> {
            UniAuthAPIClient.logger.info("Authenticating with "+url+" server: "+response.statusCode()+" "+response.body());
            if(response.statusCode() == 200) {
                var ret = new Gson().fromJson(response.body(), PlayerProfile.class);
                ret.authentication = authMethod;
                DatabaseHandler.setPreferred(username, authMethod);
                if(!DatabaseHandler.getAuthMethods(username).contains(authMethod)){
                    var session = LoginSession.getSessionMap().get(username);
                    session.setVerifyPassword(true);
                    session.setAuthMethod(authMethod);
                }
                PlayerAccount.getPlayerAccount(username).setAuthMethod(authMethod);
                future.complete(ret);
            }else{
                future.complete(null);
            }
        });
        return future;
    }
    public static PlayerProfile authenticate(String username, String serverID, String ip) {
        Map<String, String> authServers = (Map<String, String>) Config.get("yggdrasil.auth-servers");
        try{
            String preferredMethod = DatabaseHandler.getPreferredMethod(username);
            if(preferredMethod!=null&&!preferredMethod.isEmpty()&&authServers.containsKey(preferredMethod)) {
                String url = authServers.get(preferredMethod) + "session/minecraft/hasJoined?username=" + username + "&serverId=" + serverID;
                if (Config.getBoolean("yggdrasil.verify-ip")) url += "&ip=" + ip;
                var res = authenticateVia(username, preferredMethod, url);
                if (res.get() != null){
                    return res.get();
                }
            }
            var session = LoginSession.getSessionMap().get(username);
            if(!session.isEnforcePrimaryMethod()) {
                List<CompletableFuture<PlayerProfile>> futures = new ArrayList<>();
                for (String method : authServers.keySet()) {
                    if (!method.equals(preferredMethod)) {
                        String url1 = authServers.get(method) + "session/minecraft/hasJoined?username=" + username + "&serverId=" + serverID;
                        if (Config.getBoolean("yggdrasil.verify-ip")) url1 += "&ip=" + ip;
                        futures.add(authenticateVia(username, method, url1));
                    }
                }

                CompletableFuture<Void> allFutures = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
                allFutures.join();

                for (CompletableFuture<PlayerProfile> future : futures) {
                    if (future.get() != null) {
                        return future.get();
                    }
                }
            }
            return null;
        }catch (Exception e){
            UniAuthAPIClient.logger.error("Failed to authenticate " + username + ": " + e.getMessage());
        }
        return null;
    }
}
