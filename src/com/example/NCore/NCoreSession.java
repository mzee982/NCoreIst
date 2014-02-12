package com.example.NCore;

import java.util.Date;

/**
 * Singleton for storing NCore server session information
 */
// TODO: Reconsider of using Application context instead
public final class NCoreSession {
    private static NCoreSession instance = null;

    private static Date mSessionStartDate;
    private static String mLoginName;
    private static String mLoginPassword;
    private static boolean bLoggedIn;
    private static Date mLoginDate;

    public static NCoreSession getInstance() {
        if (instance == null) {
            instance = new NCoreSession();
            initInstance();
        }

        return instance;
    }

    private static void initInstance() {
        instance.mSessionStartDate = new Date();
        instance.mLoginName = null;
        instance.mLoginPassword = null;
        instance.bLoggedIn = false;
        instance.mLoginDate = null;
    }

    private NCoreSession() {

    }

    public void login(String loginName, String loginPassword) {
        mLoginName = loginName;
        mLoginPassword = loginPassword;
        bLoggedIn = true;
        mLoginDate = new Date();
    }

    public void logout() {
        mLoginName = null;
        mLoginPassword = null;
        bLoggedIn = false;
        mLoginDate = null;
    }

    public boolean isLoggedIn() {
        return bLoggedIn;
    }

}
