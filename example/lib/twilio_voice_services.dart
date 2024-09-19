import 'dart:io';

import 'package:firebase_messaging/firebase_messaging.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:permission_handler/permission_handler.dart';
import 'package:twilio_voice_flutter/model/event.dart';
import 'package:twilio_voice_flutter/twilio_voice_flutter.dart';

import 'main.dart';

class TwilioVoiceServices {
  static Future<String> _getAccessToken(String identity) async {
    String accessToken = "PLEASE PUT ACCESS TOKEN HERE";
    return accessToken;
  }

  static Stream<TwilioVoiceFlutterEvent> get callEventsListener {
    return TwilioVoiceFlutter.onCallEvent;
  }

  static Future<void> initialize() async {
    await setTwilioToken("alice");
  }

  static Future<void> _requestAudioPermission() async {
    var status = await Permission.microphone.status;
    if (!status.isGranted) {
      await Permission.microphone.request();
    }
  }

  static Future<bool> setTwilioToken(String identity) async {
    String accessToken = await _getAccessToken(identity);

    if (Platform.isAndroid) {
      String? fcmToken = await _getFcmToken();
      try {
        await TwilioVoiceFlutter.register(
            identity: identity, accessToken: accessToken, fcmToken: fcmToken);
      } on PlatformException catch (error) {
        if (error.code == "TOKEN_EXPIRED") {
          showSnackBar(error.message.toString(), MsgStatus.error);
        } else {
          showSnackBar(error.message.toString(), MsgStatus.warning);
        }
      }
    } else {
      try {
        await TwilioVoiceFlutter.register(
            identity: identity, accessToken: accessToken, fcmToken: "");
      } on PlatformException catch (error) {
        if (error.code == "TOKEN_EXPIRED") {
          showSnackBar(error.message.toString(), MsgStatus.error);
        } else {
          showSnackBar(error.message.toString(), MsgStatus.warning);
        }
      }
    }
    return true;
  }

  static Future<String> _getFcmToken() async {
    return await FirebaseMessaging.instance.getToken() ?? "";
  }

  static Future<bool> makeCall({required String to}) async {
    try {
      await _requestAudioPermission();
      await TwilioVoiceFlutter.makeCall(to: to);
      return true;
    } catch (e) {
      // Handle the exception, for example, log it or return a default value
      showSnackBar(e.toString());
      return false; // Return false if there's an error
    }
  }

  static Future<bool?> hangUp() async {
    try {
      await TwilioVoiceFlutter.hangUp();
      return true;
    } catch (e) {
      // Handle the exception, for example, log it or return a default value
      showSnackBar(e.toString());
      return null; // Return false if there's an error
    }
  }

  static Future<bool?> toggleMute() async {
    try {
      await TwilioVoiceFlutter.toggleMute();
      return await TwilioVoiceFlutter.isMuted();
    } catch (e) {
      // Handle the exception
      showSnackBar(e.toString());
      return null; // Return false if there's an error
    }
  }

  static Future<bool?> isMuted() async {
    try {
      return await TwilioVoiceFlutter.isMuted();
    } catch (e) {
      // Handle the exception
      showSnackBar(e.toString());
      return null; // Return false if there's an error
    }
  }

  static Future<bool?> isSpeaker() async {
    try {
      return await TwilioVoiceFlutter.isSpeaker();
    } catch (e) {
      // Handle the exception
      showSnackBar(e.toString());
      return null; // Return false if there's an error
    }
  }

  static Future<bool?> toggleSpeaker() async {
    try {
      await TwilioVoiceFlutter.toggleSpeaker();
      return await TwilioVoiceFlutter.isSpeaker();
    } catch (e) {
      showSnackBar(e.toString());
    }
    return null;
  }

  static showSnackBar(String message, [MsgStatus? msgStatus]) {
    if (appKey.currentState != null && message.isNotEmpty) {
      Color backgroundColor;
      switch (msgStatus) {
        case MsgStatus.success:
          backgroundColor = Colors.green;
          break;
        case MsgStatus.error:
          backgroundColor = Colors.red;
          break;
        case MsgStatus.warning:
        default:
          backgroundColor = Colors.black;
      }
      ScaffoldMessenger.of(appKey.currentState!.context).clearSnackBars();
      ScaffoldMessenger.of(appKey.currentState!.context).showSnackBar(SnackBar(
        content: Text(message),
        backgroundColor: backgroundColor,
      ));
    }
  }
}

enum MsgStatus { error, success, warning }
