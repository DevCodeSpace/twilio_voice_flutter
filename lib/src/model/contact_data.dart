/// A class that holds contact information for use with Twilio Voice in Flutter.
class TwilioVoiceFlutterContactData {
  /// The phone number of the contact.
  final String phoneNumber;

  /// The display name of the contact. Defaults to an empty string if not provided.
  final String displayName;

  /// The URL to the contact's photo. Defaults to an empty string if not provided.
  final String photoURL;

  /// Constructor for [TwilioVoiceFlutterContactData].
  ///
  /// Requires a [phoneNumber] and allows optional [displayName] and [photoURL].
  TwilioVoiceFlutterContactData(
    this.phoneNumber, {
    this.displayName = "",
    this.photoURL = "",
  });
}
