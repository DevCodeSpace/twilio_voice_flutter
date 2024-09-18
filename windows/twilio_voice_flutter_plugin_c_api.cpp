#include "include/twilio_voice_flutter/twilio_voice_flutter_plugin_c_api.h"

#include <flutter/plugin_registrar_windows.h>

#include "twilio_voice_flutter_plugin.h"

void TwilioVoiceFlutterPluginCApiRegisterWithRegistrar(
    FlutterDesktopPluginRegistrarRef registrar) {
  twilio_voice_flutter::TwilioVoiceFlutterPlugin::RegisterWithRegistrar(
      flutter::PluginRegistrarManager::GetInstance()
          ->GetRegistrar<flutter::PluginRegistrarWindows>(registrar));
}
