import 'call.dart';
import 'status.dart';

class TwilioVoiceFlutterEvent {
  final TwilioVoiceFlutterStatus status;
  final TwilioVoiceFlutterCall? call;

  TwilioVoiceFlutterEvent(this.status, this.call);
}
