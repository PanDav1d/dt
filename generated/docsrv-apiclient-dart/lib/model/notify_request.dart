//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//
// @dart=2.0

// ignore_for_file: unused_element, unused_import
// ignore_for_file: always_put_required_named_parameters_first
// ignore_for_file: lines_longer_than_80_chars

part of docsrv_api;

class NotifyRequest {
  /// Returns a new [NotifyRequest] instance.
  NotifyRequest({
    this.url,
  });

  String url;

  @override
  bool operator ==(Object other) => identical(this, other) || other is NotifyRequest &&
     other.url == url;

  @override
  int get hashCode =>
    (url == null ? 0 : url.hashCode);

  @override
  String toString() => 'NotifyRequest[url=$url]';

  Map<String, dynamic> toJson() {
    final json = <String, dynamic>{};
    if (url != null) {
      json[r'url'] = url;
    }
    return json;
  }

  /// Returns a new [NotifyRequest] instance and imports its values from
  /// [json] if it's non-null, null if [json] is null.
  static NotifyRequest fromJson(Map<String, dynamic> json) => json == null
    ? null
    : NotifyRequest(
        url: json[r'url'],
    );

  static List<NotifyRequest> listFromJson(List<dynamic> json, {bool emptyIsNull, bool growable,}) =>
    json == null || json.isEmpty
      ? true == emptyIsNull ? null : <NotifyRequest>[]
      : json.map((v) => NotifyRequest.fromJson(v)).toList(growable: true == growable);

  static Map<String, NotifyRequest> mapFromJson(Map<String, dynamic> json) {
    final map = <String, NotifyRequest>{};
    if (json != null && json.isNotEmpty) {
      json.forEach((String key, dynamic v) => map[key] = NotifyRequest.fromJson(v));
    }
    return map;
  }

  // maps a json object with a list of NotifyRequest-objects as value to a dart map
  static Map<String, List<NotifyRequest>> mapListFromJson(Map<String, dynamic> json, {bool emptyIsNull, bool growable,}) {
    final map = <String, List<NotifyRequest>>{};
    if (json != null && json.isNotEmpty) {
      json.forEach((String key, dynamic v) {
        map[key] = NotifyRequest.listFromJson(v, emptyIsNull: emptyIsNull, growable: growable);
      });
    }
    return map;
  }
}

