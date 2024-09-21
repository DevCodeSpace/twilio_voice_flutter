import 'package:equatable/equatable.dart';
import '../model/status.dart';
import '../twilio_voice_flutter.dart';

class TwilioVoiceFlutterCall extends Equatable {
  /// Unique identifier for the call
  final String id;

  /// Display name of the caller
  final String fromDisplayName;

  /// Display name of the receiver
  final String toDisplayName;

  /// Boolean indicating if the call is outgoing or incoming
  final bool outgoing;

  /// Current status of the call (using TwilioVoiceFlutterStatus)
  final TwilioVoiceFlutterStatus status;

  /// Boolean indicating if the call is muted
  final bool mute;

  /// Boolean indicating if the speaker mode is enabled
  final bool speaker;

  /// Identifier of the receiver
  final String to;

  /// Photo URL of the receiver
  final String toPhotoURL;

  /// Constructor to initialize all the required properties
  const TwilioVoiceFlutterCall({
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

  /// Factory method to create an instance of TwilioVoiceFlutterCall from a map of dynamic data
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

  /// Overriding Equatable props to include properties for comparison
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
