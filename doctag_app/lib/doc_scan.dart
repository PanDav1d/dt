import 'dart:io';

import 'package:flutter/material.dart';

import 'package:document_scanner/document_scanner.dart';

class DocScanView extends StatefulWidget {
  @override
  _DocScanViewState createState() => _DocScanViewState();
}

class _DocScanViewState extends State<DocScanView> {
  late File? scannedDocument = null;

  @override
  void initState() {
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
          appBar: AppBar(
            title: const Text('Dokument erfassen'),
          ),
          body:  Stack(
                    children: <Widget>[
                      Column(
                        children: <Widget>[
                          Expanded(
                            child: scannedDocument != null
                                ? Image(
                              image: FileImage(scannedDocument!),
                            )
                                : DocumentScanner(
                              onDocumentScanned:
                                  (ScannedImage scannedImage) {
                                print("document : " );

                                setState(() {
                                  scannedDocument = scannedImage
                                      .getScannedDocumentAsFile();
                                });

                                print("Displaying scanned doc");
                              },
                            ),
                          ),
                        ],
                      ),
                      Positioned(
                        bottom: 20,
                        left: 0,
                        right: 0,
                        child: Row(
                            mainAxisAlignment: MainAxisAlignment.center,
                            children:[
                          ElevatedButton(
                              child: Text("Übernehmen"),
                              onPressed: () {

                                setState(() {
                                  Navigator.pop(context, this.scannedDocument);
                                });
                              }),
                          TextButton(
                            child: Text("Neues Foto"),
                            onPressed: () {
                              print("Pressed new photo button");
                              setState(() {
                                scannedDocument = null;
                              });
                            }),
                          TextButton(
                              child: Text("Abbrechen"),
                              onPressed: () {
                                print("Pressed abort button");
                                Navigator.pop(context, null);
                              })

                        ]),
                      ),
                    ],
                  )
      )

    );
  }
}