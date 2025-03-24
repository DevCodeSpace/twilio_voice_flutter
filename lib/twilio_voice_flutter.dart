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
  /// Method channel used to communicate with the native platform for Twilio voice functionality.
  static const MethodChannel _channel = MethodChannel('twilio_voice_flutter');

  /// Method channel used to listen for responses from the native platform regarding Twilio voice events.
  static const EventChannel _eventChannel =
      EventChannel('twilio_voice_flutter_events');

  /// StreamController for managing the stream of [TwilioVoiceFlutterEvent] events.
  static late StreamController<TwilioVoiceFlutterEvent> _streamController;

  /// The current Twilio voice event, which can be null if no event has occurred.
  static TwilioVoiceFlutterEvent? _event;

  /// Getter for the current [TwilioVoiceFlutterEvent]. Returns the last event, or null if none exists.
  static TwilioVoiceFlutterEvent? get event => _event;

  /// Initializes the Twilio Voice Flutter plugin by setting up the event stream and method call handler.
  static void init() {
    /// Create a broadcast stream controller to allow multiple listeners.
    _streamController = StreamController.broadcast();

    /// Set the method call handler for the event channel to process events coming from the native platform.
    _eventChannel.receiveBroadcastStream().listen((event) async {
      final method = event["event"];
      final arguments = event["data"];
      log("Call event: $method . Arguments: $arguments");

      try {
        /// Get the event type based on the event method name.
        final eventType = getEventType(method);
        TwilioVoiceFlutterCall? call;

        /// Try to create a TwilioVoiceFlutterCall object from the event arguments.
        try {
          call = TwilioVoiceFlutterCall.fromMap(
              Map<String, dynamic>.from(arguments));
        } catch (error) {
          log("$error");

          /// Log any error encountered during the call creation.
        }

        /// Add the event (with its type and call data) to the stream.
        _streamController.add(TwilioVoiceFlutterEvent(eventType, call));
      } catch (error, stack) {
        /// Log any error encountered during the event handling process.
        log("Error parsing call event. ${arguments}",
            error: error, stackTrace: stack);
      }
    });

    /// Listen to the stream of events and update the current event (_event) accordingly.
    _streamController.stream.listen((event) {
      _event = event;
    });
  }

  /// Returns the corresponding [TwilioVoiceFlutterStatus] for a given event string.
  ///
  /// [event] is a string representing the event type from the Twilio voice service.
  static TwilioVoiceFlutterStatus getEventType(String event) {
    if (event == "callConnecting") {
      return TwilioVoiceFlutterStatus.connecting;

      /// Indicates the call is in the process of connecting.
    }
    if (event == "callDisconnected") {
      return TwilioVoiceFlutterStatus.disconnected;

      /// Indicates the call has been disconnected.
    }
    if (event == "callRinging") {
      return TwilioVoiceFlutterStatus.ringing;

      /// Indicates the call is ringing.
    }
    if (event == "callConnected") {
      return TwilioVoiceFlutterStatus.connected;

      /// Indicates the call has been successfully connected.
    }
    if (event == "callReconnecting") {
      return TwilioVoiceFlutterStatus.reconnecting;

      /// Indicates the call is reconnecting.
    }
    if (event == "callReconnected") {
      return TwilioVoiceFlutterStatus.reconnected;

      /// Indicates the call has successfully reconnected.
    }
    return TwilioVoiceFlutterStatus.unknown;

    /// Indicates the status of the call is unknown.
  }

  /// Stream that emits [TwilioVoiceFlutterEvent] for all call events.
  ///
  /// This stream can be listened to by multiple subscribers.
  static Stream<TwilioVoiceFlutterEvent> get onCallEvent {
    return _streamController.stream.asBroadcastStream();
  }

  /// Stream that emits [TwilioVoiceFlutterEvent] specifically for call connecting events.
  ///
  /// This stream filters the events to only include those with a status of [TwilioVoiceFlutterStatus.connecting].
  static Stream<TwilioVoiceFlutterEvent> get onCallConnecting {
    return _streamController.stream
        .asBroadcastStream()
        .where((event) => event.status == TwilioVoiceFlutterStatus.connecting);
  }

  /// Initiates a call to the specified number.
  ///
  /// [to] is the recipient's phone number to which the call is being made.
  /// [data] is optional and can include additional information to send with the call; defaults to an empty map.
  static Future<TwilioVoiceFlutterCall> makeCall({
    required String to,
    Map<String, String> data = const <String, String>{},
  }) async {
    final args = <String, Object>{
      "to": to.trim().replaceAll(" ", ""),
      "data": data,
    };

    // Invokes the 'makeCall' method on the native platform and waits for the result.
    final result = await _channel.invokeMethod('makeCall', args);

    // Returns a TwilioVoiceFlutterCall object created from the result map.
    return TwilioVoiceFlutterCall.fromMap(Map<String, dynamic>.from(result));
  }

  /// Ends the current call.
  static Future<void> hangUp() async {
    // Invokes the 'hangUp' method on the native platform to disconnect the call.
    await _channel.invokeMethod('hangUp');
  }

  /// Sends DTMF (Dual-tone multi-frequency) digits during an active call.
  ///
  /// [digits] is a string representing the DTMF digits to be sent.
  static Future<void> sendDigits(String digits) async {
    final args = <String, Object>{
      "digits": digits,
    };
    // Invokes the 'sendDigits' method on the native platform to send the specified digits.
    await _channel.invokeMethod('sendDigits', args);
  }

  /// Registers a user with the Twilio service.
  ///
  /// [identity] is the unique identifier for the user.
  /// [accessToken] is the token required for authentication.
  /// [fcmToken] is the Firebase Cloud Messaging token for push notifications.
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

    /// Invokes the 'register' method on the native platform to register the user with the provided details.
    await _channel.invokeMethod('register', args);
  }

  /// Unregisters the user from the Twilio service.
  ///
  /// This method is called to disconnect the user and clean up any resources.
  static Future<void> unregister() async {
    await _channel.invokeMethod('unregister');
  }

  /// Toggles the mute state of the current call.
  ///
  /// Returns true if the call is now muted, and false if it is unmuted.
  static Future<bool> toggleMute() async {
    return await _channel.invokeMethod('toggleMute');
  }

  /// Checks if the current call is muted.
  ///
  /// Returns true if the call is muted, false otherwise.
  static Future<bool> isMuted() async {
    return await _channel.invokeMethod('isMuted');
  }

  /// Toggles the speakerphone state of the current call.
  ///
  /// Returns true if the speaker is now enabled, and false if it is disabled.
  static Future<bool> toggleSpeaker() async {
    return await _channel.invokeMethod('toggleSpeaker');
  }

  /// Checks if the speakerphone is currently enabled.
  ///
  /// Returns true if the speaker is enabled, false otherwise.
  static Future<bool> isSpeaker() async {
    return await _channel.invokeMethod('isSpeaker');
  }

  /// Retrieves the currently active Twilio voice call.
  ///
  /// Returns a [TwilioVoiceFlutterCall] object if there is an active call,
  /// or null if no active call is found.
  static Future<TwilioVoiceFlutterCall?> getActiveCall() async {
    try {
      /// Invokes the 'activeCall' method on the native platform to get the active call data.
      final data = await _channel.invokeMethod('activeCall');

      /// Returns null if no active call data is found.
      if (data == null || data == "") return null;

      /// Creates and returns a TwilioVoiceFlutterCall object from the retrieved data.
      return TwilioVoiceFlutterCall.fromMap(Map<String, dynamic>.from(data));
    } catch (error, stack) {
      /// Logs any error encountered while parsing the call data.
      log("Error parsing call", error: error, stackTrace: stack);
      return null;

      /// Returns null in case of an error.
    }
  }

  /// Sets the contact data for Twilio voice calls.
  ///
  /// [data] is a list of [TwilioVoiceFlutterContactData] objects containing contact information.
  /// [defaultDisplayName] is the display name to use if a contact's name is not provided; defaults to "Unknown number".
  static Future<void> setContactData(
    List<TwilioVoiceFlutterContactData> data, {
    String defaultDisplayName = "Unknown number",
  }) async {
    /// Creates a map to hold the contact data.
    final args = <String, dynamic>{};

    /// Iterates over the provided contact data and populates the args map.
    for (var element in data) {
      args[element.phoneNumber] = {
        "displayName": element.displayName.trim(),

        /// Trims the display name.
        "photoURL": element.photoURL.trim(),

        /// Trims the photo URL.
      };
    }

    /// Invokes the 'setContactData' method on the native platform with the prepared contact data.
    await _channel.invokeMethod(
      'setContactData',
      {
        "contacts": args,

        /// Passes the contacts map to the method.
        "defaultDisplayName": defaultDisplayName,

        /// Passes the default display name.
      },
    );
  }

  /// Sets the styling options for the Android call interface.
  ///
  /// [backgroundColor] is the background color of the call interface.
  /// [textColor] is the color of the text displayed.
  /// [buttonColor] is the color of the buttons.
  /// [buttonIconColor] is the color of the button icons.
  /// [buttonFocusColor] is the color of the buttons when focused.
  /// [buttonFocusIconColor] is the color of the button icons when focused.
  static Future<void> setAndroidCallStyle({
    String? backgroundColor,
    String? textColor,
    String? buttonColor,
    String? buttonIconColor,
    String? buttonFocusColor,
    String? buttonFocusIconColor,
  }) async {
    /// Exits the method if the platform is web.
    if (kIsWeb) return;

    /// Exits the method if the platform is not Android.
    if (!Platform.isAndroid) return;

    /// Invokes the 'setCallStyle' method on the native platform with the provided style options.
    await _channel.invokeMethod(
      'setCallStyle',
      {
        "backgroundColor": backgroundColor,

        /// Passes the background color.
        "textColor": textColor,

        /// Passes the text color.
        "buttonColor": buttonColor,

        /// Passes the button color.
        "buttonIconColor": buttonIconColor,

        /// Passes the button icon color.
        "buttonFocusColor": buttonFocusColor,

        /// Passes the button focus color.
        "buttonFocusIconColor": buttonFocusIconColor,

        /// Passes the button focus icon color.
      },
    );
  }

  /// Resets the Android call interface style to default settings.
  ///
  /// This method is called to remove any custom styles that have been set.
  static Future<void> resetAndroidCallStyle() async {
    /// Exits the method if the platform is web.
    if (kIsWeb) return;

    /// Exits the method if the platform is not Android.
    if (!Platform.isAndroid) return;

    /// Invokes the 'resetCallStyle' method on the native platform to reset the call style.
    await _channel.invokeMethod('resetCallStyle', {});
  }

  /// Sets the app's foreground status for the call interface.
  ///
  /// [foreground] indicates whether the app should be in the foreground (true) or background (false).
  static Future<void> setForeground(bool foreground) async {
    /// Exits the method if the platform is web.
    if (kIsWeb) return;

    /// Exits the method if the platform is not Android.
    if (!Platform.isAndroid) return;

    /// Invokes the 'setForeground' method on the native platform with the foreground status.
    await _channel.invokeMethod('setForeground', {"foreground": foreground});
  }
}
