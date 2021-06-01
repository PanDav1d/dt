import 'dart:developer';
import 'dart:io';

import 'package:DocTag/doc_scan.dart';
import 'package:DocTag/remote_clients/api.dart';
import 'package:DocTag/widgets/SelectKeyWidget.dart';
import 'package:DocTag/widgets/SelectRoleWidget.dart';
import 'package:docsrv_api/api.dart';
import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:flutter_easyloading/flutter_easyloading.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'constants.dart';
import 'package:flutter_signature_pad/flutter_signature_pad.dart' as SignatureWidget;
import 'dart:ui' as ui;
import 'dart:convert';

class ListItem {
  int value;
  String name;

  ListItem(this.value, this.name);
}

class SignDocumentView extends StatefulWidget {

  final DoctagDocumentAndPdfFile docAndFile;
  SignDocumentView({Key? key, required this.docAndFile}) : super(key: key);

  @override
  _SignDocumentView createState() => _SignDocumentView();
}

class _SignDocumentView extends State<SignDocumentView> {

  List<String> availableRoles = List.empty();
  List<String> availableKeys = List.empty();
  Map<String, String> inputs = new Map();
  Map<String, GlobalKey<SignatureWidget.SignatureState>> signaturePads = new Map();
  Map<String, File> attachments = new Map();
  int selectedRoleIdx = 0;
  int selectedKeyIdx = 0;
  PreparedSignature? workflow;


  void initState() {
    super.initState();
    availableRoles = widget.docAndFile.document.document?.workflow?.actions?.asMap().entries.map((e) => e.value!.role!).toList() ?? List.empty();
    loadWorkflow();
  }

  Future<void> loadWorkflow() async {
    try {
      EasyLoading.show( status: 'Lade Workflow...');

      final storage = new FlutterSecureStorage();

      final url = await storage.read(key: DOCSERVER_URL_KEY);
      final sessionId = await storage.read(key: DOCSERVER_SESSION_ID_KEY);
      final cli = DocServerClient(url: url!, sessionId: sessionId!);

      final workflow = await cli.fetchPreparedWorkflow(
          this.widget.docAndFile.document.document!.url!);

      setState(() {
        this.workflow = workflow;
        this.availableRoles =
            workflow.workflow?.actions?.map((e) => e!.role!).toList() ??
                List.empty();

        this.availableKeys = this.workflow?.availableKeys?.map((e) => e!.verboseName!)?.toList() ?? List.empty();
      });
    }finally {
      EasyLoading.dismiss();
    }
  }


  Widget buildSingleInputWidget(WorkflowInput input){
    switch(input.kind){
      case WorkflowInputKindEnum.checkbox:
        return CheckboxListTile(
          title: Text(input.name!),
          subtitle: Text(input.description!),
          value: inputs[input.name] == "true",
          onChanged: (val) {
            setState(
                  () {
                inputs[input.name!] = val?.toString() ?? "false";
              },
            );
          },
        );
        case WorkflowInputKindEnum.textInput:
        return new Container(
            margin: EdgeInsets.only(left: 16, top: 16, right: 16),
            child:new Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                new Text(input.name!, style: Theme.of(context).textTheme.subtitle1,),
                new Text(input.description??"", style: Theme.of(context).textTheme.bodyText2?.apply(color: Colors.grey.shade700)),
                new TextField(
                  onChanged: (text){
                    setState(() {
                      inputs[input.name!] = text;
                    });
                  },
                  decoration: InputDecoration(
                    filled: true,
                    fillColor: Colors.lightGreen.shade100
                  ),
                )


              ],
        ));
      case WorkflowInputKindEnum.selectFromList:
        return DropdownButton<String>(
          value: inputs[input.name!],
          items: <DropdownMenuItem<String>>[
            DropdownMenuItem(
              value: 'Option 1',
              child: Text('Option 1'),
            ),
          ],
          onChanged: (value) {
            setState(() {
              inputs[input.name!] = value!;
            });
          },
        );
      case WorkflowInputKindEnum.sign:
        final key = signaturePads[input.name];

        if(key == null){
          signaturePads[input.name!] = GlobalKey<SignatureWidget.SignatureState>();
        }

        return new Container(
            height: 350,
            padding: EdgeInsets.all(16),
            child: new Column(children:[
              new Row(
                children: [
                  new Column(
                    mainAxisAlignment: MainAxisAlignment.start,
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      new Text(input.name!, style: Theme.of(context).textTheme.subtitle1,),
                      new Text(input.description!, style: Theme.of(context).textTheme.bodyText2?.apply(color: Colors.grey.shade700)),
                    ]
                  ),
                  Expanded(child: Container()),
                  IconButton(
                    icon: const Icon(Icons.restart_alt),
                    tooltip: 'Neu signieren',
                    onPressed: () {
                      setState(() {
                        signaturePads[input.name!]!.currentState!.clear();
                      });
                    },
                  ),
                ],
              ),

              Container(
              color: Colors.lightGreen.shade100,
              padding: const EdgeInsets.all(8.0),
              height: 250,
              child: SignatureWidget.Signature(
                color: Colors.black,
                key: signaturePads[input.name!],
                onSign: () {
                  final sign = signaturePads[input.name!]?.currentState;
                  debugPrint('${sign?.points?.length} points in the signature');
                },
                strokeWidth: 5.0,
              ))
            ],
            ),

          );
      case WorkflowInputKindEnum.fileInput:
        return new Container(
          margin: EdgeInsets.only(left: 16, top: 16, right: 16),
          child: new Row(children:[
              new Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  new Text(input.name!, style: Theme.of(context).textTheme.subtitle1,),
                  new Text(input.description??"", style: Theme.of(context).textTheme.bodyText2?.apply(color: Colors.grey.shade700)),
                ]
            ),
            Expanded(child: new Container()),
            new IconButton(onPressed: ()async{
              final fileOutput = await Navigator.push(
                context,
                MaterialPageRoute(builder: (context) => DocScanView()),
              ) as File?;

              if(fileOutput != null){
                attachments[input.name!] = fileOutput;
              }

            }, icon: const Icon(Icons.add_a_photo))
          ])
        );
      default:
        return Container();
    }
  }

  List<Widget> buildWorkflowInputWidgets() {

    final inputs = this.workflow?.workflow?.actions?[this.selectedRoleIdx]?.inputs;

    return inputs?.map((e) => buildSingleInputWidget(e!))?.toList() ?? List.empty();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: new AppBar(
        title: new Text("Dokument signieren"),
        actions: <Widget>[
          new IconButton(icon: const Icon(Icons.save), onPressed: () {})
        ],
      ),
      body: SingleChildScrollView(
        child:new Column(
          children: <Widget>[
            new Container(
                padding: EdgeInsets.only(top: 16),
              child: Text(
                  'Rolle wählen',
                  style: TextStyle(
                      color: Colors.grey[800],
                      fontWeight: FontWeight.bold,
                      fontSize: 20))
            ),
            new Container(
              child: new RoleSelectChip(
                  roles: this.availableRoles,
                  onSelectedRoleChange: (role){
                    setState(() {
                      this.selectedRoleIdx = role;
                      this.inputs = new Map();
                    });
                  },
              ),
            ),
            new Container(
                padding: EdgeInsets.only(top: 16),
                child: Text(
                    'Schlüssel wählen',
                    style: TextStyle(
                        color: Colors.grey[800],
                        fontWeight: FontWeight.bold,
                        fontSize: 20))
            ),
            new Container(
              child: new KeySelectChip(
                keys: this.availableKeys,
                onSelectedKeyChange: (key){
                  setState(() {
                    this.selectedKeyIdx = key;
                  });
                },
              ),
            ),
            const Divider(
              height: 1.0,
            ),
            ...this.buildWorkflowInputWidgets(),
            const Divider(
              height: 1.0,
            ),
            new Container(
                padding: EdgeInsets.only(left: 16, right: 16),
                child:new Row(
              mainAxisAlignment: MainAxisAlignment.center,

              children: [
                new Expanded(
                  child: new ElevatedButton(
                    onPressed: () async {


                      final storage = new FlutterSecureStorage();
                      final url = await storage.read(key: DOCSERVER_URL_KEY);
                      final sessionId = await storage.read(key: DOCSERVER_SESSION_ID_KEY);
                      final cli = DocServerClient(url: url!, sessionId: sessionId!);


                      final fileFutures = this.signaturePads.entries.map((e) async {
                        final image = await e.value.currentState!.getData();
                        final data = await image.toByteData(format: ui.ImageByteFormat.png);
                        e.value.currentState!.clear();
                        final encoded = base64.encode(data!.buffer.asUint8List());

                        log("Size of signature: ${encoded.length}");

                        return new FileData(
                          id: e.key,
                          base64Content: encoded,
                          contentType: "image/png",
                          name: "signature_${e.key}.png"
                        );
                      });

                      final attachmentFutures = this.attachments.entries.map((e) async {
                        final bytes = await e.value.readAsBytes();
                        final encoded = base64.encode(bytes);

                        return new FileData(
                            id: e.key,
                            base64Content: encoded,
                            contentType: "image/jpeg",
                            name: "attachment_${e.key}.jpg"
                        );
                      });


                      final si = new SignatureInputs();
                      si.role = this.availableRoles[this.selectedRoleIdx];
                      si.ppkId = this.workflow!.availableKeys![this.selectedKeyIdx]!.ppkId;
                      si.files = await Future.wait(fileFutures);
                      si.files!.addAll(await Future.wait(attachmentFutures));
                      
                      si.inputs = [
                        ...this.inputs.entries.map(
                                (key)  => new WorkflowInputResult(name: key.key, value: key.value, fileId: null)),
                        ...this.signaturePads.entries.map((e) => new WorkflowInputResult(
                          name: e.key,
                          value: null,
                          fileId: e.key
                        )),
                        ...this.attachments.entries.map((e) => new WorkflowInputResult(
                            name: e.key,
                            value: null,
                            fileId: e.key
                        ))
                      ];



                      try {
                        log("Submitting signature");
                        EasyLoading.show();

                        await cli.submitSignature(this.widget.docAndFile
                            .document.document!.url!, si);
                        await EasyLoading.dismiss();
                        log("Signature successfully submitted");
                        await EasyLoading.showToast("Signatur abgegeben", dismissOnTap: true);
                        Navigator.pop(context);
                      }
                      catch(ex){
                        EasyLoading.dismiss();
                        EasyLoading.showToast("Signieren fehlgeschlagen", dismissOnTap: true);
                      }

                    },
                    child: Text("Signieren"),
                  ),
                )
                ,
                new Expanded(
                  child:new TextButton(
                      onPressed: () {
                        Navigator.pop(context);
                      },
                      child: Text("Abbrechen")
                  )
                )

              ],
            )
            )

          ],
      ),
    )
    );
  }
}