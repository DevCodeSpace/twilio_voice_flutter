// Importing necessary packages.
import 'package:equatable/equatable.dart';
import '../twilio_voice_flutter.dart';
import 'status.dart';

// The TwilioVoiceFlutterCall class represents a voice call using the Twilio Voice API in Flutter.
// It extends Equatable to enable value comparison of TwilioVoiceFlutterCall objects.
class TwilioVoiceFlutterCall extends Equatable {
  // Unique identifier for the call.
  final String id;

  // Display name of the caller.
  final String fromDisplayName;

  // Display name of the recipient.
  final String toDisplayName;

  // Indicates if the call is outgoing (true) or incoming (false).
  final bool outgoing;

  // Current status of the call, represented by a TwilioVoiceFlutterStatus object.
  final TwilioVoiceFlutterStatus status;

  // Indicates if the call is currently muted.
  final bool mute;

  // Indicates if the speakerphone is enabled during the call.
  final bool speaker;

  // The phone number or identifier of the recipient.
  final String to;

  // URL of the recipient's photo, if available.
  final String toPhotoURL;

  // Constructor to initialize all properties of the TwilioVoiceFlutterCall class.
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

  // Factory method to create a TwilioVoiceFlutterCall instance from a map of key-value pairs.
  factory TwilioVoiceFlutterCall.fromMap(Map<String, dynamic> data) {
    return TwilioVoiceFlutterCall(
      id: data["id"] ??
          "", // Assigns the call ID, defaults to an empty string if not provided.
      fromDisplayName:
          data["fromDisplayName"] ?? "", // Assigns the caller's display name.
      toDisplayName:
          data["toDisplayName"] ?? "", // Assigns the recipient's display name.
      outgoing: data["outgoing"] ??
          false, // Sets the outgoing flag, defaults to false if not provided.
      mute: data["mute"] ??
          false, // Sets the mute flag, defaults to false if not provided.
      speaker: data["speaker"] ??
          false, // Sets the speaker flag, defaults to false if not provided.
      status: TwilioVoiceFlutter.getEventType(data["status"] ??
          ""), // Converts the status to a TwilioVoiceFlutterStatus.
      toPhotoURL:
          data["toPhotoURL"] ?? "", // Assigns the recipient's photo URL.
      to: data["to"] ??
          "", // Assigns the recipient's phone number or identifier.
    );
  }

  // Override the props getter to include all relevant fields for value comparison.
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
