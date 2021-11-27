//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//
// @dart=2.0

// ignore_for_file: unused_element, unused_import
// ignore_for_file: always_put_required_named_parameters_first
// ignore_for_file: lines_longer_than_80_chars

part of docsrv_api;

class AuthInfoResponse {
  /// Returns a new [AuthInfoResponse] instance.
  AuthInfoResponse({
    this.authenticated,
    this.firstName,
    this.lastName,
  });

  bool authenticated;

  String firstName;

  String lastName;

  @override
  bool operator ==(Object other) => identical(this, other) || other is AuthInfoResponse &&
     other.authenticated == authenticated &&
     other.firstName == firstName &&
     other.lastName == lastName;

  @override
  int get hashCode =>
    (authenticated == null ? 0 : authenticated.hashCode) +
    (firstName == null ? 0 : firstName.hashCode) +
    (lastName == null ? 0 : lastName.hashCode);

  @override
  String toString() => 'AuthInfoResponse[authenticated=$authenticated, firstName=$firstName, lastName=$lastName]';

  Map<String, dynamic> toJson() {
    final json = <String, dynamic>{};
    if (authenticated != null) {
      json[r'authenticated'] = authenticated;
    }
    if (firstName != null) {
      json[r'firstName'] = firstName;
    }
    if (lastName != null) {
      json[r'lastName'] = lastName;
    }
    return json;
  }

  /// Returns a new [AuthInfoResponse] instance and imports its values from
  /// [json] if it's non-null, null if [json] is null.
  static AuthInfoResponse fromJson(Map<String, dynamic> json) => json == null
    ? null
    : AuthInfoResponse(
        authenticated: json[r'authenticated'],
        firstName: json[r'firstName'],
        lastName: json[r'lastName'],
    );

  static List<AuthInfoResponse> listFromJson(List<dynamic> json, {bool emptyIsNull, bool growable,}) =>
    json == null || json.isEmpty
      ? true == emptyIsNull ? null : <AuthInfoResponse>[]
      : json.map((v) => AuthInfoResponse.fromJson(v)).toList(growable: true == growable);

  static Map<String, AuthInfoResponse> mapFromJson(Map<String, dynamic> json) {
    final map = <String, AuthInfoResponse>{};
    if (json != null && json.isNotEmpty) {
      json.forEach((String key, dynamic v) => map[key] = AuthInfoResponse.fromJson(v));
    }
    return map;
  }

  // maps a json object with a list of AuthInfoResponse-objects as value to a dart map
  static Map<String, List<AuthInfoResponse>> mapListFromJson(Map<String, dynamic> json, {bool emptyIsNull, bool growable,}) {
    final map = <String, List<AuthInfoResponse>>{};
    if (json != null && json.isNotEmpty) {
      json.forEach((String key, dynamic v) {
        map[key] = AuthInfoResponse.listFromJson(v, emptyIsNull: emptyIsNull, growable: growable);
      });
    }
    return map;
  }
}

