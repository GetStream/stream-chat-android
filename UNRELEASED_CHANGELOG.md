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
Fixed bug where files without extension in their name lost the mime type.
### ⬆️ Improved

### ✅ Added

### ⚠️ Changed

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
- Added `streamUiIconOnlyVisibleToYou` attribute to `MessageListView` to allow customizing "Only visible to you" icon placed in messages footer
- Added `GiphyViewHolderStyle` to `MessageListViewStyle` to allow customizing `GiphyViewHolder`. The new style comes together with following `MessageListView` attributes:
  - `streamUiGiphyCardBackgroundColor` attribute to customize card's background color
  - `streamUiGiphyCardElevation` attribute to customize card's elevation
  - `streamUiGiphyCardButtonDividerColor` attribute to customize dividers' colors
  - `streamUiGiphyIcon` attribute to customize Giphy icon
  - `streamUiGiphyLabelTextSize`, `streamUiGiphyLabelTextColor`, `streamUiGiphyLabelTextFont`, `streamUiGiphyLabelTextFontAssets`, `streamUiGiphyLabelTextStyle` attributes to customize label
  - `streamUiGiphyQueryTextSize`, `streamUiGiphyQueryTextColor`, `streamUiGiphyQueryTextFont`, `streamUiGiphyQueryTextFontAssets`, `streamUiGiphyQueryTextStyle` attributes to customize query text
  - `streamUiGiphyCancelButtonTextSize`, `streamUiGiphyCancelButtonTextColor`, `streamUiGiphyCancelButtonTextFont`, `streamUiGiphyCancelButtonTextFontAssets`, `streamUiGiphyCancelButtonTextStyle` attributes to customize cancel button text
  - `streamUiGiphyShuffleButtonTextSize`, `streamUiGiphyShuffleButtonTextColor`, `streamUiGiphyShuffleButtonTextFont`, `streamUiGiphyShuffleButtonTextFontAssets`, `streamUiGiphyShuffleButtonTextStyle` attributes to customize shuffle button text
  - `streamUiGiphySendButtonTextSize`, `streamUiGiphySendButtonTextColor`, `streamUiGiphySendButtonTextFont`, `streamUiGiphySendButtonTextFontAssets`, `streamUiGiphySendButtonTextStyle` attributes to customize send button text
- Adding extra XML attrs allowing to customize "Send also to channel" CheckBox at `MessageInputView` component:
  - `MessageInputView.streamUiSendAlsoToChannelCheckboxDrawable`
  - `MessageInputView.streamUiSendAlsoToChannelCheckboxDirectChatText`
  - `MessageInputView.streamUiSendAlsoToChannelCheckboxGroupChatText`
  - `MessageInputView.streamUiSendAlsoToChannelCheckboxTextStyle`
  - `MessageInputView.streamUiSendAlsoToChannelCheckboxTextColor`
  - `MessageInputView.streamUiSendAlsoToChannelCheckboxTextSize`

### ⚠️ Changed

### ❌ Removed
