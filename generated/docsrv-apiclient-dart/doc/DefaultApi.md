# docsrv_api.api.DefaultApi

## Load the API package
```dart
import 'package:docsrv_api/api.dart';
```

All URIs are relative to *http://localhost*

Method | HTTP request | Description
------------- | ------------- | -------------
[**addSignatureToDoctagDocument**](DefaultApi.md#addsignaturetodoctagdocument) | **POST** /d/{documentId}/{hostname} | Add signature to document
[**checkHealth**](DefaultApi.md#checkhealth) | **GET** /health | Perform Health Check
[**discoverInstance**](DefaultApi.md#discoverinstance) | **GET** /discovery | Perform Instance discovery
[**downloadDocument**](DefaultApi.md#downloaddocument) | **GET** /d/{documentId}/download | Download document
[**downloadFile**](DefaultApi.md#downloadfile) | **GET** /f/{fileId}/download | Perform Instance discovery
[**downloadSignSheet**](DefaultApi.md#downloadsignsheet) | **GET** /d/{documentId}/viewSignSheet | Download sign sheet
[**fetchAuthInfo**](DefaultApi.md#fetchauthinfo) | **GET** /app/auth_info | Check authentication
[**fetchDoctagDocument**](DefaultApi.md#fetchdoctagdocument) | **GET** /d/{documentId} | Fetch doctag document
[**fetchWorkflowToSign**](DefaultApi.md#fetchworkflowtosign) | **GET** /app/signature/prepare/{documentId}/{hostname} | Check authentication
[**setVerificationOfKeyPair**](DefaultApi.md#setverificationofkeypair) | **PUT** /k/{publicKeyFingerprint}/verify/{seed} | Set the verification of the private public key
[**uploadWorkflowResultAndTriggerSignature**](DefaultApi.md#uploadworkflowresultandtriggersignature) | **POST** /app/signature/push/{documentId}/{hostname} | Check authentication
[**verifyInstanceHasPrivateKey**](DefaultApi.md#verifyinstancehasprivatekey) | **GET** /k/{publicKeyFingerprint}/verify/{seed} | Check that this instance actually owns the given private key
[**viewFile**](DefaultApi.md#viewfile) | **GET** /f/{fileId}/view | Perform Instance discovery


# **addSignatureToDoctagDocument**
> Document addSignatureToDoctagDocument(documentId, hostname, embeddedSignature)

Add signature to document

### Example 
```dart
import 'package:docsrv_api/api.dart';

final api_instance = DefaultApi();
final documentId = documentId_example; // String | documentId
final hostname = hostname_example; // String | hostname
final embeddedSignature = EmbeddedSignature(); // EmbeddedSignature | 

try { 
    final result = api_instance.addSignatureToDoctagDocument(documentId, hostname, embeddedSignature);
    print(result);
} catch (e) {
    print('Exception when calling DefaultApi->addSignatureToDoctagDocument: $e\n');
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **documentId** | **String**| documentId | 
 **hostname** | **String**| hostname | 
 **embeddedSignature** | [**EmbeddedSignature**](EmbeddedSignature.md)|  | [optional] 

### Return type

[**Document**](Document.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **checkHealth**
> HealthCheckResponse checkHealth()

Perform Health Check

### Example 
```dart
import 'package:docsrv_api/api.dart';

final api_instance = DefaultApi();

try { 
    final result = api_instance.checkHealth();
    print(result);
} catch (e) {
    print('Exception when calling DefaultApi->checkHealth: $e\n');
}
```

### Parameters
This endpoint does not need any parameter.

### Return type

[**HealthCheckResponse**](HealthCheckResponse.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **discoverInstance**
> DiscoveryResponse discoverInstance()

Perform Instance discovery

### Example 
```dart
import 'package:docsrv_api/api.dart';

final api_instance = DefaultApi();

try { 
    final result = api_instance.discoverInstance();
    print(result);
} catch (e) {
    print('Exception when calling DefaultApi->discoverInstance: $e\n');
}
```

### Parameters
This endpoint does not need any parameter.

### Return type

[**DiscoveryResponse**](DiscoveryResponse.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **downloadDocument**
> DiscoveryResponse downloadDocument(documentId)

Download document

### Example 
```dart
import 'package:docsrv_api/api.dart';

final api_instance = DefaultApi();
final documentId = documentId_example; // String | documentId

try { 
    final result = api_instance.downloadDocument(documentId);
    print(result);
} catch (e) {
    print('Exception when calling DefaultApi->downloadDocument: $e\n');
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **documentId** | **String**| documentId | 

### Return type

[**DiscoveryResponse**](DiscoveryResponse.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **downloadFile**
> DiscoveryResponse downloadFile(fileId)

Perform Instance discovery

### Example 
```dart
import 'package:docsrv_api/api.dart';

final api_instance = DefaultApi();
final fileId = fileId_example; // String | fileId

try { 
    final result = api_instance.downloadFile(fileId);
    print(result);
} catch (e) {
    print('Exception when calling DefaultApi->downloadFile: $e\n');
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **fileId** | **String**| fileId | 

### Return type

[**DiscoveryResponse**](DiscoveryResponse.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **downloadSignSheet**
> DiscoveryResponse downloadSignSheet(documentId)

Download sign sheet

### Example 
```dart
import 'package:docsrv_api/api.dart';

final api_instance = DefaultApi();
final documentId = documentId_example; // String | documentId

try { 
    final result = api_instance.downloadSignSheet(documentId);
    print(result);
} catch (e) {
    print('Exception when calling DefaultApi->downloadSignSheet: $e\n');
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **documentId** | **String**| documentId | 

### Return type

[**DiscoveryResponse**](DiscoveryResponse.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **fetchAuthInfo**
> AuthInfoResponse fetchAuthInfo()

Check authentication

### Example 
```dart
import 'package:docsrv_api/api.dart';

final api_instance = DefaultApi();

try { 
    final result = api_instance.fetchAuthInfo();
    print(result);
} catch (e) {
    print('Exception when calling DefaultApi->fetchAuthInfo: $e\n');
}
```

### Parameters
This endpoint does not need any parameter.

### Return type

[**AuthInfoResponse**](AuthInfoResponse.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **fetchDoctagDocument**
> EmbeddedDocument fetchDoctagDocument(documentId)

Fetch doctag document

### Example 
```dart
import 'package:docsrv_api/api.dart';

final api_instance = DefaultApi();
final documentId = documentId_example; // String | documentId

try { 
    final result = api_instance.fetchDoctagDocument(documentId);
    print(result);
} catch (e) {
    print('Exception when calling DefaultApi->fetchDoctagDocument: $e\n');
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **documentId** | **String**| documentId | 

### Return type

[**EmbeddedDocument**](EmbeddedDocument.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **fetchWorkflowToSign**
> PreparedSignature fetchWorkflowToSign(documentId, hostname)

Check authentication

### Example 
```dart
import 'package:docsrv_api/api.dart';

final api_instance = DefaultApi();
final documentId = documentId_example; // String | documentId
final hostname = hostname_example; // String | hostname

try { 
    final result = api_instance.fetchWorkflowToSign(documentId, hostname);
    print(result);
} catch (e) {
    print('Exception when calling DefaultApi->fetchWorkflowToSign: $e\n');
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **documentId** | **String**| documentId | 
 **hostname** | **String**| hostname | 

### Return type

[**PreparedSignature**](PreparedSignature.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **setVerificationOfKeyPair**
> DiscoveryResponse setVerificationOfKeyPair(publicKeyFingerprint, seed, publicKeyVerification)

Set the verification of the private public key

### Example 
```dart
import 'package:docsrv_api/api.dart';

final api_instance = DefaultApi();
final publicKeyFingerprint = publicKeyFingerprint_example; // String | publicKeyFingerprint
final seed = seed_example; // String | seed
final publicKeyVerification = PublicKeyVerification(); // PublicKeyVerification | 

try { 
    final result = api_instance.setVerificationOfKeyPair(publicKeyFingerprint, seed, publicKeyVerification);
    print(result);
} catch (e) {
    print('Exception when calling DefaultApi->setVerificationOfKeyPair: $e\n');
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **publicKeyFingerprint** | **String**| publicKeyFingerprint | 
 **seed** | **String**| seed | 
 **publicKeyVerification** | [**PublicKeyVerification**](PublicKeyVerification.md)|  | [optional] 

### Return type

[**DiscoveryResponse**](DiscoveryResponse.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **uploadWorkflowResultAndTriggerSignature**
> AuthInfoResponse uploadWorkflowResultAndTriggerSignature(documentId, hostname, signatureInputs)

Check authentication

### Example 
```dart
import 'package:docsrv_api/api.dart';

final api_instance = DefaultApi();
final documentId = documentId_example; // String | documentId
final hostname = hostname_example; // String | hostname
final signatureInputs = SignatureInputs(); // SignatureInputs | 

try { 
    final result = api_instance.uploadWorkflowResultAndTriggerSignature(documentId, hostname, signatureInputs);
    print(result);
} catch (e) {
    print('Exception when calling DefaultApi->uploadWorkflowResultAndTriggerSignature: $e\n');
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **documentId** | **String**| documentId | 
 **hostname** | **String**| hostname | 
 **signatureInputs** | [**SignatureInputs**](SignatureInputs.md)|  | [optional] 

### Return type

[**AuthInfoResponse**](AuthInfoResponse.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **verifyInstanceHasPrivateKey**
> DiscoveryResponse verifyInstanceHasPrivateKey(publicKeyFingerprint, seed)

Check that this instance actually owns the given private key

### Example 
```dart
import 'package:docsrv_api/api.dart';

final api_instance = DefaultApi();
final publicKeyFingerprint = publicKeyFingerprint_example; // String | publicKeyFingerprint
final seed = seed_example; // String | seed

try { 
    final result = api_instance.verifyInstanceHasPrivateKey(publicKeyFingerprint, seed);
    print(result);
} catch (e) {
    print('Exception when calling DefaultApi->verifyInstanceHasPrivateKey: $e\n');
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **publicKeyFingerprint** | **String**| publicKeyFingerprint | 
 **seed** | **String**| seed | 

### Return type

[**DiscoveryResponse**](DiscoveryResponse.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **viewFile**
> DiscoveryResponse viewFile(fileId)

Perform Instance discovery

### Example 
```dart
import 'package:docsrv_api/api.dart';

final api_instance = DefaultApi();
final fileId = fileId_example; // String | fileId

try { 
    final result = api_instance.viewFile(fileId);
    print(result);
} catch (e) {
    print('Exception when calling DefaultApi->viewFile: $e\n');
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **fileId** | **String**| fileId | 

### Return type

[**DiscoveryResponse**](DiscoveryResponse.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

