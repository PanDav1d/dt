//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//
// @dart=2.0

// ignore_for_file: unused_element, unused_import
// ignore_for_file: always_put_required_named_parameters_first
// ignore_for_file: lines_longer_than_80_chars

part of docsrv_api;

class DiscoveryResponse {
  /// Returns a new [DiscoveryResponse] instance.
  DiscoveryResponse({
    this.identity,
  });

  String identity;

  @override
  bool operator ==(Object other) => identical(this, other) || other is DiscoveryResponse &&
     other.identity == identity;

  @override
  int get hashCode =>
    (identity == null ? 0 : identity.hashCode);

  @override
  String toString() => 'DiscoveryResponse[identity=$identity]';

  Map<String, dynamic> toJson() {
    final json = <String, dynamic>{};
    if (identity != null) {
      json[r'identity'] = identity;
    }
    return json;
  }

  /// Returns a new [DiscoveryResponse] instance and imports its values from
  /// [json] if it's non-null, null if [json] is null.
  static DiscoveryResponse fromJson(Map<String, dynamic> json) => json == null
    ? null
    : DiscoveryResponse(
        identity: json[r'identity'],
    );

  static List<DiscoveryResponse> listFromJson(List<dynamic> json, {bool emptyIsNull, bool growable,}) =>
    json == null || json.isEmpty
      ? true == emptyIsNull ? null : <DiscoveryResponse>[]
      : json.map((v) => DiscoveryResponse.fromJson(v)).toList(growable: true == growable);

  static Map<String, DiscoveryResponse> mapFromJson(Map<String, dynamic> json) {
    final map = <String, DiscoveryResponse>{};
    if (json != null && json.isNotEmpty) {
      json.forEach((String key, dynamic v) => map[key] = DiscoveryResponse.fromJson(v));
    }
    return map;
  }

  // maps a json object with a list of DiscoveryResponse-objects as value to a dart map
  static Map<String, List<DiscoveryResponse>> mapListFromJson(Map<String, dynamic> json, {bool emptyIsNull, bool growable,}) {
    final map = <String, List<DiscoveryResponse>>{};
    if (json != null && json.isNotEmpty) {
      json.forEach((String key, dynamic v) {
        map[key] = DiscoveryResponse.listFromJson(v, emptyIsNull: emptyIsNull, growable: growable);
      });
    }
    return map;
  }
}

