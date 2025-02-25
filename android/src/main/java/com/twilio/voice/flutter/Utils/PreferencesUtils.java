package com.twilio.voice.flutter.Utils;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferencesUtils {
    private static final String PREF_NAME = "TwilioVoicePreferences";
    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_FCM_TOKEN = "fcm_token";

    private final SharedPreferences sharedPreferences;
    private static PreferencesUtils instance;

    private PreferencesUtils(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized PreferencesUtils getInstance(Context context) {
        if (instance == null) {
            instance = new PreferencesUtils(context.getApplicationContext());
        }
        return instance;
    }

    public void setAccessToken(String accessToken) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_ACCESS_TOKEN, accessToken);
        editor.apply();
    }

    public String getAccessToken() {
        return sharedPreferences.getString(KEY_ACCESS_TOKEN, null);
    }

    public void setFcmToken(String fcmToken) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_FCM_TOKEN, fcmToken);
        editor.apply();
    }

    public String getFcmToken() {
        return sharedPreferences.getString(KEY_FCM_TOKEN, null);
    }

    public void clearTokens() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(KEY_ACCESS_TOKEN);
        editor.remove(KEY_FCM_TOKEN);
        editor.apply();
    }


    // Add this method to handle contact name lookup
    public String findContactName(String phoneNumber) {
        return phoneNumber;
    }
}