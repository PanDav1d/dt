//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//


// ignore_for_file: unused_element, unused_import
// ignore_for_file: always_put_required_named_parameters_first
// ignore_for_file: lines_longer_than_80_chars

part of docsrv_api;

class PublicKeyVerification {
  /// Returns a new [PublicKeyVerification] instance.
  PublicKeyVerification({
    this.isAddressVerified,
    this.isSigningDoctagInstanceVerified,
    this.signatureOfPublicKeyEntry,
    this.signatureValidUntil,
    this.signedAt,
    this.signedByParty,
    this.signedByPublicKey,
  });

  bool? isAddressVerified;

  bool? isSigningDoctagInstanceVerified;

  String? signatureOfPublicKeyEntry;

  String? signatureValidUntil;

  String? signedAt;

  String? signedByParty;

  String? signedByPublicKey;

  @override
  bool operator ==(Object other) => identical(this, other) || other is PublicKeyVerification &&
     other.isAddressVerified == isAddressVerified &&
     other.isSigningDoctagInstanceVerified == isSigningDoctagInstanceVerified &&
     other.signatureOfPublicKeyEntry == signatureOfPublicKeyEntry &&
     other.signatureValidUntil == signatureValidUntil &&
     other.signedAt == signedAt &&
     other.signedByParty == signedByParty &&
     other.signedByPublicKey == signedByPublicKey;

  @override
  int get hashCode =>
    (isAddressVerified == null ? 0 : isAddressVerified.hashCode) +
    (isSigningDoctagInstanceVerified == null ? 0 : isSigningDoctagInstanceVerified.hashCode) +
    (signatureOfPublicKeyEntry == null ? 0 : signatureOfPublicKeyEntry.hashCode) +
    (signatureValidUntil == null ? 0 : signatureValidUntil.hashCode) +
    (signedAt == null ? 0 : signedAt.hashCode) +
    (signedByParty == null ? 0 : signedByParty.hashCode) +
    (signedByPublicKey == null ? 0 : signedByPublicKey.hashCode);

  @override
  String toString() => 'PublicKeyVerification[isAddressVerified=$isAddressVerified, isSigningDoctagInstanceVerified=$isSigningDoctagInstanceVerified, signatureOfPublicKeyEntry=$signatureOfPublicKeyEntry, signatureValidUntil=$signatureValidUntil, signedAt=$signedAt, signedByParty=$signedByParty, signedByPublicKey=$signedByPublicKey]';

  Map<String, dynamic> toJson() {
    final json = <String, dynamic>{};
    if (isAddressVerified != null) {
      json[r'isAddressVerified'] = isAddressVerified;
    }
    if (isSigningDoctagInstanceVerified != null) {
      json[r'isSigningDoctagInstanceVerified'] = isSigningDoctagInstanceVerified;
    }
    if (signatureOfPublicKeyEntry != null) {
      json[r'signatureOfPublicKeyEntry'] = signatureOfPublicKeyEntry;
    }
    if (signatureValidUntil != null) {
      json[r'signatureValidUntil'] = signatureValidUntil;
    }
    if (signedAt != null) {
      json[r'signedAt'] = signedAt;
    }
    if (signedByParty != null) {
      json[r'signedByParty'] = signedByParty;
    }
    if (signedByPublicKey != null) {
      json[r'signedByPublicKey'] = signedByPublicKey;
    }
    return json;
  }

  /// Returns a new [PublicKeyVerification] instance and imports its values from
  /// [json] if it's non-null, null if [json] is null.
  static PublicKeyVerification? fromJson(Map<String, dynamic>? json) => json == null
    ? null
    : PublicKeyVerification(
        isAddressVerified: json[r'isAddressVerified'],
        isSigningDoctagInstanceVerified: json[r'isSigningDoctagInstanceVerified'],
        signatureOfPublicKeyEntry: json[r'signatureOfPublicKeyEntry'],
        signatureValidUntil: json[r'signatureValidUntil'],
        signedAt: json[r'signedAt'],
        signedByParty: json[r'signedByParty'],
        signedByPublicKey: json[r'signedByPublicKey'],
    );

  static List<PublicKeyVerification?>? listFromJson(List<dynamic> json, {bool? emptyIsNull, bool? growable,}) =>
    json == null || json.isEmpty
      ? true == emptyIsNull ? null : <PublicKeyVerification>[]
      : json.map((v) => PublicKeyVerification.fromJson(v)).toList(growable: true == growable);

  static Map<String, PublicKeyVerification?> mapFromJson(Map<String, dynamic> json) {
    final map = <String, PublicKeyVerification?>{};
    if (json != null && json.isNotEmpty) {
      json.forEach((String key, dynamic v) => map[key] = PublicKeyVerification.fromJson(v));
    }
    return map;
  }

  // maps a json object with a list of PublicKeyVerification-objects as value to a dart map
  static Map<String, List<PublicKeyVerification?>?> mapListFromJson(Map<String, dynamic> json, {bool? emptyIsNull, bool? growable,}) {
    final Map<String, List<PublicKeyVerification?>?> map = <String, List<PublicKeyVerification>?>{};
    if (json != null && json.isNotEmpty) {
      json.forEach((String key, dynamic v) {
        map[key] = PublicKeyVerification.listFromJson(v, emptyIsNull: emptyIsNull, growable: growable);
      });
    }
    return map;
  }
}

