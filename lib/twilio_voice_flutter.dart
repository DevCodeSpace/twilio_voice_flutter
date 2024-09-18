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
  static const MethodChannel _channel = MethodChannel('twilio_voice_flutter');

  static const MethodChannel _eventChannel = MethodChannel('twilio_voice_flutter_response');

  static late StreamController<TwilioVoiceFlutterEvent> _streamController;

  static TwilioVoiceFlutterEvent? _event;

  static TwilioVoiceFlutterEvent? get event => _event;

  static void init() {
    _streamController = StreamController.broadcast();
    _eventChannel.setMethodCallHandler((event) async {
      log("Call event: ${event.method} . Arguments: ${event.arguments}");

      try {
        final eventType = getEventType(event.method);
        TwilioVoiceFlutterCall? call;
        try {
          call = TwilioVoiceFlutterCall.fromMap(Map<String, dynamic>.from(event.arguments));
        } catch (error) {}
        _streamController.add(TwilioVoiceFlutterEvent(eventType, call));
      } catch (error, stack) {
        log("Error parsing call event. ${event.arguments}", error: error, stackTrace: stack);
      }
    });

    _streamController.stream.listen((event) {
      _event = event;
    });
  }

  static TwilioVoiceFlutterStatus getEventType(String event) {
    if (event == "callConnecting") return TwilioVoiceFlutterStatus.connecting;
    if (event == "callDisconnected") return TwilioVoiceFlutterStatus.disconnected;
    if (event == "callRinging") return TwilioVoiceFlutterStatus.ringing;
    if (event == "callConnected") return TwilioVoiceFlutterStatus.connected;
    if (event == "callReconnecting") return TwilioVoiceFlutterStatus.reconnecting;
    if (event == "callReconnected") return TwilioVoiceFlutterStatus.reconnected;
    return TwilioVoiceFlutterStatus.unknown;
  }

  static Stream<TwilioVoiceFlutterEvent> get onCallEvent {
    return _streamController.stream.asBroadcastStream();
  }

  static Stream<TwilioVoiceFlutterEvent> get onCallConnecting {
    return _streamController.stream.asBroadcastStream().where((event) => event.status == TwilioVoiceFlutterStatus.connecting);
  }

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

  static Future<void> hangUp() async {
    await _channel.invokeMethod('hangUp');
  }

  static Future<void> sendDigits(String digits) async {
    final args = <String, Object>{
      "digits": digits,
    };
    await _channel.invokeMethod('sendDigits', args);
  }

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

  static Future<void> unregister() async {
    await _channel.invokeMethod('unregister');
  }

  static Future<bool> toggleMute() async {
    return await _channel.invokeMethod('toggleMute');
  }

  static Future<bool> isMuted() async {
    return await _channel.invokeMethod('isMuted');
  }

  static Future<bool> toggleSpeaker() async {
    return await _channel.invokeMethod('toggleSpeaker');
  }

  static Future<bool> isSpeaker() async {
    return await _channel.invokeMethod('isSpeaker');
  }

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

  static Future<void> resetAndroidCallStyle() async {
    if (kIsWeb) return;
    if (!Platform.isAndroid) return;
    await _channel.invokeMethod('resetCallStyle', {});
  }

  static Future<void> setForeground(bool foreground) async {
    if (kIsWeb) return;
    if (!Platform.isAndroid) return;
    await _channel.invokeMethod('setForeground', {"foreground": foreground});
  }
}
