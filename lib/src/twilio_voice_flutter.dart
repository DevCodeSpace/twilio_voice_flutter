import 'dart:async';
import 'dart:developer';
import 'dart:io';
import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';
import 'model/call.dart';
import 'model/contact_data.dart';
import 'model/event.dart';
import 'model/status.dart';

class TwilioVoiceFlutter {
  /// A platform-specific method channel for invoking native code related to Twilio Voice.
  /// The 'twilio_voice_flutter' is the unique identifier for this channel.
  static const MethodChannel _channel = MethodChannel('twilio_voice_flutter');

  /// Another platform-specific method channel for receiving responses or events from the native side.
  /// The 'twilio_voice_flutter_response' is the unique identifier for this event channel.
  static const MethodChannel _eventChannel =
      MethodChannel('twilio_voice_flutter_response');

  /// A stream controller that will manage and broadcast events of type TwilioVoiceFlutterEvent to listeners.
  static late StreamController<TwilioVoiceFlutterEvent> _streamController;

  /// A variable to hold the latest event received from the native side, if any.
  /// This can be used to access the current state or the last event that occurred.
  static TwilioVoiceFlutterEvent? _event;

  /// A getter to access the current event.
  /// Returns the latest event stored in the _event variable, or null if no event has been received yet.
  static TwilioVoiceFlutterEvent? get event => _event;

  /// Initializes the Twilio Voice Flutter integration.
  /// This method sets up the stream controller for broadcasting events
  /// and assigns a handler to the event channel to manage incoming native events.
  static void init() {
    /// Creates a broadcast stream controller that allows multiple listeners to subscribe.
    _streamController = StreamController.broadcast();

    /// Sets a method call handler for the event channel to process incoming events from the native side.
    _eventChannel.setMethodCallHandler((event) async {
      /// Logs the method name and arguments of the incoming event for debugging purposes.
      log("Call event: ${event.method} . Arguments: ${event.arguments}");

      try {
        /// Determines the type of event using the method name.
        final eventType = getEventType(event.method);

        TwilioVoiceFlutterCall? call;
        try {
          /// Attempts to create a TwilioVoiceFlutterCall object from the event arguments.
          call = TwilioVoiceFlutterCall.fromMap(
              Map<String, dynamic>.from(event.arguments));
        } catch (error) {
          // Catch block left empty intentionally to avoid crashing on error.
        }

        /// Adds the event and call object to the stream for any listeners.
        _streamController.add(TwilioVoiceFlutterEvent(eventType, call));
      } catch (error, stack) {
        /// Logs any errors that occur while parsing the event, including the stack trace.
        log("Error parsing call event. ${event.arguments}",
            error: error, stackTrace: stack);
      }
    });

    /// Listens to the stream of events and updates the _event variable with the latest event.
    _streamController.stream.listen((event) {
      _event = event;
    });
  }

  /// Converts a string event to the corresponding `TwilioVoiceFlutterStatus`.
  /// Returns `unknown` if the event doesn't match any known status.
  static TwilioVoiceFlutterStatus getEventType(String event) {
    /// Maps "callConnecting" to `connecting`.
    if (event == "callConnecting") return TwilioVoiceFlutterStatus.connecting;

    /// Maps "callDisconnected" to `disconnected`.
    if (event == "callDisconnected") {
      return TwilioVoiceFlutterStatus.disconnected;
    }

    /// Maps "callRinging" to `ringing`.
    if (event == "callRinging") return TwilioVoiceFlutterStatus.ringing;

    /// Maps "callConnected" to `connected`.
    if (event == "callConnected") return TwilioVoiceFlutterStatus.connected;

    /// Maps "callReconnecting" to `reconnecting`.
    if (event == "callReconnecting") {
      return TwilioVoiceFlutterStatus.reconnecting;
    }

    /// Maps "callReconnected" to `reconnected`.
    if (event == "callReconnected") return TwilioVoiceFlutterStatus.reconnected;

    /// Default case: return `unknown`.
    return TwilioVoiceFlutterStatus.unknown;
  }

  /// Returns a stream of all call events from the TwilioVoiceFlutter.
  static Stream<TwilioVoiceFlutterEvent> get onCallEvent {
    return _streamController.stream.asBroadcastStream();
  }

  /// Returns a stream of call connecting events from the TwilioVoiceFlutter.
  static Stream<TwilioVoiceFlutterEvent> get onCallConnecting {
    return _streamController.stream
        .asBroadcastStream()
        .where((event) => event.status == TwilioVoiceFlutterStatus.connecting);
  }

  /// Initiates a call to the specified phone number with optional data.
  ///
  /// [to] is the phone number to call.
  /// [data] is a map of additional data to send with the call, defaults to an empty map.
  ///
  /// Returns a [Future] that resolves to a [TwilioVoiceFlutterCall] representing the initiated call.
  static Future<TwilioVoiceFlutterCall> makeCall({
    required String to,
    Map<String, dynamic> data = const <String, dynamic>{},
  }) async {
    final args = <String, Object>{
      "to": to,
      "data": data,
    };

    final result = await _channel.invokeMethod('makeCall', args);
    return TwilioVoiceFlutterCall.fromMap(Map<String, dynamic>.from(result));
  }

  /// Ends the current call by invoking the 'hangUp' method on the channel.
  ///
  /// This method does not return any value.
  static Future<void> hangUp() async {
    await _channel.invokeMethod('hangUp');
  }

  /// Sends a string of digits to the current call.
  ///
  /// [digits] is the string of digits to send.
  ///
  /// This method does not return any value.
  static Future<void> sendDigits(String digits) async {
    final args = <String, Object>{
      "digits": digits,
    };
    await _channel.invokeMethod('sendDigits', args);
  }

  /// Registers a user with the specified identity, access token, and FCM token.
  ///
  /// [identity] is the unique identifier for the user.
  /// [accessToken] is the token used for authentication.
  /// [fcmToken] is the Firebase Cloud Messaging token for push notifications.
  ///
  /// This method does not return any value.
  static Future<void> register({
    required String identity,
    required String accessToken,
    required String fcmToken,
  }) async {
    final args = <String, Object>{
      "identity": identity,
      "accessToken": accessToken,
      "fcmToken": fcmToken,
    };
    await _channel.invokeMethod('register', args);
  }

  /// Unregisters the user by invoking the 'unregister' method on the channel.
  ///
  /// This method does not return any value.
  static Future<void> unregister() async {
    await _channel.invokeMethod('unregister');
  }

  /// Toggles the mute state of the call.
  ///
  /// Returns a [Future] that resolves to a [bool] indicating the new mute state.
  static Future<bool> toggleMute() async {
    return await _channel.invokeMethod('toggleMute');
  }

  /// Checks if the call is currently muted.
  ///
  /// Returns a [Future] that resolves to a [bool] indicating whether the call is muted.
  static Future<bool> isMuted() async {
    return await _channel.invokeMethod('isMuted');
  }

  /// Toggles the speakerphone state of the call.
  ///
  /// Returns a [Future] that resolves to a [bool] indicating the new speakerphone state.
  static Future<bool> toggleSpeaker() async {
    return await _channel.invokeMethod('toggleSpeaker');
  }

  /// Checks if the call is currently using the speakerphone.
  ///
  /// Returns a [Future] that resolves to a [bool] indicating whether the speakerphone is active.
  static Future<bool> isSpeaker() async {
    return await _channel.invokeMethod('isSpeaker');
  }

  /// Retrieves the currently active call, if any.
  ///
  /// Returns a [Future] that resolves to a [TwilioVoiceFlutterCall] object representing the active call, or `null` if no active call is found.
  /// If an error occurs during the process, it logs the error and returns `null`.
  static Future<TwilioVoiceFlutterCall?> getActiveCall() async {
    try {
      final data = await _channel.invokeMethod('activeCall');
      if (data == null || data == "") return null;
      return TwilioVoiceFlutterCall.fromMap(Map<String, dynamic>.from(data));
    } catch (error, stack) {
      log("Error parsing call", error: error, stackTrace: stack);
      return null;
    }
  }

  /// Sets contact data for the TwilioVoiceFlutter.
  ///
  /// [data] is a list of [TwilioVoiceFlutterContactData] objects representing contact information to be set.
  /// [defaultDisplayName] is a default display name for contacts with unknown names, defaults to "Unknown number".
  ///
  /// This method does not return any value.
  static Future<void> setContactData(
    List<TwilioVoiceFlutterContactData> data, {
    String defaultDisplayName = "Unknown number",
  }) async {
    final args = <String, dynamic>{};
    for (var element in data) {
      args[element.phoneNumber] = {
        "displayName": element.displayName.trim(),
        "photoURL": element.photoURL.trim(),
      };
    }
    await _channel.invokeMethod(
      'setContactData',
      {
        "contacts": args,
        "defaultDisplayName": defaultDisplayName,
      },
    );
  }

  /// Configures the style of Android call UI elements.
  ///
  /// [backgroundColor] is the color for the background of the call UI (optional).
  /// [textColor] is the color for text elements in the call UI (optional).
  /// [buttonColor] is the color for buttons in the call UI (optional).
  /// [buttonIconColor] is the color for icons on buttons in the call UI (optional).
  /// [buttonFocusColor] is the color for the button when it is focused (optional).
  /// [buttonFocusIconColor] is the color for icons on focused buttons (optional).
  ///
  /// This method has no effect on the web platform or if the platform is not Android.
  /// It does not return any value.
  static Future<void> setAndroidCallStyle({
    String? backgroundColor,
    String? textColor,
    String? buttonColor,
    String? buttonIconColor,
    String? buttonFocusColor,
    String? buttonFocusIconColor,
  }) async {
    if (kIsWeb) return;
    if (!Platform.isAndroid) return;

    await _channel.invokeMethod(
      'setCallStyle',
      {
        "backgroundColor": backgroundColor,
        "textColor": textColor,
        "buttonColor": buttonColor,
        "buttonIconColor": buttonIconColor,
        "buttonFocusColor": buttonFocusColor,
        "buttonFocusIconColor": buttonFocusIconColor,
      },
    );
  }

  /// Resets the Android call UI style to default settings.
  ///
  /// This method has no effect on the web platform or if the platform is not Android.
  /// It does not return any value.
  static Future<void> resetAndroidCallStyle() async {
    if (kIsWeb) return;
    if (!Platform.isAndroid) return;
    await _channel.invokeMethod('resetCallStyle', {});
  }

  /// Sets whether the call UI should be in the foreground.
  ///
  /// [foreground] is a boolean indicating whether the call UI should be in the foreground (`true`) or background (`false`).
  ///
  /// This method has no effect on the web platform or if the platform is not Android.
  /// It does not return any value.
  static Future<void> setForeground(bool foreground) async {
    if (kIsWeb) return;
    if (!Platform.isAndroid) return;
    await _channel.invokeMethod('setForeground', {"foreground": foreground});
  }
}
