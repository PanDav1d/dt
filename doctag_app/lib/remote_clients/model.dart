class AuthInfoResponse{

  AuthInfoResponse({required this.authenticated, this.firstName, this.lastName});

  bool? authenticated;
  String? firstName;
  String? lastName;
}

class EmbeddedDocument{
  EmbeddedDocument({this.files, this.document});

  List<DoctagFile>? files;
  DoctagDocument? document;
}

class DoctagFile{
  DoctagFile({this.id, this.name, this.base64Content, this.contentType});
  String? id;
  String? name;
  String? base64Content;
  String? contentType;
}

class DoctagDocument{
  String? id;
  String? url;
  bool? isMirrored;
  String? originalFileName;
  String? attachmentId;
  String? attachmentHash;
  List<DoctagSignature>? signatures;
  List<String>? mirrors;
  Workflow? workflow;
  String? fullText;
}

class DoctagSignature {
  DoctagSignatureData? data;
  PublicKeyResponse? signedByKey;
  String? signed;
  String? originalMessage;
  String? role;
  List<WorkflowInputResult>? inputs;
}

class Workflow {
  String? id;
  String? name;
  List<WorkflowAction>? actions;
}

class WorkflowAction {
  String? role;
  List<WorkflowInput>? inputs;
}

class WorkflowInput {
  String? name;
  String? description;
  WorkflowInputKind? kind;
}

enum WorkflowInputKind {
  TextInput,
  FileInput,
  SelectFromList,
  Checkbox,
  Sign
}