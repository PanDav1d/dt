import 'package:doctag_app/qr_scan.dart';
import 'package:doctag_app/remote_clients/api.dart';
import 'package:flutter/material.dart';
import 'package:qr_code_scanner/qr_code_scanner.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'dart:developer';
import 'dart:convert';

import 'main.dart';


class LoginPage extends StatefulWidget {
  @override
  State<StatefulWidget> createState()  => _LoginPageState();
}

enum LoginState{
  AWAIT_SCAN,
  CHECKING,
  LOGIN_OK,
  ERROR
}

class _LoginPageState extends State<LoginPage> {

  String? scanResult;
  String? error;
  LoginState currentState = LoginState.AWAIT_SCAN;

  void _scanCode() async {

    setState(() {
      currentState = LoginState.CHECKING;
    });

    final result = await Navigator.push(
      context,
      MaterialPageRoute(builder: (context) => QRCodeScanner()),
    ) as Barcode;

    log("Scanned data " + result.code);

    try {
      Map<String, dynamic> loginCode = jsonDecode(result.code);
      final url = loginCode["doctagUrl"] as String?;
      final sessionId = loginCode["sessionId"] as String?;

      log("DocServer URL is $url");
      log("SessionID is $sessionId");

      try {
        final cli = DocServerClient(url: url!, sessionId: sessionId!);
        final auth = await cli.fetchAuthInfo();
        log("Auth info is ${ auth.authenticated } ${auth.firstName} ${auth
            .lastName}");

        if(auth.authenticated == true) {

          final storage = new FlutterSecureStorage();
          await storage.write(key: "docserverUrl", value: url);
          await storage.write(key: "docserverSessionId", value: sessionId);

          setState(() {
            error = null;
            currentState = LoginState.LOGIN_OK;
          });
        } else {
          setState(() {
            error = "Anmelde-Code nicht gültig. Bitte kontaktieren Sie Ihren Administrator.";
            currentState = LoginState.ERROR;
          });
        }

      }catch(ex){
        log("Failed to check  Login session " + result.code);
        log("Exception $ex");
        setState(() {
          error = "Anmelde-Code konnte nicht geprüft werden. Docserver ${url} hat einen Fehler gemeldet.";
          currentState = LoginState.ERROR;
        });
      }

      setState(() {
        scanResult = result.code;
      });

    }catch(ex){
      log("Failed to process Login Code " + result.code);
      log("Exception $ex");
      setState(() {
        error = "Anmelde-Code nicht gültig. Bitte scannen Sie einen gültigen Doctag-Anmeldecode.";
        currentState = LoginState.ERROR;
      });
    }
  }

  void _done() async {
    Navigator.pushReplacement(context, MaterialPageRoute(builder: (context) => MainPage()));
  }

  void _retry() async {
    setState(() {
      error = null;
      currentState = LoginState.AWAIT_SCAN;
    });
  }

  @override
  Widget build(BuildContext context) {
    Widget stateUi;
    Widget actionButton = Container();

    switch(this.currentState){
      case LoginState.CHECKING:
        stateUi = Container(child:Column(
            mainAxisAlignment:MainAxisAlignment.center,
            children:[
              Text("Anmeldedaten werden überprüft..."),
              SizedBox(height: 16,),
              SizedBox(child:CircularProgressIndicator(), width: 32, height: 32)
            ]));
        break;
      case LoginState.AWAIT_SCAN:
        stateUi = Text(
          'Bitten scannen Sie den Anmelde-Code um eine Verbindung zu Ihrem Doctag Server herzustellen',
          textAlign: TextAlign.center,
        );
        actionButton = ElevatedButton(onPressed:_scanCode, child: Text("Anmelde-Code scannen"));
        break;
      case LoginState.LOGIN_OK:
        stateUi = Text(
          'Registrierung erfolgreich.',
          textAlign: TextAlign.center,
        );
        actionButton = ElevatedButton.icon(
            onPressed:_done,
            label: Text("Weiter"),
            icon: Icon(Icons.check),
            style:ElevatedButton.styleFrom(primary: Colors.green, textStyle: TextStyle(color: Colors.white))
        );
        break;
      case LoginState.ERROR:
        stateUi = Text(this.error ?? "Überprüfung der Anmeldedaten fehlgeschlagen", textAlign: TextAlign.center,);
        actionButton = ElevatedButton(
            onPressed:_retry,
            child: Text("Erneut versuchen")
        );
        break;
    }

    return Scaffold(
      appBar: AppBar(
        title: Text("Doctag Server konfigurieren"),
      ),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: <Widget>[
            Container(child:Image(image: AssetImage('assets/images/logo-doctag.png'),), margin: EdgeInsets.only(left: 24, right:24),),
            Container(
              height: 120,
              margin: EdgeInsets.only(left: 16, right:16, top: 32, bottom: 32),
              child: stateUi,
            ),
            actionButton
          ],
        ),
      ),
    );
  }
}