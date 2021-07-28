## Common changes for all artifacts
### 🐞 Fixed
- Fixed adding `MessageListItem.TypingItem` to message list

### ⬆️ Improved

### ✅ Added

### ⚠️ Changed

### ❌ Removed


## stream-chat-android
### 🐞 Fixed

### ⬆️ Improved

### ✅ Added

### ⚠️ Changed

### ❌ Removed


## stream-chat-android-client
### 🐞 Fixed

### ⬆️ Improved
- Improved the names of properties in the `Config` class

### ✅ Added

### ⚠️ Changed

### ❌ Removed


## stream-chat-android-offline
### 🐞 Fixed

### ⬆️ Improved

### ✅ Added

### ⚠️ Changed

### ❌ Removed


## stream-chat-android-ui-common
### 🐞 Fixed

### ⬆️ Improved

### ✅ Added
Now it is possible to style the AttachmentActivity. Just replace the activity's theme
in your Manifest file:

```
<activity
    android:name="io.getstream.chat.android.ui.gallery.AttachmentActivity"
    android:theme="@style/yourTheme"
    tools:replace="android:theme"
    />
```
### ⚠️ Changed

### ❌ Removed


## stream-chat-android-ui-components
### 🐞 Fixed
- Fixed "operator $ne is not supported for custom fields" error when querying channels

### ⬆️ Improved

### ✅ Added
- Now you can configure the style of `MessageListItem`. Added:
  - streamUiMessageTextColorThreadSeparator
  - streamUiMessageTextFontThreadSeparator
  - streamUiMessageTextFontAssetsThreadSeparator
  - streamUiMessageTextStyleThreadSeparator
  - streamUiMessageTextSizeLinkLabel
  - streamUiMessageTextColorLinkLabel
  - streamUiMessageTextFontLinkLabel
  - streamUiMessageTextFontAssetsLinkLabel
  - streamUiMessageTextStyleLinkLabel
  - streamUiMessageListLoadingView
  - streamUiEmptyStateTextSize
  - streamUiEmptyStateTextColor
  - streamUiEmptyStateTextFont
  - streamUiEmptyStateTextFontAssets
  - streamUiEmptyStateTextStyle

- Now you can configure the style of `AttachmentMediaActivity`
- Added `streamUiLoadingView`, `streamUiEmptyStateView` and `streamUiLoadingMoreView` attributes to `ChannelListView` and `ChannelListViewStyle`
- Added possibility to customize `ChannelListView` using `streamUiChannelListViewStyle`. Check `StreamUi.ChannelListView` style
- Added `edgeEffectColor` attribute to `ChannelListView` and `ChannelListViewStyle` to allow configuring edge effect color
- Added possibility to customize `MentionListView` style via `TransformStyle.mentionListViewStyleTransformer`
- Added `streamUiSearchResultListViewStyle` attribute to application to customize `SearchResultListView`. The attribute references a style with the following attributes:
  - `streamUiSearchResultListSearchInfoBarBackground` - background for search info bar
  - `streamUiSearchResultListSearchInfoBarTextSize`, `streamUiSearchResultListSearchInfoBarTextColor`, `streamUiSearchResultListSearchInfoBarTextFont`, `streamUiSearchResultListSearchInfoBarTextFontAssets`, `streamUiSearchResultListSearchInfoBarTextStyle` attributes to customize text displayed in search info bar
  - `streamUiSearchResultListEmptyStateIcon` - icon for empty state view
  - `streamUiSearchResultListEmptyStateTextSize`, `streamUiSearchResultListEmptyStateTextColor`, `streamUiSearchResultListEmptyStateTextFont`, `streamUiSearchResultListEmptyStateTextFontAssets`, `streamUiSearchResultListEmptyStateTextStyle` attributes to customize empty state text
  - `streamUiSearchResultListProgressBarIcon` - animated progress drawable
  - `streamUiSearchResultListSenderNameTextSize`, `streamUiSearchResultListSenderNameTextColor`, `streamUiSearchResultListSenderNameTextFont`, `streamUiSearchResultListSenderNameTextFontAssets`, `streamUiSearchResultListSenderNameTextStyle` attributes to customize message sender text
  - `streamUiSearchResultListMessageTextSize`, `streamUiSearchResultListMessageTextColor`, `streamUiSearchResultListMessageTextFont`, `streamUiSearchResultListMessageTextFontAssets`, `streamUiSearchResultListMessageTextStyle` attributes to customize message text
  - `streamUiSearchResultListMessageTimeTextSize`, `streamUiSearchResultListMessageTimeTextColor`, `streamUiSearchResultListMessageTimeTextFont`, `streamUiSearchResultListMessageTimeTextFontAssets`, `streamUiSearchResultListMessageTimeTextStyle` attributes to customize message time text
- Added possibility to customize `SearchResultListView` style via `TransformStyle.searchResultListViewStyleTransformer`
- Added `streamUiTypingIndicatorViewStyle` attribute to application to customize `TypingIndicatorView`. The attribute references a style with the following attributes:
  - `streamUiTypingIndicatorAnimationView` - typing view
  - `streamUiTypingIndicatorUsersTextSize`, `streamUiTypingIndicatorUsersTextColor`, `streamUiTypingIndicatorUsersTextFont`, `streamUiTypingIndicatorUsersTextFontAssets`, `streamUiTypingIndicatorUsersTextStyle` attributes to customize typing users text
- Added possibility to customize `TypingIndicatorView` style via `TransformStyle.typingIndicatorViewStyleTransformer`
- Added new properties allowing customizing `MessageInputView` using `MessageInputViewStyle` and `AttachmentSelectionDialogStyle`:
    - `MessageInputViewStyle.fileNameTextStyle`
    - `MessageInputViewStyle.fileSizeTextStyle`
    - `MessageInputViewStyle.fileCheckboxSelectorDrawable`
    - `MessageInputViewStyle.fileCheckboxTextColor`
    - `MessageInputViewStyle.fileAttachmentEmptyStateTextStyle`
    - `MessageInputViewStyle.mediaAttachmentEmptyStateTextStyle`
    - `MessageInputViewStyle.fileAttachmentEmptyStateText`
    - `MessageInputViewStyle.mediaAttachmentEmptyStateText`
    - `MessageInputViewStyle.dismissIconDrawable`
    - `AttachmentSelectionDialogStyle.allowAccessToGalleryText`
    - `AttachmentSelectionDialogStyle.allowAccessToFilesText`
    - `AttachmentSelectionDialogStyle.allowAccessToCameraText`
    - `AttachmentSelectionDialogStyle.allowAccessToGalleryIcon`
    - `AttachmentSelectionDialogStyle.allowAccessToFilesIcon`
    - `AttachmentSelectionDialogStyle.allowAccessToCameraIcon`
    - `AttachmentSelectionDialogStyle.grantPermissionsTextStyle`
    - `AttachmentSelectionDialogStyle.recentFilesTextStyle`
    - `AttachmentSelectionDialogStyle.recentFilesText`
    - `AttachmentSelectionDialogStyle.fileManagerIcon`
    - `AttachmentSelectionDialogStyle.videoDurationTextStyle`
    - `AttachmentSelectionDialogStyle.videoIconDrawable`
    - `AttachmentSelectionDialogStyle.videoIconVisible`
    - `AttachmentSelectionDialogStyle.videoLengthLabelVisible`
- Added `StreamUi.MessageInputView` theme allowing to customize all of the `MessageInputViewStyle` properties:
    - streamUiAttachButtonEnabled
    - streamUiAttachButtonIcon
    - streamUiLightningButtonEnabled
    - streamUiLightningButtonIcon
    - streamUiMessageInputTextSize
    - streamUiMessageInputTextColor
    - streamUiMessageInputHintTextColor
    - streamUiMessageInputScrollbarEnabled
    - streamUiMessageInputScrollbarFadingEnabled
    - streamUiSendButtonEnabled
    - streamUiSendButtonEnabledIcon
    - streamUiSendButtonDisabledIcon
    - streamUiShowSendAlsoToChannelCheckbox
    - streamUiSendAlsoToChannelCheckboxGroupChatText
    - streamUiSendAlsoToChannelCheckboxDirectChatText
    - streamUiSendAlsoToChannelCheckboxTextSize
    - streamUiSendAlsoToChannelCheckboxTextColor
    - streamUiSendAlsoToChannelCheckboxTextStyle
    - streamUiMentionsEnabled
    - streamUiMessageInputTextStyle
    - streamUiMessageInputHintText
    - streamUiCommandsEnabled
    - streamUiMessageInputEditTextBackgroundDrawable
    - streamUiMessageInputDividerBackgroundDrawable
    - streamUiPictureAttachmentIcon
    - streamUiFileAttachmentIcon
    - streamUiCameraAttachmentIcon
    - streamUiAllowAccessToCameraIcon
    - streamUiAllowAccessToFilesIcon
    - streamUiAllowAccessToGalleryIcon
    - streamUiAllowAccessToGalleryText
    - streamUiAllowAccessToFilesText
    - streamUiAllowAccessToCameraText
    - streamUiGrantPermissionsTextSize
    - streamUiGrantPermissionsTextColor
    - streamUiGrantPermissionsTextStyle
    - streamUiAttachmentsRecentFilesTextSize
    - streamUiAttachmentsRecentFilesTextColor
    - streamUiAttachmentsRecentFilesTextStyle
    - streamUiAttachmentsRecentFilesText
    - streamUiAttachmentsFileManagerIcon
    - streamUiAttachmentVideoLogoIcon
    - streamUiAttachmentVideoLengthVisible
    - streamUiAttachmentVideoIconVisible
    - streamUiCommandInputCancelIcon
    - streamUiCommandInputBadgeBackgroundDrawable
    - streamUiCommandInputBadgeIcon
    - streamUiCommandInputBadgeTextSize
    - streamUiCommandInputBadgeTextColor
    - streamUiCommandInputBadgeStyle
    - streamUiAttachmentsFileNameTextSize
    - streamUiAttachmentsFileNameTextColor
    - streamUiAttachmentsFileNameTextStyle
    - streamUiAttachmentsFileSizeTextSize
    - streamUiAttachmentsFileSizeTextColor
    - streamUiAttachmentsFileSizeTextStyle
    - streamUiFileCheckBoxSelectorTextColor
    - streamUiFileCheckBoxSelectorDrawable
    - streamUiAttachmentsFilesEmptyStateTextSize
    - streamUiAttachmentsFilesEmptyStateTextColor
    - streamUiAttachmentsFilesEmptyStateStyle
    - streamUiAttachmentsMediaEmptyStateTextSize
    - streamUiAttachmentsMediaEmptyStateTextColor
    - streamUiAttachmentsMediaEmptyStateStyle
    - streamUiAttachmentsFilesEmptyStateText
    - streamUiAttachmentsMediaEmptyStateText
    - streamUiMessageInputCloseButtonIconDrawable

### ⚠️ Changed
- Made `Channel::getLastMessage` function public
- `AttachmentSelectionDialogFragment::newInstance` requires instance of `MessageInputViewStyle` as a parameter. You can obtain a default implementation of `MessageInputViewStyle` with `MessageInputViewStyle::createDefault` method.

### ❌ Removed
- 🚨 Breaking change: `MessageListItemStyle::reactionsEnabled` was deleted as doubling of the same flag from `MessageListViewStyle`
