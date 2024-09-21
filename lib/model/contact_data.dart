class TwilioVoiceFlutterContactData {
  /// The phone number of the contact
  final String phoneNumber;

  /// The display name of the contact
  final String displayName;

  /// The URL of the contact's photo (if available)
  final String photoURL;

  /// Constructor for TwilioVoiceFlutterContactData
  TwilioVoiceFlutterContactData(
    this.phoneNumber, {
    this.displayName = "",
    this.photoURL = "",
  });
}
