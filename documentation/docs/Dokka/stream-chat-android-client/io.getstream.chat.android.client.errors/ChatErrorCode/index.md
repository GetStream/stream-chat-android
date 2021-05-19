---
title: index
sidebar_position: 1
---
/[stream-chat-android-client](../../index.md)/[io.getstream.chat.android.client.errors](../index.md)/[ChatErrorCode](index.md)  
  
  
  
# ChatErrorCode  
enum [ChatErrorCode](index.md) : [Enum](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-enum/index.html)&lt;[ChatErrorCode](index.md)&gt;   
  
## Entries  
  
| | |
|---|---|
| <a name="io.getstream.chat.android.client.errors/ChatErrorCode.API_KEY_NOT_FOUND///PointingToDeclaration/"></a>[API_KEY_NOT_FOUND](API_KEY_NOT_FOUND/index.md)| <a name="io.getstream.chat.android.client.errors/ChatErrorCode.API_KEY_NOT_FOUND///PointingToDeclaration/"></a>[API_KEY_NOT_FOUND](API_KEY_NOT_FOUND/index.md)(2, "Api key is not found, verify it if it's correct or was created.")|
| <a name="io.getstream.chat.android.client.errors/ChatErrorCode.TOKEN_SIGNATURE_INCORRECT///PointingToDeclaration/"></a>[TOKEN_SIGNATURE_INCORRECT](TOKEN_SIGNATURE_INCORRECT/index.md)| <a name="io.getstream.chat.android.client.errors/ChatErrorCode.TOKEN_SIGNATURE_INCORRECT///PointingToDeclaration/"></a>[TOKEN_SIGNATURE_INCORRECT](TOKEN_SIGNATURE_INCORRECT/index.md)(43, "Unauthenticated, token signature invalid")|
| <a name="io.getstream.chat.android.client.errors/ChatErrorCode.TOKEN_DATE_INCORRECT///PointingToDeclaration/"></a>[TOKEN_DATE_INCORRECT](TOKEN_DATE_INCORRECT/index.md)| <a name="io.getstream.chat.android.client.errors/ChatErrorCode.TOKEN_DATE_INCORRECT///PointingToDeclaration/"></a>[TOKEN_DATE_INCORRECT](TOKEN_DATE_INCORRECT/index.md)(42, "Unauthenticated, token date incorrect")|
| <a name="io.getstream.chat.android.client.errors/ChatErrorCode.TOKEN_NOT_VALID///PointingToDeclaration/"></a>[TOKEN_NOT_VALID](TOKEN_NOT_VALID/index.md)| <a name="io.getstream.chat.android.client.errors/ChatErrorCode.TOKEN_NOT_VALID///PointingToDeclaration/"></a>[TOKEN_NOT_VALID](TOKEN_NOT_VALID/index.md)(41, "Unauthenticated, token not valid yet")|
| <a name="io.getstream.chat.android.client.errors/ChatErrorCode.TOKEN_EXPIRED///PointingToDeclaration/"></a>[TOKEN_EXPIRED](TOKEN_EXPIRED/index.md)| <a name="io.getstream.chat.android.client.errors/ChatErrorCode.TOKEN_EXPIRED///PointingToDeclaration/"></a>[TOKEN_EXPIRED](TOKEN_EXPIRED/index.md)(40, "Token expired, new one must be requested.")|
| <a name="io.getstream.chat.android.client.errors/ChatErrorCode.AUTHENTICATION_ERROR///PointingToDeclaration/"></a>[AUTHENTICATION_ERROR](AUTHENTICATION_ERROR/index.md)| <a name="io.getstream.chat.android.client.errors/ChatErrorCode.AUTHENTICATION_ERROR///PointingToDeclaration/"></a>[AUTHENTICATION_ERROR](AUTHENTICATION_ERROR/index.md)(5, "Unauthenticated, problem with authentication")|
| <a name="io.getstream.chat.android.client.errors/ChatErrorCode.NO_ERROR_BODY///PointingToDeclaration/"></a>[NO_ERROR_BODY](NO_ERROR_BODY/index.md)| <a name="io.getstream.chat.android.client.errors/ChatErrorCode.NO_ERROR_BODY///PointingToDeclaration/"></a>[NO_ERROR_BODY](NO_ERROR_BODY/index.md)(1009, "No error body. See http status code")|
| <a name="io.getstream.chat.android.client.errors/ChatErrorCode.UNABLE_TO_PARSE_SOCKET_EVENT///PointingToDeclaration/"></a>[UNABLE_TO_PARSE_SOCKET_EVENT](UNABLE_TO_PARSE_SOCKET_EVENT/index.md)| <a name="io.getstream.chat.android.client.errors/ChatErrorCode.UNABLE_TO_PARSE_SOCKET_EVENT///PointingToDeclaration/"></a>[UNABLE_TO_PARSE_SOCKET_EVENT](UNABLE_TO_PARSE_SOCKET_EVENT/index.md)(1008, "Socket event payload either invalid or null")|
| <a name="io.getstream.chat.android.client.errors/ChatErrorCode.UNDEFINED_TOKEN///PointingToDeclaration/"></a>[UNDEFINED_TOKEN](UNDEFINED_TOKEN/index.md)| <a name="io.getstream.chat.android.client.errors/ChatErrorCode.UNDEFINED_TOKEN///PointingToDeclaration/"></a>[UNDEFINED_TOKEN](UNDEFINED_TOKEN/index.md)(1007, "No defined token. Check if client.setUser was called and finished")|
| <a name="io.getstream.chat.android.client.errors/ChatErrorCode.INVALID_TOKEN///PointingToDeclaration/"></a>[INVALID_TOKEN](INVALID_TOKEN/index.md)| <a name="io.getstream.chat.android.client.errors/ChatErrorCode.INVALID_TOKEN///PointingToDeclaration/"></a>[INVALID_TOKEN](INVALID_TOKEN/index.md)(1006, "Invalid token")|
| <a name="io.getstream.chat.android.client.errors/ChatErrorCode.CANT_PARSE_EVENT///PointingToDeclaration/"></a>[CANT_PARSE_EVENT](CANT_PARSE_EVENT/index.md)| <a name="io.getstream.chat.android.client.errors/ChatErrorCode.CANT_PARSE_EVENT///PointingToDeclaration/"></a>[CANT_PARSE_EVENT](CANT_PARSE_EVENT/index.md)(1005, "Unable to parse event")|
| <a name="io.getstream.chat.android.client.errors/ChatErrorCode.CANT_PARSE_CONNECTION_EVENT///PointingToDeclaration/"></a>[CANT_PARSE_CONNECTION_EVENT](CANT_PARSE_CONNECTION_EVENT/index.md)| <a name="io.getstream.chat.android.client.errors/ChatErrorCode.CANT_PARSE_CONNECTION_EVENT///PointingToDeclaration/"></a>[CANT_PARSE_CONNECTION_EVENT](CANT_PARSE_CONNECTION_EVENT/index.md)(1004, "Unable to parse connection event")|
| <a name="io.getstream.chat.android.client.errors/ChatErrorCode.SOCKET_FAILURE///PointingToDeclaration/"></a>[SOCKET_FAILURE](SOCKET_FAILURE/index.md)| <a name="io.getstream.chat.android.client.errors/ChatErrorCode.SOCKET_FAILURE///PointingToDeclaration/"></a>[SOCKET_FAILURE](SOCKET_FAILURE/index.md)(1003, "See stack trace in logs. Intercept error in error handler of setUser")|
| <a name="io.getstream.chat.android.client.errors/ChatErrorCode.SOCKET_CLOSED///PointingToDeclaration/"></a>[SOCKET_CLOSED](SOCKET_CLOSED/index.md)| <a name="io.getstream.chat.android.client.errors/ChatErrorCode.SOCKET_CLOSED///PointingToDeclaration/"></a>[SOCKET_CLOSED](SOCKET_CLOSED/index.md)(1002, "Server closed connection")|
| <a name="io.getstream.chat.android.client.errors/ChatErrorCode.PARSER_ERROR///PointingToDeclaration/"></a>[PARSER_ERROR](PARSER_ERROR/index.md)| <a name="io.getstream.chat.android.client.errors/ChatErrorCode.PARSER_ERROR///PointingToDeclaration/"></a>[PARSER_ERROR](PARSER_ERROR/index.md)(1001, "Unable to parse error")|
| <a name="io.getstream.chat.android.client.errors/ChatErrorCode.NETWORK_FAILED///PointingToDeclaration/"></a>[NETWORK_FAILED](NETWORK_FAILED/index.md)| <a name="io.getstream.chat.android.client.errors/ChatErrorCode.NETWORK_FAILED///PointingToDeclaration/"></a>[NETWORK_FAILED](NETWORK_FAILED/index.md)(1000, "Response is failed. See cause")|
  
  
## Types  
  
|  Name |  Summary | 
|---|---|
| <a name="io.getstream.chat.android.client.errors/ChatErrorCode.Companion///PointingToDeclaration/"></a>[Companion](Companion/index.md)| <a name="io.getstream.chat.android.client.errors/ChatErrorCode.Companion///PointingToDeclaration/"></a>object [Companion](Companion/index.md)|
  
  
## Properties  
  
|  Name |  Summary | 
|---|---|
| <a name="io.getstream.chat.android.client.errors/ChatErrorCode/code/#/PointingToDeclaration/"></a>[code](code.md)| <a name="io.getstream.chat.android.client.errors/ChatErrorCode/code/#/PointingToDeclaration/"></a>val [code](code.md): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)|
| <a name="io.getstream.chat.android.client.errors/ChatErrorCode/description/#/PointingToDeclaration/"></a>[description](description.md)| <a name="io.getstream.chat.android.client.errors/ChatErrorCode/description/#/PointingToDeclaration/"></a>val [description](description.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)|
  
  
## Inherited properties  
  
|  Name |  Summary | 
|---|---|
| <a name="io.getstream.chat.android.client.errors/ChatErrorCode/name/#/PointingToDeclaration/"></a>[name](index.md#-1423787670%2FProperties%2F-423410878)| <a name="io.getstream.chat.android.client.errors/ChatErrorCode/name/#/PointingToDeclaration/"></a>val [name](index.md#-1423787670%2FProperties%2F-423410878): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)|
| <a name="io.getstream.chat.android.client.errors/ChatErrorCode/ordinal/#/PointingToDeclaration/"></a>[ordinal](index.md#512867732%2FProperties%2F-423410878)| <a name="io.getstream.chat.android.client.errors/ChatErrorCode/ordinal/#/PointingToDeclaration/"></a>val [ordinal](index.md#512867732%2FProperties%2F-423410878): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)|

