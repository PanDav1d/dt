//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//


// ignore_for_file: unused_element, unused_import
// ignore_for_file: always_put_required_named_parameters_first
// ignore_for_file: lines_longer_than_80_chars

part of docsrv_api;

class WorkflowInput {
  /// Returns a new [WorkflowInput] instance.
  WorkflowInput({
    this.description,
    this.kind,
    this.name,
  });

  String? description;

  WorkflowInputKindEnum? kind;

  String? name;

  @override
  bool operator ==(Object other) => identical(this, other) || other is WorkflowInput &&
     other.description == description &&
     other.kind == kind &&
     other.name == name;

  @override
  int get hashCode =>
    (description == null ? 0 : description.hashCode) +
    (kind == null ? 0 : kind.hashCode) +
    (name == null ? 0 : name.hashCode);

  @override
  String toString() => 'WorkflowInput[description=$description, kind=$kind, name=$name]';

  Map<String, dynamic> toJson() {
    final json = <String, dynamic>{};
    if (description != null) {
      json[r'description'] = description;
    }
    if (kind != null) {
      json[r'kind'] = kind;
    }
    if (name != null) {
      json[r'name'] = name;
    }
    return json;
  }

  /// Returns a new [WorkflowInput] instance and imports its values from
  /// [json] if it's non-null, null if [json] is null.
  static WorkflowInput? fromJson(Map<String, dynamic>? json) => json == null
    ? null
    : WorkflowInput(
        description: json[r'description'],
        kind: WorkflowInputKindEnum.fromJson(json[r'kind']),
        name: json[r'name'],
    );

  static List<WorkflowInput?>? listFromJson(List<dynamic>? json, {bool? emptyIsNull, bool? growable,}) =>
    json == null || json.isEmpty
      ? true == emptyIsNull ? null : <WorkflowInput>[]
      : json.map((v) => WorkflowInput.fromJson(v)).toList(growable: true == growable);

  static Map<String, WorkflowInput?> mapFromJson(Map<String, dynamic> json) {
    final map = <String, WorkflowInput?>{};
    if (json != null && json.isNotEmpty) {
      json.forEach((String key, dynamic v) => map[key] = WorkflowInput.fromJson(v));
    }
    return map;
  }

  // maps a json object with a list of WorkflowInput-objects as value to a dart map
  static Map<String, List<WorkflowInput?>?> mapListFromJson(Map<String, dynamic> json, {bool? emptyIsNull, bool? growable,}) {
    final Map<String, List<WorkflowInput?>?> map = <String, List<WorkflowInput>?>{};
    if (json != null && json.isNotEmpty) {
      json.forEach((String key, dynamic v) {
        map[key] = WorkflowInput.listFromJson(v, emptyIsNull: emptyIsNull, growable: growable);
      });
    }
    return map;
  }
}


class WorkflowInputKindEnum {
  /// Instantiate a new enum with the provided [value].
  const WorkflowInputKindEnum._(this.value);

  /// The underlying value of this enum member.
  final String value;

  @override
  String toString() => value;

  String toJson() => value;

  static const textInput = WorkflowInputKindEnum._(r'TextInput');
  static const fileInput = WorkflowInputKindEnum._(r'FileInput');
  static const selectFromList = WorkflowInputKindEnum._(r'SelectFromList');
  static const checkbox = WorkflowInputKindEnum._(r'Checkbox');
  static const sign = WorkflowInputKindEnum._(r'Sign');

  /// List of all possible values in this [enum][WorkflowInputKindEnum].
  static const values = <WorkflowInputKindEnum>[
    textInput,
    fileInput,
    selectFromList,
    checkbox,
    sign,
  ];

  static WorkflowInputKindEnum? fromJson(dynamic value) =>
    WorkflowInputKindEnumTypeTransformer().decode(value);

  static List<WorkflowInputKindEnum?>? listFromJson(List<dynamic> json, {bool? emptyIsNull, bool? growable,}) =>
    json == null || json.isEmpty
      ? true == emptyIsNull ? null : <WorkflowInputKindEnum>[]
      : json
          .map((value) => WorkflowInputKindEnum.fromJson(value))
          .toList(growable: true == growable);
}

/// Transformation class that can [encode] an instance of [WorkflowInputKindEnum] to String,
/// and [decode] dynamic data back to [WorkflowInputKindEnum].
class WorkflowInputKindEnumTypeTransformer {
  const WorkflowInputKindEnumTypeTransformer._();

  factory WorkflowInputKindEnumTypeTransformer() => _instance ??= WorkflowInputKindEnumTypeTransformer._();

  String encode(WorkflowInputKindEnum data) => data.value;

  /// Decodes a [dynamic value][data] to a WorkflowInputKindEnum.
  ///
  /// If [allowNull] is true and the [dynamic value][data] cannot be decoded successfully,
  /// then null is returned. However, if [allowNull] is false and the [dynamic value][data]
  /// cannot be decoded successfully, then an [UnimplementedError] is thrown.
  ///
  /// The [allowNull] is very handy when an API changes and a new enum value is added or removed,
  /// and users are still using an old app with the old code.
  WorkflowInputKindEnum? decode(dynamic data, {bool? allowNull}) {
    switch (data) {
      case r'TextInput': return WorkflowInputKindEnum.textInput;
      case r'FileInput': return WorkflowInputKindEnum.fileInput;
      case r'SelectFromList': return WorkflowInputKindEnum.selectFromList;
      case r'Checkbox': return WorkflowInputKindEnum.checkbox;
      case r'Sign': return WorkflowInputKindEnum.sign;
      default:
        if (allowNull == false) {
          throw ArgumentError('Unknown enum value to decode: $data');
        }
    }
    return null;
  }

  /// Singleton [WorkflowInputKindEnumTypeTransformer] instance.
  static WorkflowInputKindEnumTypeTransformer? _instance;
}

