import 'call.dart';
import 'status.dart';

/// Define a class to represent an event in the Twilio Voice Flutter plugin
class TwilioVoiceFlutterEvent {
  /// A field representing the current status of the Twilio voice call event
  final TwilioVoiceFlutterStatus status;

  /// An optional field representing details about the current or recent call
  final TwilioVoiceFlutterCall? call;

  /// Constructor to initialize the TwilioVoiceFlutterEvent with a status and an optional call
  TwilioVoiceFlutterEvent(this.status, this.call);
}
