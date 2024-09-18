class TwilioVoiceFlutterContactData {
  final String phoneNumber;
  final String displayName;
  final String photoURL;

  TwilioVoiceFlutterContactData(
    this.phoneNumber, {
    this.displayName = "",
    this.photoURL = "",
  });
}
