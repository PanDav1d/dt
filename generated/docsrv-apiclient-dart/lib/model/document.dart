//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//
// @dart=2.0

// ignore_for_file: unused_element, unused_import
// ignore_for_file: always_put_required_named_parameters_first
// ignore_for_file: lines_longer_than_80_chars

part of docsrv_api;

class Document {
  /// Returns a new [Document] instance.
  Document({
    this.id,
    this.attachmentHash,
    this.attachmentId,
    this.created,
    this.fullText,
    this.isMirrored,
    this.mirrors = const [],
    this.originalFileName,
    this.signatures = const [],
    this.url,
    this.workflow,
  });

  String id;

  String attachmentHash;

  String attachmentId;

  DateTime created;

  String fullText;

  bool isMirrored;

  List<String> mirrors;

  String originalFileName;

  List<Signature> signatures;

  String url;

  Workflow workflow;

  @override
  bool operator ==(Object other) => identical(this, other) || other is Document &&
     other.id == id &&
     other.attachmentHash == attachmentHash &&
     other.attachmentId == attachmentId &&
     other.created == created &&
     other.fullText == fullText &&
     other.isMirrored == isMirrored &&
     other.mirrors == mirrors &&
     other.originalFileName == originalFileName &&
     other.signatures == signatures &&
     other.url == url &&
     other.workflow == workflow;

  @override
  int get hashCode =>
    (id == null ? 0 : id.hashCode) +
    (attachmentHash == null ? 0 : attachmentHash.hashCode) +
    (attachmentId == null ? 0 : attachmentId.hashCode) +
    (created == null ? 0 : created.hashCode) +
    (fullText == null ? 0 : fullText.hashCode) +
    (isMirrored == null ? 0 : isMirrored.hashCode) +
    (mirrors == null ? 0 : mirrors.hashCode) +
    (originalFileName == null ? 0 : originalFileName.hashCode) +
    (signatures == null ? 0 : signatures.hashCode) +
    (url == null ? 0 : url.hashCode) +
    (workflow == null ? 0 : workflow.hashCode);

  @override
  String toString() => 'Document[id=$id, attachmentHash=$attachmentHash, attachmentId=$attachmentId, created=$created, fullText=$fullText, isMirrored=$isMirrored, mirrors=$mirrors, originalFileName=$originalFileName, signatures=$signatures, url=$url, workflow=$workflow]';

  Map<String, dynamic> toJson() {
    final json = <String, dynamic>{};
    if (id != null) {
      json[r'_id'] = id;
    }
    if (attachmentHash != null) {
      json[r'attachmentHash'] = attachmentHash;
    }
    if (attachmentId != null) {
      json[r'attachmentId'] = attachmentId;
    }
    if (created != null) {
      json[r'created'] = created.toUtc().toIso8601String();
    }
    if (fullText != null) {
      json[r'fullText'] = fullText;
    }
    if (isMirrored != null) {
      json[r'isMirrored'] = isMirrored;
    }
    if (mirrors != null) {
      json[r'mirrors'] = mirrors;
    }
    if (originalFileName != null) {
      json[r'originalFileName'] = originalFileName;
    }
    if (signatures != null) {
      json[r'signatures'] = signatures;
    }
    if (url != null) {
      json[r'url'] = url;
    }
    if (workflow != null) {
      json[r'workflow'] = workflow;
    }
    return json;
  }

  /// Returns a new [Document] instance and imports its values from
  /// [json] if it's non-null, null if [json] is null.
  static Document fromJson(Map<String, dynamic> json) => json == null
    ? null
    : Document(
        id: json[r'_id'],
        attachmentHash: json[r'attachmentHash'],
        attachmentId: json[r'attachmentId'],
        created: json[r'created'] == null
          ? null
          : DateTime.parse(json[r'created']),
        fullText: json[r'fullText'],
        isMirrored: json[r'isMirrored'],
        mirrors: json[r'mirrors'] == null
          ? null
          : (json[r'mirrors'] as List).cast<String>(),
        originalFileName: json[r'originalFileName'],
        signatures: Signature.listFromJson(json[r'signatures']),
        url: json[r'url'],
        workflow: Workflow.fromJson(json[r'workflow']),
    );

  static List<Document> listFromJson(List<dynamic> json, {bool emptyIsNull, bool growable,}) =>
    json == null || json.isEmpty
      ? true == emptyIsNull ? null : <Document>[]
      : json.map((v) => Document.fromJson(v)).toList(growable: true == growable);

  static Map<String, Document> mapFromJson(Map<String, dynamic> json) {
    final map = <String, Document>{};
    if (json != null && json.isNotEmpty) {
      json.forEach((String key, dynamic v) => map[key] = Document.fromJson(v));
    }
    return map;
  }

  // maps a json object with a list of Document-objects as value to a dart map
  static Map<String, List<Document>> mapListFromJson(Map<String, dynamic> json, {bool emptyIsNull, bool growable,}) {
    final map = <String, List<Document>>{};
    if (json != null && json.isNotEmpty) {
      json.forEach((String key, dynamic v) {
        map[key] = Document.listFromJson(v, emptyIsNull: emptyIsNull, growable: growable);
      });
    }
    return map;
  }
}

