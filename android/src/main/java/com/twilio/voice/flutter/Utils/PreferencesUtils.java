package com.twilio.voice.flutter.Utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.util.Log;

import androidx.core.content.res.ResourcesCompat;

import java.util.Map;

public class PreferencesUtils {
    private static final String TAG = "PreferencesUtils";

    private SharedPreferences sharedPreferencesAccess;
    private SharedPreferences sharedPreferencesContactData;
    private SharedPreferences sharedPreferencesCallStyle;
    private Context context;

    @SuppressLint("StaticFieldLeak")
    private static PreferencesUtils instance;

    private PreferencesUtils() {

    }

    public static PreferencesUtils getInstance(Context context) {
        if (instance == null) {
            instance = new PreferencesUtils();
            instance.sharedPreferencesAccess = context.getSharedPreferences(TwilioConstants.SHARED_PREFERENCES_ACCESS, Context.MODE_PRIVATE);
            instance.sharedPreferencesContactData = context.getSharedPreferences(TwilioConstants.SHARED_PREFERENCES_CONTACT_DATA, Context.MODE_PRIVATE);
            instance.sharedPreferencesCallStyle = context.getSharedPreferences(TwilioConstants.SHARED_PREFERENCES_CALL_STYLE, Context.MODE_PRIVATE);
        }

        instance.context = context;
        return instance;
    }


    public void setContacts(Map<String, Object> data, String defaultDisplayName) {
        SharedPreferences.Editor editor = this.sharedPreferencesContactData.edit();

        // Save default display name
        if (defaultDisplayName == null) defaultDisplayName = "";
        editor.putString(TwilioConstants.SHARED_PREFERENCES_KEY_DEFAULT_DISPLAY_NAME, defaultDisplayName);

        // Save contacts
        if (data != null) {
            int i = 0;
            for (Map.Entry<String, Object> keyValue : data.entrySet()) {
                //noinspection unchecked
                Map<String, Object> item = (Map<String, Object>) keyValue.getValue();
                String phoneNumber = keyValue.getKey();
                String displayName = (String) item.get("displayName");
                if (displayName == null) displayName = "";
                String photoURL = (String) item.get("photoURL");
                if (photoURL == null) photoURL = "";
                editor.putString(phoneNumber, displayName + ";" + photoURL);
                i++;
            }
            Log.i(TAG, "Saved " + i + " contacts");
        }

        editor.apply();
    }

    public String findContactName(String phoneNumber) {
        String defaultDisplayName = this.getDefaultDisplayName();

        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            Log.i(TAG, "Error finding the contact display. No phone number");
            return defaultDisplayName;
        }

        final String value = this.sharedPreferencesContactData.getString(phoneNumber, null);
        if (value == null || value.isEmpty()) {
            Log.i(TAG, "Error finding the contact display name for " + phoneNumber + ". No value stored");
            return defaultDisplayName;
        }

        try {
            final String[] parts = value.split(";");
            if (parts.length == 0) {
                Log.i(TAG, "Error finding the contact display name for " + phoneNumber + ". The stored value is wrong " + value + ". Contains " + parts.length + " parts.");
                return defaultDisplayName;
            }

            return parts[0];
        } catch (Exception e) {
            Log.i(TAG, "Error finding the contact display name for " + phoneNumber + ". Error: " + e.getMessage());
            return defaultDisplayName;
        }
    }

    public String findPhotoURL(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            Log.i(TAG, "Error finding the contact photo URL. No phone number");
            return "";
        }

        final String value = this.sharedPreferencesContactData.getString(phoneNumber, null);
        if (value == null || value.isEmpty()) {
            Log.i(TAG, "Error finding the contact photo URL name for " + phoneNumber + ". No value stored");
            return "";
        }

        try {
            final String[] parts = value.split(";");
            if (parts.length < 2) {
                Log.i(TAG, "Error finding the contact photo URL name for " + phoneNumber + ". The stored value is wrong " + value + ". Contains " + parts.length + " parts.");
                return "";
            }
            return parts[1];
        } catch (Exception e) {
            Log.i(TAG, "Error finding the contact photo URL name for " + phoneNumber + ". Error: " + e.getMessage());
            return "";
        }
    }

    public String getDefaultDisplayName() {
        String value = this.sharedPreferencesContactData.getString(TwilioConstants.SHARED_PREFERENCES_KEY_DEFAULT_DISPLAY_NAME, "");
        if (value.isEmpty()) return "";
        return value;
    }

    public void storeAccess(String identity, String accessToken, String fcmToken) {
        SharedPreferences.Editor editor = this.sharedPreferencesAccess.edit();
        editor.putString(TwilioConstants.SHARED_PREFERENCES_KEY_IDENTITY, identity);
        editor.putString(TwilioConstants.SHARED_PREFERENCES_KEY_ACCESS_TOKEN, accessToken);
        editor.putString(TwilioConstants.SHARED_PREFERENCES_KEY_FCM_TOKEN, fcmToken);
        editor.apply();
    }

    public void clearAccess() {
        this.sharedPreferencesAccess.edit().clear().apply();
    }

    public String getAccessToken() {
        return this.sharedPreferencesAccess.getString(TwilioConstants.SHARED_PREFERENCES_KEY_ACCESS_TOKEN, null);
    }

    public String getFcmToken() {
        return this.sharedPreferencesAccess.getString(TwilioConstants.SHARED_PREFERENCES_KEY_FCM_TOKEN, null);
    }


    private int parseColor(SharedPreferences prefs, String key, int defaultColor) {
        try {
            String defaultHexColor = String.format("#%06X", (0xFFFFFF & defaultColor));
            String color = prefs.getString(key, defaultHexColor);
            return Color.parseColor(color);
        } catch (Exception exception) {
            exception.printStackTrace();
            return defaultColor;
        }
    }

    // Call background color
    public int getCallBackgroundColor() {
        int defaultColor = ResourcesCompat.getColor(
                this.context.getResources(),
                android.R.color.white,
                this.context.getTheme()
        );

        return parseColor(this.sharedPreferencesCallStyle, TwilioConstants.SHARED_PREFERENCES_KEY_CALL_BACKGROUND_COLOR, defaultColor);
    }

    public void storeCallBackgroundColor(String color) {
        SharedPreferences.Editor editor = this.sharedPreferencesCallStyle.edit();
        editor.putString(TwilioConstants.SHARED_PREFERENCES_KEY_CALL_BACKGROUND_COLOR, color);
        editor.apply();
    }

    public void clearCallBackgroundColor() {
        SharedPreferences.Editor editor = this.sharedPreferencesCallStyle.edit();
        editor.remove(TwilioConstants.SHARED_PREFERENCES_KEY_CALL_BACKGROUND_COLOR);
        editor.apply();
    }

    // Call text color
    public int getCallTextColor() {
        int defaultColor = ResourcesCompat.getColor(
                this.context.getResources(),
                android.R.color.black,
                this.context.getTheme()
        );

        return parseColor(
                this.sharedPreferencesCallStyle,
                TwilioConstants.SHARED_PREFERENCES_KEY_CALL_TEXT_COLOR,
                defaultColor
        );
    }

    public void storeCallTextColor(String color) {
        SharedPreferences.Editor editor = this.sharedPreferencesCallStyle.edit();
        editor.putString(TwilioConstants.SHARED_PREFERENCES_KEY_CALL_TEXT_COLOR, color);
        editor.apply();
    }

    public void clearCallTextColor() {
        SharedPreferences.Editor editor = this.sharedPreferencesCallStyle.edit();
        editor.remove(TwilioConstants.SHARED_PREFERENCES_KEY_CALL_TEXT_COLOR);
        editor.apply();
    }

    // Call button color
    public int getCallButtonColor() {
        int defaultColor = ResourcesCompat.getColor(
                this.context.getResources(),
                android.R.color.darker_gray,
                this.context.getTheme()
        );

        return parseColor(
                this.sharedPreferencesCallStyle,
                TwilioConstants.SHARED_PREFERENCES_KEY_CALL_BUTTON_COLOR,
                defaultColor
        );
    }

    public void storeCallButtonColor(String color) {
        SharedPreferences.Editor editor = this.sharedPreferencesCallStyle.edit();
        editor.putString(TwilioConstants.SHARED_PREFERENCES_KEY_CALL_BUTTON_COLOR, color);
        editor.apply();
    }

    public void clearCallButtonColor() {
        SharedPreferences.Editor editor = this.sharedPreferencesCallStyle.edit();
        editor.remove(TwilioConstants.SHARED_PREFERENCES_KEY_CALL_BUTTON_COLOR);
        editor.apply();
    }

    // Call button icon color
    public int getCallButtonIconColor() {
        int defaultColor = ResourcesCompat.getColor(
                this.context.getResources(),
                android.R.color.white,
                this.context.getTheme()
        );

        return parseColor(
                this.sharedPreferencesCallStyle,
                TwilioConstants.SHARED_PREFERENCES_KEY_CALL_BUTTON_ICON_COLOR,
                defaultColor
        );
    }

    public void storeCallButtonIconColor(String color) {
        SharedPreferences.Editor editor = this.sharedPreferencesCallStyle.edit();
        editor.putString(TwilioConstants.SHARED_PREFERENCES_KEY_CALL_BUTTON_ICON_COLOR, color);
        editor.apply();
    }

    public void clearCallButtonIconColor() {
        SharedPreferences.Editor editor = this.sharedPreferencesCallStyle.edit();
        editor.remove(TwilioConstants.SHARED_PREFERENCES_KEY_CALL_BUTTON_ICON_COLOR);
        editor.apply();
    }

    // Call button focus color
    public int getCallButtonFocusColor() {
        int defaultColor = ResourcesCompat.getColor(
                this.context.getResources(),
                android.R.color.holo_blue_dark,
                this.context.getTheme()
        );

        return parseColor(
                this.sharedPreferencesCallStyle,
                TwilioConstants.SHARED_PREFERENCES_KEY_CALL_BUTTON_FOCUS_COLOR,
                defaultColor
        );
    }

    public void storeCallButtonFocusColor(String color) {
        SharedPreferences.Editor editor = this.sharedPreferencesCallStyle.edit();
        editor.putString(TwilioConstants.SHARED_PREFERENCES_KEY_CALL_BUTTON_FOCUS_COLOR, color);
        editor.apply();
    }

    public void clearCallButtonFocusColor() {
        SharedPreferences.Editor editor = this.sharedPreferencesCallStyle.edit();
        editor.remove(TwilioConstants.SHARED_PREFERENCES_KEY_CALL_BUTTON_FOCUS_COLOR);
        editor.apply();
    }

    // Call button focus icon color
    public int getCallButtonFocusIconColor() {
        int defaultColor = ResourcesCompat.getColor(
                this.context.getResources(),
                android.R.color.white,
                this.context.getTheme()
        );

        return parseColor(
                this.sharedPreferencesCallStyle,
                TwilioConstants.SHARED_PREFERENCES_KEY_CALL_BUTTON_FOCUS_ICON_COLOR,
                defaultColor
        );
    }

    public void storeCallButtonFocusIconColor(String color) {
        SharedPreferences.Editor editor = this.sharedPreferencesCallStyle.edit();
        editor.putString(TwilioConstants.SHARED_PREFERENCES_KEY_CALL_BUTTON_FOCUS_ICON_COLOR, color);
        editor.apply();
    }

    public void clearCallButtonFocusIconColor() {
        SharedPreferences.Editor editor = this.sharedPreferencesCallStyle.edit();
        editor.remove(TwilioConstants.SHARED_PREFERENCES_KEY_CALL_BUTTON_FOCUS_ICON_COLOR);
        editor.apply();
    }
}
