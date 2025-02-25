package com.twilio.voice.flutter;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ProcessLifecycleOwner;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.twilio.voice.CallInvite;
import com.twilio.voice.flutter.Utils.SoundUtils;
import com.twilio.voice.flutter.Utils.TwilioConstants;

public class IncomingCallNotificationService extends Service {

    private static final String TAG = IncomingCallNotificationService.class.getSimpleName();
    private SoundUtils soundUtils;

    @Override
    public void onCreate() {
        super.onCreate();
        soundUtils = SoundUtils.getInstance(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();

        if (action != null) {
            CallInvite callInvite = intent.getParcelableExtra(TwilioConstants.EXTRA_INCOMING_CALL_INVITE);
            Log.e(TAG, action);
            // if (callInvite == null) {
            //
            // Log.e(TAG, "Received null CallInvite");
            // return START_NOT_STICKY;
            // }
            int notificationId = intent.getIntExtra(TwilioConstants.INCOMING_CALL_NOTIFICATION_ID, 0);
            switch (action) {
                case TwilioConstants.ACTION_INCOMING_CALL:
                    if (callInvite == null) {
                        return START_NOT_STICKY;
                    }
                    handleIncomingCall(intent, callInvite, notificationId);
                    break;
                case TwilioConstants.ACTION_ACCEPT:
                    if (callInvite == null) {
                        return START_NOT_STICKY;
                    }
                    accept(callInvite, notificationId);
                    break;
                case TwilioConstants.ACTION_REJECT:
                    if (callInvite == null) {
                        return START_NOT_STICKY;
                    }
                    reject(callInvite, notificationId);
                    break;
                case TwilioConstants.ACTION_CANCEL_CALL:
                    handleCancelledCall(intent, notificationId);
                    break;
                default:
                    break;
            }
        }
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private Notification createNotification(CallInvite callInvite, int notificationId) {
        Intent intent = new Intent(this, NotificationProxyActivity.class);
        intent.setAction(TwilioConstants.ACTION_INCOMING_CALL_NOTIFICATION);
        intent.putExtra(TwilioConstants.INCOMING_CALL_NOTIFICATION_ID, notificationId);
        intent.putExtra(TwilioConstants.INCOMING_CALL_INVITE, callInvite);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, notificationId, intent,
                PendingIntent.FLAG_IMMUTABLE);

        Bundle extras = new Bundle();
        extras.putString(TwilioConstants.CALL_SID_KEY, callInvite.getCallSid());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return buildNotification(callInvite.getFrom(),
                    pendingIntent,
                    extras,
                    callInvite,
                    notificationId,
                    createChannel());
        } else {
            return new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.ic_phone_call)
                    .setContentTitle(getString(R.string.notification_incoming_call_title))
                    .setContentText(callInvite.getFrom() + " is calling.")
                    .setAutoCancel(true)
                    .setExtras(extras)
                    .setContentIntent(pendingIntent)
                    .setGroup("test_app_notification")
                    .setCategory(Notification.CATEGORY_CALL)
                    .setColor(Color.rgb(214, 10, 37)).build();
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    private Notification buildNotification(String text, PendingIntent pendingIntent, Bundle extras,
            final CallInvite callInvite, int notificationId, String channelId) {
        Log.d(TAG, "Creating notification for call: " + callInvite.getCallSid());

        // Create intent for reject action
        Intent rejectIntent = new Intent(getApplicationContext(), NotificationProxyActivity.class);
        rejectIntent.setAction(TwilioConstants.ACTION_REJECT);
        rejectIntent.putExtra(TwilioConstants.EXTRA_INCOMING_CALL_INVITE, callInvite);
        rejectIntent.putExtra(TwilioConstants.INCOMING_CALL_NOTIFICATION_ID, notificationId);
        PendingIntent piRejectIntent;
        piRejectIntent = PendingIntent.getActivity(this, 0, rejectIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Intent acceptIntent = new Intent(getApplicationContext(), NotificationProxyActivity.class);
        acceptIntent.setAction(TwilioConstants.ACTION_ACCEPT);
        acceptIntent.putExtra(TwilioConstants.EXTRA_INCOMING_CALL_INVITE, callInvite);
        acceptIntent.putExtra(TwilioConstants.INCOMING_CALL_NOTIFICATION_ID, notificationId);
        acceptIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent piAcceptIntent;
        piAcceptIntent = PendingIntent.getActivity(this, 0, acceptIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Notification.Builder builder = new Notification.Builder(getApplicationContext(), channelId)
                .setSmallIcon(R.drawable.ic_phone_call)
                .setContentTitle(getString(R.string.notification_incoming_call_title))
                .setContentText(text)
                .setCategory(Notification.CATEGORY_CALL)
                .setExtras(extras)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .addAction(android.R.drawable.ic_menu_call, getString(R.string.btn_reject), piRejectIntent)
                .addAction(android.R.drawable.ic_menu_close_clear_cancel, getString(R.string.btn_accept),
                        piAcceptIntent)
                .setFullScreenIntent(pendingIntent, true);

        // Get the notification manager and notify
        Notification notification = builder.build();
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(notificationId, notification);

        return notification;
    }

    @TargetApi(Build.VERSION_CODES.O)
    private String createChannel() {
        NotificationChannel callInviteChannel = new NotificationChannel(TwilioConstants.VOICE_CHANNEL_HIGH_IMPORTANCE,
                "Primary Voice Channel", NotificationManager.IMPORTANCE_HIGH);
        String channelId = TwilioConstants.VOICE_CHANNEL_HIGH_IMPORTANCE;

        callInviteChannel.setLightColor(Color.GREEN);
        callInviteChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(callInviteChannel);
        Log.d(TAG, "Created notification channel: " + channelId);

        return channelId;
    }

    private void accept(CallInvite callInvite, int notificationId) {
        if (callInvite != null) {

            callInvite.accept(this, TwilioVoiceFlutterPlugin.callListener);
            Log.d(TAG, "Accepted call invite: " + callInvite.getCallSid());
            // Dismiss the notification
            NotificationManager notificationManager = (NotificationManager) getSystemService(
                    Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(notificationId);

            Log.d(TAG, "Starting activity");
            // Start the main activity of your app
            Intent launchIntent = getPackageManager().getLaunchIntentForPackage(getPackageName());
            if (launchIntent != null) {
                launchIntent.setAction(TwilioConstants.ACTION_ACCEPT);
                launchIntent.putExtra(TwilioConstants.EXTRA_INCOMING_CALL_INVITE, callInvite);
                launchIntent.putExtra(TwilioConstants.INCOMING_CALL_NOTIFICATION_ID, notificationId);
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(launchIntent);
            } else {
                Log.e(TAG, "Unable to find launch intent for package: " + getPackageName());
            }

            Log.d(TAG, "Broadcasting accept action");
            TwilioVoiceFlutterPlugin.activeCallInvite = callInvite;
            // Broadcast the accept action
            Intent acceptIntent = new Intent(TwilioConstants.ACTION_ACCEPT);
            acceptIntent.putExtra(TwilioConstants.INCOMING_CALL_INVITE, callInvite);
            LocalBroadcastManager.getInstance(this).sendBroadcast(acceptIntent);

            Log.d(TAG, "Broadcasting end foreground");

            // Stop the service after call accepted
            endForeground();
            stopService(new Intent(getApplicationContext(), IncomingCallNotificationService.class));
        } else {
            Log.e(TAG, "CallInvite is null or expired.");
        }
    }

    private void reject(CallInvite callInvite, int notificationId) {
        callInvite.reject(this);
        endForeground();
        TwilioVoiceFlutterPlugin.activeCallInvite = null;
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(notificationId);
        soundUtils.stopRinging();
    }

    private void handleCancelledCall(Intent intent, int notificationId) {
        endForeground();
        TwilioVoiceFlutterPlugin.activeCallInvite = null;
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(notificationId);
        soundUtils.stopRinging();
    }

    private void handleIncomingCall(Intent intent, CallInvite callInvite, int notificationId) {
        soundUtils.playRinging();
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            setCallInProgressNotification(callInvite, notificationId);
        }
    }

    private void endForeground() {
        stopForeground(true);
        soundUtils.stopRinging();
    }

    @TargetApi(Build.VERSION_CODES.O)
    private void setCallInProgressNotification(CallInvite callInvite, int notificationId) {
        if (isAppVisible()) {
            Log.i(TAG, "setCallInProgressNotification - app is visible.");
            Notification notification = createNotification(callInvite, notificationId
            );
            startForeground(notificationId, notification);
        } else {
            Log.i(TAG, "setCallInProgressNotification - app is NOT visible.");
            Notification notification = createNotification(callInvite, notificationId
            );
            startForeground(notificationId, notification);
        }
    }

    private boolean isAppVisible() {
        return ProcessLifecycleOwner
                .get()
                .getLifecycle()
                .getCurrentState()
                .isAtLeast(Lifecycle.State.STARTED);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        soundUtils.stopRinging();
    }
}