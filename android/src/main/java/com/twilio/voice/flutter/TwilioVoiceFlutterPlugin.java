//package com.twilio.voice.flutter;
//
//import androidx.annotation.NonNull;
//
//import io.flutter.embedding.engine.plugins.FlutterPlugin;
//import io.flutter.plugin.common.MethodCall;
//import io.flutter.plugin.common.MethodChannel;
//import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
//import io.flutter.plugin.common.MethodChannel.Result;
//
///** TwilioVoiceFlutterPlugin */
//public class TwilioVoiceFlutterPlugin implements FlutterPlugin, MethodCallHandler {
//  /// The MethodChannel that will the communication between Flutter and native Android
//  ///
//  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
//  /// when the Flutter Engine is detached from the Activity
//  private MethodChannel channel;
//
//  @Override
//  public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
//    channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "twilio_voice_flutter");
//    channel.setMethodCallHandler(this);
//  }
//
//  @Override
//  public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
//    if (call.method.equals("getPlatformVersion")) {
//      result.success("Android " + android.os.Build.VERSION.RELEASE);
//    } else {
//      result.notImplemented();
//    }
//  }
//
//  @Override
//  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
//    channel.setMethodCallHandler(null);
//  }
//}

package com.twilio.voice.flutter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;

import com.twilio.voice.Call;
import com.twilio.voice.CallException;
import com.twilio.voice.CallInvite;

import java.util.Map;
import java.util.Set;

import com.twilio.voice.flutter.Utils.AppForegroundStateUtils;
import com.twilio.voice.flutter.Utils.PreferencesUtils;
import com.twilio.voice.flutter.Utils.TwilioConstants;
import com.twilio.voice.flutter.Utils.TwilioRegistrationListener;
import com.twilio.voice.flutter.Utils.TwilioUtils;
import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry;

public class TwilioVoiceFlutterPlugin implements
        FlutterPlugin,
        MethodChannel.MethodCallHandler,
        ActivityAware,
        PluginRegistry.NewIntentListener {

  private static final String TAG = "TwilioVoiceFlutter";

  private Context context;
  private MethodChannel responseChannel;
  private CustomBroadcastReceiver broadcastReceiver;
  private boolean broadcastReceiverRegistered = false;

  public TwilioVoiceFlutterPlugin() {
  }

  private void setupMethodChannel(BinaryMessenger messenger, Context context) {
    this.context = context;
    MethodChannel channel = new MethodChannel(messenger, "twilio_voice_flutter");
    channel.setMethodCallHandler(this);
    this.responseChannel = new MethodChannel(messenger, "twilio_voice_flutter_response");
  }

  private void registerReceiver() {
    if (!this.broadcastReceiverRegistered) {
      this.broadcastReceiverRegistered = true;

      Log.i(TAG, "Registered broadcast");
      this.broadcastReceiver = new CustomBroadcastReceiver(this);
      IntentFilter intentFilter = new IntentFilter();
      intentFilter.addAction(TwilioConstants.ACTION_ACCEPT);
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
          this.context.registerReceiver(this.broadcastReceiver, intentFilter, Context.RECEIVER_NOT_EXPORTED);
        }
      }
    }
  }

  private void unregisterReceiver() {
    if (this.broadcastReceiverRegistered) {
      this.broadcastReceiverRegistered = false;

      Log.i(TAG, "Unregistered broadcast");
      this.context.unregisterReceiver(this.broadcastReceiver);
//            LocalBroadcastManager.getInstance(this.context).unregisterReceiver(this.broadcastReceiver);
    }
  }


  @Override
  public void onAttachedToActivity(ActivityPluginBinding activityPluginBinding) {
    Log.d(TAG, "onAttachedToActivity");
    activityPluginBinding.addOnNewIntentListener(this);
    this.registerReceiver();
  }

  @Override
  public void onDetachedFromActivityForConfigChanges() {
    Log.d(TAG, "onDetachedFromActivityForConfigChanges");
    this.unregisterReceiver();
  }

  @Override
  public void onReattachedToActivityForConfigChanges(ActivityPluginBinding activityPluginBinding) {
    Log.d(TAG, "onReattachedToActivityForConfigChanges");
    activityPluginBinding.addOnNewIntentListener(this);
    this.registerReceiver();
  }

  @Override
  public void onDetachedFromActivity() {
    Log.d(TAG, "onDetachedFromActivity");
    this.unregisterReceiver();
  }

  @Override
  public boolean onNewIntent(Intent intent) {
    Log.d(TAG, "onNewIntent");
    this.handleIncomingCallIntent(intent);
    return false;
  }

  private void handleIncomingCallIntent(Intent intent) {
    if (intent != null && intent.getAction() != null) {
      String action = intent.getAction();
      Log.i(TAG, "onReceive. Action: " + action);

      if (TwilioConstants.ACTION_ACCEPT.equals(action)) {
        CallInvite callInvite = intent.getParcelableExtra(TwilioConstants.EXTRA_INCOMING_CALL_INVITE);
        answer(callInvite);
      }
    }
  }

  @Override
  public void onMethodCall(MethodCall call, @NonNull Result result) {
    Log.i(TAG, "onMethodCall. Method: " + call.method);
    TwilioUtils twilioUtils = TwilioUtils.getInstance(this.context);

    switch (call.method) {
      case "register": {
        String identity = call.argument("identity");
        String accessToken = call.argument("accessToken");
        String fcmToken = call.argument("fcmToken");

        try {
          twilioUtils.register(identity, accessToken, fcmToken, new TwilioRegistrationListener() {
            @Override
            public void onRegistered() {
              result.success("");
            }

            @Override
            public void onError() {
              result.error("CALL_TRANSACTION_FAILED", "Failed to register with Twilio", "An error occurred while setting up the registration.");
            }
          });
        } catch (Exception exception) {
          Log.e(TAG, "Exception occurred during register: ", exception);
          result.error("TOKEN_EXPIRED", "Exception occurred during registration", exception.getMessage());
        }
      }
      break;

      case "unregister": {
        try {
          twilioUtils.unregister();
          result.success("");
        } catch (Exception exception) {
          Log.e(TAG, "Exception occurred during unregister: ", exception);
          result.error("UNREGISTER_ERROR", "Failed to unregister from Twilio Voice", exception.getMessage());
        }
      }
      break;

      case "makeCall": {
        try {
          String to = call.argument("to");
          Map<String, Object> data = call.argument("data");
          twilioUtils.makeCall(to, data, getCallListener());
          responseChannel.invokeMethod("callConnecting", twilioUtils.getCallDetails());
          result.success(twilioUtils.getCallDetails());
        } catch (Exception exception) {
          Log.e(TAG, "Exception occurred during makeCall: ", exception);
          result.error("ACTIVE_CALL_EXISTS", "Failed to make a call", exception.getMessage());
        }
      }
      break;

      case "toggleMute": {
        try {
          boolean isMuted = twilioUtils.toggleMute();
          responseChannel.invokeMethod(twilioUtils.getCallStatus(), twilioUtils.getCallDetails());
          result.success(isMuted);
        } catch (Exception exception) {
          Log.e(TAG, "Exception occurred during toggleMute: ", exception);
          result.error("NO_ACTIVE_CALL", "There is no active call.", exception.getMessage());
        }
      }
      break;

      case "isMuted": {
        try {
          boolean isMuted = twilioUtils.isMuted();
          result.success(isMuted);
        } catch (Exception exception) {
          Log.e(TAG, "Exception occurred during isMuted: ", exception);
          result.error("NO_ACTIVE_CALL", "There is no active call.", exception.getMessage());
        }
      }
      break;

      case "toggleSpeaker": {
        try {
          boolean isSpeaker = twilioUtils.toggleSpeaker();
          responseChannel.invokeMethod(twilioUtils.getCallStatus(), twilioUtils.getCallDetails());
          result.success(isSpeaker);
        } catch (Exception exception) {
          Log.e(TAG, "Exception occurred during toggleSpeaker: ", exception);
          result.error("NO_ACTIVE_CALL", "There is no active call.", exception.getMessage());
        }
      }
      break;

      case "sendDigits": {
        try {
          String digits = call.argument("digits");
          twilioUtils.sendDigits(digits);
          result.success("");
        } catch (Exception exception) {
          Log.e(TAG, "Exception occurred during sendDigits: ", exception);
          result.error("DIGITS_ERROR", "Failed to send digits", exception.getMessage());
        }
      }
      break;

      case "isSpeaker": {
        try {
          boolean isSpeaker = twilioUtils.isSpeaker();
          result.success(isSpeaker);
        } catch (Exception exception) {
          Log.e(TAG, "Exception occurred during isSpeaker: ", exception);
          result.error("NO_ACTIVE_CALL", "There is no active call.", exception.getMessage());
        }
      }
      break;

      case "hangUp": {
        try {
          twilioUtils.disconnect();
          result.success("");
        } catch (Exception exception) {
          Log.e(TAG, "Exception occurred during hangUp: ", exception);
          result.error("CALL_TRANSACTION_FAILED", "Failed to hang up the call", exception.getMessage());
        }
      }
      break;

      case "activeCall": {
        if (twilioUtils.getActiveCall() == null) {
          result.error("NO_ACTIVE_CALL", "There is no active call.", "Operation cannot be performed without an active call.");
        } else {
          result.success(twilioUtils.getCallDetails());
        }
      }
      break;

      case "setContactData": {
        try {
          Map<String, Object> data = call.argument("contacts");
          String defaultDisplayName = call.argument("defaultDisplayName");
          PreferencesUtils.getInstance(this.context).setContacts(data, defaultDisplayName);
          result.success("");
        } catch (Exception exception) {
          Log.e(TAG, "Exception occurred during setContactData: ", exception);
          result.error("CONTACT_DATA_ERROR", "Failed to set contact data", exception.getMessage());
        }
      }
      break;

      case "setCallStyle": {
        try {
          final PreferencesUtils preferencesUtils = PreferencesUtils.getInstance(this.context);

          // Background color
          if (call.argument("backgroundColor") != null) {
            String color = call.argument("backgroundColor");
            preferencesUtils.storeCallBackgroundColor(color);
          } else {
            preferencesUtils.clearCallBackgroundColor();
          }

          // Text Color
          if (call.argument("textColor") != null) {
            String color = call.argument("textColor");
            preferencesUtils.storeCallTextColor(color);
          } else {
            preferencesUtils.clearCallTextColor();
          }

          // Button
          if (call.argument("buttonColor") != null) {
            String color = call.argument("buttonColor");
            preferencesUtils.storeCallButtonColor(color);
          } else {
            preferencesUtils.clearCallButtonColor();
          }

          // Button Icon
          if (call.argument("buttonIconColor") != null) {
            String color = call.argument("buttonIconColor");
            preferencesUtils.storeCallButtonIconColor(color);
          } else {
            preferencesUtils.clearCallButtonIconColor();
          }

          // Button focus
          if (call.argument("buttonFocusColor") != null) {
            String color = call.argument("buttonFocusColor");
            preferencesUtils.storeCallButtonFocusColor(color);
          } else {
            preferencesUtils.clearCallButtonFocusColor();
          }

          // Button focus icon
          if (call.argument("buttonFocusIconColor") != null) {
            String color = call.argument("buttonFocusIconColor");
            preferencesUtils.storeCallButtonFocusIconColor(color);
          } else {
            preferencesUtils.clearCallButtonFocusIconColor();
          }

          result.success("");
        } catch (Exception exception) {
          Log.e(TAG, "Exception occurred during setCallStyle: ", exception);

          result.error("CALL_STYLE_ERROR", "Failed to set call style", exception.getMessage());
        }
      }
      break;

      case "resetCallStyle": {
        try {
          final PreferencesUtils preferencesUtils = PreferencesUtils.getInstance(this.context);
          preferencesUtils.clearCallBackgroundColor();
          preferencesUtils.clearCallTextColor();
          preferencesUtils.clearCallButtonColor();
          preferencesUtils.clearCallButtonIconColor();
          preferencesUtils.clearCallButtonFocusColor();
          preferencesUtils.clearCallButtonFocusIconColor();
          result.success("");
        } catch (Exception exception) {
          Log.e(TAG, "Exception occurred during resetCallStyle: ", exception);
          result.error("RESET_CALL_STYLE_ERROR", "Failed to reset call style", exception.getMessage());
        }
      }
      break;

      case "setForeground": {
        try {
          AppForegroundStateUtils.getInstance().setForeground(call.argument("foreground"));
          result.success("");
        } catch (Exception exception) {
          Log.e(TAG, "Exception occurred during setForeground: ", exception);
          result.error("FOREGROUND_ERROR", "Failed to set foreground state", exception.getMessage());
        }
      }
      break;
    }


  }

  @Override
  public void onAttachedToEngine(FlutterPlugin.FlutterPluginBinding binding) {
    setupMethodChannel(binding.getBinaryMessenger(), binding.getApplicationContext());
  }

  @Override
  public void onDetachedFromEngine(@NonNull FlutterPlugin.FlutterPluginBinding binding) {
  }

  private void answer(CallInvite callInvite) {
    try {
      TwilioUtils t = TwilioUtils.getInstance(this.context);
      t.acceptInvite(callInvite, getCallListener());
      responseChannel.invokeMethod("callConnecting", t.getCallDetails());
    } catch (Exception exception) {
      Log.e(TAG, "Exception occurred during answer: ", exception);
    }
  }

  Call.Listener getCallListener() {
    TwilioUtils t = TwilioUtils.getInstance(this.context);

    return new Call.Listener() {
      @Override
      public void onConnectFailure(@NonNull Call call, @NonNull CallException error) {
        Log.d(TAG, "onConnectFailure. Error: " + error.getMessage());
        responseChannel.invokeMethod("callDisconnected", "");
      }

      @Override
      public void onRinging(@NonNull Call call) {
        Log.d(TAG, "onRinging");
        responseChannel.invokeMethod("callRinging", t.getCallDetails());
      }

      @Override
      public void onConnected(@NonNull Call call) {
        Log.d(TAG, "onConnected");
        responseChannel.invokeMethod("callConnected", t.getCallDetails());
      }

      @Override
      public void onReconnecting(@NonNull Call call, @NonNull CallException e) {
        Log.d(TAG, "onReconnecting. Error: " + e.getMessage());
        responseChannel.invokeMethod("callReconnecting", t.getCallDetails());
      }

      @Override
      public void onReconnected(@NonNull Call call) {
        Log.d(TAG, "onReconnected");
        responseChannel.invokeMethod("callReconnected", t.getCallDetails());
      }

      @Override
      public void onDisconnected(@NonNull Call call, CallException e) {
        if (e != null) {
          Log.d(TAG, "onDisconnected. Error: " + e.getMessage());
        } else {
          Log.d(TAG, "onDisconnected");
        }
        Log.d(TAG, call.getState().toString());
        responseChannel.invokeMethod("callDisconnected", null);
      }

      @Override
      public void onCallQualityWarningsChanged(
              @NonNull Call call,
              @NonNull Set<Call.CallQualityWarning> currentWarnings,
              @NonNull Set<Call.CallQualityWarning> previousWarnings
      ) {
        Log.d(TAG, "onCallQualityWarningsChanged");
      }
    };
  }

  private static class CustomBroadcastReceiver extends BroadcastReceiver {

    private final TwilioVoiceFlutterPlugin plugin;

    private CustomBroadcastReceiver(TwilioVoiceFlutterPlugin plugin) {
      this.plugin = plugin;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
      plugin.handleIncomingCallIntent(intent);
    }
  }


}


