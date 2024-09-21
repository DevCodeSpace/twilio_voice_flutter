import 'call.dart';
import 'status.dart';

class TwilioVoiceFlutterEvent {
  /// The current status of the Twilio voice call, represented by [TwilioVoiceFlutterStatus]
  final TwilioVoiceFlutterStatus status;

  /// The current Twilio voice call, represented by [TwilioVoiceFlutterCall].
  /// It can be null if no call is active.
  final TwilioVoiceFlutterCall? call;

  /// Constructor for TwilioVoiceFlutterEvent
  TwilioVoiceFlutterEvent(this.status, this.call);
}
