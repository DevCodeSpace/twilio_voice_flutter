#
# To learn more about a Podspec see http://guides.cocoapods.org/syntax/podspec.html.
# Run `pod lib lint twilio_voice_flutter.podspec` to validate before publishing.
#
#
Pod::Spec.new do |s|
  s.name             = 'twilio_voice_flutter'
  s.version          = '0.0.6'
  s.summary          = 'Voice SDK to allow adding voice-over-IP (VoIP) calling into your Flutter applications.'
  s.description      = <<-DESC
The twilio_voice_flutter package simplifies integration with Twilio's Programmable Voice SDK, enabling VoIP calling within your Flutter apps. It supports both iOS and Android, offering an easy-to-use API for managing calls. Ideal for customer service, communication, or any app needing real-time voice, it leverages Twilio's reliable infrastructure to deliver high-quality VoIP features.
  DESC
  s.homepage         = 'https://github.com/DevCodeSpace/twilio_voice_flutter'
  s.license          = { :file => '../LICENSE' }
  s.author           = { 'DevCodeSpace' => 'pubdev@devcodespace.com' }
  s.source           = { :path => '.' }

  s.source_files = 'Classes/**/*.{h,m,swift}'
  s.public_header_files = 'Classes/**/*.h'

  # Specify the Flutter dependency and Twilio Voice SDK
  s.dependency 'Flutter'
  s.dependency 'TwilioVoice', '~> 6.11.1'

  # Set the iOS deployment target
  s.ios.deployment_target = '12.0'

  # Ensure that the Twilio SDK framework is linked correctly
  s.ios.vendored_frameworks = 'Pods/TwilioVoice/TwilioVoice.framework'

  # This ensures that Swift files are compiled correctly
  s.swift_version = '5.0'
end
