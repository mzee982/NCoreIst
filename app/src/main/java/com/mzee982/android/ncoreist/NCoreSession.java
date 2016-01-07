package com.mzee982.android.ncoreist;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Date;

/**
 * Singleton for storing NCore server session information
 */
// TODO: Reconsider of using Application context instead
public final class NCoreSession {

    // Singleton
    private static NCoreSession instance = null;

    // Members
    private static Date mSessionStartDate;
    private static String mLoginName;
    private static String mLoginPassword;
    private static boolean bRememberCredentials;
    private static boolean bLoggedIn;
    private static Date mLoginDate;
    private static String mLogoutUrl;

    public static NCoreSession getInstance(Context aContext) {
        if (instance == null) {
            instance = new NCoreSession();
            initInstance(aContext);
        }

        return instance;
    }

    private static void initInstance(Context aContext) {

        // Defaults
        instance.mSessionStartDate = new Date();
        instance.mLoginName = null;
        instance.mLoginPassword = null;
        instance.bRememberCredentials = false;
        instance.bLoggedIn = false;
        instance.mLoginDate = null;
        instance.mLogoutUrl = null;

        // Credentials from shared preferences
        SharedPreferences sharedPref = aContext.getSharedPreferences(NCoreSession.class.getName(),
                Context.MODE_PRIVATE);

        instance.bRememberCredentials = sharedPref.getBoolean(
                aContext.getResources().getString(R.string.shared_pref_remember_credentials), false);
        instance.mLoginName = sharedPref.getString(
                aContext.getResources().getString(R.string.shared_pref_login_name), null);
        instance.mLoginPassword = sharedPref.getString(
                aContext.getResources().getString(R.string.shared_pref_login_password), null);

    }

    private NCoreSession() {}

    public void login(Context context, String loginName, String loginPassword, boolean rememberCredentials) {
        mLoginName = loginName;
        mLoginPassword = loginPassword;
        bRememberCredentials = rememberCredentials;
        bLoggedIn = true;
        mLoginDate = new Date();

        // Save credentials to shared preferences
        SharedPreferences sharedPref = context.getSharedPreferences(NCoreSession.class.getName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor sharedPrefEditor = sharedPref.edit();

        sharedPrefEditor.putBoolean(context.getResources().getString(R.string.shared_pref_remember_credentials),
                bRememberCredentials);

        // Save
        if (bRememberCredentials) {
            sharedPrefEditor.putString(context.getResources().getString(R.string.shared_pref_login_name), mLoginName);
            sharedPrefEditor.putString(context.getResources().getString(R.string.shared_pref_login_password),
                    mLoginPassword);
        }

        // Remove
        else {
            sharedPrefEditor.remove(context.getResources().getString(R.string.shared_pref_login_name));
            sharedPrefEditor.remove(context.getResources().getString(R.string.shared_pref_login_password));
        }

        sharedPrefEditor.commit();

    }

    public void logout(Context aContext) {

        // Forget credentials
        if (!bRememberCredentials) {
            mLoginName = null;
            mLoginPassword = null;
        }

        // Defaults
        bLoggedIn = false;
        mLoginDate = null;

    }

    public String getLoginName() {
        return mLoginName;
    }

    public String getLoginPassword() {
        return mLoginPassword;
    }

    public boolean isRememberCredentials() {
        return bRememberCredentials;
    }

    public boolean isLoggedIn() {
        return bLoggedIn;
    }

    public String getLogoutUrl() {
        return mLogoutUrl;
    }

    public void setLogoutUrl(String url) {
        mLogoutUrl = url;
    }

}
