//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//


// ignore_for_file: unused_element, unused_import
// ignore_for_file: always_put_required_named_parameters_first
// ignore_for_file: lines_longer_than_80_chars

part of docsrv_api;

class HealthCheckResponse {
  /// Returns a new [HealthCheckResponse] instance.
  HealthCheckResponse({
    this.isHealthy,
  });

  bool? isHealthy;

  @override
  bool operator ==(Object other) => identical(this, other) || other is HealthCheckResponse &&
     other.isHealthy == isHealthy;

  @override
  int get hashCode =>
    (isHealthy == null ? 0 : isHealthy.hashCode);

  @override
  String toString() => 'HealthCheckResponse[isHealthy=$isHealthy]';

  Map<String, dynamic> toJson() {
    final json = <String, dynamic>{};
    if (isHealthy != null) {
      json[r'isHealthy'] = isHealthy;
    }
    return json;
  }

  /// Returns a new [HealthCheckResponse] instance and imports its values from
  /// [json] if it's non-null, null if [json] is null.
  static HealthCheckResponse? fromJson(Map<String, dynamic>? json) => json == null
    ? null
    : HealthCheckResponse(
        isHealthy: json[r'isHealthy'],
    );

  static List<HealthCheckResponse?>? listFromJson(List<dynamic> json, {bool? emptyIsNull, bool? growable,}) =>
    json == null || json.isEmpty
      ? true == emptyIsNull ? null : <HealthCheckResponse>[]
      : json.map((v) => HealthCheckResponse.fromJson(v)).toList(growable: true == growable);

  static Map<String, HealthCheckResponse?> mapFromJson(Map<String, dynamic> json) {
    final map = <String, HealthCheckResponse?>{};
    if (json != null && json.isNotEmpty) {
      json.forEach((String key, dynamic v) => map[key] = HealthCheckResponse.fromJson(v));
    }
    return map;
  }

  // maps a json object with a list of HealthCheckResponse-objects as value to a dart map
  static Map<String, List<HealthCheckResponse?>?> mapListFromJson(Map<String, dynamic> json, {bool? emptyIsNull, bool? growable,}) {
    final Map<String, List<HealthCheckResponse?>?> map = <String, List<HealthCheckResponse>?>{};
    if (json != null && json.isNotEmpty) {
      json.forEach((String key, dynamic v) {
        map[key] = HealthCheckResponse.listFromJson(v, emptyIsNull: emptyIsNull, growable: growable);
      });
    }
    return map;
  }
}

