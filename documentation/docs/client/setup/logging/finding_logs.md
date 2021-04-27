---
id: clientFindingLogs
title: Finding Logs
sidebar_position: 3
---

# Finding Logs

All SDK log tags have the `Chat:` prefix, so you can filter for that those in the logs:

```bash
adb logcat com.your.package | grep "Chat:"
```

Here's a set of useful tags for debugging network communication:

- `Chat:Http`
- `Chat:Events`
- `Chat:SocketService`
