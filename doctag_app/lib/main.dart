import 'dart:developer';

import 'package:docsrv_api/api.dart';
import 'package:doctag_app/pdf_view.dart';
import 'package:doctag_app/remote_clients/api.dart';
import 'package:flutter/material.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:qr_code_scanner/qr_code_scanner.dart';

import 'login.dart';
import 'qr_scan.dart';

void main() async {
  runApp(MyApp());
}

class MyApp extends StatelessWidget {
  MyApp();

  @override
  Widget build(BuildContext context) {

    Widget main = MainPage();

    return MaterialApp(
      title: 'DocTag',
      theme: ThemeData(
        primarySwatch: Colors.blue,
      ),
      home: main,
    );
  }
}

class MainPage extends StatefulWidget {
  MainPage({Key? key, this.title}) : super(key: key);


  final String? title;

  @override
  _MainPageState createState() => _MainPageState();
}

enum MainPageState{
  CheckingConnection,
  ConnectionOk,
  ConnectionNotOk,
  AuthNotOk
}

const DOCSERVER_URL_KEY = "docserverUrl";
const DOCSERVER_SESSION_ID_KEY = "docserverSessionId";

class _MainPageState extends State<MainPage> {
  String? url;
  String? sessionId;
  MainPageState state = MainPageState.CheckingConnection;
  bool isLoading = false;

  @override
  void initState(){
    super.initState();

    _asyncInit();
  }

  _asyncInit() async {
    final storage = new FlutterSecureStorage();

    final url = await storage.read(key: DOCSERVER_URL_KEY);
    final sessionId = await storage.read(key: DOCSERVER_SESSION_ID_KEY);

    if(sessionId == null || url == null){
      Navigator.pushReplacement(context, MaterialPageRoute(builder: (context) => LoginPage()));
      return;
    }
    else {
      setState(() {
        this.sessionId = sessionId;
        this.url = url.startsWith("https") ? url : "https://$url";
      });

      _checkConnection();
    }
  }

  _checkConnection() async {
    try {
      final cli = DocServerClient(url: url!, sessionId: sessionId!);
      final auth = await cli.fetchAuthInfo();
      log("Auth info is ${ auth?.authenticated } ${auth?.firstName} ${auth
          ?.lastName}");

      if (auth?.authenticated == true) {
        setState(() {
          state = MainPageState.ConnectionOk;
        });
      }
    } on ApiException catch(apiEx){
      if(apiEx.code == 401){
        setState(() {
          state = MainPageState.AuthNotOk;
        });
      }
    }
    catch(ex){
      log(ex.toString());
      setState(() {
        state = MainPageState.ConnectionNotOk;
      });
    }
  }

  _scanDoctag() async {
    final result = await Navigator.push(
      context,
      MaterialPageRoute(builder: (context) => QRCodeScanner()),
    ) as Barcode;

    setState(() {
      isLoading=true;
    });

    final cli = RemoteDocServerClient();
    try {
      log("Scanned barcode is ${result.code}");
      final doc = await cli.fetchDocumentPdf(result.code);

      await Navigator.push(
        context,
        MaterialPageRoute(
          builder: (context) => PDFScreen(docAndFile: doc),
        ),
      );

      log("fetched document ok");
    } on ApiException catch(ex){
      log("Failed to fetch document");
      log("Exception is ${ex.message}, Status Code ${ex.code}");
      log(ex.innerException?.toString()??"");
    }
    finally{
      setState(() {
        isLoading=false;
      });

    }
  }

  @override
  Widget build(BuildContext context) {

    Widget bottomBarContent;
    switch(this.state) {
      case MainPageState.ConnectionOk:
        bottomBarContent = Text("Verbunden mit $url", textAlign: TextAlign.center, style: TextStyle(fontSize: 16),);
        break;
      case MainPageState.CheckingConnection:
        bottomBarContent = Text("Prüfe Verbindung zu $url", textAlign: TextAlign.center, style: TextStyle(fontSize: 16));
        break;
      case MainPageState.ConnectionNotOk:
        bottomBarContent = Text("Nicht verbunden mit $url", textAlign: TextAlign.center,style: TextStyle(fontSize: 16));
        break;
      case MainPageState.AuthNotOk:
        bottomBarContent = Text("Session bei $url ungültig", textAlign: TextAlign.center,style: TextStyle(fontSize: 16));
        break;
    }

    return Scaffold(
      appBar: AppBar(
        title: Text("DocTag"),
        actions: [Padding(
            padding: EdgeInsets.only(right: 20.0),
            child: GestureDetector(
              onTap: () {
                log("Clicked on logout");
                final storage = new FlutterSecureStorage();
                storage.delete(key: DOCSERVER_SESSION_ID_KEY);
                storage.delete(key: DOCSERVER_URL_KEY);
                Navigator.pushReplacement(context, MaterialPageRoute(builder: (context) => LoginPage()));
              },
              child: Icon(
                  Icons.logout
              ),
            )
        ),],
      ),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
            children:[

              (this.isLoading) ?
            Padding(
              padding: const EdgeInsets.all(8.0),
              child: SizedBox(
                height: 125.0,
                width: 125.0,
                child: CircularProgressIndicator(
                  valueColor: AlwaysStoppedAnimation<Color>(Colors.blue),
                ),
              ),
            )
                  :
            MaterialButton(
              onPressed: _scanDoctag,
              color: Colors.blue,
              textColor: Colors.white,
              child: Icon(
                Icons.qr_code,
                size: 125,
              ),
              padding: EdgeInsets.all(16),
              shape: CircleBorder(),
              elevation: 24,
          ),
        SizedBox(height: 16),
        Text((this.isLoading)?"Lade Dokument":"Bereit zum Scannen",style: TextStyle(color: Colors.black.withOpacity(0.8), fontSize: 28))

        ]
      )),
      bottomNavigationBar: BottomAppBar(child: bottomBarContent,notchMargin: 16),
    );
  }
}
