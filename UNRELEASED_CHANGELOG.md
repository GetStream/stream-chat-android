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
- Fixed setting notification's `contentTitle` when a Channel doesn't have the name. It will now show members names instead

### ⬆️ Improved

### ✅ Added
- Added a new way to paginate through search message results using limit and next/previous values.

### ⚠️ Changed
- Deprecated `Channel#name`, `Channel#image`, `User#name`, `Ues#image` extension properties. Use class members instead.

### ❌ Removed
- Completely removed the old serialization implementation. You can no longer opt-out of using the new serialization implementation.
- Removed the `UpdateUsersRequest` class.

## stream-chat-android-offline
### 🐞 Fixed

### ⬆️ Improved
- Improving logs for Message deletion error.
### ✅ Added

### ⚠️ Changed

### ❌ Removed


## stream-chat-android-ui-common
### 🐞 Fixed
- Fixed theme for `AttachmentDocumentActivity`. Now it is applied: `Theme.AppCompat.DayNight.NoActionBar`
### ⬆️ Improved

### ✅ Added

### ⚠️ Changed

### ❌ Removed


## stream-chat-android-ui-components
### 🐞 Fixed
- Fixed the bug when MessageInputView let send a message with large attachments. Such message is never sent.
- Fixed bug related to `ScrollHelper` when `MessageListView` is initialised more than once.

### ⬆️ Improved
- The search for mentions now includes transliteration, diacritics removal, and ignore typos. To use transliteration, pass the id of the desired alphabets to `DefaultStreamTransliterator`, add it to DefaultUserLookupHandler and set it using `MessageInputView.setUserLookupHandler`. Transliteration works only for android API 29. If you like to add your own transliteration use https://unicode-org.github.io/icu/userguide/icu4j/.
- Improved scroll of message when many gif images are present in `MessageListView`

### ✅ Added
- Added scroll behaviour to `MessageListViewStyle`.
### ⚠️ Changed

### ❌ Removed


## stream-chat-android-compose
### 🐞 Fixed
- Fixed a bug where the Message list flickered when sending new messages
- Fixed a few bugs where some attachments had upload state and weren't file/image uploads

### ⬆️ Improved
- Improved the Message list scrolling behavior and scroll to bottom actions
- Added an unread count on the Message list's scroll to bottom CTA
- Improved the way we build items in the Message list
- Added line limit to link attachment descriptions
- Added a way to customize the default line limit for link descriptions

### ✅ Added
- Added an uploading indicator to files and images
- Images being uploaded are now preloaded from the system
- Upload indicators show the upload progress and how much data is left to send
- Added UploadAttachmentFactory that handles attachment uploads

### ⚠️ Changed
- `StreamAttachment.defaultFactories()` is a function now, instead of a property.
- Updated all default value factories to functions (e.g. StreamTypography)
- Re-organized all attachment factories and split up code in multiple packages
- Changed the `AttachmentState` `message` property name to `messageItem`

### ❌ Removed


## stream-chat-android-pushprovider-firebase
### 🐞 Fixed

### ⬆️ Improved

### ✅ Added

### ⚠️ Changed

### ❌ Removed
