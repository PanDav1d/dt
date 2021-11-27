//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//
// @dart=2.0

// ignore_for_file: unused_element, unused_import
// ignore_for_file: always_put_required_named_parameters_first
// ignore_for_file: lines_longer_than_80_chars

part of docsrv_api;

class PreparedSignature {
  /// Returns a new [PreparedSignature] instance.
  PreparedSignature({
    this.availableKeys = const [],
    this.workflow,
  });

  List<PrivatePublicKeyInfo> availableKeys;

  Workflow workflow;

  @override
  bool operator ==(Object other) => identical(this, other) || other is PreparedSignature &&
     other.availableKeys == availableKeys &&
     other.workflow == workflow;

  @override
  int get hashCode =>
    (availableKeys == null ? 0 : availableKeys.hashCode) +
    (workflow == null ? 0 : workflow.hashCode);

  @override
  String toString() => 'PreparedSignature[availableKeys=$availableKeys, workflow=$workflow]';

  Map<String, dynamic> toJson() {
    final json = <String, dynamic>{};
    if (availableKeys != null) {
      json[r'availableKeys'] = availableKeys;
    }
    if (workflow != null) {
      json[r'workflow'] = workflow;
    }
    return json;
  }

  /// Returns a new [PreparedSignature] instance and imports its values from
  /// [json] if it's non-null, null if [json] is null.
  static PreparedSignature fromJson(Map<String, dynamic> json) => json == null
    ? null
    : PreparedSignature(
        availableKeys: PrivatePublicKeyInfo.listFromJson(json[r'availableKeys']),
        workflow: Workflow.fromJson(json[r'workflow']),
    );

  static List<PreparedSignature> listFromJson(List<dynamic> json, {bool emptyIsNull, bool growable,}) =>
    json == null || json.isEmpty
      ? true == emptyIsNull ? null : <PreparedSignature>[]
      : json.map((v) => PreparedSignature.fromJson(v)).toList(growable: true == growable);

  static Map<String, PreparedSignature> mapFromJson(Map<String, dynamic> json) {
    final map = <String, PreparedSignature>{};
    if (json != null && json.isNotEmpty) {
      json.forEach((String key, dynamic v) => map[key] = PreparedSignature.fromJson(v));
    }
    return map;
  }

  // maps a json object with a list of PreparedSignature-objects as value to a dart map
  static Map<String, List<PreparedSignature>> mapListFromJson(Map<String, dynamic> json, {bool emptyIsNull, bool growable,}) {
    final map = <String, List<PreparedSignature>>{};
    if (json != null && json.isNotEmpty) {
      json.forEach((String key, dynamic v) {
        map[key] = PreparedSignature.listFromJson(v, emptyIsNull: emptyIsNull, growable: growable);
      });
    }
    return map;
  }
}

