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
- Fixed thrown exception type while checking if `ChatClient` is initialized

### ⬆️ Improved

### ✅ Added

### ⚠️ Changed

### ❌ Removed


## stream-chat-android-offline
### 🐞 Fixed
- Fixed bug where reactions of other users were sometimes displayed as reactions of the current user.
- Fixed bug where deleted user reactions were sometimes displayed on the message options overlay.

### ⬆️ Improved

### ✅ Added

### ⚠️ Changed

### ❌ Removed

## stream-chat-android-ui-common
### 🐞 Fixed
Fixed bug where files without extension in their name lost the mime type.

Using offline.ChatDomain instead of livedata.ChatDomain in ChannelListViewModel.
### ⬆️ Improved

### ✅ Added

### ⚠️ Changed

### ❌ Removed

## stream-chat-android-ui-components
### 🐞 Fixed
Fixing the save of pictures from AttachmentGalleryActivity. When external storage
permission is not granted, now it asks for it.
### ⬆️ Improved
- Added default implementation of "Leave channel" click listener to `ChannelListViewModelBinding`

### ✅ Added
- Added `streamUiChannelActionsDialogStyle` attribute to application theme and `ChannelListView` to customize channel actions dialog appearance. The attribute references a style with the following attributes:
  - `streamUiChannelActionsMemberNamesTextSize`, `streamUiChannelActionsMemberNamesTextColor`, `streamUiChannelActionsMemberNamesTextFont`, `streamUiChannelActionsMemberNamesTextFontAssets`, `streamUiChannelActionsMemberNamesTextStyle` attributes to customize dialog title with member names
  - `streamUiChannelActionsMemberInfoTextSize`, `streamUiChannelActionsMemberInfoTextColor`, `streamUiChannelActionsMemberInfoTextFont`, `streamUiChannelActionsMemberInfoTextFontAssets`, `streamUiChannelActionsMemberInfoTextStyle` attributes to customize dialog subtitle with member info
  - `streamUiChannelActionsItemTextSize`, `streamUiChannelActionsItemTextColor`, `streamUiChannelActionsItemTextFont`, `streamUiChannelActionsItemTextFontAssets`, `streamUiChannelActionsItemTextStyle` attributes to customize action item text style
  - `streamUiChannelActionsWarningItemTextSize`, `streamUiChannelActionsWarningItemTextColor`, `streamUiChannelActionsWarningItemTextFont`, `streamUiChannelActionsWarningItemTextFontAssets`, `streamUiChannelActionsWarningItemTextStyle` attributes to customize warning action item text style
  - `streamUiChannelActionsViewInfoIcon` attribute to customize "View Info" action icon
  - `streamUiChannelActionsViewInfoEnabled` attribute to hide/show "View Info" action item
  - `streamUiChannelActionsLeaveGroupIcon` attribute to customize "Leave Group" action icon
  - `streamUiChannelActionsLeaveGroupEnabled` attribute to hide/show "Leave Group" action item
  - `streamUiChannelActionsDeleteConversationIcon` attribute to customize "Delete Conversation" action icon
  - `streamUiChannelActionsDeleteConversationEnabled` attribute to hide/show "Delete Conversation" action item
  - `streamUiChannelActionsCancelIcon` attribute to customize "Cancel" action icon
  - `streamUiChannelActionsCancelEnabled` attribute to hide/show "Cancel" action item
  - `streamUiChannelActionsBackground` attribute for dialog's background
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
- Added `streamUiWarningMessageOptionsTextSize`, `streamUiWarningMessageOptionsTextColor`, `streamUiWarningMessageOptionsTextFont`, `streamUiWarningMessageOptionsFontAssets`, `streamUiWarningMessageOptionsTextStyle` attributes to `MessageListView` for customizing warning actions text appearance
- Deprecated multiple views' tint properties and attributes. Use custom drawables instead.
- Added `MediaAttachmentViewStyle` to allow customizing the appearance of media attachments in the message list. The new style comes together with following `MediaAttachmentView` attributes:
  - `progressIcon` - attribute to customize animated progress drawable when image is loading
  - `giphyIcon` - attribute to customize Giphy icon
  - `imageBackgroundColor` - attribute to customize image background color
  - `moreCountOverlayColor` - attribute to customize the color of "more count" semi-transparent overlay
  - `moreCountTextStyle` - attribute to customize text appearance of more count text
- Added `MessageReplyStyle` class allowing to customize MessageReply item view on MessageListView.
Customization can be done using `TransformStyle` API or XML attributes of `MessageListView`:
  - `streamUiMessageReplyBackgroundColorMine`
  - `streamUiMessageReplyBackgroundColorTheirs`
  - `streamUiMessageReplyTextSizeMine`
  - `streamUiMessageReplyTextColorMine`
  - `streamUiMessageReplyTextFontMine`
  - `streamUiMessageReplyTextFontAssetsMine`
  - `streamUiMessageReplyTextStyleMine`
  - `streamUiMessageReplyTextSizeTheirs`
  - `streamUiMessageReplyTextColorTheirs`
  - `streamUiMessageReplyTextFontTheirs`
  - `streamUiMessageReplyTextFontAssetsTheirs`
  - `streamUiMessageReplyTextStyleTheirs`
  - `streamUiMessageReplyLinkColorMine`
  - `streamUiMessageReplyLinkColorTheirs`
  - `streamUiMessageReplyLinkBackgroundColorMine`
  - `streamUiMessageReplyLinkBackgroundColorTheirs`
  - `streamUiMessageReplyStrokeColorMine`
  - `streamUiMessageReplyStrokeWidthMine`
  - `streamUiMessageReplyStrokeColorTheirs`
  - `streamUiMessageReplyStrokeWidthTheirs`
- Added `FileAttachmentsViewStyle` class allowing to customize FileAttachmentsView item view on MessageListView.
- Added `MessageInputView::setSuggestionListViewHolderFactory` method which allows to provide custom views from suggestion list popup.

### ⚠️ Changed
- Changed the naming of string resources. The updated names can be reviewed in:
  - `strings_common.xml`
  - `strings_attachment_gallery.xml`
  - `strings_channel_list.xml`
  - `strings_channel_list_header.xml`
  - `strings_mention_list.xml`
  - `strings_message_input.xml`
  - `strings_message_list.xml`
  - `strings_message_list_header.xml`
  - `strings_search.xml`

### ❌ Removed
