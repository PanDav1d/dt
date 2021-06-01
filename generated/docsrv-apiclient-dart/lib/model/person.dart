//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//


// ignore_for_file: unused_element, unused_import
// ignore_for_file: always_put_required_named_parameters_first
// ignore_for_file: lines_longer_than_80_chars

part of docsrv_api;

class Person {
  /// Returns a new [Person] instance.
  Person({
    this.email,
    this.firstName,
    this.lastName,
    this.phone,
    this.userId,
  });

  String? email;

  String? firstName;

  String? lastName;

  String? phone;

  String? userId;

  @override
  bool operator ==(Object other) => identical(this, other) || other is Person &&
     other.email == email &&
     other.firstName == firstName &&
     other.lastName == lastName &&
     other.phone == phone &&
     other.userId == userId;

  @override
  int get hashCode =>
    (email == null ? 0 : email.hashCode) +
    (firstName == null ? 0 : firstName.hashCode) +
    (lastName == null ? 0 : lastName.hashCode) +
    (phone == null ? 0 : phone.hashCode) +
    (userId == null ? 0 : userId.hashCode);

  @override
  String toString() => 'Person[email=$email, firstName=$firstName, lastName=$lastName, phone=$phone, userId=$userId]';

  Map<String, dynamic> toJson() {
    final json = <String, dynamic>{};
    if (email != null) {
      json[r'email'] = email;
    }
    if (firstName != null) {
      json[r'firstName'] = firstName;
    }
    if (lastName != null) {
      json[r'lastName'] = lastName;
    }
    if (phone != null) {
      json[r'phone'] = phone;
    }
    if (userId != null) {
      json[r'userId'] = userId;
    }
    return json;
  }

  /// Returns a new [Person] instance and imports its values from
  /// [json] if it's non-null, null if [json] is null.
  static Person? fromJson(Map<String, dynamic>? json) => json == null
    ? null
    : Person(
        email: json[r'email'],
        firstName: json[r'firstName'],
        lastName: json[r'lastName'],
        phone: json[r'phone'],
        userId: json[r'userId'],
    );

  static List<Person?>? listFromJson(List<dynamic> json, {bool? emptyIsNull, bool? growable,}) =>
    json == null || json.isEmpty
      ? true == emptyIsNull ? null : <Person>[]
      : json.map((v) => Person.fromJson(v)).toList(growable: true == growable);

  static Map<String, Person?> mapFromJson(Map<String, dynamic> json) {
    final map = <String, Person?>{};
    if (json != null && json.isNotEmpty) {
      json.forEach((String key, dynamic v) => map[key] = Person.fromJson(v));
    }
    return map;
  }

  // maps a json object with a list of Person-objects as value to a dart map
  static Map<String, List<Person?>?> mapListFromJson(Map<String, dynamic> json, {bool? emptyIsNull, bool? growable,}) {
    final Map<String, List<Person?>?> map = <String, List<Person>?>{};
    if (json != null && json.isNotEmpty) {
      json.forEach((String key, dynamic v) {
        map[key] = Person.listFromJson(v, emptyIsNull: emptyIsNull, growable: growable);
      });
    }
    return map;
  }
}

