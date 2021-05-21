
import 'dart:async';
import 'dart:developer';
import 'dart:io';
import 'dart:typed_data';

import 'package:docsrv_api/api.dart';
import 'package:http/http.dart' as http;
import 'package:path_provider/path_provider.dart';


class DocServerClient{
  String url;
  String sessionId;
  

  DocServerClient({required this.url, required this.sessionId});

  Future<AuthInfoResponse?> fetchAuthInfo() async {
    
    final cli = DefaultApi(ApiClient(basePath: this.url));
    cli.apiClient.addDefaultHeader("Cookie", "SESSION=$sessionId");
    
    return cli.fetchAuthInfo();
  }
}


class DoctagDocumentAndPdfFile {
  DoctagDocumentAndPdfFile({required this.file, required this.document});

  File file;
  EmbeddedDocument document;
}

class RemoteDocServerClient{

  Future<EmbeddedDocument?> fetchDoctagDocument(String uri) async {
    final splittingChar = uri.indexOf("/d/");
    final hostName = uri.substring(0, splittingChar);
    final documentId = uri.substring(splittingChar+3);

    log("HostName $hostName");
    log("DocumentId $documentId");

    final cli = DefaultApi(ApiClient(basePath: hostName));
    return cli.fetchDoctagDocument(documentId);
  }

  Future<DoctagDocumentAndPdfFile> fetchDocumentPdf(String uri) async {
    Completer<DoctagDocumentAndPdfFile> completer = Completer();

    final splittingChar = uri.indexOf("/d/");
    final hostName = uri.substring(0, splittingChar);
    final documentId = uri.substring(splittingChar+3);

    log("HostName $hostName");
    log("DocumentId $documentId");

    final cli = DefaultApi(ApiClient(basePath: hostName));
    cli.apiClient.addDefaultHeader("Accept", "application/json");
    cli.apiClient.addDefaultHeader("Content-type", "application/json");
    final doc = await cli.fetchDoctagDocument(documentId);
    log("Doctag document fetched");

    var url = Uri.parse('${hostName}/d/${doc!.document!.id!}/download');
    var response = await http.get(url);

    log("${response.contentLength} bytes received");

    var dir = await getApplicationDocumentsDirectory();
    File file = File("${dir.path}/${doc.document!.originalFileName!}");
    await file.writeAsBytes(response.bodyBytes, flush: true);

    completer.complete(DoctagDocumentAndPdfFile(file: file, document: doc));

    return completer.future;
  }
}