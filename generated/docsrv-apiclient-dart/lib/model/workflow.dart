//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//
// @dart=2.0

// ignore_for_file: unused_element, unused_import
// ignore_for_file: always_put_required_named_parameters_first
// ignore_for_file: lines_longer_than_80_chars

part of docsrv_api;

class Workflow {
  /// Returns a new [Workflow] instance.
  Workflow({
    this.id,
    this.actions = const [],
    this.name,
  });

  String id;

  List<WorkflowAction> actions;

  String name;

  @override
  bool operator ==(Object other) => identical(this, other) || other is Workflow &&
     other.id == id &&
     other.actions == actions &&
     other.name == name;

  @override
  int get hashCode =>
    (id == null ? 0 : id.hashCode) +
    (actions == null ? 0 : actions.hashCode) +
    (name == null ? 0 : name.hashCode);

  @override
  String toString() => 'Workflow[id=$id, actions=$actions, name=$name]';

  Map<String, dynamic> toJson() {
    final json = <String, dynamic>{};
    if (id != null) {
      json[r'_id'] = id;
    }
    if (actions != null) {
      json[r'actions'] = actions;
    }
    if (name != null) {
      json[r'name'] = name;
    }
    return json;
  }

  /// Returns a new [Workflow] instance and imports its values from
  /// [json] if it's non-null, null if [json] is null.
  static Workflow fromJson(Map<String, dynamic> json) => json == null
    ? null
    : Workflow(
        id: json[r'_id'],
        actions: WorkflowAction.listFromJson(json[r'actions']),
        name: json[r'name'],
    );

  static List<Workflow> listFromJson(List<dynamic> json, {bool emptyIsNull, bool growable,}) =>
    json == null || json.isEmpty
      ? true == emptyIsNull ? null : <Workflow>[]
      : json.map((v) => Workflow.fromJson(v)).toList(growable: true == growable);

  static Map<String, Workflow> mapFromJson(Map<String, dynamic> json) {
    final map = <String, Workflow>{};
    if (json != null && json.isNotEmpty) {
      json.forEach((String key, dynamic v) => map[key] = Workflow.fromJson(v));
    }
    return map;
  }

  // maps a json object with a list of Workflow-objects as value to a dart map
  static Map<String, List<Workflow>> mapListFromJson(Map<String, dynamic> json, {bool emptyIsNull, bool growable,}) {
    final map = <String, List<Workflow>>{};
    if (json != null && json.isNotEmpty) {
      json.forEach((String key, dynamic v) {
        map[key] = Workflow.listFromJson(v, emptyIsNull: emptyIsNull, growable: growable);
      });
    }
    return map;
  }
}

