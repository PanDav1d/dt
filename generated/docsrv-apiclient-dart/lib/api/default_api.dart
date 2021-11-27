//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//
// @dart=2.0

// ignore_for_file: unused_element, unused_import
// ignore_for_file: always_put_required_named_parameters_first
// ignore_for_file: lines_longer_than_80_chars

part of docsrv_api;


class DefaultApi {
  DefaultApi([ApiClient apiClient]) : apiClient = apiClient ?? defaultApiClient;

  final ApiClient apiClient;

  /// Add signature to document
  ///
  /// Note: This method returns the HTTP [Response].
  ///
  /// Parameters:
  ///
  /// * [String] documentId (required):
  ///   documentId
  ///
  /// * [String] hostname (required):
  ///   hostname
  ///
  /// * [EmbeddedSignature] embeddedSignature:
  Future<Response> addSignatureToDoctagDocumentWithHttpInfo(String documentId, String hostname, { EmbeddedSignature embeddedSignature }) async {
    // Verify required params are set.
    if (documentId == null) {
     throw ApiException(HttpStatus.badRequest, 'Missing required param: documentId');
    }
    if (hostname == null) {
     throw ApiException(HttpStatus.badRequest, 'Missing required param: hostname');
    }

    final path = r'/d/{documentId}/{hostname}'
      .replaceAll('{' + 'documentId' + '}', documentId.toString())
      .replaceAll('{' + 'hostname' + '}', hostname.toString());

    Object postBody = embeddedSignature;

    final queryParams = <QueryParam>[];
    final headerParams = <String, String>{};
    final formParams = <String, String>{};

    final contentTypes = <String>['application/json'];
    final nullableContentType = contentTypes.isNotEmpty ? contentTypes[0] : null;
    final authNames = <String>[];

    if (
      nullableContentType != null &&
      nullableContentType.toLowerCase().startsWith('multipart/form-data')
    ) {
      bool hasFields = false;
      final mp = MultipartRequest(null, null);
      if (hasFields) {
        postBody = mp;
      }
    } else {
    }

    return await apiClient.invokeAPI(
      path,
      'POST',
      queryParams,
      postBody,
      headerParams,
      formParams,
      nullableContentType,
      authNames,
    );
  }

  /// Add signature to document
  ///
  /// Parameters:
  ///
  /// * [String] documentId (required):
  ///   documentId
  ///
  /// * [String] hostname (required):
  ///   hostname
  ///
  /// * [EmbeddedSignature] embeddedSignature:
  Future<Document> addSignatureToDoctagDocument(String documentId, String hostname, { EmbeddedSignature embeddedSignature }) async {
    final response = await addSignatureToDoctagDocumentWithHttpInfo(documentId, hostname,  embeddedSignature: embeddedSignature );
    if (response.statusCode >= HttpStatus.badRequest) {
      throw ApiException(response.statusCode, await _decodeBodyBytes(response));
    }
    // When a remote server returns no body with a status of 204, we shall not decode it.
    // At the time of writing this, `dart:convert` will throw an "Unexpected end of input"
    // FormatException when trying to decode an empty string.
    if (response.body != null && response.statusCode != HttpStatus.noContent) {
      return await apiClient.deserializeAsync(await _decodeBodyBytes(response), 'Document',) as Document;
        }
    return Future<Document>.value(null);
  }

  /// Perform Health Check
  ///
  /// Note: This method returns the HTTP [Response].
  Future<Response> checkHealthWithHttpInfo() async {
    final path = r'/health';

    Object postBody;

    final queryParams = <QueryParam>[];
    final headerParams = <String, String>{};
    final formParams = <String, String>{};

    final contentTypes = <String>[];
    final nullableContentType = contentTypes.isNotEmpty ? contentTypes[0] : null;
    final authNames = <String>[];

    if (
      nullableContentType != null &&
      nullableContentType.toLowerCase().startsWith('multipart/form-data')
    ) {
      bool hasFields = false;
      final mp = MultipartRequest(null, null);
      if (hasFields) {
        postBody = mp;
      }
    } else {
    }

    return await apiClient.invokeAPI(
      path,
      'GET',
      queryParams,
      postBody,
      headerParams,
      formParams,
      nullableContentType,
      authNames,
    );
  }

  /// Perform Health Check
  Future<HealthCheckResponse> checkHealth() async {
    final response = await checkHealthWithHttpInfo();
    if (response.statusCode >= HttpStatus.badRequest) {
      throw ApiException(response.statusCode, await _decodeBodyBytes(response));
    }
    // When a remote server returns no body with a status of 204, we shall not decode it.
    // At the time of writing this, `dart:convert` will throw an "Unexpected end of input"
    // FormatException when trying to decode an empty string.
    if (response.body != null && response.statusCode != HttpStatus.noContent) {
      return await apiClient.deserializeAsync(await _decodeBodyBytes(response), 'HealthCheckResponse',) as HealthCheckResponse;
        }
    return Future<HealthCheckResponse>.value(null);
  }

  /// Perform Instance discovery
  ///
  /// Note: This method returns the HTTP [Response].
  Future<Response> discoverInstanceWithHttpInfo() async {
    final path = r'/discovery';

    Object postBody;

    final queryParams = <QueryParam>[];
    final headerParams = <String, String>{};
    final formParams = <String, String>{};

    final contentTypes = <String>[];
    final nullableContentType = contentTypes.isNotEmpty ? contentTypes[0] : null;
    final authNames = <String>[];

    if (
      nullableContentType != null &&
      nullableContentType.toLowerCase().startsWith('multipart/form-data')
    ) {
      bool hasFields = false;
      final mp = MultipartRequest(null, null);
      if (hasFields) {
        postBody = mp;
      }
    } else {
    }

    return await apiClient.invokeAPI(
      path,
      'GET',
      queryParams,
      postBody,
      headerParams,
      formParams,
      nullableContentType,
      authNames,
    );
  }

  /// Perform Instance discovery
  Future<DiscoveryResponse> discoverInstance() async {
    final response = await discoverInstanceWithHttpInfo();
    if (response.statusCode >= HttpStatus.badRequest) {
      throw ApiException(response.statusCode, await _decodeBodyBytes(response));
    }
    // When a remote server returns no body with a status of 204, we shall not decode it.
    // At the time of writing this, `dart:convert` will throw an "Unexpected end of input"
    // FormatException when trying to decode an empty string.
    if (response.body != null && response.statusCode != HttpStatus.noContent) {
      return await apiClient.deserializeAsync(await _decodeBodyBytes(response), 'DiscoveryResponse',) as DiscoveryResponse;
        }
    return Future<DiscoveryResponse>.value(null);
  }

  /// Download document
  ///
  /// Note: This method returns the HTTP [Response].
  ///
  /// Parameters:
  ///
  /// * [String] documentId (required):
  ///   documentId
  Future<Response> downloadDocumentWithHttpInfo(String documentId) async {
    // Verify required params are set.
    if (documentId == null) {
     throw ApiException(HttpStatus.badRequest, 'Missing required param: documentId');
    }

    final path = r'/d/{documentId}/download'
      .replaceAll('{' + 'documentId' + '}', documentId.toString());

    Object postBody;

    final queryParams = <QueryParam>[];
    final headerParams = <String, String>{};
    final formParams = <String, String>{};

    final contentTypes = <String>[];
    final nullableContentType = contentTypes.isNotEmpty ? contentTypes[0] : null;
    final authNames = <String>[];

    if (
      nullableContentType != null &&
      nullableContentType.toLowerCase().startsWith('multipart/form-data')
    ) {
      bool hasFields = false;
      final mp = MultipartRequest(null, null);
      if (hasFields) {
        postBody = mp;
      }
    } else {
    }

    return await apiClient.invokeAPI(
      path,
      'GET',
      queryParams,
      postBody,
      headerParams,
      formParams,
      nullableContentType,
      authNames,
    );
  }

  /// Download document
  ///
  /// Parameters:
  ///
  /// * [String] documentId (required):
  ///   documentId
  Future<DiscoveryResponse> downloadDocument(String documentId) async {
    final response = await downloadDocumentWithHttpInfo(documentId);
    if (response.statusCode >= HttpStatus.badRequest) {
      throw ApiException(response.statusCode, await _decodeBodyBytes(response));
    }
    // When a remote server returns no body with a status of 204, we shall not decode it.
    // At the time of writing this, `dart:convert` will throw an "Unexpected end of input"
    // FormatException when trying to decode an empty string.
    if (response.body != null && response.statusCode != HttpStatus.noContent) {
      return await apiClient.deserializeAsync(await _decodeBodyBytes(response), 'DiscoveryResponse',) as DiscoveryResponse;
        }
    return Future<DiscoveryResponse>.value(null);
  }

  /// Perform Instance discovery
  ///
  /// Note: This method returns the HTTP [Response].
  ///
  /// Parameters:
  ///
  /// * [String] fileId (required):
  ///   fileId
  Future<Response> downloadFileWithHttpInfo(String fileId) async {
    // Verify required params are set.
    if (fileId == null) {
     throw ApiException(HttpStatus.badRequest, 'Missing required param: fileId');
    }

    final path = r'/f/{fileId}/download'
      .replaceAll('{' + 'fileId' + '}', fileId.toString());

    Object postBody;

    final queryParams = <QueryParam>[];
    final headerParams = <String, String>{};
    final formParams = <String, String>{};

    final contentTypes = <String>[];
    final nullableContentType = contentTypes.isNotEmpty ? contentTypes[0] : null;
    final authNames = <String>[];

    if (
      nullableContentType != null &&
      nullableContentType.toLowerCase().startsWith('multipart/form-data')
    ) {
      bool hasFields = false;
      final mp = MultipartRequest(null, null);
      if (hasFields) {
        postBody = mp;
      }
    } else {
    }

    return await apiClient.invokeAPI(
      path,
      'GET',
      queryParams,
      postBody,
      headerParams,
      formParams,
      nullableContentType,
      authNames,
    );
  }

  /// Perform Instance discovery
  ///
  /// Parameters:
  ///
  /// * [String] fileId (required):
  ///   fileId
  Future<DiscoveryResponse> downloadFile(String fileId) async {
    final response = await downloadFileWithHttpInfo(fileId);
    if (response.statusCode >= HttpStatus.badRequest) {
      throw ApiException(response.statusCode, await _decodeBodyBytes(response));
    }
    // When a remote server returns no body with a status of 204, we shall not decode it.
    // At the time of writing this, `dart:convert` will throw an "Unexpected end of input"
    // FormatException when trying to decode an empty string.
    if (response.body != null && response.statusCode != HttpStatus.noContent) {
      return await apiClient.deserializeAsync(await _decodeBodyBytes(response), 'DiscoveryResponse',) as DiscoveryResponse;
        }
    return Future<DiscoveryResponse>.value(null);
  }

  /// Download sign sheet
  ///
  /// Note: This method returns the HTTP [Response].
  ///
  /// Parameters:
  ///
  /// * [String] documentId (required):
  ///   documentId
  Future<Response> downloadSignSheetWithHttpInfo(String documentId) async {
    // Verify required params are set.
    if (documentId == null) {
     throw ApiException(HttpStatus.badRequest, 'Missing required param: documentId');
    }

    final path = r'/d/{documentId}/viewSignSheet'
      .replaceAll('{' + 'documentId' + '}', documentId.toString());

    Object postBody;

    final queryParams = <QueryParam>[];
    final headerParams = <String, String>{};
    final formParams = <String, String>{};

    final contentTypes = <String>[];
    final nullableContentType = contentTypes.isNotEmpty ? contentTypes[0] : null;
    final authNames = <String>[];

    if (
      nullableContentType != null &&
      nullableContentType.toLowerCase().startsWith('multipart/form-data')
    ) {
      bool hasFields = false;
      final mp = MultipartRequest(null, null);
      if (hasFields) {
        postBody = mp;
      }
    } else {
    }

    return await apiClient.invokeAPI(
      path,
      'GET',
      queryParams,
      postBody,
      headerParams,
      formParams,
      nullableContentType,
      authNames,
    );
  }

  /// Download sign sheet
  ///
  /// Parameters:
  ///
  /// * [String] documentId (required):
  ///   documentId
  Future<DiscoveryResponse> downloadSignSheet(String documentId) async {
    final response = await downloadSignSheetWithHttpInfo(documentId);
    if (response.statusCode >= HttpStatus.badRequest) {
      throw ApiException(response.statusCode, await _decodeBodyBytes(response));
    }
    // When a remote server returns no body with a status of 204, we shall not decode it.
    // At the time of writing this, `dart:convert` will throw an "Unexpected end of input"
    // FormatException when trying to decode an empty string.
    if (response.body != null && response.statusCode != HttpStatus.noContent) {
      return await apiClient.deserializeAsync(await _decodeBodyBytes(response), 'DiscoveryResponse',) as DiscoveryResponse;
        }
    return Future<DiscoveryResponse>.value(null);
  }

  /// Check authentication
  ///
  /// Note: This method returns the HTTP [Response].
  Future<Response> fetchAuthInfoWithHttpInfo() async {
    final path = r'/app/auth_info';

    Object postBody;

    final queryParams = <QueryParam>[];
    final headerParams = <String, String>{};
    final formParams = <String, String>{};

    final contentTypes = <String>[];
    final nullableContentType = contentTypes.isNotEmpty ? contentTypes[0] : null;
    final authNames = <String>[];

    if (
      nullableContentType != null &&
      nullableContentType.toLowerCase().startsWith('multipart/form-data')
    ) {
      bool hasFields = false;
      final mp = MultipartRequest(null, null);
      if (hasFields) {
        postBody = mp;
      }
    } else {
    }

    return await apiClient.invokeAPI(
      path,
      'GET',
      queryParams,
      postBody,
      headerParams,
      formParams,
      nullableContentType,
      authNames,
    );
  }

  /// Check authentication
  Future<AuthInfoResponse> fetchAuthInfo() async {
    final response = await fetchAuthInfoWithHttpInfo();
    if (response.statusCode >= HttpStatus.badRequest) {
      throw ApiException(response.statusCode, await _decodeBodyBytes(response));
    }
    // When a remote server returns no body with a status of 204, we shall not decode it.
    // At the time of writing this, `dart:convert` will throw an "Unexpected end of input"
    // FormatException when trying to decode an empty string.
    if (response.body != null && response.statusCode != HttpStatus.noContent) {
      return await apiClient.deserializeAsync(await _decodeBodyBytes(response), 'AuthInfoResponse',) as AuthInfoResponse;
        }
    return Future<AuthInfoResponse>.value(null);
  }

  /// Fetch doctag document
  ///
  /// Note: This method returns the HTTP [Response].
  ///
  /// Parameters:
  ///
  /// * [String] documentId (required):
  ///   documentId
  Future<Response> fetchDoctagDocumentWithHttpInfo(String documentId) async {
    // Verify required params are set.
    if (documentId == null) {
     throw ApiException(HttpStatus.badRequest, 'Missing required param: documentId');
    }

    final path = r'/d/{documentId}'
      .replaceAll('{' + 'documentId' + '}', documentId.toString());

    Object postBody;

    final queryParams = <QueryParam>[];
    final headerParams = <String, String>{};
    final formParams = <String, String>{};

    final contentTypes = <String>[];
    final nullableContentType = contentTypes.isNotEmpty ? contentTypes[0] : null;
    final authNames = <String>[];

    if (
      nullableContentType != null &&
      nullableContentType.toLowerCase().startsWith('multipart/form-data')
    ) {
      bool hasFields = false;
      final mp = MultipartRequest(null, null);
      if (hasFields) {
        postBody = mp;
      }
    } else {
    }

    return await apiClient.invokeAPI(
      path,
      'GET',
      queryParams,
      postBody,
      headerParams,
      formParams,
      nullableContentType,
      authNames,
    );
  }

  /// Fetch doctag document
  ///
  /// Parameters:
  ///
  /// * [String] documentId (required):
  ///   documentId
  Future<EmbeddedDocument> fetchDoctagDocument(String documentId) async {
    final response = await fetchDoctagDocumentWithHttpInfo(documentId);
    if (response.statusCode >= HttpStatus.badRequest) {
      throw ApiException(response.statusCode, await _decodeBodyBytes(response));
    }
    // When a remote server returns no body with a status of 204, we shall not decode it.
    // At the time of writing this, `dart:convert` will throw an "Unexpected end of input"
    // FormatException when trying to decode an empty string.
    if (response.body != null && response.statusCode != HttpStatus.noContent) {
      return await apiClient.deserializeAsync(await _decodeBodyBytes(response), 'EmbeddedDocument',) as EmbeddedDocument;
        }
    return Future<EmbeddedDocument>.value(null);
  }

  /// Check authentication
  ///
  /// Note: This method returns the HTTP [Response].
  ///
  /// Parameters:
  ///
  /// * [String] documentId (required):
  ///   documentId
  ///
  /// * [String] hostname (required):
  ///   hostname
  Future<Response> fetchWorkflowToSignWithHttpInfo(String documentId, String hostname) async {
    // Verify required params are set.
    if (documentId == null) {
     throw ApiException(HttpStatus.badRequest, 'Missing required param: documentId');
    }
    if (hostname == null) {
     throw ApiException(HttpStatus.badRequest, 'Missing required param: hostname');
    }

    final path = r'/app/signature/prepare/{documentId}/{hostname}'
      .replaceAll('{' + 'documentId' + '}', documentId.toString())
      .replaceAll('{' + 'hostname' + '}', hostname.toString());

    Object postBody;

    final queryParams = <QueryParam>[];
    final headerParams = <String, String>{};
    final formParams = <String, String>{};

    final contentTypes = <String>[];
    final nullableContentType = contentTypes.isNotEmpty ? contentTypes[0] : null;
    final authNames = <String>[];

    if (
      nullableContentType != null &&
      nullableContentType.toLowerCase().startsWith('multipart/form-data')
    ) {
      bool hasFields = false;
      final mp = MultipartRequest(null, null);
      if (hasFields) {
        postBody = mp;
      }
    } else {
    }

    return await apiClient.invokeAPI(
      path,
      'GET',
      queryParams,
      postBody,
      headerParams,
      formParams,
      nullableContentType,
      authNames,
    );
  }

  /// Check authentication
  ///
  /// Parameters:
  ///
  /// * [String] documentId (required):
  ///   documentId
  ///
  /// * [String] hostname (required):
  ///   hostname
  Future<PreparedSignature> fetchWorkflowToSign(String documentId, String hostname) async {
    final response = await fetchWorkflowToSignWithHttpInfo(documentId, hostname);
    if (response.statusCode >= HttpStatus.badRequest) {
      throw ApiException(response.statusCode, await _decodeBodyBytes(response));
    }
    // When a remote server returns no body with a status of 204, we shall not decode it.
    // At the time of writing this, `dart:convert` will throw an "Unexpected end of input"
    // FormatException when trying to decode an empty string.
    if (response.body != null && response.statusCode != HttpStatus.noContent) {
      return await apiClient.deserializeAsync(await _decodeBodyBytes(response), 'PreparedSignature',) as PreparedSignature;
        }
    return Future<PreparedSignature>.value(null);
  }

  /// Add signature to document
  ///
  /// Note: This method returns the HTTP [Response].
  ///
  /// Parameters:
  ///
  /// * [NotifyRequest] notifyRequest:
  Future<Response> notifyChangesOfDoctagDocumentWithHttpInfo({ NotifyRequest notifyRequest }) async {
    // Verify required params are set.

    final path = r'/d/notifyChanges/';

    Object postBody = notifyRequest;

    final queryParams = <QueryParam>[];
    final headerParams = <String, String>{};
    final formParams = <String, String>{};

    final contentTypes = <String>['application/json'];
    final nullableContentType = contentTypes.isNotEmpty ? contentTypes[0] : null;
    final authNames = <String>[];

    if (
      nullableContentType != null &&
      nullableContentType.toLowerCase().startsWith('multipart/form-data')
    ) {
      bool hasFields = false;
      final mp = MultipartRequest(null, null);
      if (hasFields) {
        postBody = mp;
      }
    } else {
    }

    return await apiClient.invokeAPI(
      path,
      'POST',
      queryParams,
      postBody,
      headerParams,
      formParams,
      nullableContentType,
      authNames,
    );
  }

  /// Add signature to document
  ///
  /// Parameters:
  ///
  /// * [NotifyRequest] notifyRequest:
  Future<Object> notifyChangesOfDoctagDocument({ NotifyRequest notifyRequest }) async {
    final response = await notifyChangesOfDoctagDocumentWithHttpInfo( notifyRequest: notifyRequest );
    if (response.statusCode >= HttpStatus.badRequest) {
      throw ApiException(response.statusCode, await _decodeBodyBytes(response));
    }
    // When a remote server returns no body with a status of 204, we shall not decode it.
    // At the time of writing this, `dart:convert` will throw an "Unexpected end of input"
    // FormatException when trying to decode an empty string.
    if (response.body != null && response.statusCode != HttpStatus.noContent) {
      return await apiClient.deserializeAsync(await _decodeBodyBytes(response), 'Object',) as Object;
        }
    return Future<Object>.value(null);
  }

  /// Set the verification of the private public key
  ///
  /// Note: This method returns the HTTP [Response].
  ///
  /// Parameters:
  ///
  /// * [String] publicKeyFingerprint (required):
  ///   publicKeyFingerprint
  ///
  /// * [PublicKeyVerification] publicKeyVerification:
  Future<Response> setVerificationOfKeyPairWithHttpInfo(String publicKeyFingerprint, { PublicKeyVerification publicKeyVerification }) async {
    // Verify required params are set.
    if (publicKeyFingerprint == null) {
     throw ApiException(HttpStatus.badRequest, 'Missing required param: publicKeyFingerprint');
    }

    final path = r'/k/{publicKeyFingerprint}/verification'
      .replaceAll('{' + 'publicKeyFingerprint' + '}', publicKeyFingerprint.toString());

    Object postBody = publicKeyVerification;

    final queryParams = <QueryParam>[];
    final headerParams = <String, String>{};
    final formParams = <String, String>{};

    final contentTypes = <String>['application/json'];
    final nullableContentType = contentTypes.isNotEmpty ? contentTypes[0] : null;
    final authNames = <String>[];

    if (
      nullableContentType != null &&
      nullableContentType.toLowerCase().startsWith('multipart/form-data')
    ) {
      bool hasFields = false;
      final mp = MultipartRequest(null, null);
      if (hasFields) {
        postBody = mp;
      }
    } else {
    }

    return await apiClient.invokeAPI(
      path,
      'PUT',
      queryParams,
      postBody,
      headerParams,
      formParams,
      nullableContentType,
      authNames,
    );
  }

  /// Set the verification of the private public key
  ///
  /// Parameters:
  ///
  /// * [String] publicKeyFingerprint (required):
  ///   publicKeyFingerprint
  ///
  /// * [PublicKeyVerification] publicKeyVerification:
  Future<DiscoveryResponse> setVerificationOfKeyPair(String publicKeyFingerprint, { PublicKeyVerification publicKeyVerification }) async {
    final response = await setVerificationOfKeyPairWithHttpInfo(publicKeyFingerprint,  publicKeyVerification: publicKeyVerification );
    if (response.statusCode >= HttpStatus.badRequest) {
      throw ApiException(response.statusCode, await _decodeBodyBytes(response));
    }
    // When a remote server returns no body with a status of 204, we shall not decode it.
    // At the time of writing this, `dart:convert` will throw an "Unexpected end of input"
    // FormatException when trying to decode an empty string.
    if (response.body != null && response.statusCode != HttpStatus.noContent) {
      return await apiClient.deserializeAsync(await _decodeBodyBytes(response), 'DiscoveryResponse',) as DiscoveryResponse;
        }
    return Future<DiscoveryResponse>.value(null);
  }

  /// Check authentication
  ///
  /// Note: This method returns the HTTP [Response].
  ///
  /// Parameters:
  ///
  /// * [String] documentId (required):
  ///   documentId
  ///
  /// * [String] hostname (required):
  ///   hostname
  ///
  /// * [SignatureInputs] signatureInputs:
  Future<Response> uploadWorkflowResultAndTriggerSignatureWithHttpInfo(String documentId, String hostname, { SignatureInputs signatureInputs }) async {
    // Verify required params are set.
    if (documentId == null) {
     throw ApiException(HttpStatus.badRequest, 'Missing required param: documentId');
    }
    if (hostname == null) {
     throw ApiException(HttpStatus.badRequest, 'Missing required param: hostname');
    }

    final path = r'/app/signature/push/{documentId}/{hostname}'
      .replaceAll('{' + 'documentId' + '}', documentId.toString())
      .replaceAll('{' + 'hostname' + '}', hostname.toString());

    Object postBody = signatureInputs;

    final queryParams = <QueryParam>[];
    final headerParams = <String, String>{};
    final formParams = <String, String>{};

    final contentTypes = <String>['application/json'];
    final nullableContentType = contentTypes.isNotEmpty ? contentTypes[0] : null;
    final authNames = <String>[];

    if (
      nullableContentType != null &&
      nullableContentType.toLowerCase().startsWith('multipart/form-data')
    ) {
      bool hasFields = false;
      final mp = MultipartRequest(null, null);
      if (hasFields) {
        postBody = mp;
      }
    } else {
    }

    return await apiClient.invokeAPI(
      path,
      'POST',
      queryParams,
      postBody,
      headerParams,
      formParams,
      nullableContentType,
      authNames,
    );
  }

  /// Check authentication
  ///
  /// Parameters:
  ///
  /// * [String] documentId (required):
  ///   documentId
  ///
  /// * [String] hostname (required):
  ///   hostname
  ///
  /// * [SignatureInputs] signatureInputs:
  Future<AuthInfoResponse> uploadWorkflowResultAndTriggerSignature(String documentId, String hostname, { SignatureInputs signatureInputs }) async {
    final response = await uploadWorkflowResultAndTriggerSignatureWithHttpInfo(documentId, hostname,  signatureInputs: signatureInputs );
    if (response.statusCode >= HttpStatus.badRequest) {
      throw ApiException(response.statusCode, await _decodeBodyBytes(response));
    }
    // When a remote server returns no body with a status of 204, we shall not decode it.
    // At the time of writing this, `dart:convert` will throw an "Unexpected end of input"
    // FormatException when trying to decode an empty string.
    if (response.body != null && response.statusCode != HttpStatus.noContent) {
      return await apiClient.deserializeAsync(await _decodeBodyBytes(response), 'AuthInfoResponse',) as AuthInfoResponse;
        }
    return Future<AuthInfoResponse>.value(null);
  }

  /// Check that this instance actually owns the given private key
  ///
  /// Note: This method returns the HTTP [Response].
  ///
  /// Parameters:
  ///
  /// * [String] publicKeyFingerprint (required):
  ///   publicKeyFingerprint
  ///
  /// * [String] seed (required):
  ///   seed
  Future<Response> verifyInstanceHasPrivateKeyWithHttpInfo(String publicKeyFingerprint, String seed) async {
    // Verify required params are set.
    if (publicKeyFingerprint == null) {
     throw ApiException(HttpStatus.badRequest, 'Missing required param: publicKeyFingerprint');
    }
    if (seed == null) {
     throw ApiException(HttpStatus.badRequest, 'Missing required param: seed');
    }

    final path = r'/k/{publicKeyFingerprint}/verify/{seed}'
      .replaceAll('{' + 'publicKeyFingerprint' + '}', publicKeyFingerprint.toString())
      .replaceAll('{' + 'seed' + '}', seed.toString());

    Object postBody;

    final queryParams = <QueryParam>[];
    final headerParams = <String, String>{};
    final formParams = <String, String>{};

    final contentTypes = <String>[];
    final nullableContentType = contentTypes.isNotEmpty ? contentTypes[0] : null;
    final authNames = <String>[];

    if (
      nullableContentType != null &&
      nullableContentType.toLowerCase().startsWith('multipart/form-data')
    ) {
      bool hasFields = false;
      final mp = MultipartRequest(null, null);
      if (hasFields) {
        postBody = mp;
      }
    } else {
    }

    return await apiClient.invokeAPI(
      path,
      'GET',
      queryParams,
      postBody,
      headerParams,
      formParams,
      nullableContentType,
      authNames,
    );
  }

  /// Check that this instance actually owns the given private key
  ///
  /// Parameters:
  ///
  /// * [String] publicKeyFingerprint (required):
  ///   publicKeyFingerprint
  ///
  /// * [String] seed (required):
  ///   seed
  Future<DiscoveryResponse> verifyInstanceHasPrivateKey(String publicKeyFingerprint, String seed) async {
    final response = await verifyInstanceHasPrivateKeyWithHttpInfo(publicKeyFingerprint, seed);
    if (response.statusCode >= HttpStatus.badRequest) {
      throw ApiException(response.statusCode, await _decodeBodyBytes(response));
    }
    // When a remote server returns no body with a status of 204, we shall not decode it.
    // At the time of writing this, `dart:convert` will throw an "Unexpected end of input"
    // FormatException when trying to decode an empty string.
    if (response.body != null && response.statusCode != HttpStatus.noContent) {
      return await apiClient.deserializeAsync(await _decodeBodyBytes(response), 'DiscoveryResponse',) as DiscoveryResponse;
        }
    return Future<DiscoveryResponse>.value(null);
  }

  /// Perform Instance discovery
  ///
  /// Note: This method returns the HTTP [Response].
  ///
  /// Parameters:
  ///
  /// * [String] fileId (required):
  ///   fileId
  Future<Response> viewFileWithHttpInfo(String fileId) async {
    // Verify required params are set.
    if (fileId == null) {
     throw ApiException(HttpStatus.badRequest, 'Missing required param: fileId');
    }

    final path = r'/f/{fileId}/view'
      .replaceAll('{' + 'fileId' + '}', fileId.toString());

    Object postBody;

    final queryParams = <QueryParam>[];
    final headerParams = <String, String>{};
    final formParams = <String, String>{};

    final contentTypes = <String>[];
    final nullableContentType = contentTypes.isNotEmpty ? contentTypes[0] : null;
    final authNames = <String>[];

    if (
      nullableContentType != null &&
      nullableContentType.toLowerCase().startsWith('multipart/form-data')
    ) {
      bool hasFields = false;
      final mp = MultipartRequest(null, null);
      if (hasFields) {
        postBody = mp;
      }
    } else {
    }

    return await apiClient.invokeAPI(
      path,
      'GET',
      queryParams,
      postBody,
      headerParams,
      formParams,
      nullableContentType,
      authNames,
    );
  }

  /// Perform Instance discovery
  ///
  /// Parameters:
  ///
  /// * [String] fileId (required):
  ///   fileId
  Future<DiscoveryResponse> viewFile(String fileId) async {
    final response = await viewFileWithHttpInfo(fileId);
    if (response.statusCode >= HttpStatus.badRequest) {
      throw ApiException(response.statusCode, await _decodeBodyBytes(response));
    }
    // When a remote server returns no body with a status of 204, we shall not decode it.
    // At the time of writing this, `dart:convert` will throw an "Unexpected end of input"
    // FormatException when trying to decode an empty string.
    if (response.body != null && response.statusCode != HttpStatus.noContent) {
      return await apiClient.deserializeAsync(await _decodeBodyBytes(response), 'DiscoveryResponse',) as DiscoveryResponse;
        }
    return Future<DiscoveryResponse>.value(null);
  }
}
