---
id: clientEnablingLogs
title: Enabling Logs
sidebar_position: 1
---

# Enabling Logs
SDK logs are disabled by default. You can enable logs and set a log level when initializing `ChatClient`:

```kotlin
val client = ChatClient.Builder("apiKey", context)
    .logLevel(ChatLogLevel.ALL)
    .build()
```
