//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//
// @dart=2.0

// ignore_for_file: unused_element, unused_import
// ignore_for_file: always_put_required_named_parameters_first
// ignore_for_file: lines_longer_than_80_chars

part of docsrv_api;

class WorkflowInputResult {
  /// Returns a new [WorkflowInputResult] instance.
  WorkflowInputResult({
    this.fileId,
    this.name,
    this.value,
  });

  String fileId;

  String name;

  String value;

  @override
  bool operator ==(Object other) => identical(this, other) || other is WorkflowInputResult &&
     other.fileId == fileId &&
     other.name == name &&
     other.value == value;

  @override
  int get hashCode =>
    (fileId == null ? 0 : fileId.hashCode) +
    (name == null ? 0 : name.hashCode) +
    (value == null ? 0 : value.hashCode);

  @override
  String toString() => 'WorkflowInputResult[fileId=$fileId, name=$name, value=$value]';

  Map<String, dynamic> toJson() {
    final json = <String, dynamic>{};
    if (fileId != null) {
      json[r'fileId'] = fileId;
    }
    if (name != null) {
      json[r'name'] = name;
    }
    if (value != null) {
      json[r'value'] = value;
    }
    return json;
  }

  /// Returns a new [WorkflowInputResult] instance and imports its values from
  /// [json] if it's non-null, null if [json] is null.
  static WorkflowInputResult fromJson(Map<String, dynamic> json) => json == null
    ? null
    : WorkflowInputResult(
        fileId: json[r'fileId'],
        name: json[r'name'],
        value: json[r'value'],
    );

  static List<WorkflowInputResult> listFromJson(List<dynamic> json, {bool emptyIsNull, bool growable,}) =>
    json == null || json.isEmpty
      ? true == emptyIsNull ? null : <WorkflowInputResult>[]
      : json.map((v) => WorkflowInputResult.fromJson(v)).toList(growable: true == growable);

  static Map<String, WorkflowInputResult> mapFromJson(Map<String, dynamic> json) {
    final map = <String, WorkflowInputResult>{};
    if (json != null && json.isNotEmpty) {
      json.forEach((String key, dynamic v) => map[key] = WorkflowInputResult.fromJson(v));
    }
    return map;
  }

  // maps a json object with a list of WorkflowInputResult-objects as value to a dart map
  static Map<String, List<WorkflowInputResult>> mapListFromJson(Map<String, dynamic> json, {bool emptyIsNull, bool growable,}) {
    final map = <String, List<WorkflowInputResult>>{};
    if (json != null && json.isNotEmpty) {
      json.forEach((String key, dynamic v) {
        map[key] = WorkflowInputResult.listFromJson(v, emptyIsNull: emptyIsNull, growable: growable);
      });
    }
    return map;
  }
}

