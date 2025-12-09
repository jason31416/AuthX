package cn.jason31416.authX.authbackend;

import cn.jason31416.authX.util.Logger;

public class UniauthAuthenticator extends AbstractAuthenticator {
    @Override
    public UserStatus fetchStatus(String username) {
        var userInfo = UniAuthAPIClient.fetchUserInfo(username);
        if(!userInfo.getBoolean("profile.exists")) return UserStatus.NOT_EXIST;
        return userInfo.getBoolean("profile.registered")?UserStatus.REGISTERED:UserStatus.IMPORTED;
    }

    @Override
    public RequestResult requestRegister(String username, String email) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public RequestResult verifyEmail(String username, String email, String password, String code) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public RequestResult forceRegister(String username, String password) {
        var res = UniAuthAPIClient.registerWithoutEmail(username, password);
        if(res == UniAuthAPIClient.AuthResult.SUCCESS){
            return RequestResult.SUCCESS;
        }else if(res == UniAuthAPIClient.AuthResult.ALREADY_REGISTERED){
            return RequestResult.USER_ALREADY_EXISTS;
        }else{
            return RequestResult.UNKNOWN_ERROR;
        }
    }

    @Override
    public RequestResult authenticate(String username, String password) {
        var res = UniAuthAPIClient.login(username, password);
        return switch(res){
            case SUCCESS -> RequestResult.SUCCESS;
            case INVALID_PASSWORD -> RequestResult.INVALID_PASSWORD;
            case NOT_REGISTERED -> RequestResult.USER_DOESNT_EXIST;
            case EMAIL_NOT_VERIFIED -> RequestResult.EMAIL_NOT_LINKED;
            default -> RequestResult.UNKNOWN_ERROR;
        };
    }

    @Override
    public RequestResult unregister(String username) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public RequestResult changePassword(String username, String newPassword) {
        var res = UniAuthAPIClient.forceResetPassword(username, newPassword);
        if(!res.isEmpty()) Logger.warn("Failed to change password for user: "+res);
        return res.isEmpty()?RequestResult.SUCCESS:RequestResult.UNKNOWN_ERROR;
    }

    public RequestResult changePasswordWithOld(String username, String oldPassword, String newPassword){
        var res = UniAuthAPIClient.resetPassword(username, oldPassword, newPassword);
        if(res) return RequestResult.SUCCESS;
        return RequestResult.INVALID_PASSWORD;
    }
}
