package com.example.nminhanh.spacesharing.Utils;

import android.app.Activity;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

public class FacebookConnectUtils {
    CallbackManager mFacebookCallbackManager;

    public FacebookConnectUtils() {
        this.mFacebookCallbackManager = CallbackManager.Factory.create();
    }

    public void loginWithReadPermission(Activity activity, java.util.Collection<String> permissionList) {
        LoginManager.getInstance().logInWithReadPermissions(activity, permissionList);
    }

    public void logOut() {
        LoginManager.getInstance().logOut();
    }

    public void registerLoginCallback(FacebookCallback<LoginResult> onLoginComplete) {
        LoginManager.getInstance().registerCallback(mFacebookCallbackManager, onLoginComplete);
    }

    public void unregisterLoginCallback() {
        LoginManager.getInstance().unregisterCallback(mFacebookCallbackManager);
    }

    public CallbackManager getmFacebookCallbackManager() {
        return mFacebookCallbackManager;
    }
    // TODO: hoàn thành nốt việc kết nối giữa tài khoản SĐT và facebook
}
