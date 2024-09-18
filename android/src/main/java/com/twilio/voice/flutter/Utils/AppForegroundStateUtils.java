package com.twilio.voice.flutter.Utils;

public class AppForegroundStateUtils {


    private static AppForegroundStateUtils instance;
    private Boolean foreground = false;

    private AppForegroundStateUtils() {

    }

    public static AppForegroundStateUtils getInstance() {
        if (instance == null) {
            instance = new AppForegroundStateUtils();
        }

        return instance;
    }


    public void setForeground(Boolean foreground) {
        this.foreground = foreground;
    }

    public Boolean isForeground() {
        return foreground;
    }
}
