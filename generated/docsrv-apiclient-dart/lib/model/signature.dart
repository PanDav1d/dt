//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//


// ignore_for_file: unused_element, unused_import
// ignore_for_file: always_put_required_named_parameters_first
// ignore_for_file: lines_longer_than_80_chars

part of docsrv_api;

class Signature {
  /// Returns a new [Signature] instance.
  Signature({
    this.data,
    this.inputs = const [],
    this.originalMessage,
    this.role,
    this.signed,
    this.signedByKey,
  });

  DoctagSignatureData? data;

  List<WorkflowInputResult?>? inputs;

  String? originalMessage;

  String? role;

  DateTime? signed;

  PublicKeyResponse? signedByKey;

  @override
  bool operator ==(Object other) => identical(this, other) || other is Signature &&
     other.data == data &&
     other.inputs == inputs &&
     other.originalMessage == originalMessage &&
     other.role == role &&
     other.signed == signed &&
     other.signedByKey == signedByKey;

  @override
  int get hashCode =>
    (data == null ? 0 : data.hashCode) +
    (inputs == null ? 0 : inputs.hashCode) +
    (originalMessage == null ? 0 : originalMessage.hashCode) +
    (role == null ? 0 : role.hashCode) +
    (signed == null ? 0 : signed.hashCode) +
    (signedByKey == null ? 0 : signedByKey.hashCode);

  @override
  String toString() => 'Signature[data=$data, inputs=$inputs, originalMessage=$originalMessage, role=$role, signed=$signed, signedByKey=$signedByKey]';

  Map<String, dynamic> toJson() {
    final json = <String, dynamic>{};
    if (data != null) {
      json[r'data'] = data;
    }
    if (inputs != null) {
      json[r'inputs'] = inputs;
    }
    if (originalMessage != null) {
      json[r'originalMessage'] = originalMessage;
    }
    if (role != null) {
      json[r'role'] = role;
    }
    if (signed != null) {
      json[r'signed'] = signed!.toUtc().toIso8601String();
    }
    if (signedByKey != null) {
      json[r'signedByKey'] = signedByKey;
    }
    return json;
  }

  /// Returns a new [Signature] instance and imports its values from
  /// [json] if it's non-null, null if [json] is null.
  static Signature? fromJson(Map<String, dynamic>? json) => json == null
    ? null
    : Signature(
        data: DoctagSignatureData.fromJson(json[r'data']),
        inputs: WorkflowInputResult.listFromJson(json[r'inputs']),
        originalMessage: json[r'originalMessage'],
        role: json[r'role'],
        signed: json[r'signed'] == null
          ? null
          : DateTime.parse(json[r'signed']),
        signedByKey: PublicKeyResponse.fromJson(json[r'signedByKey']),
    );

  static List<Signature?>? listFromJson(List<dynamic>? json, {bool? emptyIsNull, bool? growable,}) =>
    json == null || json.isEmpty
      ? true == emptyIsNull ? null : <Signature>[]
      : json.map((v) => Signature.fromJson(v)).toList(growable: true == growable);

  static Map<String, Signature?> mapFromJson(Map<String, dynamic> json) {
    final map = <String, Signature?>{};
    if (json != null && json.isNotEmpty) {
      json.forEach((String key, dynamic v) => map[key] = Signature.fromJson(v));
    }
    return map;
  }

  // maps a json object with a list of Signature-objects as value to a dart map
  static Map<String, List<Signature?>?> mapListFromJson(Map<String, dynamic> json, {bool? emptyIsNull, bool? growable,}) {
    final Map<String, List<Signature?>?> map = <String, List<Signature>?>{};
    if (json != null && json.isNotEmpty) {
      json.forEach((String key, dynamic v) {
        map[key] = Signature.listFromJson(v, emptyIsNull: emptyIsNull, growable: growable);
      });
    }
    return map;
  }
}

