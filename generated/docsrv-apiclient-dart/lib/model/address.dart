//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//


// ignore_for_file: unused_element, unused_import
// ignore_for_file: always_put_required_named_parameters_first
// ignore_for_file: lines_longer_than_80_chars

part of docsrv_api;

class Address {
  /// Returns a new [Address] instance.
  Address({
    this.city,
    this.countryCode,
    this.name1,
    this.name2,
    this.street,
    this.zipCode,
  });

  String? city;

  String? countryCode;

  String? name1;

  String? name2;

  String? street;

  String? zipCode;

  @override
  bool operator ==(Object other) => identical(this, other) || other is Address &&
     other.city == city &&
     other.countryCode == countryCode &&
     other.name1 == name1 &&
     other.name2 == name2 &&
     other.street == street &&
     other.zipCode == zipCode;

  @override
  int get hashCode =>
    (city == null ? 0 : city.hashCode) +
    (countryCode == null ? 0 : countryCode.hashCode) +
    (name1 == null ? 0 : name1.hashCode) +
    (name2 == null ? 0 : name2.hashCode) +
    (street == null ? 0 : street.hashCode) +
    (zipCode == null ? 0 : zipCode.hashCode);

  @override
  String toString() => 'Address[city=$city, countryCode=$countryCode, name1=$name1, name2=$name2, street=$street, zipCode=$zipCode]';

  Map<String, dynamic> toJson() {
    final json = <String, dynamic>{};
    if (city != null) {
      json[r'city'] = city;
    }
    if (countryCode != null) {
      json[r'countryCode'] = countryCode;
    }
    if (name1 != null) {
      json[r'name1'] = name1;
    }
    if (name2 != null) {
      json[r'name2'] = name2;
    }
    if (street != null) {
      json[r'street'] = street;
    }
    if (zipCode != null) {
      json[r'zipCode'] = zipCode;
    }
    return json;
  }

  /// Returns a new [Address] instance and imports its values from
  /// [json] if it's non-null, null if [json] is null.
  static Address? fromJson(Map<String, dynamic>? json) => json == null
    ? null
    : Address(
        city: json[r'city'],
        countryCode: json[r'countryCode'],
        name1: json[r'name1'],
        name2: json[r'name2'],
        street: json[r'street'],
        zipCode: json[r'zipCode'],
    );

  static List<Address?>? listFromJson(List<dynamic> json, {bool? emptyIsNull, bool? growable,}) =>
    json == null || json.isEmpty
      ? true == emptyIsNull ? null : <Address>[]
      : json.map((v) => Address.fromJson(v)).toList(growable: true == growable);

  static Map<String, Address?> mapFromJson(Map<String, dynamic> json) {
    final map = <String, Address?>{};
    if (json != null && json.isNotEmpty) {
      json.forEach((String key, dynamic v) => map[key] = Address.fromJson(v));
    }
    return map;
  }

  // maps a json object with a list of Address-objects as value to a dart map
  static Map<String, List<Address?>?> mapListFromJson(Map<String, dynamic> json, {bool? emptyIsNull, bool? growable,}) {
    final Map<String, List<Address?>?> map = <String, List<Address>?>{};
    if (json != null && json.isNotEmpty) {
      json.forEach((String key, dynamic v) {
        map[key] = Address.listFromJson(v, emptyIsNull: emptyIsNull, growable: growable);
      });
    }
    return map;
  }
}

