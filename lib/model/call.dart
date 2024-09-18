import 'package:equatable/equatable.dart';
import '../model/status.dart';

import '../twilio_voice_flutter.dart';

class TwilioVoiceFlutterCall extends Equatable {
  final String id;
  final String fromDisplayName;
  final String toDisplayName;
  final bool outgoing;
  final TwilioVoiceFlutterStatus status;
  final bool mute;
  final bool speaker;

  //olds
  final String to;
  final String toPhotoURL;

  TwilioVoiceFlutterCall({
    required this.id,
    required this.fromDisplayName,
    required this.toDisplayName,
    required this.mute,
    required this.speaker,
    required this.status,
    required this.outgoing,
    required this.to,
    required this.toPhotoURL,
  });

  factory TwilioVoiceFlutterCall.fromMap(Map<String, dynamic> data) {
    return TwilioVoiceFlutterCall(
      id: data["id"] ?? "",
      fromDisplayName: data["fromDisplayName"] ?? "",
      toDisplayName: data["toDisplayName"] ?? "",
      outgoing: data["outgoing"] ?? false,
      mute: data["mute"] ?? false,
      speaker: data["speaker"] ?? false,
      status: TwilioVoiceFlutter.getEventType(data["status"] ?? ""),
      toPhotoURL: data["toPhotoURL"] ?? "",
      to: data["to"] ?? "",
    );
  }

  @override
  List<Object?> get props => [
        id,
        fromDisplayName,
        toDisplayName,
        outgoing,
        mute,
        speaker,
        status,
      ];
}
