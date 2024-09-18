#ifndef FLUTTER_PLUGIN_TWILIO_VOICE_FLUTTER_PLUGIN_H_
#define FLUTTER_PLUGIN_TWILIO_VOICE_FLUTTER_PLUGIN_H_

#include <flutter/method_channel.h>
#include <flutter/plugin_registrar_windows.h>

#include <memory>

namespace twilio_voice_flutter {

class TwilioVoiceFlutterPlugin : public flutter::Plugin {
 public:
  static void RegisterWithRegistrar(flutter::PluginRegistrarWindows *registrar);

  TwilioVoiceFlutterPlugin();

  virtual ~TwilioVoiceFlutterPlugin();

  // Disallow copy and assign.
  TwilioVoiceFlutterPlugin(const TwilioVoiceFlutterPlugin&) = delete;
  TwilioVoiceFlutterPlugin& operator=(const TwilioVoiceFlutterPlugin&) = delete;

  // Called when a method is called on this plugin's channel from Dart.
  void HandleMethodCall(
      const flutter::MethodCall<flutter::EncodableValue> &method_call,
      std::unique_ptr<flutter::MethodResult<flutter::EncodableValue>> result);
};

}  // namespace twilio_voice_flutter

#endif  // FLUTTER_PLUGIN_TWILIO_VOICE_FLUTTER_PLUGIN_H_
