package com.twilio.voice.flutter;

import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.twilio.voice.CallInvite;
import com.twilio.voice.CancelledCallInvite;

import java.util.List;

import com.twilio.voice.flutter.Utils.AppForegroundStateUtils;
import com.twilio.voice.flutter.Utils.NotificationUtils;
import com.twilio.voice.flutter.Utils.SoundUtils;
import com.twilio.voice.flutter.Utils.TwilioConstants;
import com.twilio.voice.flutter.Utils.TwilioUtils;

public class IncomingCallNotificationService extends Service {

    private static final String TAG = IncomingCallNotificationService.class.getSimpleName();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        Log.i(TAG, "onStartCommand " + action);
        if (action != null) {
            switch (action) {
                case TwilioConstants.ACTION_INCOMING_CALL: {
                    CallInvite callInvite = intent.getParcelableExtra(TwilioConstants.EXTRA_INCOMING_CALL_INVITE);
                    handleIncomingCall(callInvite);
                }
                break;

                case TwilioConstants.ACTION_ACCEPT: {
                    CallInvite callInvite = intent.getParcelableExtra(TwilioConstants.EXTRA_INCOMING_CALL_INVITE);
                    accept(callInvite);
                }
                break;
                case TwilioConstants.ACTION_REJECT: {
                    CallInvite callInvite = intent.getParcelableExtra(TwilioConstants.EXTRA_INCOMING_CALL_INVITE);
                    reject(callInvite);
                }
                break;
                case TwilioConstants.ACTION_CANCEL_CALL: {
                    CancelledCallInvite cancelledCallInvite = intent.getParcelableExtra(TwilioConstants.EXTRA_CANCELLED_CALL_INVITE);
                    handleCancelledCall(cancelledCallInvite);
                }
                break;

                case TwilioConstants.ACTION_STOP_SERVICE: {
                    stopServiceIncomingCall();
                }
                break;
            }
        }
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void handleIncomingCall(CallInvite callInvite) {
        if (callInvite == null) {
            Log.i(TAG, "Incoming call. No call invite");
            return;
        }

        Log.i(TAG, "Incoming call. App visible: " + isAppVisible() + ". Locked: " + isLocked());

        if (TwilioUtils.getInstance(this).getActiveCall() != null) {
            Log.i(TAG, "Incoming call. There is already an active call");
            return;
        }

        this.startServiceIncomingCall(callInvite);
    }

    private void accept(CallInvite callInvite) {
        Log.i(TAG, "Accept call invite. App visible: " + isAppVisible() + ". Locked: " + isLocked());
        this.stopServiceIncomingCall();

        if (!isLocked() && isAppVisible()) {
            // Inform call accepted
            Log.i(TAG, "Answering from APP");
            this.informAppAcceptCall(callInvite);
        } else {
            Log.i(TAG, "Answering from custom UI");
            this.openBackgroundCallActivityForAcceptCall(callInvite);
        }
    }

    private void reject(CallInvite callInvite) {
        Log.i(TAG, "Reject call invite. App visible: " + isAppVisible() + ". Locked: " + isLocked());
        this.stopServiceIncomingCall();

        // Reject call
        try {
            TwilioUtils.getInstance(this).rejectInvite(callInvite);
        } catch (Exception exception) {
            Log.e(TAG, "Exception occurred during reject: ", exception);
        }
    }

    private void handleCancelledCall(CancelledCallInvite cancelledCallInvite) {
        Log.i(TAG, "Call canceled. App visible: " + isAppVisible() + ". Locked: " + isLocked());
        this.stopServiceIncomingCall();

        if (cancelledCallInvite == null) return;
        if (cancelledCallInvite.getFrom() == null) return;

        Log.i(TAG, "From: " + cancelledCallInvite.getFrom() + ". To: " + cancelledCallInvite.getTo());
        this.informAppCancelCall();
    }

    private void startServiceIncomingCall(CallInvite callInvite) {
        Log.i(TAG, "Start service incoming call");
        SoundUtils.getInstance(this).playRinging();
        Notification notification = NotificationUtils.createIncomingCallNotification(getApplicationContext(), callInvite, true);
        startForeground(TwilioConstants.NOTIFICATION_INCOMING_CALL, notification);
    }

    private void stopServiceIncomingCall() {
        Log.i(TAG, "Stop service incoming call");
        stopForeground(true);
        NotificationUtils.cancel(this, TwilioConstants.NOTIFICATION_INCOMING_CALL);
        SoundUtils.getInstance(this).stopRinging();
    }

    private boolean isLocked() {
        KeyguardManager myKM = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        return myKM.inKeyguardRestrictedInputMode();
    }

    private boolean isAppVisible() {
        return AppForegroundStateUtils.getInstance().isForeground();
    }

    // UTILS

    private void informAppAcceptCall(CallInvite callInvite) {
        Intent intent = new Intent();
        intent.putExtra(TwilioConstants.EXTRA_INCOMING_CALL_INVITE, callInvite);
        intent.setAction(TwilioConstants.ACTION_ACCEPT);
        this.sendBroadcast(intent);
//        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void informAppCancelCall() {
        Intent intent = new Intent();
        intent.setAction(TwilioConstants.ACTION_CANCEL_CALL);
        this.sendBroadcast(intent);
//        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void openBackgroundCallActivityForAcceptCall(CallInvite callInvite) {
        Intent intent = new Intent(this,BackgroundCallJavaActivity.class);
        intent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                        Intent.FLAG_ACTIVITY_MULTIPLE_TASK |
                        Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
        );
        intent.putExtra(TwilioConstants.EXTRA_INCOMING_CALL_INVITE, callInvite);
        intent.setAction(TwilioConstants.ACTION_ACCEPT);
        startActivity(intent);
    }
}
