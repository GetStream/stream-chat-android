## Common changes for all artifacts
### 🐞 Fixed

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

### ⬆️ Improved

### ✅ Added
- Now you can configure the style of `AttachmentMediaActivity`
- Added `streamUiLoadingView`, `streamUiEmptyStateView` and `streamUiLoadingMoreView` attributes to `ChannelListView` and `ChannelListViewStyle`
- Added possibility to customize `ChannelListView` using `streamUiChannelListViewStyle`. Check `StreamUi.ChannelListView` style.
- Added `edgeEffectColor` attribute to `ChannelListView` and `ChannelListViewStyle` to allow configuring edge effect color.
- Added possibility to customize `MentionListView` style via `TransformStyle.mentionListViewStyleTransformer`.
- Added `streamUiSearchResultListViewStyle` attribute to application to customize `SearchResultListView`. The attribute references a style with the following attributes:
  - `streamUiSearchResultSearchInfoBarBackground` - background for search info bar
  - `streamUiSearchResultSearchInfoBarTextSize`, `streamUiSearchResultSearchInfoBarTextColor`, `streamUiSearchResultSearchInfoBarTextFont`, `streamUiSearchResultSearchInfoBarTextFontAssets`, `streamUiSearchResultSearchInfoBarTextStyle` attributes to customize text displayed in search info bar
  - `streamUiSearchResultEmptyStateIcon` - icon for empty state view
  - `streamUiSearchResultEmptyStateTextSize`, `streamUiSearchResultEmptyStateTextColor`, `streamUiSearchResultEmptyStateTextFont`, `streamUiSearchResultEmptyStateTextFontAssets`, `streamUiSearchResultEmptyStateTextStyle` attributes to customize empty state text
  - `streamUiSearchResultProgressBarIcon` - animated progress drawable
  - `streamUiSearchResultSenderNameTextSize`, `streamUiSearchResultSenderNameTextColor`, `streamUiSearchResultSenderNameTextFont`, `streamUiSearchResultSenderNameTextFontAssets`, `streamUiSearchResultSenderNameTextStyle` attributes to customize message sender text
  - `streamUiSearchResultMessageTextSize`, `streamUiSearchResultMessageTextColor`, `streamUiSearchResultMessageTextFont`, `streamUiSearchResultMessageTextFontAssets`, `streamUiSearchResultMessageTextStyle` attributes to customize message text
  - `streamUiSearchResultMessageTimeTextSize`, `streamUiSearchResultMessageTimeTextColor`, `streamUiSearchResultMessageTimeTextFont`, `streamUiSearchResultMessageTimeTextFontAssets`, `streamUiSearchResultMessageTimeTextStyle` attributes to customize message time text
- Added possibility to customize `SearchResultListView` style via `TransformStyle.searchResultListViewStyleTransformer`.

### ⚠️ Changed

### ❌ Removed
