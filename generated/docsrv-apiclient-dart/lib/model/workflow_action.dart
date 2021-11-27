//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//
// @dart=2.0

// ignore_for_file: unused_element, unused_import
// ignore_for_file: always_put_required_named_parameters_first
// ignore_for_file: lines_longer_than_80_chars

part of docsrv_api;

class WorkflowAction {
  /// Returns a new [WorkflowAction] instance.
  WorkflowAction({
    this.inputs = const [],
    this.role,
  });

  List<WorkflowInput> inputs;

  String role;

  @override
  bool operator ==(Object other) => identical(this, other) || other is WorkflowAction &&
     other.inputs == inputs &&
     other.role == role;

  @override
  int get hashCode =>
    (inputs == null ? 0 : inputs.hashCode) +
    (role == null ? 0 : role.hashCode);

  @override
  String toString() => 'WorkflowAction[inputs=$inputs, role=$role]';

  Map<String, dynamic> toJson() {
    final json = <String, dynamic>{};
    if (inputs != null) {
      json[r'inputs'] = inputs;
    }
    if (role != null) {
      json[r'role'] = role;
    }
    return json;
  }

  /// Returns a new [WorkflowAction] instance and imports its values from
  /// [json] if it's non-null, null if [json] is null.
  static WorkflowAction fromJson(Map<String, dynamic> json) => json == null
    ? null
    : WorkflowAction(
        inputs: WorkflowInput.listFromJson(json[r'inputs']),
        role: json[r'role'],
    );

  static List<WorkflowAction> listFromJson(List<dynamic> json, {bool emptyIsNull, bool growable,}) =>
    json == null || json.isEmpty
      ? true == emptyIsNull ? null : <WorkflowAction>[]
      : json.map((v) => WorkflowAction.fromJson(v)).toList(growable: true == growable);

  static Map<String, WorkflowAction> mapFromJson(Map<String, dynamic> json) {
    final map = <String, WorkflowAction>{};
    if (json != null && json.isNotEmpty) {
      json.forEach((String key, dynamic v) => map[key] = WorkflowAction.fromJson(v));
    }
    return map;
  }

  // maps a json object with a list of WorkflowAction-objects as value to a dart map
  static Map<String, List<WorkflowAction>> mapListFromJson(Map<String, dynamic> json, {bool emptyIsNull, bool growable,}) {
    final map = <String, List<WorkflowAction>>{};
    if (json != null && json.isNotEmpty) {
      json.forEach((String key, dynamic v) {
        map[key] = WorkflowAction.listFromJson(v, emptyIsNull: emptyIsNull, growable: growable);
      });
    }
    return map;
  }
}

