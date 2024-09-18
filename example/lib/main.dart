import 'dart:async';

import 'package:firebase_core/firebase_core.dart';
import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:twilio_voice_flutter/model/event.dart';
import 'package:twilio_voice_flutter/model/status.dart';
import 'package:twilio_voice_flutter/twilio_voice_flutter.dart';
import 'package:twilio_voice_flutter_example/firebase_options.dart';
import 'package:twilio_voice_flutter_example/twilio_voice_services.dart';


GlobalKey<NavigatorState> appKey = GlobalKey<NavigatorState>();

void main()async {
  WidgetsFlutterBinding.ensureInitialized();
  await Firebase.initializeApp(options: DefaultFirebaseOptions.currentPlatform);
  TwilioVoiceFlutter.init();
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  // This widget is the root of your application.
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      navigatorKey: appKey,
      title: 'Twilio Voice Demo',
      theme: ThemeData(

        colorScheme: ColorScheme.fromSeed(seedColor: Colors.deepPurple),
        useMaterial3: true,
      ),
      home: const MyHomePage(),
    );
  }
}

class MyHomePage extends StatefulWidget {
  const MyHomePage({super.key});

  @override
  State<MyHomePage> createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {

  bool _isSpeaker = false;
  bool _isMuted = false;
  bool _isCalling = false;
  String _callStatus = "";

  StreamSubscription<TwilioVoiceFlutterEvent>? callEventsListener;

  final TextEditingController identifyController = TextEditingController();

  void setCallEventsListener() {
    callEventsListener?.cancel();
    callEventsListener = null;
    callEventsListener = TwilioVoiceServices.callEventsListener.listen((event) {
      if (event.status == TwilioVoiceFlutterStatus.ringing || event.status == TwilioVoiceFlutterStatus.connected) {
        _callStatus = "Ringing...";
      }else if(event.status == TwilioVoiceFlutterStatus.connecting){
        _callStatus = "Connecting...";
      }else if(event.status == TwilioVoiceFlutterStatus.reconnected){
        _callStatus ="Reconnected...";
      }else if(event.status == TwilioVoiceFlutterStatus.disconnected){
        endCall();
      }
      setState(() {});
    });
  }

  @override
  void initState() {
    TwilioVoiceServices.initialize();
    setCallEventsListener();
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        centerTitle: true,
        title: const Text("Twilio Voice Call Example"),
      ),
      body: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            TextField(
              controller: identifyController,
              decoration: InputDecoration(
                hintText: "Enter call identifier",
                enabled: !_isCalling
              ),
            ),
            const Spacer(),
            Text(_callStatus,style: const TextStyle(fontSize: 18,fontWeight: FontWeight.w600),),
            const Spacer(),
            Row(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                IconButton.filled(
                    onPressed: (){
                      toggleSpeaker();
                    },
                    icon: _isMuted ?  const Icon(Icons.mic,size: 30,) : const Icon(Icons.mic_off_rounded,size: 30,),
                ),
                const SizedBox(width: 15,),
                Theme(
                  data: ThemeData(
                    iconButtonTheme: IconButtonThemeData(
                      style: ButtonStyle(
                        backgroundColor: WidgetStatePropertyAll(_isCalling ? Colors.red : Colors.green)
                      )
                    )
                  ),
                  child: IconButton.filled(
                    color: Colors.white,
                    onPressed: (){
                      if(!_isCalling){
                        makeCall(identifyController.text);
                      }else{
                        endCall();
                      }
                    },
                    icon: _isCalling ?  const Icon(Icons.call_end,size: 30,) : const Icon(Icons.call,size: 30,),
                  ),
                ),
                const SizedBox(width: 15,),
                IconButton.filled(
                  onPressed: (){
                    toggleSpeaker();
                  },
                  icon: _isSpeaker ? const Icon(CupertinoIcons.speaker_fill,size: 30,) : const Icon(CupertinoIcons.speaker_slash_fill,size: 30,)
                ),
              ],
            )
          ],
        ),
      ),
    );
  }

  void endCall()async{
    await TwilioVoiceServices.hangUp();
    setState(() {
      _isCalling = false;
      _callStatus = "";
    });
  }

  void makeCall(String identify)async{
    setState(() {
     _isCalling = true;
    });
    final status = await TwilioVoiceServices.makeCall(to: identify);
    if(!status){
      setState(() {
        _isCalling = false;
      });
    }
  }

  toggleSpeaker()async{
     _isSpeaker = await TwilioVoiceServices.toggleSpeaker()??_isSpeaker;
     setState(() {});
  }

  toggleMuted()async{
    _isMuted = await TwilioVoiceServices.toggleMute()??_isMuted;
    setState(() {});
  }
}
