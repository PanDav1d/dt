//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//


// ignore_for_file: unused_element, unused_import
// ignore_for_file: always_put_required_named_parameters_first
// ignore_for_file: lines_longer_than_80_chars

part of docsrv_api;

class EmbeddedDocument {
  /// Returns a new [EmbeddedDocument] instance.
  EmbeddedDocument({
    this.document,
    this.files = const [],
  });

  Document? document;

  List<FileData?>? files;

  @override
  bool operator ==(Object other) => identical(this, other) || other is EmbeddedDocument &&
     other.document == document &&
     other.files == files;

  @override
  int get hashCode =>
    (document == null ? 0 : document.hashCode) +
    (files == null ? 0 : files.hashCode);

  @override
  String toString() => 'EmbeddedDocument[document=$document, files=$files]';

  Map<String, dynamic> toJson() {
    final json = <String, dynamic>{};
    if (document != null) {
      json[r'document'] = document;
    }
    if (files != null) {
      json[r'files'] = files;
    }
    return json;
  }

  /// Returns a new [EmbeddedDocument] instance and imports its values from
  /// [json] if it's non-null, null if [json] is null.
  static EmbeddedDocument? fromJson(Map<String, dynamic>? json) => json == null
    ? null
    : EmbeddedDocument(
        document: Document.fromJson(json[r'document']),
        files: FileData.listFromJson(json[r'files']),
    );

  static List<EmbeddedDocument?>? listFromJson(List<dynamic> json, {bool? emptyIsNull, bool? growable,}) =>
    json == null || json.isEmpty
      ? true == emptyIsNull ? null : <EmbeddedDocument>[]
      : json.map((v) => EmbeddedDocument.fromJson(v)).toList(growable: true == growable);

  static Map<String, EmbeddedDocument?> mapFromJson(Map<String, dynamic> json) {
    final map = <String, EmbeddedDocument?>{};
    if (json != null && json.isNotEmpty) {
      json.forEach((String key, dynamic v) => map[key] = EmbeddedDocument.fromJson(v));
    }
    return map;
  }

  // maps a json object with a list of EmbeddedDocument-objects as value to a dart map
  static Map<String, List<EmbeddedDocument?>?> mapListFromJson(Map<String, dynamic> json, {bool? emptyIsNull, bool? growable,}) {
    final Map<String, List<EmbeddedDocument?>?> map = <String, List<EmbeddedDocument>?>{};
    if (json != null && json.isNotEmpty) {
      json.forEach((String key, dynamic v) {
        map[key] = EmbeddedDocument.listFromJson(v, emptyIsNull: emptyIsNull, growable: growable);
      });
    }
    return map;
  }
}

