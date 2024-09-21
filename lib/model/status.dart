/// Enum representing the different statuses of a Twilio voice call.
enum TwilioVoiceFlutterStatus {
  /// The call is in the process of connecting.
  connecting,

  /// The call has been disconnected.
  disconnected,

  /// The call is ringing.
  ringing,

  /// The call has been successfully connected.
  connected,

  /// The call is reconnecting due to a temporary connection loss.
  reconnecting,

  /// The call has successfully reconnected after a connection loss.
  reconnected,

  /// The status of the call is unknown.
  unknown,
}
