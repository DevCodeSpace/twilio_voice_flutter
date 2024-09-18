package com.twilio.voice.flutter.Utils;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.twilio.voice.CallInvite;


import java.util.Map;

import com.twilio.voice.flutter.BackgroundCallJavaActivity;
import com.twilio.voice.flutter.IncomingCallNotificationService;
import com.twilio.voice.flutter.R;

public class NotificationUtils {

    public static Notification createIncomingCallNotification(Context context, CallInvite callInvite, boolean showHeadsUp) {
        if (callInvite == null) return null;

        String fromDisplayName = null;
        for (Map.Entry<String, String> entry : callInvite.getCustomParameters().entrySet()) {
            if (entry.getKey().equals("fromDisplayName")) {
                fromDisplayName = entry.getValue();
            }
        }
        if (fromDisplayName == null || fromDisplayName.trim().isEmpty()) {
            final String contactName = PreferencesUtils.getInstance(context).findContactName(callInvite.getFrom());
            if (contactName != null && !contactName.trim().isEmpty()) {
                fromDisplayName = contactName;
            } else {
                fromDisplayName = "Unknown name";
            }
        }

        String notificationTitle = context.getString(R.string.notification_incoming_call_title);
        String notificationText = fromDisplayName;

        /*
         * Pass the notification id and call sid to use as an identifier to cancel the
         * notification later
         */
        Bundle extras = new Bundle();
        extras.putString(TwilioConstants.CALL_SID_KEY, callInvite.getCallSid());

        // Click intent
        Intent intent = new Intent(context, BackgroundCallJavaActivity.class);
        intent.setAction(TwilioConstants.ACTION_INCOMING_CALL);
        intent.putExtra(TwilioConstants.EXTRA_INCOMING_CALL_INVITE, callInvite);
        intent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                        Intent.FLAG_ACTIVITY_MULTIPLE_TASK |
                        Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
        );

        @SuppressLint("UnspecifiedImmutableFlag")
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT| PendingIntent.FLAG_IMMUTABLE
        );


        //Reject intent
        Intent rejectIntent = new Intent(context, IncomingCallNotificationService.class);
        rejectIntent.setAction(TwilioConstants.ACTION_REJECT);
        rejectIntent.putExtra(TwilioConstants.EXTRA_INCOMING_CALL_INVITE, callInvite);
        @SuppressLint("UnspecifiedImmutableFlag")
        PendingIntent piRejectIntent = PendingIntent.getService(
                context,
                0,
                rejectIntent,
                PendingIntent.FLAG_UPDATE_CURRENT| PendingIntent.FLAG_IMMUTABLE
        );

        // Accept intent
        Intent acceptIntent = new Intent(context, BackgroundCallJavaActivity.class);
        acceptIntent.setAction(TwilioConstants.ACTION_ACCEPT);
        acceptIntent.putExtra(TwilioConstants.EXTRA_INCOMING_CALL_INVITE, callInvite);
        @SuppressLint("UnspecifiedImmutableFlag")
        PendingIntent piAcceptIntent = PendingIntent.getActivity(
                context,
                0,
                acceptIntent,
                PendingIntent.FLAG_UPDATE_CURRENT| PendingIntent.FLAG_IMMUTABLE
        );

        // Notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, createChannel(context, showHeadsUp));
        builder.setSmallIcon(R.drawable.ic_phone_call);
        builder.setContentTitle(notificationTitle);
        builder.setContentText(notificationText);
        builder.setCategory(NotificationCompat.CATEGORY_CALL);
        builder.setAutoCancel(true);
        builder.setExtras(extras);
        builder.setVibrate(new long[]{0, 400, 400, 400, 400, 400, 400, 400});
        builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        builder.addAction(android.R.drawable.ic_menu_delete, context.getString(R.string.btn_reject), piRejectIntent);
        builder.addAction(android.R.drawable.ic_menu_call, context.getString(R.string.btn_accept), piAcceptIntent);
        builder.setFullScreenIntent(pendingIntent, true);
        builder.setColor(Color.rgb(20, 10, 200));
        builder.setOngoing(true);
        builder.setPriority(NotificationCompat.PRIORITY_MAX);
        builder.setContentIntent(pendingIntent);
        return builder.build();
    }

    private static String createChannel(Context context, boolean highPriority) {
        String id = highPriority ? TwilioConstants.VOICE_CHANNEL_HIGH_IMPORTANCE : TwilioConstants.VOICE_CHANNEL_LOW_IMPORTANCE;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel;
            if (highPriority) {
                channel = new NotificationChannel(
                        TwilioConstants.VOICE_CHANNEL_HIGH_IMPORTANCE,
                        "Bivo high importance notification call channel",
                        NotificationManager.IMPORTANCE_HIGH
                );
            } else {
                channel = new NotificationChannel(
                        TwilioConstants.VOICE_CHANNEL_LOW_IMPORTANCE,
                        "Bivo low importance notification call channel",
                        NotificationManager.IMPORTANCE_LOW
                );
            }
            channel.setLightColor(Color.GREEN);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }

        return id;
    }

    public static void cancel(Context context, int id) {
        NotificationManagerCompat.from(context).cancel(id);
    }
}
