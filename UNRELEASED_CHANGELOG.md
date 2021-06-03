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

### ⚠️ Changed
Using `io.getstream.chat.android.offline.ChatDomain` instead of `io.getstream.chat.android.livedata.ChatDomain` inside ChannelListViewModel and ChannelListViewModelFactory.
### ❌ Removed

## stream-chat-android-ui-components
### 🐞 Fixed

### ⬆️ Improved
- Added default implementation of "Leave channel" click listener to `ChannelListViewModelBinding`

### ✅ Added
- Added `streamUiChannelActionsDialogStyle` attribute to application theme and `ChannelListView` to customize channel actions dialog appearance. The attribute references a style with the following attributes:
  - `streamUiChannelActionsMemberNamesTextSize`, `streamUiChannelActionsMemberNamesTextColor`, `streamUiChannelActionsMemberNamesTextFont`, `streamUiChannelActionsMemberNamesTextFontAssets`, `streamUiChannelActionsMemberNamesTextStyle` attributes to customize dialog title with member names
  - `streamUiChannelActionsMemberInfoTextSize`, `streamUiChannelActionsMemberInfoTextColor`, `streamUiChannelActionsMemberInfoTextFont`, `streamUiChannelActionsMemberInfoTextFontAssets`, `streamUiChannelActionsMemberInfoTextStyle` attributes to customize dialog subtitle with member info
  - `streamUiChannelActionsItemTextSize`, `streamUiChannelActionsItemTextColor`, `streamUiChannelActionsItemTextFont`, `streamUiChannelActionsItemTextFontAssets`, `streamUiChannelActionsItemTextStyle` attributes to customize action item text style
  - `streamUiChannelActionsViewInfoIcon` attribute to customize "View Info" action icon
  - `streamUiChannelActionsViewInfoEnabled` attribute to hide/show "View Info" action item
  - `streamUiChannelActionsLeaveGroupIcon` attribute to customize "Leave Group" action icon
  - `streamUiChannelActionsLeaveGroupEnabled` attribute to hide/show "Leave Group" action item
  - `streamUiChannelActionsDeleteConversationIcon` attribute to customize "Delete Conversation" action icon
  - `streamUiChannelActionsDeleteConversationEnabled` attribute to hide/show "Delete Conversation" action item
  - `streamUiChannelActionsCancelIcon` attribute to customize "Cancel" action icon
  - `streamUiChannelActionsCancelEnabled` attribute to hide/show "Cancel" action item
  - `streamUiChannelActionsIconsTint` attribute to customize action ions tint color
  - `streamUiChannelActionsWarningActionsTint` attribute to customize the color of "Delete Conversation" action item

### ⚠️ Changed

### ❌ Removed
