//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//
// @dart=2.0

// ignore_for_file: unused_element, unused_import
// ignore_for_file: always_put_required_named_parameters_first
// ignore_for_file: lines_longer_than_80_chars

part of docsrv_api;

class WorkflowInputOptions {
  /// Returns a new [WorkflowInputOptions] instance.
  WorkflowInputOptions({
    this.signInputOptions,
  });

  SignInputOptions signInputOptions;

  @override
  bool operator ==(Object other) => identical(this, other) || other is WorkflowInputOptions &&
     other.signInputOptions == signInputOptions;

  @override
  int get hashCode =>
    (signInputOptions == null ? 0 : signInputOptions.hashCode);

  @override
  String toString() => 'WorkflowInputOptions[signInputOptions=$signInputOptions]';

  Map<String, dynamic> toJson() {
    final json = <String, dynamic>{};
    if (signInputOptions != null) {
      json[r'signInputOptions'] = signInputOptions;
    }
    return json;
  }

  /// Returns a new [WorkflowInputOptions] instance and imports its values from
  /// [json] if it's non-null, null if [json] is null.
  static WorkflowInputOptions fromJson(Map<String, dynamic> json) => json == null
    ? null
    : WorkflowInputOptions(
        signInputOptions: SignInputOptions.fromJson(json[r'signInputOptions']),
    );

  static List<WorkflowInputOptions> listFromJson(List<dynamic> json, {bool emptyIsNull, bool growable,}) =>
    json == null || json.isEmpty
      ? true == emptyIsNull ? null : <WorkflowInputOptions>[]
      : json.map((v) => WorkflowInputOptions.fromJson(v)).toList(growable: true == growable);

  static Map<String, WorkflowInputOptions> mapFromJson(Map<String, dynamic> json) {
    final map = <String, WorkflowInputOptions>{};
    if (json != null && json.isNotEmpty) {
      json.forEach((String key, dynamic v) => map[key] = WorkflowInputOptions.fromJson(v));
    }
    return map;
  }

  // maps a json object with a list of WorkflowInputOptions-objects as value to a dart map
  static Map<String, List<WorkflowInputOptions>> mapListFromJson(Map<String, dynamic> json, {bool emptyIsNull, bool growable,}) {
    final map = <String, List<WorkflowInputOptions>>{};
    if (json != null && json.isNotEmpty) {
      json.forEach((String key, dynamic v) {
        map[key] = WorkflowInputOptions.listFromJson(v, emptyIsNull: emptyIsNull, growable: growable);
      });
    }
    return map;
  }
}

