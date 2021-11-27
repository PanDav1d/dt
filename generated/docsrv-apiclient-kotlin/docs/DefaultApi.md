# DefaultApi

All URIs are relative to *http://localhost*

Method | HTTP request | Description
------------- | ------------- | -------------
[**addSignatureToDoctagDocument**](DefaultApi.md#addSignatureToDoctagDocument) | **POST** /d/{documentId}/{hostname} | Add signature to document
[**addSignatureToDoctagDocumentWithHttpInfo**](DefaultApi.md#addSignatureToDoctagDocumentWithHttpInfo) | **POST** /d/{documentId}/{hostname} | Add signature to document
[**checkHealth**](DefaultApi.md#checkHealth) | **GET** /health | Perform Health Check
[**checkHealthWithHttpInfo**](DefaultApi.md#checkHealthWithHttpInfo) | **GET** /health | Perform Health Check
[**discoverInstance**](DefaultApi.md#discoverInstance) | **GET** /discovery | Perform Instance discovery
[**discoverInstanceWithHttpInfo**](DefaultApi.md#discoverInstanceWithHttpInfo) | **GET** /discovery | Perform Instance discovery
[**downloadDocument**](DefaultApi.md#downloadDocument) | **GET** /d/{documentId}/download | Download document
[**downloadDocumentWithHttpInfo**](DefaultApi.md#downloadDocumentWithHttpInfo) | **GET** /d/{documentId}/download | Download document
[**downloadFile**](DefaultApi.md#downloadFile) | **GET** /f/{fileId}/download | Perform Instance discovery
[**downloadFileWithHttpInfo**](DefaultApi.md#downloadFileWithHttpInfo) | **GET** /f/{fileId}/download | Perform Instance discovery
[**downloadSignSheet**](DefaultApi.md#downloadSignSheet) | **GET** /d/{documentId}/viewSignSheet | Download sign sheet
[**downloadSignSheetWithHttpInfo**](DefaultApi.md#downloadSignSheetWithHttpInfo) | **GET** /d/{documentId}/viewSignSheet | Download sign sheet
[**fetchAuthInfo**](DefaultApi.md#fetchAuthInfo) | **GET** /app/auth_info | Check authentication
[**fetchAuthInfoWithHttpInfo**](DefaultApi.md#fetchAuthInfoWithHttpInfo) | **GET** /app/auth_info | Check authentication
[**fetchDoctagDocument**](DefaultApi.md#fetchDoctagDocument) | **GET** /d/{documentId} | Fetch doctag document
[**fetchDoctagDocumentWithHttpInfo**](DefaultApi.md#fetchDoctagDocumentWithHttpInfo) | **GET** /d/{documentId} | Fetch doctag document
[**fetchWorkflowToSign**](DefaultApi.md#fetchWorkflowToSign) | **GET** /app/signature/prepare/{documentId}/{hostname} | Check authentication
[**fetchWorkflowToSignWithHttpInfo**](DefaultApi.md#fetchWorkflowToSignWithHttpInfo) | **GET** /app/signature/prepare/{documentId}/{hostname} | Check authentication
[**notifyChangesOfDoctagDocument**](DefaultApi.md#notifyChangesOfDoctagDocument) | **POST** /d/notifyChanges/ | Add signature to document
[**notifyChangesOfDoctagDocumentWithHttpInfo**](DefaultApi.md#notifyChangesOfDoctagDocumentWithHttpInfo) | **POST** /d/notifyChanges/ | Add signature to document
[**setVerificationOfKeyPair**](DefaultApi.md#setVerificationOfKeyPair) | **PUT** /k/{publicKeyFingerprint}/verification | Set the verification of the private public key
[**setVerificationOfKeyPairWithHttpInfo**](DefaultApi.md#setVerificationOfKeyPairWithHttpInfo) | **PUT** /k/{publicKeyFingerprint}/verification | Set the verification of the private public key
[**uploadWorkflowResultAndTriggerSignature**](DefaultApi.md#uploadWorkflowResultAndTriggerSignature) | **POST** /app/signature/push/{documentId}/{hostname} | Check authentication
[**uploadWorkflowResultAndTriggerSignatureWithHttpInfo**](DefaultApi.md#uploadWorkflowResultAndTriggerSignatureWithHttpInfo) | **POST** /app/signature/push/{documentId}/{hostname} | Check authentication
[**verifyInstanceHasPrivateKey**](DefaultApi.md#verifyInstanceHasPrivateKey) | **GET** /k/{publicKeyFingerprint}/verify/{seed} | Check that this instance actually owns the given private key
[**verifyInstanceHasPrivateKeyWithHttpInfo**](DefaultApi.md#verifyInstanceHasPrivateKeyWithHttpInfo) | **GET** /k/{publicKeyFingerprint}/verify/{seed} | Check that this instance actually owns the given private key
[**viewFile**](DefaultApi.md#viewFile) | **GET** /f/{fileId}/view | Perform Instance discovery
[**viewFileWithHttpInfo**](DefaultApi.md#viewFileWithHttpInfo) | **GET** /f/{fileId}/view | Perform Instance discovery



## addSignatureToDoctagDocument

> Document addSignatureToDoctagDocument(documentId, hostname, embeddedSignature)

Add signature to document

### Example

```java
// Import classes:
import de.doctag.docsrv_api.invoker.ApiClient;
import de.doctag.docsrv_api.invoker.ApiException;
import de.doctag.docsrv_api.invoker.Configuration;
import de.doctag.docsrv_api.invoker.models.*;
import de.doctag.docsrv_api.DefaultApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost");

        DefaultApi apiInstance = new DefaultApi(defaultClient);
        String documentId = "documentId_example"; // String | documentId
        String hostname = "hostname_example"; // String | hostname
        EmbeddedSignature embeddedSignature = new EmbeddedSignature(); // EmbeddedSignature | 
        try {
            Document result = apiInstance.addSignatureToDoctagDocument(documentId, hostname, embeddedSignature);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling DefaultApi#addSignatureToDoctagDocument");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
        }
    }
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

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Document |  -  |

## addSignatureToDoctagDocumentWithHttpInfo

> ApiResponse<Document> addSignatureToDoctagDocument addSignatureToDoctagDocumentWithHttpInfo(documentId, hostname, embeddedSignature)

Add signature to document

### Example

```java
// Import classes:
import de.doctag.docsrv_api.invoker.ApiClient;
import de.doctag.docsrv_api.invoker.ApiException;
import de.doctag.docsrv_api.invoker.ApiResponse;
import de.doctag.docsrv_api.invoker.Configuration;
import de.doctag.docsrv_api.invoker.models.*;
import de.doctag.docsrv_api.DefaultApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost");

        DefaultApi apiInstance = new DefaultApi(defaultClient);
        String documentId = "documentId_example"; // String | documentId
        String hostname = "hostname_example"; // String | hostname
        EmbeddedSignature embeddedSignature = new EmbeddedSignature(); // EmbeddedSignature | 
        try {
            ApiResponse<Document> response = apiInstance.addSignatureToDoctagDocumentWithHttpInfo(documentId, hostname, embeddedSignature);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling DefaultApi#addSignatureToDoctagDocument");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Response headers: " + e.getResponseHeaders());
            System.err.println("Reason: " + e.getResponseBody());
            e.printStackTrace();
        }
    }
}
```

### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **documentId** | **String**| documentId |
 **hostname** | **String**| hostname |
 **embeddedSignature** | [**EmbeddedSignature**](EmbeddedSignature.md)|  | [optional]

### Return type

ApiResponse<[**Document**](Document.md)>


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Document |  -  |


## checkHealth

> HealthCheckResponse checkHealth()

Perform Health Check

### Example

```java
// Import classes:
import de.doctag.docsrv_api.invoker.ApiClient;
import de.doctag.docsrv_api.invoker.ApiException;
import de.doctag.docsrv_api.invoker.Configuration;
import de.doctag.docsrv_api.invoker.models.*;
import de.doctag.docsrv_api.DefaultApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost");

        DefaultApi apiInstance = new DefaultApi(defaultClient);
        try {
            HealthCheckResponse result = apiInstance.checkHealth();
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling DefaultApi#checkHealth");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
        }
    }
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

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | HealthCheckResponse |  -  |

## checkHealthWithHttpInfo

> ApiResponse<HealthCheckResponse> checkHealth checkHealthWithHttpInfo()

Perform Health Check

### Example

```java
// Import classes:
import de.doctag.docsrv_api.invoker.ApiClient;
import de.doctag.docsrv_api.invoker.ApiException;
import de.doctag.docsrv_api.invoker.ApiResponse;
import de.doctag.docsrv_api.invoker.Configuration;
import de.doctag.docsrv_api.invoker.models.*;
import de.doctag.docsrv_api.DefaultApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost");

        DefaultApi apiInstance = new DefaultApi(defaultClient);
        try {
            ApiResponse<HealthCheckResponse> response = apiInstance.checkHealthWithHttpInfo();
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling DefaultApi#checkHealth");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Response headers: " + e.getResponseHeaders());
            System.err.println("Reason: " + e.getResponseBody());
            e.printStackTrace();
        }
    }
}
```

### Parameters

This endpoint does not need any parameter.

### Return type

ApiResponse<[**HealthCheckResponse**](HealthCheckResponse.md)>


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | HealthCheckResponse |  -  |


## discoverInstance

> DiscoveryResponse discoverInstance()

Perform Instance discovery

### Example

```java
// Import classes:
import de.doctag.docsrv_api.invoker.ApiClient;
import de.doctag.docsrv_api.invoker.ApiException;
import de.doctag.docsrv_api.invoker.Configuration;
import de.doctag.docsrv_api.invoker.models.*;
import de.doctag.docsrv_api.DefaultApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost");

        DefaultApi apiInstance = new DefaultApi(defaultClient);
        try {
            DiscoveryResponse result = apiInstance.discoverInstance();
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling DefaultApi#discoverInstance");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
        }
    }
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

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | DiscoveryResponse |  -  |

## discoverInstanceWithHttpInfo

> ApiResponse<DiscoveryResponse> discoverInstance discoverInstanceWithHttpInfo()

Perform Instance discovery

### Example

```java
// Import classes:
import de.doctag.docsrv_api.invoker.ApiClient;
import de.doctag.docsrv_api.invoker.ApiException;
import de.doctag.docsrv_api.invoker.ApiResponse;
import de.doctag.docsrv_api.invoker.Configuration;
import de.doctag.docsrv_api.invoker.models.*;
import de.doctag.docsrv_api.DefaultApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost");

        DefaultApi apiInstance = new DefaultApi(defaultClient);
        try {
            ApiResponse<DiscoveryResponse> response = apiInstance.discoverInstanceWithHttpInfo();
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling DefaultApi#discoverInstance");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Response headers: " + e.getResponseHeaders());
            System.err.println("Reason: " + e.getResponseBody());
            e.printStackTrace();
        }
    }
}
```

### Parameters

This endpoint does not need any parameter.

### Return type

ApiResponse<[**DiscoveryResponse**](DiscoveryResponse.md)>


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | DiscoveryResponse |  -  |


## downloadDocument

> DiscoveryResponse downloadDocument(documentId)

Download document

### Example

```java
// Import classes:
import de.doctag.docsrv_api.invoker.ApiClient;
import de.doctag.docsrv_api.invoker.ApiException;
import de.doctag.docsrv_api.invoker.Configuration;
import de.doctag.docsrv_api.invoker.models.*;
import de.doctag.docsrv_api.DefaultApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost");

        DefaultApi apiInstance = new DefaultApi(defaultClient);
        String documentId = "documentId_example"; // String | documentId
        try {
            DiscoveryResponse result = apiInstance.downloadDocument(documentId);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling DefaultApi#downloadDocument");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
        }
    }
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

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | DiscoveryResponse |  -  |

## downloadDocumentWithHttpInfo

> ApiResponse<DiscoveryResponse> downloadDocument downloadDocumentWithHttpInfo(documentId)

Download document

### Example

```java
// Import classes:
import de.doctag.docsrv_api.invoker.ApiClient;
import de.doctag.docsrv_api.invoker.ApiException;
import de.doctag.docsrv_api.invoker.ApiResponse;
import de.doctag.docsrv_api.invoker.Configuration;
import de.doctag.docsrv_api.invoker.models.*;
import de.doctag.docsrv_api.DefaultApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost");

        DefaultApi apiInstance = new DefaultApi(defaultClient);
        String documentId = "documentId_example"; // String | documentId
        try {
            ApiResponse<DiscoveryResponse> response = apiInstance.downloadDocumentWithHttpInfo(documentId);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling DefaultApi#downloadDocument");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Response headers: " + e.getResponseHeaders());
            System.err.println("Reason: " + e.getResponseBody());
            e.printStackTrace();
        }
    }
}
```

### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **documentId** | **String**| documentId |

### Return type

ApiResponse<[**DiscoveryResponse**](DiscoveryResponse.md)>


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | DiscoveryResponse |  -  |


## downloadFile

> DiscoveryResponse downloadFile(fileId)

Perform Instance discovery

### Example

```java
// Import classes:
import de.doctag.docsrv_api.invoker.ApiClient;
import de.doctag.docsrv_api.invoker.ApiException;
import de.doctag.docsrv_api.invoker.Configuration;
import de.doctag.docsrv_api.invoker.models.*;
import de.doctag.docsrv_api.DefaultApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost");

        DefaultApi apiInstance = new DefaultApi(defaultClient);
        String fileId = "fileId_example"; // String | fileId
        try {
            DiscoveryResponse result = apiInstance.downloadFile(fileId);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling DefaultApi#downloadFile");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
        }
    }
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

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | DiscoveryResponse |  -  |

## downloadFileWithHttpInfo

> ApiResponse<DiscoveryResponse> downloadFile downloadFileWithHttpInfo(fileId)

Perform Instance discovery

### Example

```java
// Import classes:
import de.doctag.docsrv_api.invoker.ApiClient;
import de.doctag.docsrv_api.invoker.ApiException;
import de.doctag.docsrv_api.invoker.ApiResponse;
import de.doctag.docsrv_api.invoker.Configuration;
import de.doctag.docsrv_api.invoker.models.*;
import de.doctag.docsrv_api.DefaultApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost");

        DefaultApi apiInstance = new DefaultApi(defaultClient);
        String fileId = "fileId_example"; // String | fileId
        try {
            ApiResponse<DiscoveryResponse> response = apiInstance.downloadFileWithHttpInfo(fileId);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling DefaultApi#downloadFile");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Response headers: " + e.getResponseHeaders());
            System.err.println("Reason: " + e.getResponseBody());
            e.printStackTrace();
        }
    }
}
```

### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **fileId** | **String**| fileId |

### Return type

ApiResponse<[**DiscoveryResponse**](DiscoveryResponse.md)>


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | DiscoveryResponse |  -  |


## downloadSignSheet

> DiscoveryResponse downloadSignSheet(documentId)

Download sign sheet

### Example

```java
// Import classes:
import de.doctag.docsrv_api.invoker.ApiClient;
import de.doctag.docsrv_api.invoker.ApiException;
import de.doctag.docsrv_api.invoker.Configuration;
import de.doctag.docsrv_api.invoker.models.*;
import de.doctag.docsrv_api.DefaultApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost");

        DefaultApi apiInstance = new DefaultApi(defaultClient);
        String documentId = "documentId_example"; // String | documentId
        try {
            DiscoveryResponse result = apiInstance.downloadSignSheet(documentId);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling DefaultApi#downloadSignSheet");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
        }
    }
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

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | DiscoveryResponse |  -  |

## downloadSignSheetWithHttpInfo

> ApiResponse<DiscoveryResponse> downloadSignSheet downloadSignSheetWithHttpInfo(documentId)

Download sign sheet

### Example

```java
// Import classes:
import de.doctag.docsrv_api.invoker.ApiClient;
import de.doctag.docsrv_api.invoker.ApiException;
import de.doctag.docsrv_api.invoker.ApiResponse;
import de.doctag.docsrv_api.invoker.Configuration;
import de.doctag.docsrv_api.invoker.models.*;
import de.doctag.docsrv_api.DefaultApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost");

        DefaultApi apiInstance = new DefaultApi(defaultClient);
        String documentId = "documentId_example"; // String | documentId
        try {
            ApiResponse<DiscoveryResponse> response = apiInstance.downloadSignSheetWithHttpInfo(documentId);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling DefaultApi#downloadSignSheet");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Response headers: " + e.getResponseHeaders());
            System.err.println("Reason: " + e.getResponseBody());
            e.printStackTrace();
        }
    }
}
```

### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **documentId** | **String**| documentId |

### Return type

ApiResponse<[**DiscoveryResponse**](DiscoveryResponse.md)>


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | DiscoveryResponse |  -  |


## fetchAuthInfo

> AuthInfoResponse fetchAuthInfo()

Check authentication

### Example

```java
// Import classes:
import de.doctag.docsrv_api.invoker.ApiClient;
import de.doctag.docsrv_api.invoker.ApiException;
import de.doctag.docsrv_api.invoker.Configuration;
import de.doctag.docsrv_api.invoker.models.*;
import de.doctag.docsrv_api.DefaultApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost");

        DefaultApi apiInstance = new DefaultApi(defaultClient);
        try {
            AuthInfoResponse result = apiInstance.fetchAuthInfo();
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling DefaultApi#fetchAuthInfo");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
        }
    }
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

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | AuthInfoResponse |  -  |

## fetchAuthInfoWithHttpInfo

> ApiResponse<AuthInfoResponse> fetchAuthInfo fetchAuthInfoWithHttpInfo()

Check authentication

### Example

```java
// Import classes:
import de.doctag.docsrv_api.invoker.ApiClient;
import de.doctag.docsrv_api.invoker.ApiException;
import de.doctag.docsrv_api.invoker.ApiResponse;
import de.doctag.docsrv_api.invoker.Configuration;
import de.doctag.docsrv_api.invoker.models.*;
import de.doctag.docsrv_api.DefaultApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost");

        DefaultApi apiInstance = new DefaultApi(defaultClient);
        try {
            ApiResponse<AuthInfoResponse> response = apiInstance.fetchAuthInfoWithHttpInfo();
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling DefaultApi#fetchAuthInfo");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Response headers: " + e.getResponseHeaders());
            System.err.println("Reason: " + e.getResponseBody());
            e.printStackTrace();
        }
    }
}
```

### Parameters

This endpoint does not need any parameter.

### Return type

ApiResponse<[**AuthInfoResponse**](AuthInfoResponse.md)>


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | AuthInfoResponse |  -  |


## fetchDoctagDocument

> EmbeddedDocument fetchDoctagDocument(documentId)

Fetch doctag document

### Example

```java
// Import classes:
import de.doctag.docsrv_api.invoker.ApiClient;
import de.doctag.docsrv_api.invoker.ApiException;
import de.doctag.docsrv_api.invoker.Configuration;
import de.doctag.docsrv_api.invoker.models.*;
import de.doctag.docsrv_api.DefaultApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost");

        DefaultApi apiInstance = new DefaultApi(defaultClient);
        String documentId = "documentId_example"; // String | documentId
        try {
            EmbeddedDocument result = apiInstance.fetchDoctagDocument(documentId);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling DefaultApi#fetchDoctagDocument");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
        }
    }
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

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | EmbeddedDocument |  -  |

## fetchDoctagDocumentWithHttpInfo

> ApiResponse<EmbeddedDocument> fetchDoctagDocument fetchDoctagDocumentWithHttpInfo(documentId)

Fetch doctag document

### Example

```java
// Import classes:
import de.doctag.docsrv_api.invoker.ApiClient;
import de.doctag.docsrv_api.invoker.ApiException;
import de.doctag.docsrv_api.invoker.ApiResponse;
import de.doctag.docsrv_api.invoker.Configuration;
import de.doctag.docsrv_api.invoker.models.*;
import de.doctag.docsrv_api.DefaultApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost");

        DefaultApi apiInstance = new DefaultApi(defaultClient);
        String documentId = "documentId_example"; // String | documentId
        try {
            ApiResponse<EmbeddedDocument> response = apiInstance.fetchDoctagDocumentWithHttpInfo(documentId);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling DefaultApi#fetchDoctagDocument");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Response headers: " + e.getResponseHeaders());
            System.err.println("Reason: " + e.getResponseBody());
            e.printStackTrace();
        }
    }
}
```

### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **documentId** | **String**| documentId |

### Return type

ApiResponse<[**EmbeddedDocument**](EmbeddedDocument.md)>


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | EmbeddedDocument |  -  |


## fetchWorkflowToSign

> PreparedSignature fetchWorkflowToSign(documentId, hostname)

Check authentication

### Example

```java
// Import classes:
import de.doctag.docsrv_api.invoker.ApiClient;
import de.doctag.docsrv_api.invoker.ApiException;
import de.doctag.docsrv_api.invoker.Configuration;
import de.doctag.docsrv_api.invoker.models.*;
import de.doctag.docsrv_api.DefaultApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost");

        DefaultApi apiInstance = new DefaultApi(defaultClient);
        String documentId = "documentId_example"; // String | documentId
        String hostname = "hostname_example"; // String | hostname
        try {
            PreparedSignature result = apiInstance.fetchWorkflowToSign(documentId, hostname);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling DefaultApi#fetchWorkflowToSign");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
        }
    }
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

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | PreparedSignature |  -  |

## fetchWorkflowToSignWithHttpInfo

> ApiResponse<PreparedSignature> fetchWorkflowToSign fetchWorkflowToSignWithHttpInfo(documentId, hostname)

Check authentication

### Example

```java
// Import classes:
import de.doctag.docsrv_api.invoker.ApiClient;
import de.doctag.docsrv_api.invoker.ApiException;
import de.doctag.docsrv_api.invoker.ApiResponse;
import de.doctag.docsrv_api.invoker.Configuration;
import de.doctag.docsrv_api.invoker.models.*;
import de.doctag.docsrv_api.DefaultApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost");

        DefaultApi apiInstance = new DefaultApi(defaultClient);
        String documentId = "documentId_example"; // String | documentId
        String hostname = "hostname_example"; // String | hostname
        try {
            ApiResponse<PreparedSignature> response = apiInstance.fetchWorkflowToSignWithHttpInfo(documentId, hostname);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling DefaultApi#fetchWorkflowToSign");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Response headers: " + e.getResponseHeaders());
            System.err.println("Reason: " + e.getResponseBody());
            e.printStackTrace();
        }
    }
}
```

### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **documentId** | **String**| documentId |
 **hostname** | **String**| hostname |

### Return type

ApiResponse<[**PreparedSignature**](PreparedSignature.md)>


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | PreparedSignature |  -  |


## notifyChangesOfDoctagDocument

> Object notifyChangesOfDoctagDocument(notifyRequest)

Add signature to document

### Example

```java
// Import classes:
import de.doctag.docsrv_api.invoker.ApiClient;
import de.doctag.docsrv_api.invoker.ApiException;
import de.doctag.docsrv_api.invoker.Configuration;
import de.doctag.docsrv_api.invoker.models.*;
import de.doctag.docsrv_api.DefaultApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost");

        DefaultApi apiInstance = new DefaultApi(defaultClient);
        NotifyRequest notifyRequest = new NotifyRequest(); // NotifyRequest | 
        try {
            Object result = apiInstance.notifyChangesOfDoctagDocument(notifyRequest);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling DefaultApi#notifyChangesOfDoctagDocument");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
        }
    }
}
```

### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **notifyRequest** | [**NotifyRequest**](NotifyRequest.md)|  | [optional]

### Return type

**Object**


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | NotifyResult |  -  |

## notifyChangesOfDoctagDocumentWithHttpInfo

> ApiResponse<Object> notifyChangesOfDoctagDocument notifyChangesOfDoctagDocumentWithHttpInfo(notifyRequest)

Add signature to document

### Example

```java
// Import classes:
import de.doctag.docsrv_api.invoker.ApiClient;
import de.doctag.docsrv_api.invoker.ApiException;
import de.doctag.docsrv_api.invoker.ApiResponse;
import de.doctag.docsrv_api.invoker.Configuration;
import de.doctag.docsrv_api.invoker.models.*;
import de.doctag.docsrv_api.DefaultApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost");

        DefaultApi apiInstance = new DefaultApi(defaultClient);
        NotifyRequest notifyRequest = new NotifyRequest(); // NotifyRequest | 
        try {
            ApiResponse<Object> response = apiInstance.notifyChangesOfDoctagDocumentWithHttpInfo(notifyRequest);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling DefaultApi#notifyChangesOfDoctagDocument");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Response headers: " + e.getResponseHeaders());
            System.err.println("Reason: " + e.getResponseBody());
            e.printStackTrace();
        }
    }
}
```

### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **notifyRequest** | [**NotifyRequest**](NotifyRequest.md)|  | [optional]

### Return type

ApiResponse<**Object**>


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | NotifyResult |  -  |


## setVerificationOfKeyPair

> DiscoveryResponse setVerificationOfKeyPair(publicKeyFingerprint, publicKeyVerification)

Set the verification of the private public key

### Example

```java
// Import classes:
import de.doctag.docsrv_api.invoker.ApiClient;
import de.doctag.docsrv_api.invoker.ApiException;
import de.doctag.docsrv_api.invoker.Configuration;
import de.doctag.docsrv_api.invoker.models.*;
import de.doctag.docsrv_api.DefaultApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost");

        DefaultApi apiInstance = new DefaultApi(defaultClient);
        String publicKeyFingerprint = "publicKeyFingerprint_example"; // String | publicKeyFingerprint
        PublicKeyVerification publicKeyVerification = new PublicKeyVerification(); // PublicKeyVerification | 
        try {
            DiscoveryResponse result = apiInstance.setVerificationOfKeyPair(publicKeyFingerprint, publicKeyVerification);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling DefaultApi#setVerificationOfKeyPair");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
        }
    }
}
```

### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **publicKeyFingerprint** | **String**| publicKeyFingerprint |
 **publicKeyVerification** | [**PublicKeyVerification**](PublicKeyVerification.md)|  | [optional]

### Return type

[**DiscoveryResponse**](DiscoveryResponse.md)


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | DiscoveryResponse |  -  |

## setVerificationOfKeyPairWithHttpInfo

> ApiResponse<DiscoveryResponse> setVerificationOfKeyPair setVerificationOfKeyPairWithHttpInfo(publicKeyFingerprint, publicKeyVerification)

Set the verification of the private public key

### Example

```java
// Import classes:
import de.doctag.docsrv_api.invoker.ApiClient;
import de.doctag.docsrv_api.invoker.ApiException;
import de.doctag.docsrv_api.invoker.ApiResponse;
import de.doctag.docsrv_api.invoker.Configuration;
import de.doctag.docsrv_api.invoker.models.*;
import de.doctag.docsrv_api.DefaultApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost");

        DefaultApi apiInstance = new DefaultApi(defaultClient);
        String publicKeyFingerprint = "publicKeyFingerprint_example"; // String | publicKeyFingerprint
        PublicKeyVerification publicKeyVerification = new PublicKeyVerification(); // PublicKeyVerification | 
        try {
            ApiResponse<DiscoveryResponse> response = apiInstance.setVerificationOfKeyPairWithHttpInfo(publicKeyFingerprint, publicKeyVerification);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling DefaultApi#setVerificationOfKeyPair");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Response headers: " + e.getResponseHeaders());
            System.err.println("Reason: " + e.getResponseBody());
            e.printStackTrace();
        }
    }
}
```

### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **publicKeyFingerprint** | **String**| publicKeyFingerprint |
 **publicKeyVerification** | [**PublicKeyVerification**](PublicKeyVerification.md)|  | [optional]

### Return type

ApiResponse<[**DiscoveryResponse**](DiscoveryResponse.md)>


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | DiscoveryResponse |  -  |


## uploadWorkflowResultAndTriggerSignature

> AuthInfoResponse uploadWorkflowResultAndTriggerSignature(documentId, hostname, signatureInputs)

Check authentication

### Example

```java
// Import classes:
import de.doctag.docsrv_api.invoker.ApiClient;
import de.doctag.docsrv_api.invoker.ApiException;
import de.doctag.docsrv_api.invoker.Configuration;
import de.doctag.docsrv_api.invoker.models.*;
import de.doctag.docsrv_api.DefaultApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost");

        DefaultApi apiInstance = new DefaultApi(defaultClient);
        String documentId = "documentId_example"; // String | documentId
        String hostname = "hostname_example"; // String | hostname
        SignatureInputs signatureInputs = new SignatureInputs(); // SignatureInputs | 
        try {
            AuthInfoResponse result = apiInstance.uploadWorkflowResultAndTriggerSignature(documentId, hostname, signatureInputs);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling DefaultApi#uploadWorkflowResultAndTriggerSignature");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
        }
    }
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

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | AuthInfoResponse |  -  |

## uploadWorkflowResultAndTriggerSignatureWithHttpInfo

> ApiResponse<AuthInfoResponse> uploadWorkflowResultAndTriggerSignature uploadWorkflowResultAndTriggerSignatureWithHttpInfo(documentId, hostname, signatureInputs)

Check authentication

### Example

```java
// Import classes:
import de.doctag.docsrv_api.invoker.ApiClient;
import de.doctag.docsrv_api.invoker.ApiException;
import de.doctag.docsrv_api.invoker.ApiResponse;
import de.doctag.docsrv_api.invoker.Configuration;
import de.doctag.docsrv_api.invoker.models.*;
import de.doctag.docsrv_api.DefaultApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost");

        DefaultApi apiInstance = new DefaultApi(defaultClient);
        String documentId = "documentId_example"; // String | documentId
        String hostname = "hostname_example"; // String | hostname
        SignatureInputs signatureInputs = new SignatureInputs(); // SignatureInputs | 
        try {
            ApiResponse<AuthInfoResponse> response = apiInstance.uploadWorkflowResultAndTriggerSignatureWithHttpInfo(documentId, hostname, signatureInputs);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling DefaultApi#uploadWorkflowResultAndTriggerSignature");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Response headers: " + e.getResponseHeaders());
            System.err.println("Reason: " + e.getResponseBody());
            e.printStackTrace();
        }
    }
}
```

### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **documentId** | **String**| documentId |
 **hostname** | **String**| hostname |
 **signatureInputs** | [**SignatureInputs**](SignatureInputs.md)|  | [optional]

### Return type

ApiResponse<[**AuthInfoResponse**](AuthInfoResponse.md)>


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | AuthInfoResponse |  -  |


## verifyInstanceHasPrivateKey

> DiscoveryResponse verifyInstanceHasPrivateKey(publicKeyFingerprint, seed)

Check that this instance actually owns the given private key

### Example

```java
// Import classes:
import de.doctag.docsrv_api.invoker.ApiClient;
import de.doctag.docsrv_api.invoker.ApiException;
import de.doctag.docsrv_api.invoker.Configuration;
import de.doctag.docsrv_api.invoker.models.*;
import de.doctag.docsrv_api.DefaultApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost");

        DefaultApi apiInstance = new DefaultApi(defaultClient);
        String publicKeyFingerprint = "publicKeyFingerprint_example"; // String | publicKeyFingerprint
        String seed = "seed_example"; // String | seed
        try {
            DiscoveryResponse result = apiInstance.verifyInstanceHasPrivateKey(publicKeyFingerprint, seed);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling DefaultApi#verifyInstanceHasPrivateKey");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
        }
    }
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

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | DiscoveryResponse |  -  |

## verifyInstanceHasPrivateKeyWithHttpInfo

> ApiResponse<DiscoveryResponse> verifyInstanceHasPrivateKey verifyInstanceHasPrivateKeyWithHttpInfo(publicKeyFingerprint, seed)

Check that this instance actually owns the given private key

### Example

```java
// Import classes:
import de.doctag.docsrv_api.invoker.ApiClient;
import de.doctag.docsrv_api.invoker.ApiException;
import de.doctag.docsrv_api.invoker.ApiResponse;
import de.doctag.docsrv_api.invoker.Configuration;
import de.doctag.docsrv_api.invoker.models.*;
import de.doctag.docsrv_api.DefaultApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost");

        DefaultApi apiInstance = new DefaultApi(defaultClient);
        String publicKeyFingerprint = "publicKeyFingerprint_example"; // String | publicKeyFingerprint
        String seed = "seed_example"; // String | seed
        try {
            ApiResponse<DiscoveryResponse> response = apiInstance.verifyInstanceHasPrivateKeyWithHttpInfo(publicKeyFingerprint, seed);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling DefaultApi#verifyInstanceHasPrivateKey");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Response headers: " + e.getResponseHeaders());
            System.err.println("Reason: " + e.getResponseBody());
            e.printStackTrace();
        }
    }
}
```

### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **publicKeyFingerprint** | **String**| publicKeyFingerprint |
 **seed** | **String**| seed |

### Return type

ApiResponse<[**DiscoveryResponse**](DiscoveryResponse.md)>


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | DiscoveryResponse |  -  |


## viewFile

> DiscoveryResponse viewFile(fileId)

Perform Instance discovery

### Example

```java
// Import classes:
import de.doctag.docsrv_api.invoker.ApiClient;
import de.doctag.docsrv_api.invoker.ApiException;
import de.doctag.docsrv_api.invoker.Configuration;
import de.doctag.docsrv_api.invoker.models.*;
import de.doctag.docsrv_api.DefaultApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost");

        DefaultApi apiInstance = new DefaultApi(defaultClient);
        String fileId = "fileId_example"; // String | fileId
        try {
            DiscoveryResponse result = apiInstance.viewFile(fileId);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling DefaultApi#viewFile");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
        }
    }
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

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | DiscoveryResponse |  -  |

## viewFileWithHttpInfo

> ApiResponse<DiscoveryResponse> viewFile viewFileWithHttpInfo(fileId)

Perform Instance discovery

### Example

```java
// Import classes:
import de.doctag.docsrv_api.invoker.ApiClient;
import de.doctag.docsrv_api.invoker.ApiException;
import de.doctag.docsrv_api.invoker.ApiResponse;
import de.doctag.docsrv_api.invoker.Configuration;
import de.doctag.docsrv_api.invoker.models.*;
import de.doctag.docsrv_api.DefaultApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost");

        DefaultApi apiInstance = new DefaultApi(defaultClient);
        String fileId = "fileId_example"; // String | fileId
        try {
            ApiResponse<DiscoveryResponse> response = apiInstance.viewFileWithHttpInfo(fileId);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling DefaultApi#viewFile");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Response headers: " + e.getResponseHeaders());
            System.err.println("Reason: " + e.getResponseBody());
            e.printStackTrace();
        }
    }
}
```

### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **fileId** | **String**| fileId |

### Return type

ApiResponse<[**DiscoveryResponse**](DiscoveryResponse.md)>


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | DiscoveryResponse |  -  |

