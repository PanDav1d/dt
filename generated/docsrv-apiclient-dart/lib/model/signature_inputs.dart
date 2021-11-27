//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//
// @dart=2.0

// ignore_for_file: unused_element, unused_import
// ignore_for_file: always_put_required_named_parameters_first
// ignore_for_file: lines_longer_than_80_chars

part of docsrv_api;

class SignatureInputs {
  /// Returns a new [SignatureInputs] instance.
  SignatureInputs({
    this.files = const [],
    this.inputs = const [],
    this.ppkId,
    this.role,
  });

  List<FileData> files;

  List<WorkflowInputResult> inputs;

  String ppkId;

  String role;

  @override
  bool operator ==(Object other) => identical(this, other) || other is SignatureInputs &&
     other.files == files &&
     other.inputs == inputs &&
     other.ppkId == ppkId &&
     other.role == role;

  @override
  int get hashCode =>
    (files == null ? 0 : files.hashCode) +
    (inputs == null ? 0 : inputs.hashCode) +
    (ppkId == null ? 0 : ppkId.hashCode) +
    (role == null ? 0 : role.hashCode);

  @override
  String toString() => 'SignatureInputs[files=$files, inputs=$inputs, ppkId=$ppkId, role=$role]';

  Map<String, dynamic> toJson() {
    final json = <String, dynamic>{};
    if (files != null) {
      json[r'files'] = files;
    }
    if (inputs != null) {
      json[r'inputs'] = inputs;
    }
    if (ppkId != null) {
      json[r'ppkId'] = ppkId;
    }
    if (role != null) {
      json[r'role'] = role;
    }
    return json;
  }

  /// Returns a new [SignatureInputs] instance and imports its values from
  /// [json] if it's non-null, null if [json] is null.
  static SignatureInputs fromJson(Map<String, dynamic> json) => json == null
    ? null
    : SignatureInputs(
        files: FileData.listFromJson(json[r'files']),
        inputs: WorkflowInputResult.listFromJson(json[r'inputs']),
        ppkId: json[r'ppkId'],
        role: json[r'role'],
    );

  static List<SignatureInputs> listFromJson(List<dynamic> json, {bool emptyIsNull, bool growable,}) =>
    json == null || json.isEmpty
      ? true == emptyIsNull ? null : <SignatureInputs>[]
      : json.map((v) => SignatureInputs.fromJson(v)).toList(growable: true == growable);

  static Map<String, SignatureInputs> mapFromJson(Map<String, dynamic> json) {
    final map = <String, SignatureInputs>{};
    if (json != null && json.isNotEmpty) {
      json.forEach((String key, dynamic v) => map[key] = SignatureInputs.fromJson(v));
    }
    return map;
  }

  // maps a json object with a list of SignatureInputs-objects as value to a dart map
  static Map<String, List<SignatureInputs>> mapListFromJson(Map<String, dynamic> json, {bool emptyIsNull, bool growable,}) {
    final map = <String, List<SignatureInputs>>{};
    if (json != null && json.isNotEmpty) {
      json.forEach((String key, dynamic v) {
        map[key] = SignatureInputs.listFromJson(v, emptyIsNull: emptyIsNull, growable: growable);
      });
    }
    return map;
  }
}

