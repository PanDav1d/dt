//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//
// @dart=2.0

// ignore_for_file: unused_element, unused_import
// ignore_for_file: always_put_required_named_parameters_first
// ignore_for_file: lines_longer_than_80_chars

part of docsrv_api;

class DoctagSignatureData {
  /// Returns a new [DoctagSignatureData] instance.
  DoctagSignatureData({
    this.documentHash,
    this.documentUrl,
    this.keyFingerprint,
    this.previousSignaturesHash,
    this.randomBuffer,
    this.signature,
    this.signingDoctagInstance,
    this.signingUser,
    this.validFrom,
    this.validFromDateTime,
    this.workflowHash,
  });

  String documentHash;

  String documentUrl;

  String keyFingerprint;

  String previousSignaturesHash;

  String randomBuffer;

  String signature;

  String signingDoctagInstance;

  String signingUser;

  String validFrom;

  DateTime validFromDateTime;

  String workflowHash;

  @override
  bool operator ==(Object other) => identical(this, other) || other is DoctagSignatureData &&
     other.documentHash == documentHash &&
     other.documentUrl == documentUrl &&
     other.keyFingerprint == keyFingerprint &&
     other.previousSignaturesHash == previousSignaturesHash &&
     other.randomBuffer == randomBuffer &&
     other.signature == signature &&
     other.signingDoctagInstance == signingDoctagInstance &&
     other.signingUser == signingUser &&
     other.validFrom == validFrom &&
     other.validFromDateTime == validFromDateTime &&
     other.workflowHash == workflowHash;

  @override
  int get hashCode =>
    (documentHash == null ? 0 : documentHash.hashCode) +
    (documentUrl == null ? 0 : documentUrl.hashCode) +
    (keyFingerprint == null ? 0 : keyFingerprint.hashCode) +
    (previousSignaturesHash == null ? 0 : previousSignaturesHash.hashCode) +
    (randomBuffer == null ? 0 : randomBuffer.hashCode) +
    (signature == null ? 0 : signature.hashCode) +
    (signingDoctagInstance == null ? 0 : signingDoctagInstance.hashCode) +
    (signingUser == null ? 0 : signingUser.hashCode) +
    (validFrom == null ? 0 : validFrom.hashCode) +
    (validFromDateTime == null ? 0 : validFromDateTime.hashCode) +
    (workflowHash == null ? 0 : workflowHash.hashCode);

  @override
  String toString() => 'DoctagSignatureData[documentHash=$documentHash, documentUrl=$documentUrl, keyFingerprint=$keyFingerprint, previousSignaturesHash=$previousSignaturesHash, randomBuffer=$randomBuffer, signature=$signature, signingDoctagInstance=$signingDoctagInstance, signingUser=$signingUser, validFrom=$validFrom, validFromDateTime=$validFromDateTime, workflowHash=$workflowHash]';

  Map<String, dynamic> toJson() {
    final json = <String, dynamic>{};
    if (documentHash != null) {
      json[r'documentHash'] = documentHash;
    }
    if (documentUrl != null) {
      json[r'documentUrl'] = documentUrl;
    }
    if (keyFingerprint != null) {
      json[r'keyFingerprint'] = keyFingerprint;
    }
    if (previousSignaturesHash != null) {
      json[r'previousSignaturesHash'] = previousSignaturesHash;
    }
    if (randomBuffer != null) {
      json[r'randomBuffer'] = randomBuffer;
    }
    if (signature != null) {
      json[r'signature'] = signature;
    }
    if (signingDoctagInstance != null) {
      json[r'signingDoctagInstance'] = signingDoctagInstance;
    }
    if (signingUser != null) {
      json[r'signingUser'] = signingUser;
    }
    if (validFrom != null) {
      json[r'validFrom'] = validFrom;
    }
    if (validFromDateTime != null) {
      json[r'validFromDateTime'] = validFromDateTime.toUtc().toIso8601String();
    }
    if (workflowHash != null) {
      json[r'workflowHash'] = workflowHash;
    }
    return json;
  }

  /// Returns a new [DoctagSignatureData] instance and imports its values from
  /// [json] if it's non-null, null if [json] is null.
  static DoctagSignatureData fromJson(Map<String, dynamic> json) => json == null
    ? null
    : DoctagSignatureData(
        documentHash: json[r'documentHash'],
        documentUrl: json[r'documentUrl'],
        keyFingerprint: json[r'keyFingerprint'],
        previousSignaturesHash: json[r'previousSignaturesHash'],
        randomBuffer: json[r'randomBuffer'],
        signature: json[r'signature'],
        signingDoctagInstance: json[r'signingDoctagInstance'],
        signingUser: json[r'signingUser'],
        validFrom: json[r'validFrom'],
        validFromDateTime: json[r'validFromDateTime'] == null
          ? null
          : DateTime.parse(json[r'validFromDateTime']),
        workflowHash: json[r'workflowHash'],
    );

  static List<DoctagSignatureData> listFromJson(List<dynamic> json, {bool emptyIsNull, bool growable,}) =>
    json == null || json.isEmpty
      ? true == emptyIsNull ? null : <DoctagSignatureData>[]
      : json.map((v) => DoctagSignatureData.fromJson(v)).toList(growable: true == growable);

  static Map<String, DoctagSignatureData> mapFromJson(Map<String, dynamic> json) {
    final map = <String, DoctagSignatureData>{};
    if (json != null && json.isNotEmpty) {
      json.forEach((String key, dynamic v) => map[key] = DoctagSignatureData.fromJson(v));
    }
    return map;
  }

  // maps a json object with a list of DoctagSignatureData-objects as value to a dart map
  static Map<String, List<DoctagSignatureData>> mapListFromJson(Map<String, dynamic> json, {bool emptyIsNull, bool growable,}) {
    final map = <String, List<DoctagSignatureData>>{};
    if (json != null && json.isNotEmpty) {
      json.forEach((String key, dynamic v) {
        map[key] = DoctagSignatureData.listFromJson(v, emptyIsNull: emptyIsNull, growable: growable);
      });
    }
    return map;
  }
}

