package com.twilio.voice.flutter.fcm;

import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.twilio.voice.CallException;
import com.twilio.voice.CallInvite;
import com.twilio.voice.CancelledCallInvite;
import com.twilio.voice.MessageListener;
import com.twilio.voice.Voice;

import com.twilio.voice.flutter.IncomingCallNotificationService;
import com.twilio.voice.flutter.Utils.TwilioConstants;


public class VoiceFirebaseMessagingService extends FirebaseMessagingService {

    public static final String ACTION_TOKEN = "io.flutter.plugins.firebase.messaging.TOKEN";
    public static final String EXTRA_TOKEN = "token";

    private static final String TAG = "FlutterFcmService";

    @Override
    public void onNewToken(@NonNull String token) {
        Intent onMessageIntent = new Intent(ACTION_TOKEN);
        onMessageIntent.putExtra(EXTRA_TOKEN, token);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(onMessageIntent);
    }

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    @Override
    public void onMessageReceived(final RemoteMessage remoteMessage) {
        Log.d(TAG, "Received onMessageReceived()");
        Log.d(TAG, "Bundle data: " + remoteMessage.getData());
        Log.d(TAG, "From: " + remoteMessage.getFrom());
        // If application is running in the foreground use local broadcast to handle message.
        // Otherwise use the background isolate to handle message.

        if (!remoteMessage.getData().isEmpty()) {
            boolean valid = Voice.handleMessage(this, remoteMessage.getData(), new MessageListener() {
                @Override
                public void onCallInvite(@NonNull CallInvite callInvite) {
                    handleInvite(callInvite);
                }

                @Override
                public void onCancelledCallInvite(@NonNull CancelledCallInvite cancelledCallInvite, @Nullable CallException callException) {
                    handleCanceledCallInvite(cancelledCallInvite);
                }
            });

            if (!valid) {
                Log.e(TAG, "The message was not a valid Twilio Voice SDK payload: " + remoteMessage.getData());
                notificationReceived(remoteMessage);
            }
        }

    }

    // Override if you also receive notifications from other plugins
    public void notificationReceived(final RemoteMessage remoteMessage) {
    }

    private void handleInvite(CallInvite callInvite) {
        Intent intent = new Intent(this, IncomingCallNotificationService.class);
        intent.setAction(TwilioConstants.ACTION_INCOMING_CALL);
        intent.putExtra(TwilioConstants.EXTRA_INCOMING_CALL_INVITE, callInvite);

        startService(intent);
    }

    private void handleCanceledCallInvite(CancelledCallInvite cancelledCallInvite) {
        Intent intent = new Intent(this, IncomingCallNotificationService.class);
        intent.setAction(TwilioConstants.ACTION_CANCEL_CALL);
        intent.putExtra(TwilioConstants.EXTRA_CANCELLED_CALL_INVITE, cancelledCallInvite);
        startService(intent);
    }
}



