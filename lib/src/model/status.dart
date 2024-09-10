/// Enum representing the various states of a Twilio voice call.
enum TwilioVoiceFlutterStatus {
  /// The call is currently in the process of connecting.
  connecting,

  /// The call has been disconnected.
  disconnected,

  /// The call is ringing on the recipient's end.
  ringing,

  /// The call is successfully connected.
  connected,

  /// The call is attempting to reconnect after a disruption.
  reconnecting,

  /// The call has successfully reconnected after a disruption.
  reconnected,

  /// The call status is unknown.
  unknown,
}
