# Deprecations

This document lists deprecated constructs in the SDK, with their expected time ⌛ of further deprecations and removals.

| API / Feature | Deprecated (warning) | Deprecated (error) | Removed | Notes |
| --- | --- | --- | --- | --- |
| `ImageAttachmentQuotedContent` | 2022.09.13 <br/>5.9.1 | 2022.09.27<br/>5.9.1 | 2023.08.29<br/>6.0.0 | Deprecated in favor of `MediaAttachmentQuotedContent`. The new function has the ability to preview videos as well as images. |
| `StreamDimens` constructor containing parameter `attachmentsContentImageGridSpacing`  | 2022.09.13 <br/>5.9.1 | 2022.09.27<br/>5.9.1 | 2023.08.29<br/>6.0.0 | This constructor has been deprecated. Use the constructor that does not contain the parameter `attachmentsContentImageGridSpacing`. |
| `ImageAttachmentContent` | 2022.09.13 <br/>5.9.1 | 2022.09.27<br/>5.9.1 | 2023.08.29<br/>6.0.0 | `ImageAttachmentContent` has been deprecated in favor of `MediattachmentContent`. The new function is able to preview videos as well as images and has access to a new and improved media gallery. |
| `ImageAttachmentFactory` | 2022.09.13 <br/>5.9.1 | 2022.09.27<br/>5.9.1 | 2023.08.29<br/>6.0.0 | `ImageAttachmentFactory` has been deprecated in favor of `MediaAttachmentFactory`. The new factory is able to preview videos as well as images and has access to a new and improved media gallery. |
| `ImagePreviewContract` | 2022.09.13 <br/>5.9.1 | 2022.09.27<br/>5.9.1 | 2023.08.29<br/>6.0.0 | `ImagePreviewContract` has been deprecated in favor of `MediaGalleryPreviewContract`, please use it in conjunction with `MediaGalleryPreviewActivity`. The new gallery holds multiple improvements such as the ability to reproduce mixed image and video content, automatic reloading upon regaining network connection and more. |
| `ImagePreviewActivity` | 2022.09.13 <br/>5.9.1 | 2022.09.27<br/>5.9.1 | 2023.08.29<br/>6.0.0 | This gallery activity has been deprecated in favour of `MediaGalleryPreviewContract`. The new gallery holds multiple improvements such as the ability to reproduce mixed image and video content, automatic reloading upon regaining network connection and more. |
| Lambda parameter `AttachmentState.onImagePreviewResult` | 2022.09.13 <br/>5.8.2 | 2023.08.29<br/>6.0.0 | 2023.08.29<br/>6.0.0 | Replace it with lambda parameter `AttachmentState.onMediaGalleryPreviewResult` |
| `AttachmentState` constructor containing parameter `onImagePreviewResult`  | 2022.09.17 <br/>5.8.2 | 2023.08.29<br/>6.0.0 | 2023.08.29<br/>6.0.0 | This constructor has been deprecated. Use the constructor that does not contain the parameter `onImagePreviewResult`. |
| `ClientState.initialized` | 2022.08.02 <br/>5.7.0 | 2022.09.13<br/>5.10.0 | 2023.08.29<br/>6.0.0 | Use ClientState.initializationState instead. |
| `MessageListViewModel.BlockUser` | 2022.08.02 <br/>5.7.0 | 2022.09.13<br/>5.10.0 | 2023.08.29<br/>6.0.0 | Deprecated in order to make the action more explicit. Use `MessageListViewModel.ShadowBanUser` if you want to retain the same functionality, or `MessageListViewModel.BanUser` if you want to outright ban the user. The difference between banning and shadow banning can be found here: https://getstream.io/blog/feature-announcement-shadow-ban/ |
| `MessageAction.MuteUser` | 2022.08.02 <br/>5.7.0 | 2022.09.13<br/>5.10.0 | 2023.08.29<br/>6.0.0 | The option to mute users via a message option has been deprecated and will be removed. |
| `MessageListView::setUserUnmuteHandler` | 2022.08.02 <br/>5.7.0 | 2022.09.13<br/>5.10.0 | 2023.08.29<br/>6.0.0 | The option to unmute the user from `MessageListView` has been deprecated and will be removed. |
| `MessageListView::setUserMuteHandler` | 2022.08.02 <br/>5.7.0 | 2022.09.13<br/>5.10.0 | 2023.08.29<br/>6.0.0 | The option to mute the user from `MessageListView` has been deprecated and will be removed. |
| `MessageListView.UserUnmuteHandler` | 2022.08.02 <br/>5.7.0 | 2022.09.13<br/>5.10.0 | 2023.08.29<br/>6.0.0 | The option to unmute the user from `MessageListView` has been deprecated and will be removed. `UserUnmuteHandler` will be removed with it too. |
| `MessageListView.UserMuteHandler` | 2022.08.02 <br/>5.7.0 | 2022.09.13<br/>5.10.0 | 2023.08.29<br/>6.0.0 | The option to mute the user from `MessageListView` has been deprecated and will be removed. `UserMuteHandler` will be removed with it too. |
| `MessageListView::setMuteUserEnabled` | 2022.08.02 <br/>5.7.0 | 2022.09.13<br/>5.10.0 | 2023.08.29<br/>6.0.0 | The option to mute the user from `MessageListView` has been deprecated and will be removed. |
| `MessageListView.UserBlockHandler` | 2022.08.02 <br/>5.7.0 | 2022.09.13<br/>5.10.0 | 2023.08.29<br/>6.0.0 | The option to block the user from `MessageListView` has been deprecated and will be removed. `UserBlockHandler` will be removed with it too. |
| `MessageListView::setBlockUserEnabled` | 2022.08.02 <br/>5.7.0 | 2022.09.13<br/>5.10.0 | 2023.08.29<br/>6.0.0| The option to block the user from `MessageListView` has been deprecated and will be removed. |
| `MessageListView` attributes | 2022.08.02 <br/>5.7.0 | 2022.09.13<br/>5.10.0 | 2023.08.29<br/>6.0.0 | The attributes `streamUiMuteOptionIcon`, `streamUiUnmuteOptionIcon`, `streamUiMuteUserEnabled`, `streamUiBlockOptionIcon` and `streamUiBlockUserEnabled` have been deprecated and will be removed. The options to block and mute users will no longer be contained inside `MessageListView` |
| `MessageListViewStyle` constructor containing params: `muteIcon`, `unmuteIcon`, `muteEnabled`, `blockIcon` and `blockEnabled` | 2022.08.02 <br/>5.7.0 | 2022.09.13<br/>5.10.0  | 2023.08.29<br/>6.0.0 | This constructor has been deprecated. Use the constructor that does not contain these parameters. |
| `StreamColors.ownMessagesBackground` | 2023.07.11 <br/>5.17.7 | 2023.08.29<br/>6.0.0 | 2023.10.11<br/> ⌛ | Use `MessageTheme.backgroundColor` instead. |
| `StreamColors.otherMessagesBackground` | 2023.07.11 <br/>5.17.7 | 2023.08.29<br/>6.0.0 | 2023.10.11<br/> ⌛ | Use `MessageTheme.backgroundColor` instead. |
| `StreamColors.deletedMessagesBackground` | 2023.07.11 <br/>5.17.7 | 2023.08.29<br/>6.0.0 | 2023.10.11<br/> ⌛ | Use `MessageTheme.deletedBackgroundColor` instead. |
| `StreamColors.ownMessageText` | 2023.07.11 <br/>5.17.7 | 2023.08.29<br/>6.0.0 | 2023.10.11<br/> ⌛ | Use `MessageTheme.textStyle.color` instead. |
| `StreamColors.otherMessageText` | 2023.07.11 <br/>5.17.7 | 2023.08.29<br/>6.0.0 | 2023.10.11<br/> ⌛ | Use `MessageTheme.textStyle.color` instead. |
| `StreamColors.ownMessageQuotedBackground` | 2023.07.11 <br/>5.17.7 | 2023.08.29<br/>6.0.0 | 2023.10.11<br/> ⌛ | Use `MessageTheme.quotedBackgroundColor` instead. |
| `StreamColors.otherMessageQuotedBackground` | 2023.07.11 <br/>5.17.7 | 2023.08.29<br/>6.0.0 | 2023.10.11<br/> ⌛ | Use `MessageTheme.quotedBackgroundColor` instead. |
| `StreamColors.ownMessageQuotedText` | 2023.07.11 <br/>5.17.7 | 2023.08.29<br/>6.0.0 | 2023.10.11<br/> ⌛ | Use `MessageTheme.quotedTextStyle.color` instead. |
| `StreamColors.otherMessageQuotedText` | 2023.07.11 <br/>5.17.7 | 2023.08.29<br/>6.0.0 | 2023.10.11<br/> ⌛ | Use `MessageTheme.quotedTextStyle.color` instead. |
| `StreamDimens` constructor containing parameter `attachmentsContentImageHeight`  | 2022.08.16 <br/>5.8.0 | 2022.08.30<br/>5.9.0 | 2022.09.13<br/>5.10.0 | This constructor has been deprecated. Use the constructor that does not contain the parameter `attachmentsContentImageHeight`. |
| `QueryChannelsState.chatEventHandler` | 2022.08.16 <br/>5.8.0 | 2022.08.30<br/>5.9.0 | 2022.09.13<br/>5.10.0 | Use `QueryChannelsState.chatEventHandlerFactory` instead. |
| Multiple event specific `BaseChatEventHandler` methods | 2022.08.16 <br/>5.8.0 | 2022.08.30<br/>5.9.0 | 2022.09.13<br/>5.10.0 | Use `handleChatEvent()` or `handleCidEvent()` instead. |
| `NonMemberChatEventHandler` | 2022.08.16 <br/>5.8.0 | 2022.08.30<br/>5.9.0 | 2022.09.13<br/>5.10.0 | Use `BaseChatEventHandler` or `DefaultChatEventHandler` instead. |
| `DefaultTypingUpdatesBuffer` | 2022.08.02 <br/>5.7.0 | 2022.08.16<br/>5.8.0 | 2022.08.30<br/>5.9.0 | This implementation of `TypingUpdatesBuffer` has been deprecated and will be removed. Should you wish to user your own typing updates buffer, you should create a custom implementation of `TypingUpdatesBuffer`. |
| `ChannelListView.showLoadingMore()` | 2022.08.02 <br/>5.7.0 | 2022.09.13<br/>5.10.0 | 2023.08.29<br/>6.0.0 | Insert the loading item before passing the list to the adapter. |
| `ChannelListView.hideLoadingMore()` | 2022.08.02 <br/>5.7.0 | 2022.09.13<br/>5.10.0 | 2023.08.29<br/>6.0.0 | Insert the loading item before passing the list to the adapter. |
| `RowScope.DefaultComposerInputContent` | 2022.08.02<br/>5.7.0 | 2022.09.13<br/>5.10.0 | 2023.08.29<br/>6.0.0 | Use `MessageInput` instead. |
| `LegacyDateFormatter` | 2022.08.02<br/>5.7.0 | 2022.08.16<br/>5.8.0 | 2022.08.30<br/>5.9.0 | The class is unused and will be removed. |
| `PorterImageView` | 2022.08.02<br/>5.7.0 | 2022.08.16<br/>5.8.0 | 2022.08.30<br/>5.9.0 | The class is unused and will be removed. |
| `PorterShapeImageView` | 2022.08.02<br/>5.7.0 | 2022.08.16<br/>5.8.0 | 2022.08.30<br/>5.9.0 | The class is unused and will be removed. |
| `ChatClient::disconnect` | 2022.07.19<br/>5.6.0 | 2022.08.16<br/>5.8.0 | 2022.09.13<br/>5.10.0 | Use `ChatClient.disconnect(Boolean)` instead. |
| `TaggedLogger` | 2022.07.19<br/>5.6.0 | 2022.08.16<br/>5.8.0 | 2022.09.13<br/>5.10.0 | Use `StreamLog` instead.|
| `ChatLogger` | 2022.07.19<br/>5.6.0 | 2022.08.16<br/>5.8.0 | 2022.09.13<br/>5.10.0 | Use `StreamLog` instead.|
| `ChatLogger.Config` | 2022.07.19<br/>5.6.0 | 2022.08.16<br/>5.8.0 | 2022.09.13<br/>5.10.0 | Use `ChatLoggerConfig` instead.|
| `ChatLogger::get` | 2022.07.19<br/>5.6.0 | 2022.08.16<br/>5.8.0 | 2022.09.13<br/>5.10.0 | Use `StreamLog::getLogger` instead.|
| `GlobalState::isInitialized` | 2022.07.19<br/>5.6.0 | 2022.08.02<br/>5.7.0| 2022.09.13<br/>5.10.0 | Use `ClientState:isInitialized` instead.|
| `GlobalState::isConnecting` | 2022.07.19<br/>5.6.0 | 2022.08.02<br/>5.7.0 | 2022.09.13<br/>5.10.0 | Use `ClientState:isConnecting` instead.|
| `GlobalState::isOffline` | 2022.07.19<br/>5.6.0 | 2022.08.02<br/>5.7.0 | 2022.09.13<br/>5.10.0 | Use `ClientState:isOffline` instead.|
| `GlobalState::isOnline` | 2022.07.19<br/>5.6.0 | 2022.08.02<br/>5.7.0 | 2022.09.13<br/>5.10.0 | Use `ClientState:isOnline` instead.|
| `GlobalState::connectionState` | 2022.07.19<br/>5.6.0 | 2022.08.02<br/>5.7.0 | 2022.09.13<br/>5.10.0 | Use `ClientState:connectionState` instead.|
| `GlobalState::initialized` | 2022.07.19<br/>5.6.0 | 2022.08.02<br/>5.7.0 | 2022.09.13<br/>5.10.0 | Use `ClientState:initialized` instead.|
| `GlobalState::user` | 2022.07.19<br/>5.6.0 | 2022.08.02<br/>5.7.0 | 2022.09.13<br/>5.10.0 | Use `ClientState::user` instead.|
| `GlobalState::errorEvents` | 2022.07.19<br/>5.6.0 | 2022.08.02<br/>5.7.0 | 2022.09.13<br/>5.10.0 | This method is no longer used.|
| `GlobalState::typingUpdates` | 2022.07.04 <br/>5.5.0 | 2022.07.19<br/>5.6.0 | 2022.08.02<br/>5.7.0 | Use `GlobalState::typingChannels` instead. |
| `MessageListView.setUserBlockHandler` | 2022.07.04 <br/>5.5.0 | 2022.07.19<br/>5.6.0 | 2022.08.02<br/>5.7.0 | The block action has been removed. Use `MessageOptionItemsFactory.setMessageOptionItemsFactory()` in conjunction with `MessageOptionItemsFactory.setCustomActionHandler()` to add support for custom block action. |
| `QuerySort` | 2022.06.22 <br/>5.4.0 | 2022.07.19<br/>5.6.0 | 2022.08.16<br/>5.8.0 | Use QuerySortByReflection. |
| `ChatClient.loadMessageById` | 2022.06.22 <br/>5.4.0 | 2022.07.19<br/>5.6.0 | 2022.08.16<br/>5.8.0 | Use the version without offsets, as it uses less requests to backend. |
| `InputField` | 2022.06.22 <br/>5.4.0 | 2022.07.19<br/>5.6.0 | 2022.08.16<br/>5.8.0 | Use the new implementation of `InputField`. |
| `Member.isOwnerOrAdmin` | 2022.05.24<br/>5.3.0 | 2022.06.22 <br/>5.4.0 | 2022.07.04 <br/>5.5.0 | Use Channel::ownCapabilities to determine user permissions. |
| `List<Member?>.isCurrentUserOwnerOrAdmin` | 2022.05.24<br/>5.3.0 | 2022.06.22 <br/>5.4.0 | 2022.07.04 <br/>5.5.0 | Use Channel::ownCapabilities to determine user permissions. |
| `QuotedMessage` | 2022.05.24<br/>5.3.0 | 2022.06.22 <br/>5.4.0 | 2022.07.04 <br/>5.5.0 | Use new implementation of `QuotedText`. |
| `MessageText` | 2022.05.24<br/>5.3.0 | 2022.06.22 <br/>5.4.0 | 2022.07.04 <br/>5.5.0 | Use the new implementation of `MessageText`. |
| `DeletedMessageListItemPredicate` | 2022.05.03<br/>5.1.0 | 2022.05.24<br/>5.3.0 | 2022.06.22 <br/>5.4.0 | Use `DeletedMessageVisibility` in conjunction with `MessageListViewModel.setDeletedMessagesVisibility` instead. |
| `MessageListView.setDeletedMessageListItemPredicate` | 2022.05.03<br/>5.1.0 | 2022.05.24<br/>5.3.0 | 2022.06.22 <br/>5.4.0 | Use `MessageListViewModel.setDeletedMessagesVisibility` instead. |
| `Member.role` | 2022.01.11<br/>4.26.0 | 2022.03.23<br/>5.0.0 | 2022.07.19<br/>5.6.0 | Use `Member.channelRole`in conjunction with `Member.user.role` and `Channel.createdBy` instead. |
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
| `ChatDomain#currentUser` <br/>*offline* | 2021.04.30 | 2021.07.13<br/>4.14.0 | 2021.08.24<br/>4.17.0 | Subscribe to `ChatDomain::user` and handle nullable state |
| `ChatDomain.usecases` <br/>*offline* | 2021.04.06 | 2021.05.06<br/>4.10.0 | 2021.06.23<br/>4.12.1 | Replace this property call by obtaining a specific use case directly from ChatDomain |
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
