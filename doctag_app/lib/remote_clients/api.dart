
import 'dart:convert';
import 'dart:io';

import 'package:doctag_app/remote_clients/model.dart';



class DocServerClient{
  String url;
  String sessionId;

  DocServerClient({required this.url, required this.sessionId});

  Future<AuthInfoResponse> fetchAuthInfo() async {
    final cli = HttpClient();

    final req = await cli.getUrl(Uri.parse("https://$url/app/auth_info"));
    req.cookies.add(Cookie("SESSION", sessionId));
    final resp = await req.close();
    final responseBody = await resp.transform(utf8.decoder).first;


    Map<String, dynamic> responseData = json.decode(responseBody);

    return AuthInfoResponse(authenticated: resp.statusCode == 200, firstName: responseData["firstName"], lastName: responseData["lastName"]);
  }

  Future<Void> fetchDoctagDocument(String uri) async {

  }
}