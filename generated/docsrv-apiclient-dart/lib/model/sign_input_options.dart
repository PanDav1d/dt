//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//
// @dart=2.0

// ignore_for_file: unused_element, unused_import
// ignore_for_file: always_put_required_named_parameters_first
// ignore_for_file: lines_longer_than_80_chars

part of docsrv_api;

class SignInputOptions {
  /// Returns a new [SignInputOptions] instance.
  SignInputOptions({
    this.backgroundImage,
  });

  String backgroundImage;

  @override
  bool operator ==(Object other) => identical(this, other) || other is SignInputOptions &&
     other.backgroundImage == backgroundImage;

  @override
  int get hashCode =>
    (backgroundImage == null ? 0 : backgroundImage.hashCode);

  @override
  String toString() => 'SignInputOptions[backgroundImage=$backgroundImage]';

  Map<String, dynamic> toJson() {
    final json = <String, dynamic>{};
    if (backgroundImage != null) {
      json[r'backgroundImage'] = backgroundImage;
    }
    return json;
  }

  /// Returns a new [SignInputOptions] instance and imports its values from
  /// [json] if it's non-null, null if [json] is null.
  static SignInputOptions fromJson(Map<String, dynamic> json) => json == null
    ? null
    : SignInputOptions(
        backgroundImage: json[r'backgroundImage'],
    );

  static List<SignInputOptions> listFromJson(List<dynamic> json, {bool emptyIsNull, bool growable,}) =>
    json == null || json.isEmpty
      ? true == emptyIsNull ? null : <SignInputOptions>[]
      : json.map((v) => SignInputOptions.fromJson(v)).toList(growable: true == growable);

  static Map<String, SignInputOptions> mapFromJson(Map<String, dynamic> json) {
    final map = <String, SignInputOptions>{};
    if (json != null && json.isNotEmpty) {
      json.forEach((String key, dynamic v) => map[key] = SignInputOptions.fromJson(v));
    }
    return map;
  }

  // maps a json object with a list of SignInputOptions-objects as value to a dart map
  static Map<String, List<SignInputOptions>> mapListFromJson(Map<String, dynamic> json, {bool emptyIsNull, bool growable,}) {
    final map = <String, List<SignInputOptions>>{};
    if (json != null && json.isNotEmpty) {
      json.forEach((String key, dynamic v) {
        map[key] = SignInputOptions.listFromJson(v, emptyIsNull: emptyIsNull, growable: growable);
      });
    }
    return map;
  }
}

