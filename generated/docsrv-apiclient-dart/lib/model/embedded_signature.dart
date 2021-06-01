//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//


// ignore_for_file: unused_element, unused_import
// ignore_for_file: always_put_required_named_parameters_first
// ignore_for_file: lines_longer_than_80_chars

part of docsrv_api;

class EmbeddedSignature {
  /// Returns a new [EmbeddedSignature] instance.
  EmbeddedSignature({
    this.files = const [],
    this.signature,
  });

  List<FileData?>? files;

  Signature? signature;

  @override
  bool operator ==(Object other) => identical(this, other) || other is EmbeddedSignature &&
     other.files == files &&
     other.signature == signature;

  @override
  int get hashCode =>
    (files == null ? 0 : files.hashCode) +
    (signature == null ? 0 : signature.hashCode);

  @override
  String toString() => 'EmbeddedSignature[files=$files, signature=$signature]';

  Map<String, dynamic> toJson() {
    final json = <String, dynamic>{};
    if (files != null) {
      json[r'files'] = files;
    }
    if (signature != null) {
      json[r'signature'] = signature;
    }
    return json;
  }

  /// Returns a new [EmbeddedSignature] instance and imports its values from
  /// [json] if it's non-null, null if [json] is null.
  static EmbeddedSignature? fromJson(Map<String, dynamic>? json) => json == null
    ? null
    : EmbeddedSignature(
        files: FileData.listFromJson(json[r'files']),
        signature: Signature.fromJson(json[r'signature']),
    );

  static List<EmbeddedSignature?>? listFromJson(List<dynamic> json, {bool? emptyIsNull, bool? growable,}) =>
    json == null || json.isEmpty
      ? true == emptyIsNull ? null : <EmbeddedSignature>[]
      : json.map((v) => EmbeddedSignature.fromJson(v)).toList(growable: true == growable);

  static Map<String, EmbeddedSignature?> mapFromJson(Map<String, dynamic> json) {
    final map = <String, EmbeddedSignature?>{};
    if (json != null && json.isNotEmpty) {
      json.forEach((String key, dynamic v) => map[key] = EmbeddedSignature.fromJson(v));
    }
    return map;
  }

  // maps a json object with a list of EmbeddedSignature-objects as value to a dart map
  static Map<String, List<EmbeddedSignature?>?> mapListFromJson(Map<String, dynamic> json, {bool? emptyIsNull, bool? growable,}) {
    final Map<String, List<EmbeddedSignature?>?> map = <String, List<EmbeddedSignature>?>{};
    if (json != null && json.isNotEmpty) {
      json.forEach((String key, dynamic v) {
        map[key] = EmbeddedSignature.listFromJson(v, emptyIsNull: emptyIsNull, growable: growable);
      });
    }
    return map;
  }
}

