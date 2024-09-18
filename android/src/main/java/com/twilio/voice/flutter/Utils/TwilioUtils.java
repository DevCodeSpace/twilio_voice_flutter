package com.twilio.voice.flutter.Utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AudioManager;
import android.util.Log;

import androidx.annotation.NonNull;

import com.twilio.voice.Call;
import com.twilio.voice.CallException;
import com.twilio.voice.CallInvite;
import com.twilio.voice.ConnectOptions;
import com.twilio.voice.RegistrationException;
import com.twilio.voice.RegistrationListener;
import com.twilio.voice.UnregistrationListener;
import com.twilio.voice.Voice;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class TwilioUtils {
    private static final String TAG = "TwilioUtils";

    @SuppressLint("StaticFieldLeak")
    private static TwilioUtils instance;

    public static TwilioUtils getInstance(Context context) {
        if (instance == null) {
            instance = new TwilioUtils();
        }

        instance.context = context;
        return instance;
    }

    private Call activeCall;
    private CallInvite callInvite;
    private String fromDisplayName;
    private String toDisplayName;
    private Context context;
    private String status;

    public void register(String identity, String accessToken, String fcmToken, TwilioRegistrationListener listener) {
        PreferencesUtils.getInstance(this.context).storeAccess(identity, accessToken, fcmToken);
        Voice.register(accessToken, Voice.RegistrationChannel.FCM, fcmToken, new RegistrationListener() {
            @Override
            public void onRegistered(@NonNull String accessToken, @NonNull String fcmToken) {
                Log.d(TAG, "Successfully registered");
                if (listener != null) {
                    listener.onRegistered();
                }
            }

            @Override
            public void onError(@NonNull RegistrationException error, @NonNull String accessToken, @NonNull String fcmToken
            ) {
                String message = String.format(
                        Locale.US,
                        "Registration Error: %d, %s",
                        error.getErrorCode(),
                        error.getMessage());

                Log.d(TAG, "Error registering. " + message);

                if (listener != null) {
                    listener.onError();
                }

            }
        });
    }

    public void unregister() {
        String accessToken = PreferencesUtils.getInstance(this.context).getAccessToken();
        if (accessToken == null) return;

        String fcmToken = PreferencesUtils.getInstance(this.context).getFcmToken();
        if (fcmToken == null) return;

        PreferencesUtils.getInstance(this.context).clearAccess();
        Voice.unregister(accessToken, Voice.RegistrationChannel.FCM, fcmToken, new UnregistrationListener() {
            @Override
            public void onUnregistered(String s, String s1) {
                Log.d(TAG, "Successfully unregistered");
            }

            @Override
            public void onError(RegistrationException error, String accessToken, String fcmToken) {
                String message = String.format(
                        Locale.US,
                        "Registration Error: %d, %s",
                        error.getErrorCode(),
                        error.getMessage());

                Log.d(TAG, "Error unregistering. " + message);
            }
        });
    }

    public void makeCall(String to, Map<String, Object> data, Call.Listener listener) {
        if (this.activeCall != null) {
            throw new RuntimeException("There is a call in progress");
        }

        String accessToken = PreferencesUtils.getInstance(this.context).getAccessToken();
        if (accessToken == null) {
            throw new RuntimeException("No access token");
        }

        HashMap<String, String> params = new HashMap<>();
        params.put("To", to);
        if (data != null) {
            for (Map.Entry<String, Object> entry : data.entrySet()) {
                params.put(entry.getKey(), entry.getValue().toString());
            }
        }

        ConnectOptions connectOptions = new ConnectOptions.Builder(accessToken)
                .params(params)
                .build();


        String fromDisplayName = null;
        String toDisplayName = null;
        if (data != null && data.containsKey("fromDisplayName")) {
            if (data.containsKey("fromDisplayName") && data.get("fromDisplayName") != null) {
                fromDisplayName = Objects.requireNonNull(data.get("fromDisplayName")).toString();
            }

            if (data.containsKey("toDisplayName") && data.get("toDisplayName") != null) {
                toDisplayName = Objects.requireNonNull(data.get("toDisplayName")).toString();
            }
        }

        this.status = "callConnecting";
        this.callInvite = null;
        this.fromDisplayName = fromDisplayName;
        this.toDisplayName = toDisplayName;
        this.activeCall = Voice.connect(this.context, connectOptions, getCallListener(listener));
    }

    public void sendDigits(String digit){
        if (this.activeCall != null) {
            Log.i(TAG, "sending digit: " + digit);
            this.activeCall.sendDigits(digit);
            Log.i(TAG, "digit sent: ");
        }else   {
            Log.i(TAG, "Error sending digits, no active call");
        }
    }

    public void acceptInvite(CallInvite callInvite, Call.Listener listener) {
        if (this.activeCall != null) {
            throw new RuntimeException("There is a call in progress");
        }

        if (callInvite == null) {
            throw new RuntimeException("No call invite");
        }

        this.status = "callConnecting";
        this.fromDisplayName = null;
        this.toDisplayName = null;
        this.callInvite = callInvite;
        this.activeCall = callInvite.accept(this.context, getCallListener(listener));
    }

    public void rejectInvite(CallInvite callInvite) {
        if (callInvite == null) {
            throw new RuntimeException("No call invite");
        }

        callInvite.reject(this.context);
        this.activeCall = null;
        this.callInvite = null;
        this.fromDisplayName = null;
        this.toDisplayName = null;
        SoundUtils.getInstance(this.context).playDisconnect();
    }

    public void disconnect() {
        this.status = "callDisconnected";
        if (this.activeCall != null) {
            this.activeCall.disconnect();
            SoundUtils.getInstance(this.context).playDisconnect();
        }

        this.callInvite = null;
        this.activeCall = null;
        this.fromDisplayName = null;
        this.toDisplayName = null;
    }

    public boolean toggleMute() {
        if (this.activeCall == null) {
            throw new RuntimeException("No active call");
        }

        boolean mute = !this.activeCall.isMuted();
        this.activeCall.mute(mute);
        return mute;
    }

    public boolean isMuted() {
        if (this.activeCall == null) {
            throw new RuntimeException("No active call");
        }

        return this.activeCall.isMuted();
    }

    public boolean toggleSpeaker() {
        if (this.activeCall == null) {
            throw new RuntimeException("No active call");
        }

        AudioManager audioManager = (AudioManager) this.context.getSystemService(Context.AUDIO_SERVICE);
        boolean isSpeaker = !audioManager.isSpeakerphoneOn();
        audioManager.setSpeakerphoneOn(isSpeaker);
        return isSpeaker;
    }

    public void setSpeaker(boolean speaker) {
        if (this.activeCall == null) {
            throw new RuntimeException("No active call");
        }

        AudioManager audioManager = (AudioManager) this.context.getSystemService(Context.AUDIO_SERVICE);
        audioManager.setSpeakerphoneOn(speaker);
    }

    public boolean isSpeaker() {
        AudioManager audioManager = (AudioManager) this.context.getSystemService(Context.AUDIO_SERVICE);
        return audioManager.isSpeakerphoneOn();
    }

    public Call getActiveCall() {
        return this.activeCall;
    }

    public HashMap<String, Object> getCallDetails() {
        HashMap<String, Object> map = new HashMap<>();

        if (this.activeCall == null) {
            map.put("id", "");
            map.put("mute", false);
            map.put("speaker", false);
        } else {
            map.put("id", activeCall.getSid());
            map.put("mute", isMuted());
            map.put("speaker", isSpeaker());
        }

        if (this.callInvite != null) {
            map.put("customParameters", this.callInvite.getCustomParameters());
        }

        map.put("fromDisplayName", this.getFromDisplayName());
        map.put("toDisplayName", this.getToDisplayName());
        map.put("outgoing", this.callInvite == null);
        map.put("status", this.status);
        return map;
    }

    private String getFromDisplayName() {
        String result = null;

        if (this.callInvite != null) {
            for (Map.Entry<String, String> entry : callInvite.getCustomParameters().entrySet()) {
                if (entry.getKey().equals("fromDisplayName")) {
                    result = entry.getValue();
                }
            }

            if (result == null || result.trim().isEmpty()) {
                final String contactName = PreferencesUtils.getInstance(context).findContactName(this.callInvite.getFrom());
                if (contactName != null && !contactName.trim().isEmpty()) {
                    result = contactName;
                }
            }
        } else {
            result = this.fromDisplayName;
        }

        if (result == null || result.trim().isEmpty()) {
            result = "Unknown name";
        }

        return result;
    }

    private String getToDisplayName() {
        String result = null;

        if (this.callInvite != null) {
            for (Map.Entry<String, String> entry : callInvite.getCustomParameters().entrySet()) {
                if (entry.getKey().equals("toDisplayName")) {
                    result = entry.getValue();
                }
            }

            if (result == null || result.trim().isEmpty()) {
                final String contactName = PreferencesUtils.getInstance(context).findContactName(this.callInvite.getTo());
                if (contactName != null && !contactName.trim().isEmpty()) {
                    result = contactName;
                }
            }
        } else {
            result = this.toDisplayName;
        }

        if (result == null || result.trim().isEmpty()) {
            result = "Unknown name";
        }

        return result;
    }


    public String getCallStatus() {
        if (this.activeCall == null) return null;
        return this.status;
    }

    private Call.Listener getCallListener(Call.Listener listener) {
        return new Call.Listener() {
            @Override
            public void onConnectFailure(@NonNull Call call, @NonNull CallException e) {
                e.printStackTrace();

                Log.i(TAG, "onConnectFailure. Error: " + e.getMessage());

                status = "callDisconnected";
                if (listener != null) {
                    listener.onConnectFailure(call, e);
                }

                activeCall = null;
            }

            @Override
            public void onRinging(@NonNull Call call) {
                Log.i(TAG, "onRinging");
                status = "callRinging";
                activeCall = call;

                if (listener != null) {
                    listener.onRinging(call);
                }
            }

            @Override
            public void onConnected(@NonNull Call call) {
                Log.i(TAG, "onConnected");
                activeCall = call;
                status = "callConnected";

                if (listener != null) {
                    listener.onConnected(call);
                }
            }

            @Override
            public void onReconnecting(@NonNull Call call, @NonNull CallException e) {
                e.printStackTrace();

                Log.i(TAG, "onReconnecting. Error: " + e.getMessage());
                activeCall = call;
                status = "callReconnecting";

                if (listener != null) {
                    listener.onReconnecting(call, e);
                }
            }

            @Override
            public void onReconnected(@NonNull Call call) {
                Log.i(TAG, "onReconnected");
                activeCall = call;
                status = "callReconnected";

                if (listener != null) {
                    listener.onReconnected(call);
                }
            }

            @Override
            public void onDisconnected(@NonNull Call call, CallException e) {
                if (e != null) {
                    e.printStackTrace();
                    Log.i(TAG, "onDisconnected. Error: " + e.getMessage());
                } else {
                    Log.i(TAG, "onDisconnected");
                }
                status = "callDisconnected";

                if (listener != null) {
                    listener.onDisconnected(call, e);
                }

                activeCall = null;
            }

            @Override
            public void onCallQualityWarningsChanged(
                    @NonNull Call call,
                    @NonNull Set<Call.CallQualityWarning> currentWarnings,
                    @NonNull Set<Call.CallQualityWarning> previousWarnings
            ) {
                Log.i(TAG, "onCallQualityWarningsChanged");
                activeCall = call;

                if (listener != null) {
                    listener.onCallQualityWarningsChanged(call, currentWarnings, previousWarnings);
                }
            }
        };
    }


}
