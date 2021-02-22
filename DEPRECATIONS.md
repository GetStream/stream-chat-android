# Deprecations

This document lists deprecated constructs in the SDK, with their expected time ⌛ of further deprecations and removals.

| API / Feature | Deprecated (warning) | Deprecated (error) | Removed | Notes |
| --- | --- | --- | --- | --- |
| `setUser` (and similar) methods<br/>*client* | 2021.02.03<br/>4.5.3 | 2021.05.03 ⌛ | 2021.08.03 ⌛ | Replaced by `connectUser` style methods that return `Call` objects, see the updated documentation for [Initialization & Users](https://getstream.io/chat/docs/android/init_and_users/?language=kotlin)) |
| `MessageListViewModel.Event.AttachmentDownload`<br/>*ui-common* | 2021.01.29<br/>4.5.2 | 2021.02.29 ⌛ | 2021.03.29 ⌛ | Use `DownloadAttachment` instead |
| `subscribe` methods with Kotlin function parameters<br/>*client* | 2021.01.27<br/>4.5.2 | 2021.03.27 ⌛ | 2021.05.27 ⌛ | Use methods with `ChatEventListener` parameters instead (only affects Java clients) |
| `ChatDomain.Builder#notificationConfig`<br/>*offline* | - | 2020.12.14<br/>4.4.7 | 2021.03.14 ⌛ | Configure this on `ChatClient.Builder` instead |
| `Call#enqueue((Result<T>) -> Unit)`<br/>*core* | - | 2020.12.09<br/>4.4.7 | 2021.03.09 ⌛ | Use `enqueue(Callback<T>)` instead (only affects Java clients) |
| `ChatUI(client, domain, context)`<br/>*ui-common* | 2020.11.09<br/>4.4.2 | 2021.02.22 ⌛ | 2021.03.22 ⌛ | Use ctor with just Context param instead |
| `User#unreadCount`<br/>*client* | 2020.11.05<br/>4.4.2 | 2021.02.22 ⌛ | 2021.03.22 ⌛ | Use `totalUnreadCount` instead |
| `ChannelController`<br/>*client* | 2020.11.04<br/>4.4.1 | 2021.02.22 ⌛ | 2021.04.22 ⌛ | Renamed due to conflicting name with a type in the offline library, use `ChannelClient` instead |
| `Chat` interface<br/>*ui-common* | 2020.10.27<br/>4.4.1 | 2021.02.22 ⌛ | 2021.04.22 ⌛ | Use `ChatUI` instead |
| `Pagination#get`<br/>*client* | 2020.10.12<br/>4.3.0 | 2021.02.22 ⌛ | 2021.03.22 ⌛ | Use `toString` instead |
| `MessageListView#setViewHolderFactory`<br/>*ui (old)* | 2020.10.15<br/>4.3.1 | 2021.02.22 ⌛ | 2021.04.22 ⌛ | Use the more explicit `setMessageViewHolderFactory` method instead |
| `MessageListItemAdapter#replaceEntities`<br/>*ui (old)* | - | 2020.10.05<br/>4.3.0 | 2021.02.22 ⌛ | Use `submitList` instead | 
| `ChatObservable` based event APIs (`events()`)<br/>*client* | 2020.09.18 | 2021.02.22 ⌛ | 2021.04.22 ⌛ | Replace with direct `subscribe` calls on `ChatClient` and `ChannelClient`, see [migration guide](https://github.com/GetStream/stream-chat-android/wiki/Migration-guide:-ChatObserver-and-events()-APIs) |
| `SendMessageWithAttachments` use case <br/>*offline* | 2020.09.30<br/>4.3.0 | 2021.02.22 ⌛ | 2021.04.22 ⌛ | - |
| `ChatClient#flag(userId)`<br/>*client* | 2020.07.28 | 2021.02.22 ⌛ | 2021.03.22 ⌛ | Use the more explicit `flagUser` method instead |
