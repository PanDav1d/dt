//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//
// @dart=2.0

// ignore_for_file: unused_element, unused_import
// ignore_for_file: always_put_required_named_parameters_first
// ignore_for_file: lines_longer_than_80_chars

part of docsrv_api;

class FileData {
  /// Returns a new [FileData] instance.
  FileData({
    this.id,
    this.base64Content,
    this.contentType,
    this.name,
  });

  String id;

  String base64Content;

  String contentType;

  String name;

  @override
  bool operator ==(Object other) => identical(this, other) || other is FileData &&
     other.id == id &&
     other.base64Content == base64Content &&
     other.contentType == contentType &&
     other.name == name;

  @override
  int get hashCode =>
    (id == null ? 0 : id.hashCode) +
    (base64Content == null ? 0 : base64Content.hashCode) +
    (contentType == null ? 0 : contentType.hashCode) +
    (name == null ? 0 : name.hashCode);

  @override
  String toString() => 'FileData[id=$id, base64Content=$base64Content, contentType=$contentType, name=$name]';

  Map<String, dynamic> toJson() {
    final json = <String, dynamic>{};
    if (id != null) {
      json[r'_id'] = id;
    }
    if (base64Content != null) {
      json[r'base64Content'] = base64Content;
    }
    if (contentType != null) {
      json[r'contentType'] = contentType;
    }
    if (name != null) {
      json[r'name'] = name;
    }
    return json;
  }

  /// Returns a new [FileData] instance and imports its values from
  /// [json] if it's non-null, null if [json] is null.
  static FileData fromJson(Map<String, dynamic> json) => json == null
    ? null
    : FileData(
        id: json[r'_id'],
        base64Content: json[r'base64Content'],
        contentType: json[r'contentType'],
        name: json[r'name'],
    );

  static List<FileData> listFromJson(List<dynamic> json, {bool emptyIsNull, bool growable,}) =>
    json == null || json.isEmpty
      ? true == emptyIsNull ? null : <FileData>[]
      : json.map((v) => FileData.fromJson(v)).toList(growable: true == growable);

  static Map<String, FileData> mapFromJson(Map<String, dynamic> json) {
    final map = <String, FileData>{};
    if (json != null && json.isNotEmpty) {
      json.forEach((String key, dynamic v) => map[key] = FileData.fromJson(v));
    }
    return map;
  }

  // maps a json object with a list of FileData-objects as value to a dart map
  static Map<String, List<FileData>> mapListFromJson(Map<String, dynamic> json, {bool emptyIsNull, bool growable,}) {
    final map = <String, List<FileData>>{};
    if (json != null && json.isNotEmpty) {
      json.forEach((String key, dynamic v) {
        map[key] = FileData.listFromJson(v, emptyIsNull: emptyIsNull, growable: growable);
      });
    }
    return map;
  }
}

