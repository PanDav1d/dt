# docsrv_api.model.Document

## Load the model package
```dart
import 'package:docsrv_api/api.dart';
```

## Properties
Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**id** | **String** |  | [optional] 
**attachmentHash** | **String** |  | [optional] 
**attachmentId** | **String** |  | [optional] 
**created** | [**DateTime**](DateTime.md) |  | [optional] 
**fullText** | **String** |  | [optional] 
**isMirrored** | **bool** |  | [optional] 
**mirrors** | **List<String>** |  | [optional] [default to const []]
**originalFileName** | **String** |  | [optional] 
**signatures** | [**List<Signature>**](Signature.md) |  | [optional] [default to const []]
**url** | **String** |  | [optional] 
**workflow** | [**Workflow**](Workflow.md) |  | [optional] 

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


