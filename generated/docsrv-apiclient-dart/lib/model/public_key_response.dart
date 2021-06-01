//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//


// ignore_for_file: unused_element, unused_import
// ignore_for_file: always_put_required_named_parameters_first
// ignore_for_file: lines_longer_than_80_chars

part of docsrv_api;

class PublicKeyResponse {
  /// Returns a new [PublicKeyResponse] instance.
  PublicKeyResponse({
    this.created,
    this.owner,
    this.ownerAddress,
    this.publicKey,
    this.signingDoctagInstance,
    this.verboseName,
    this.verification,
  });

  String? created;

  Person? owner;

  Address? ownerAddress;

  String? publicKey;

  String? signingDoctagInstance;

  String? verboseName;

  PublicKeyVerification? verification;

  @override
  bool operator ==(Object other) => identical(this, other) || other is PublicKeyResponse &&
     other.created == created &&
     other.owner == owner &&
     other.ownerAddress == ownerAddress &&
     other.publicKey == publicKey &&
     other.signingDoctagInstance == signingDoctagInstance &&
     other.verboseName == verboseName &&
     other.verification == verification;

  @override
  int get hashCode =>
    (created == null ? 0 : created.hashCode) +
    (owner == null ? 0 : owner.hashCode) +
    (ownerAddress == null ? 0 : ownerAddress.hashCode) +
    (publicKey == null ? 0 : publicKey.hashCode) +
    (signingDoctagInstance == null ? 0 : signingDoctagInstance.hashCode) +
    (verboseName == null ? 0 : verboseName.hashCode) +
    (verification == null ? 0 : verification.hashCode);

  @override
  String toString() => 'PublicKeyResponse[created=$created, owner=$owner, ownerAddress=$ownerAddress, publicKey=$publicKey, signingDoctagInstance=$signingDoctagInstance, verboseName=$verboseName, verification=$verification]';

  Map<String, dynamic> toJson() {
    final json = <String, dynamic>{};
    if (created != null) {
      json[r'created'] = created;
    }
    if (owner != null) {
      json[r'owner'] = owner;
    }
    if (ownerAddress != null) {
      json[r'ownerAddress'] = ownerAddress;
    }
    if (publicKey != null) {
      json[r'publicKey'] = publicKey;
    }
    if (signingDoctagInstance != null) {
      json[r'signingDoctagInstance'] = signingDoctagInstance;
    }
    if (verboseName != null) {
      json[r'verboseName'] = verboseName;
    }
    if (verification != null) {
      json[r'verification'] = verification;
    }
    return json;
  }

  /// Returns a new [PublicKeyResponse] instance and imports its values from
  /// [json] if it's non-null, null if [json] is null.
  static PublicKeyResponse? fromJson(Map<String, dynamic>? json) => json == null
    ? null
    : PublicKeyResponse(
        created: json[r'created'],
        owner: Person.fromJson(json[r'owner']),
        ownerAddress: Address.fromJson(json[r'ownerAddress']),
        publicKey: json[r'publicKey'],
        signingDoctagInstance: json[r'signingDoctagInstance'],
        verboseName: json[r'verboseName'],
        verification: PublicKeyVerification.fromJson(json[r'verification']),
    );

  static List<PublicKeyResponse?>? listFromJson(List<dynamic> json, {bool? emptyIsNull, bool? growable,}) =>
    json == null || json.isEmpty
      ? true == emptyIsNull ? null : <PublicKeyResponse>[]
      : json.map((v) => PublicKeyResponse.fromJson(v)).toList(growable: true == growable);

  static Map<String, PublicKeyResponse?> mapFromJson(Map<String, dynamic> json) {
    final map = <String, PublicKeyResponse?>{};
    if (json != null && json.isNotEmpty) {
      json.forEach((String key, dynamic v) => map[key] = PublicKeyResponse.fromJson(v));
    }
    return map;
  }

  // maps a json object with a list of PublicKeyResponse-objects as value to a dart map
  static Map<String, List<PublicKeyResponse?>?> mapListFromJson(Map<String, dynamic> json, {bool? emptyIsNull, bool? growable,}) {
    final Map<String, List<PublicKeyResponse?>?> map = <String, List<PublicKeyResponse>?>{};
    if (json != null && json.isNotEmpty) {
      json.forEach((String key, dynamic v) {
        map[key] = PublicKeyResponse.listFromJson(v, emptyIsNull: emptyIsNull, growable: growable);
      });
    }
    return map;
  }
}

