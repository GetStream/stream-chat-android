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

### ⚠️ Changed
- Made `Channel::getLastMessage` function public

### ❌ Removed
