//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//


// ignore_for_file: unused_element, unused_import
// ignore_for_file: always_put_required_named_parameters_first
// ignore_for_file: lines_longer_than_80_chars

part of docsrv_api;

class PrivatePublicKeyInfo {
  /// Returns a new [PrivatePublicKeyInfo] instance.
  PrivatePublicKeyInfo({
    this.ppkId,
    this.verboseName,
  });

  String? ppkId;

  String? verboseName;

  @override
  bool operator ==(Object other) => identical(this, other) || other is PrivatePublicKeyInfo &&
     other.ppkId == ppkId &&
     other.verboseName == verboseName;

  @override
  int get hashCode =>
    (ppkId == null ? 0 : ppkId.hashCode) +
    (verboseName == null ? 0 : verboseName.hashCode);

  @override
  String toString() => 'PrivatePublicKeyInfo[ppkId=$ppkId, verboseName=$verboseName]';

  Map<String, dynamic> toJson() {
    final json = <String, dynamic>{};
    if (ppkId != null) {
      json[r'ppkId'] = ppkId;
    }
    if (verboseName != null) {
      json[r'verboseName'] = verboseName;
    }
    return json;
  }

  /// Returns a new [PrivatePublicKeyInfo] instance and imports its values from
  /// [json] if it's non-null, null if [json] is null.
  static PrivatePublicKeyInfo? fromJson(Map<String, dynamic>? json) => json == null
    ? null
    : PrivatePublicKeyInfo(
        ppkId: json[r'ppkId'],
        verboseName: json[r'verboseName'],
    );

  static List<PrivatePublicKeyInfo?>? listFromJson(List<dynamic>? json, {bool? emptyIsNull, bool? growable,}) =>
    json == null || json.isEmpty
      ? true == emptyIsNull ? null : <PrivatePublicKeyInfo>[]
      : json.map((v) => PrivatePublicKeyInfo.fromJson(v)).toList(growable: true == growable);

  static Map<String, PrivatePublicKeyInfo?> mapFromJson(Map<String, dynamic> json) {
    final map = <String, PrivatePublicKeyInfo?>{};
    if (json != null && json.isNotEmpty) {
      json.forEach((String key, dynamic v) => map[key] = PrivatePublicKeyInfo.fromJson(v));
    }
    return map;
  }

  // maps a json object with a list of PrivatePublicKeyInfo-objects as value to a dart map
  static Map<String, List<PrivatePublicKeyInfo?>?> mapListFromJson(Map<String, dynamic> json, {bool? emptyIsNull, bool? growable,}) {
    final Map<String, List<PrivatePublicKeyInfo?>?> map = <String, List<PrivatePublicKeyInfo>?>{};
    if (json != null && json.isNotEmpty) {
      json.forEach((String key, dynamic v) {
        map[key] = PrivatePublicKeyInfo.listFromJson(v, emptyIsNull: emptyIsNull, growable: growable);
      });
    }
    return map;
  }
}

