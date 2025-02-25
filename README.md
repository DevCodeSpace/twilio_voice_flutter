The twilio_voice_flutter package simplifies integration with Twilio's Programmable Voice SDK, enabling VoIP calling within your Flutter apps. It supports both iOS and Android, offering an easy-to-use API for managing calls. Ideal for customer service, communication, or any app needing real-time voice, it leverages Twilio's reliable infrastructure to deliver high-quality VoIP features.

<img src="https://raw.githubusercontent.com/DevCodeSpace/twilio_voice_flutter/main/assets/banner1.png"/>

## Getting started
 
Add dependency to your `pubspec.yaml` file & run Pub get

```yaml
dependencies:
  twilio_voice_flutter: ^0.0.6
```
And import package into your class file

```dart
import 'package:twilio_voice_flutter/twilio_voice_flutter.dart';
```
## Important Note
⚠️ This plugin requires Firebase setup for Twilio voice calls! It uses FCM tokens for handling calls on Android 📲 and VoIP notifications for iOS 📞. Ensure both platforms are properly configured for smooth call functionality. ⚠️


## Platform Setup

### Android

To integrate the Twilio Voice plugin into your Android project, follow these steps:

- Open your project's `AndroidManifest.xml` file.
- Add the following service declaration inside the `<application>` tag:

   ```xml
   <application>
       ...
       <service
           android:name="com.twilio.voice.flutter.fcm.VoiceFirebaseMessagingService"
           android:exported="false"
           android:stopWithTask="false">
           <intent-filter> 
               <action android:name="com.google.firebase.MESSAGING_EVENT" />
           </intent-filter> 
       </service>
       ...
   </application>
   ```

### iOS

To configure your iOS project to support VoIP calls, follow these steps:

- Open your project in Xcode.
- Select your project from the Project Navigator.
- Go to the **Signing & Capabilities** tab.
- Enable the following Background Modes:
    - **Audio, AirPlay, and Picture in Picture**: Allows your app to continue playing audio while in the background.
    - **Voice over IP**: Enables your app to receive incoming VoIP calls while in the background.
- Ensure that your `Info.plist` file includes the required keys:

   ```xml
   <key>UIBackgroundModes</key>
   <array>
       <string>audio</string>
       <string>voip</string>
   </array>
   ```

## 🔑 Twilio Voice Call Access Token Setup

This plugin supports Twilio voice calls for both Android and iOS. To generate the access token and enable Twilio calls, follow the steps in the official Twilio repositories:

- https://github.com/twilio/voice-quickstart-android
- https://github.com/twilio/voice-quickstart-ios

These repositories provide detailed instructions for generating access tokens and making voice calls through Twilio on both Android and iOS platforms. ⚡ Make sure to follow the steps outlined to successfully integrate Twilio voice functionality into your application.

## Setting Up Twilio Token

The `setTwilioToken` function is used to register a user's identity with Twilio Voice using the provided access token. This function handles the retrieval of the Firebase Cloud Messaging (FCM) token on Android devices and registers the user with Twilio Voice.

```dart
static Future<bool> setTwilioToken(String identity, String accessToken) async {
    try{
      String accessToken = await _getAccessToken(identity);
      if (Platform.isAndroid) {
        String? fcmToken = await FirebaseMessaging.instance.getToken() ?? "";
        await TwilioVoiceFlutter.register(
            identity: identity, accessToken: accessToken, fcmToken: fcmToken);
      } else {
        await TwilioVoiceFlutter.register(
            identity: identity, accessToken: accessToken, fcmToken: "");
      }
      return true;
    }catch(_){
      return false;
    }
}
```
[🔗 Visit example for more info... 😊](https://github.com/DevCodeSpace/twilio_voice_flutter/tree/main/example)

## Features

The Twilio Voice Plugin for Flutter enables seamless integration of voice-over-IP (VoIP) calling into your Flutter applications. Below are the key features supported by this plugin:

### 1. **VoIP Call Management**
- **Initiate Calls:** Easily start a VoIP call to any recipient using the `makeCall` method, with the ability to pass custom call data.
- **Receive Incoming Calls:** Handle incoming call invites via push notifications, and answer calls using built-in CallKit support on iOS.
- **Call Status Notifications:** Stay updated on call status changes such as ringing, connecting, and disconnecting through Flutter MethodChannel callbacks.

### 2. **In-Call Controls**
- **Mute/Unmute Calls:** Toggle the mute status of the ongoing call using the `toggleMute` method. Check if the call is currently muted with the `isMuted` method.
- **Speakerphone Control:** Switch between the device's built-in speaker and receiver during an ongoing call using `toggleSpeaker`. Verify the current audio route with `isSpeaker`.

### 3. **CallKit Integration for iOS**
- **CallKit Support:** Utilize native CallKit functionality on iOS for managing incoming and outgoing VoIP calls, ensuring a familiar user experience.
- **Incoming Call Handling:** Report incoming calls to CallKit, allowing the system to display the native call UI and handle interruptions correctly.
- **Background VoIP Support:** Ensure your app can receive VoIP calls while in the background by enabling the necessary background modes and configuring the `Info.plist` accordingly.

### 4. **Push Notification Support**
- **VoIP Push Notifications:** Register and handle VoIP push notifications via Firebase Cloud Messaging (FCM) on Android and Apple Push Notification service (APNs) on iOS.
- **Push Credential Management:** Manage device and access tokens required for receiving VoIP push notifications, ensuring reliable communication.

### 5. **DTMF (Dual-tone Multi-frequency) Signaling**
- **Send DTMF Digits:** During an active call, send DTMF tones (e.g., for interacting with automated phone systems) using the `sendDigits` method.

### 6. **Contact Management**
- **Custom Contact Data:** Store and retrieve custom contact data (like display names and photo URLs) to enhance the calling experience with personalized information.

### 7. **Persistent Data Storage**
- **Token and Contact Data Persistence:** Securely store and retrieve access tokens and contact data using `UserDefaults` on iOS to maintain state across app sessions.

### 8. **Call Handling with Error Management**
- **Graceful Error Handling:** Comprehensive error handling ensures that issues like token expiration or failed call connections are managed and reported to the user effectively.

With these features, the Twilio Voice Plugin provides a robust foundation for integrating voice communication into your Flutter applications, supporting a wide range of use cases from customer support to in-app communication.

## Function Overview

- **`register()`**:
  Registers the device with Twilio using the access token and device token for VoIP push notifications.

- **`unregister()`**:
  Unregisters the device from Twilio VoIP push notifications, removing the access token and device token.

- **`makeCall(String to)`**:
  Initiates a voice call to the specified recipient. If an active call exists, it returns an error.

- **`toggleMute()`**:
  Toggles the mute status of the ongoing call. Notifies the Flutter side of the change.

- **`isMuted()`**:
  Returns the current mute status of the ongoing call.

- **`toggleSpeaker()`**:
  Toggles the speaker mode during an ongoing call. Notifies the Flutter side of the change.

- **`isSpeaker() -> Bool`**:
  Checks if the speaker mode is currently active.

- **`hangUp()`**:
  Ends the current call. If no active call exists, it clears the call-related data.

- **`activeCall()`**:
  Returns details of the active call if one exists.

- **`sendDigits(String digits)`**:
  Sends DTMF digits during an active call.

## Code Contributors

[![](https://raw.githubusercontent.com/DevCodeSpace/twilio_voice_flutter/main/assets/contributors.png)](https://github.com/DevCodeSpace/twilio_voice_flutter/graphs/contributors)
