# Deprecations

This document lists deprecated constructs in the SDK, with their expected time ⌛ of further deprecations and removals.

| API / Feature | Deprecated (warning) | Deprecated (error) | Removed | Notes |
| --- | --- | --- | --- | --- |
| `DeletedMessageListItemPredicate` | 2022.05.03<br/>5.1.0 | 2022.05.24 ⌛ | 2022.06.07 ⌛ | Use `DeletedMessageVisibility` in conjunction with `MessageListViewModel.setDeletedMessagesVisibility` instead. |
| `MessageListView.setDeletedMessageListItemPredicate` | 2022.05.03<br/>5.1.0 | 2022.05.24 ⌛ | 2022.06.07 ⌛ | Use `MessageListViewModel.setDeletedMessagesVisibility` instead. |
| `Member.role` | 2022.01.11<br/>4.26.0 | 2022.03.23<br/>5.0.0 | 2022.05.11 ⌛ | Use `Member.channelRole` instead. |
| `ChannelController` | 2022.03.23<br/>5.0.0 | 2022.03.23<br/>5.0.0 | 2022.03.23<br/>5.0.0 | Use `ChannelState` instead |
| `QueryChannelsController` | 2022.03.23<br/>5.0.0 | 2022.03.23<br/>5.0.0 | 2022.03.23<br/>5.0.0 | Use `QueryChannelsState` instead |
| `ChatDomain` | 2022.03.23<br/>5.0.0 | 2022.03.23<br/>5.0.0 | 2022.03.23<br/>5.0.0 | Use `OfflinePlugin` instead |
| `ChatUI.markdown` | 2022.01.11<br/>4.26.0 | 2022.02.08<br/>4.28.0 | 2022.03.23<br/>5.0.0 | Use `ChatUI.messageTextTransformer` instead. |
| `ChatMarkdown` | 2022.01.11<br/>4.26.0 | 2022.02.08<br/>4.28.0 | 2022.03.23<br/>5.0.0 | `ChatMarkdown` is deprecated in favour of `ChatMessageTextTransformer`. Use `MarkdownTextTransformer` from module `stream-chat-android-markdown-transformer` instead. If you want to use your own markdown implementation, you need to implement `ChatMessageTextTransformer`. |
| `ChatDomain#showChannel` | 2021.12.21<br/>4.25.0 | 2022.01.25<br/>4.27.0 | 2022.03.23<br/>5.0.0 | Use `ChatClient#showChannel` instead |
| `ChatDomain#loadOlderMessages` | 2021.12.21<br/>4.25.0 | 2022.01.25<br/>4.27.0  | 2022.03.23<br/>5.0.0 | Use `ChatClient#loadOlderMessages` instead |
| `ChatDomain#stopTyping` | 2021.11.29<br/>4.24.0 | 2022.01.11<br/>4.26.0 | 2022.02.08<br/>4.28.0 | Use `ChatClient#stopTyping` instead |
| `ChatDomain#keystroke` | 2021.11.29<br/>4.24.0 | 2022.01.11<br/>4.26.0 |  2022.02.08<br/>4.28.0 | Use `ChatClient#keystroke` instead |
| `QueryChannelsController#mutedChannelIds` | 2021.11.23<br/>4.23.0 | 2021.12.09<br/>4.24.0 | 2022.01.11<br/>4.26.0 | Use ChatDomain.mutedChannels instead |
| `ChatDomain#downloadAttachment` | 2021.11.23<br/>4.23.0 | 2022.01.11<br/>4.26.0 | 2022.02.08<br/>4.28.0 | Use `ChatClient#downloadAttachment` instead |
| `ChatDomain#setMessageForReply` | 2021.11.23<br/>4.23.0 | 2022.01.11<br/>4.26.0 | 2022.02.08<br/>4.28.0 | Use `ChatClient#setMessageForReply` instead |
| `ChatDomain#replayEventsForActiveChannels` | 2021.11.24<br/>4.23.0 | 2022.01.11<br/>4.26.0 | 2022.02.08<br/>4.28.0 | Use `ChatClient#replayEventsForActiveChannels` instead |
| `ChatDomain#online` | 2021.10.26<br/>4.21.0 | 2021.11.24<br/>4.23.0 | 2022.01.11<br/>4.26.0 | Use ChatDomain#connectionState instead |
| `QueryChannelsController#newChannelEventFilter` | 2021.10.12<br/>4.20.0 | 2021.11.08<br/>4.22 | 2021.11.08<br/>4.22 | Use QueryChannelsController::chatEventHandler instead |
| `QueryChannelsController#checkFilterOnChannelUpdatedEvent` | 2021.10.12<br/>4.20.0 | 2021.11.08<br/>4.22 | 2021.11.08<br/>4.22 | Use QueryChannelsController::chatEventHandler instead |
| `ChatUI#uiMode` <br/>*ui-components* | 2021.10.12<br/>4.20.0 | 2021.11.10<br/>4.22.0 | 2021.12.09<br/>4.24.0 | This behavior is not supported anymore. Our SDK already use Day/Night themes that follow the standard process Android provide to support them. If you want to force your app to use Dark/Light mode, you need tu use `AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO|AppCompatDelegate.MODE_NIGHT_YES)` |
| `ProgressCallback#onProgress(Long)` <br/>*client* | 2021.09.28<br/> | 2021.11.10<br/>4.22.0 | 2021.12.09<br/>4.24.0 | This function is not used anymore. Use `ProgressCallback#onProgress(Long, Long)` |
| `ChatNotificationHandler` <br/>*client* | 2021.10.12<br/>4.20.0 | 2021.11.10<br/>4.22.0 | 2021.12.09<br/>4.24.0 | If you want to continue using our implementation, you can use our `NotificationHandlerFactory` to create the default implementation we provide. If you need a more customized implementation, you need to implement `NotificationHandler` interface |
| `NotificationConfig` attributes <br/>*client* | 2021.10.12<br/>4.20.0 | 2021.10.12<br/>4.20.0 | 2021.11.10<br/>4.22.0 | Attributes to customize notifications are not used anymore. You need to override those Strings/Drawable into resources of your app |
| `ChatClient#cdnUrl`  <br/>*client* | 2021.10.12<br/>4.20.0 | 2021.10.12<br/>4.20.0 | 2021.11.10<br/>4.22.0 | Use `ChatClient.fileUploader()` to add custom file uploading logic instead  |
| `ChatClient#cdnTimeout` and `ChatClient#baseTimeout` <br/>*client* | 2021.10.12<br/>4.20.0 | 2021.11.10<br/>4.22.0 | 2021.12.09<br/>4.24.0 | Use `ChatClient.okHttpClient()` to set the timeouts instead |
| `DeviceRegisteredListener` <br/>*client* | 2021.09.28<br/>4.19.0 | 2021.09.28<br/>4.19.0 | 2021.10.12<br/>4.20.0 | This class is not used anymore |
| `ViewReactionsViewStyle#bubbleBorderColor` <br/>*client* | 2021.09.28<br/>4.19.0 | 2021.10.12<br/>4.20.0 | 2021.11.10<br/>4.22.0 | Use bubbleBorderColorMine instead  |
| `NotificationConfig` attributes <br/>*client* | 2021.09.28<br/>4.19.0 | 2021.09.28<br/>4.19.0 | 2021.10.12<br/>4.20.0 | Some attributes are not needed anymore |
| `NotificationLoadDataListener` <br/>*client* | 2021.09.28<br/>4.19.0 | 2021.09.28<br/>4.19.0 | 2021.10.12<br/>4.20.0 | This class is not used anymore, you will be asked to build your notification |
| `ChatClient#searchMessages` <br/>*client* | 2021.09.15<br/>4.18.0 | 2021.10.12<br/>4.20.0 | 2021.11.10<br/>4.22.0 | Use the `ChatClient#searchMessages` method with unwrapped parameters instead |
| `ChatDomain#createDistinctChannel` <br/>*offline* | 2021.09.15<br/>4.18.0 | 2021.10.12<br/>4.20.0 | 2021.11.10<br/>4.22.0 | Use ChatClient::createChannel directly |
| `ChatDomain#removeMembers` <br/>*offline* | 2021.09.15<br/>4.18.0 | 2021.10.12<br/>4.20.0 | 2021.11.10<br/>4.22.0 | Use ChatClient::removeMembers directly |
| `User#name` extension<br/>*client* | 2021.09.15<br/>4.18.0 | 2021.09.15<br/>4.18.0 | 2021.10.12<br/>4.20.0  | Use class member instead |
| `User#image` extension<br/>*client* | 2021.09.15<br/>4.18.0 | 2021.09.15<br/>4.18.0 | 2021.10.12<br/>4.20.0  | Use class member instead |
| `Channel#name` extension<br/>*client* | 2021.09.15<br/>4.18.0 | 2021.09.15<br/>4.18.0 | 2021.10.12<br/>4.20.0  | Use class member instead |
| `Channel#image` extension<br/>*client* | 2021.09.15<br/>4.18.0 | 2021.09.15<br/>4.18.0 | 2021.10.12<br/>4.20.0  | Use class member instead |
| `ChatClient#getMessagesWithAttachments`<br/>*client* | 2021.08.24<br/>4.17.0 | 2021.09.15<br/>4.18.0 | 2021.10.12<br/>4.20.0 | Use getMessagesWithAttachments function with types list instead |
| `ChannelClient#getMessagesWithAttachments`<br/>*client* | 2021.08.24<br/>4.17.0 | 2021.09.15<br/>4.18.0 | 2021.10.12<br/>4.20.0 | Use getMessagesWithAttachments function with types list instead |
| `created_at`, `updated_at`, `isTypingEvents`, `isReadEvents`, `isConnectEvents`, `isSearch`, `isMutes` in Config class are all deprecated. <br/>*ui-components* | 2021.07.13<br/>4.14.0 | 2021.08.25<br/>4.17.0 | 2021.08.25<br/>4.17.0 | Use `createdAt`, `updatedAt`, `typingEventsEnabled`, `readEventsEnabled`, `connectEventsEnabled`, `searchEnabled` and `mutesEnabled` instead |
| `MessageListViewModel#currentUser` <br/>*ui-components* | 2021.07.13<br/>4.14.0 | 2021.08.24<br/>4.17.0 | 2021.08.24<br/>4.17.0 | Use `MessageListViewModel#user.value` instead |
| `ChatClient.Builder#logLevel(String)`<br/>*client* | 2021.07.01 | 2021.07.13<br/>4.14.0 | 2021.08.24<br/>4.17.0 | Use `ChatClient.Builder#logLevel(ChatLogLevel)` instead |
| `ChatDomain#sendMessage(message: Message, attachmentTransformer: ((at: Attachment, file: File) -> Attachment)?,)` <br/>*offline* | 2021.06.14 | 2021.07.13<br/>4.14.0 | 2021.08.24<br/>4.17.0 | Use `ChatDomain#sendMessage(message: Message)` instead |
| Multiple `MessageListView` tint related attributes<br/>*ui-components* | 2021.06.10 | 2021.07.13<br/>4.14.0 | 2021.08.24<br/>4.17.0 | Override drawables instead |
| Multiple `MessageInputView` tint related attributes<br/>*ui-components* | 2021.06.10 | 2021.07.13<br/>4.14.0 | 2021.08.24<br/>4.17.0 | Override drawables instead |
| `ChannelListHeaderView.streamUiActionButtonTint` attribute<br/>*ui-components* | 2021.06.10 | 2021.07.13<br/>4.14.0 | 2021.08.24<br/>4.17.0 | Override drawable instead |
| `ChannelListView.streamUiMutedChannelIconTint` attribute<br/>*ui-components* | 2021.06.10 | 2021.07.13<br/>4.14.0 | 2021.08.24<br/>4.17.0 | Override drawable instead |
| Multiple `AttachmentOptionsView` tint related attributes<br/>*ui-components* | 2021.06.10 | 2021.07.13<br/>4.14.0 | 2021.08.24<br/>4.17.0 | Override drawables instead |
| `MessageListViewStyle#warningActionsTintColor`<br/>*ui-components* | 2021.06.10 | 2021.07.13<br/>4.14.0 | 2021.08.24<br/>4.17.0 | Override drawable instead |
| `MessageListViewStyle#iconsTint`<br/>*ui-components* | 2021.06.10 | 2021.07.13<br/>4.14.0 | 2021.08.24<br/>4.17.0 | Override drawables instead |
| `AttachmentSelectionDialogStyle#pictureAttachmentIconTint`<br/>*ui-components* | 2021.06.10 | 2021.07.13<br/>4.14.0 | 2021.08.24<br/>4.17.0 | Use the `AttachmentDialogStyle#pictureAttachmentIcon` instead |
| `AttachmentSelectionDialogStyle#fileAttachmentIconTint`<br/>*ui-components* | 2021.06.10 | 2021.07.13<br/>4.14.0 | 2021.08.24<br/>4.17.0 | Use the `AttachmentDialogStyle#fileAttachmentIcon` instead |
| `AttachmentSelectionDialogStyle#cameraAttachmentIconTint`<br/>*ui-components* | 2021.06.10 | 2021.07.13<br/>4.14.0 | 2021.08.24<br/>4.17.0 | Use the `AttachmentDialogStyle#cameraAttachmentIcon` instead |
| `ChannelListViewStyle#mutedChannelIconTint`<br/>*ui-components* | 2021.06.10 | 2021.07.13<br/>4.14.0 | 2021.08.24<br/>4.17.0 | Use the `ChannelListViewStyle#mutedChannelIcon` instead |
| `AvatarView.OnlineIndicatorPosition.TOP`<br/>*ui-components* | 2021.06.01 | 2021.07.13<br/>4.14.0 | 2021.08.24<br/>4.17.0 | Use the `OnlineIndicatorPosition.TOP_RIGHT` constant instead |
| `AvatarView.OnlineIndicatorPosition.BOTTOM`<br/>*ui-components* | 2021.06.01 | 2021.07.13<br/>4.14.0 | 2021.08.24<br/>4.17.0 | Use the `OnlineIndicatorPosition.BOTTOM_RIGHT` constant instead |
| `SocketListener::onDisconnected` <br/>*client* | 2021.05.17 | 2021.06.23<br/>4.12.1  | 2021.07.13<br/>4.14.0 | Use method with DisconnectCause instead of it |
| `ChatClient#onMessageReceived`<br/>*client* | 2021.05.14<br/>4.11.0 | 2021.06.23<br/>4.12.1  | 2021.07.13<br/>4.14.0 | Use the `ChatClient.handleRemoteMessage` method instead |
| `ChatClient#onNewTokenReceived`<br/>*client* | 2021.05.14<br/>4.11.0 | 2021.06.23<br/>4.12.1  | 2021.07.13<br/>4.14.0 | Use the `ChatClient.setFirebaseToken` method instead |
| `ChatNotificationHandler#getSmallIcon`<br/>*client* | 2021.05.14<br/>4.11.0 | 2021.06.23<br/>4.12.1  | 2021.07.13<br/>4.14.0 | Use the `NotificationsConfig.smallIcon` instead |
| `ChatNotificationHandler#getFirebaseMessageIdKey`<br/>*client* | 2021.05.14<br/>4.11.0 | 2021.06.23<br/>4.12.1  | 2021.07.13<br/>4.14.0 | Use the `NotificationsConfig.firebaseMessageIdKey` instead |
| `ChatNotificationHandler#getFirebaseChannelIdKey`<br/>*client* | 2021.05.14<br/>4.11.0 | 2021.06.23<br/>4.12.1  | 2021.07.13<br/>4.14.0 | Use the `NotificationsConfig.firebaseChannelIdKey` instead |
| `ChatNotificationHandler#getFirebaseChannelTypeKey`<br/>*client* | 2021.05.14<br/>4.11.0 | 2021.06.23<br/>4.12.1  | 2021.07.13<br/>4.14.0 | Use the `NotificationsConfig.firebaseChannelTypeKey` instead |
| Old serialization implementation<br/>*client* | 2021.05.10<br/>4.10.0 | 2021.06.30<br/>4.13.0 | 2021.09.15<br/>4.18.0 | See the [4.10.0 release notes](https://github.com/GetStream/stream-chat-android/releases/tag/4.10.0) for details |
| `io.getstream.chat.android.livedata.ChannelData` <br/>*offline* | 2021.05.07 | 2021.06.23<br/>4.12.1 | 2021.07.13<br/>4.14.0 | Use `io.getstream.chat.android.offline.channel.ChannelData` instead |
| `ChannelController#channelData` <br/>*offline* | 2021.05.07 | 2021.06.23<br/>4.12.1 | 2021.07.13<br/>4.14.0 | Use `ChannelController::offlineChannelData` instead|
| `MessageInputViewModel#editMessage` <br/>*ui-common* | 2021.05.05 | 2021.06.23<br/>4.12.1 | 2021.07.13<br/>4.14.0 | Use `MessageInputViewModel::messageToEdit` and `MessageInputViewModel::postMessageToEdit` |
| `ChatDomain#currentUser` <br/>*offline* | 2021.04.30 | 2021.07.13<br/>4.14.0 | 2021.08.24<br/>4.17.0 | Subscribe to `ChatDomain::user` and handle nullable state |
| `MessageInputView#setSuggestionListView` <br/>*ui-components* | 2021.04.13 | 2021.04.27<br/>4.10.0 | 2021.06.23<br/>4.12.1 | Setting external SuggestionListView is no longer necessary |
| `ChatDomain.usecases` <br/>*offline* | 2021.04.06 | 2021.05.06<br/>4.10.0 | 2021.06.23<br/>4.12.1 | Replace this property call by obtaining a specific use case directly from ChatDomain |
| `MessageInputView#setMembers` <br/>*ui-components* | 2021.04.07 | 2021.04.21<br/>4.9.0 | 2021.05.05<br>4.10.0 | Use MessageInputView::setUserLookupHandler instead of manually passing the list of users |
| `ChannelListView's empty state methods` <br/>*ui-components* | 2021.04.05 | 2021.04.21<br/>4.9.0 | 2021.05.05<br/>4.10.0 | These methods no longer need to be called directly, `setChannel` handles empty state changes automatically |
| `MessageListItemStyle#messageTextColorTheirs` <br/>*ui-components* | 2021.03.25 | 2021.04.21<br/>4.9.0 | 2021.06.23<br/>4.12.1 | Use MessageListItemStyle::textStyleTheirs::colorOrNull() instead |
| `MessageListItemStyle#messageTextColorMine` <br/>*ui-components* | 2021.03.25 | 2021.04.21<br/>4.9.0 | 2021.06.23<br/>4.12.1 | Use MessageListItemStyle::textStyleMine::colorOrNull() instead |
| `com.getstream.sdk.chat.ChatUI`<br/>*ui-components* | 2021.03.19<br/>4.8.0 | 2021.04.21<br/>4.9.0 | 2021.06.23<br/>4.12.1 | Use the new ChatUI implementation `io.getstream.chat.android.ui.ChatUI`|
| `GetTotalUnreadCount#invoke`<br/> | 2021.03.17<br/>4.7.2  | 2021.04.21<br/>4.9.0 | 2021.06.23<br/>4.12.1 | Use ChatDomain::totalUnreadCount instead |
| `GetUnreadChannelCount#invoke`<br/> | 2021.03.17<br/>4.7.2  | 2021.04.21<br/>4.9.0 | 2021.06.23<br/>4.12.1 | Use ChatDomain::channelUnreadCount instead |
| `ChatClient#unMuteChannel`<br/>*client* | 2021.03.15<br/>4.7.1 | 2021.04.21<br/>4.9.0 | 2021.06.23<br/>4.12.1 | Use the `unmuteChannel` method instead |
| `ChatClient#unBanUser`<br/>*client* | 2021.03.15<br/>4.7.1 | 2021.04.21<br/>4.9.0 | 2021.06.23<br/>4.12.1 | Use the `unbanUser` method instead |
| `ChannelClient#unBanUser`<br/>*client* | 2021.03.15<br/>4.7.1 | 2021.04.21<br/>4.9.0 | 2021.06.23<br/>4.12.1 | Use the `unbanUser` method instead |
| `ChannelController#unBanUser`<br/>*client* | 2021.03.15<br/>4.7.1 | 2021.04.21<br/>4.9.0 | 2021.04.21<br/>4.9.0 | Use the `unbanUser` method instead |
| `ChatDomain.Builder` constructors with user params | 2021.02.26<br/>4.7.0 | 2021.06.23<br/>4.12.1 | 2021.08.24<br/>4.17.0 | Use `ChatDomain.Builder(context, chatClient)` instead |
| `ChatDomain#disconnect` | 2021.02.25<br/>4.7.0 | 2021.06.23<br/>4.12.1 | 2021.08.24<br/>4.17.0 | Use just `ChatClient#disconnect` instead |
| `setUser` (and similar) methods<br/>*client* | 2021.02.03<br/>4.5.3 | 2021.05.03<br/>4.10.0 | 2021.08.24<br/>4.17.0 | Replaced by `connectUser` style methods that return `Call` objects, see the updated documentation for [Initialization & Users](https://getstream.io/chat/docs/android/init_and_users/?language=kotlin)) |
| `MessageListViewModel.Event.AttachmentDownload`<br/>*ui-common* | 2021.01.29<br/>4.5.2 | 2021.02.29<br/>4.7.0 | 2021.03.29<br/>4.8.0 | Use `DownloadAttachment` instead |
| `subscribe` methods with Kotlin function parameters<br/>*client* | 2021.01.27<br/>4.5.2 | 2021.03.27<br/>4.8.0 | 2021.06.23<br/>4.12.1 | Use methods with `ChatEventListener` parameters instead (only affects Java clients) |
| `ChatUI(client, domain, context)`<br/>*ui-common* | 2020.11.09<br/>4.4.2 | 2021.02.22<br/>4.6.0 | 2021.03.22<br/>4.8.0 | Use ctor with just Context param instead |
| `User#unreadCount`<br/>*client* | 2020.11.05<br/>4.4.2 | 2021.02.22<br/>4.6.0 | 2021.03.22<br/>4.8.0 | Use `totalUnreadCount` instead |
| `ChannelController`<br/>*client* | 2020.11.04<br/>4.4.1 | 2021.02.22<br/>4.6.0 | 2021.04.21<br/>4.9.0 | Renamed due to conflicting name with a type in the offline library, use `ChannelClient` instead |
| `Chat` interface<br/>*ui-common* | 2020.10.27<br/>4.4.1 | 2021.02.22<br/>4.6.0 | 2021.04.21<br/>4.9.0 | Use `ChatUI` instead |
| `MessageListView#setViewHolderFactory`<br/>*ui (old)* | 2020.10.15<br/>4.3.1 | 2021.02.22<br/>4.6.0 | 2021.04.21<br/>4.9.0 | Use the more explicit `setMessageViewHolderFactory` method instead |
| `SendMessageWithAttachments` use case <br/>*offline* | 2020.09.30<br/>4.3.0 | 2021.02.22<br/>4.6.0 | 2021.04.21<br/>4.9.0 | - |
| `ChatObservable` based event APIs (`events()`)<br/>*client* | 2020.09.18 | 2021.02.22<br/>4.6.0 | 2021.04.21<br/>4.9.0 | Replace with direct `subscribe` calls on `ChatClient` and `ChannelClient`, see [migration guide](https://github.com/GetStream/stream-chat-android/wiki/Migration-guide:-ChatObserver-and-events()-APIs) |
| `ChatClient#flag(userId)`<br/>*client* | 2020.07.28 | 2021.02.22<br/>4.6.0 | 2021.03.22<br/>4.8.0 | Use the more explicit `flagUser` method instead |
| `ChatDomain.Builder#notificationConfig`<br/>*offline* | - | 2020.12.14<br/>4.4.7 | 2021.03.14<br/>4.8.0 | Configure this on `ChatClient.Builder` instead |
| `Call#enqueue((Result<T>) -> Unit)`<br/>*core* | - | 2020.12.09<br/>4.4.7 | 2021.03.09<br/>4.8.0 | Use `enqueue(Callback<T>)` instead (only affects Java clients) |
| `Pagination#get`<br/>*client* | - | 2020.10.12<br/>4.3.0 | 2021.02.22<br/>4.6.0 | Use `toString` instead |
| `MessageListItemAdapter#replaceEntities`<br/>*ui (old)* | - | 2020.10.05<br/>4.3.0 | 2021.02.22<br/>4.6.0 | Use `submitList` instead |
