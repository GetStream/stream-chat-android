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

### ⬆️ Improved

### ✅ Added

### ⚠️ Changed

### ❌ Removed


## stream-chat-android-ui-components
### 🐞 Fixed

### ⬆️ Improved

### ✅ Added

### ⚠️ Changed

### ❌ Removed


## stream-chat-android-compose
### 🐞 Fixed
- Fixed a bug where the Message list flickered when sending new messages

### ⬆️ Improved
- Improved the Message list scrolling behavior and scroll to bottom actions
- Added an unread count on the Message list's scroll to bottom CTA
- Improved the way we build items in the Message list
- Added line limit to link attachment descriptions
- Added a way to customize the default line limit for link descriptions

### ✅ Added

### ⚠️ Changed
- `StreamAttachment.defaultFactories()` is a function now, instead of a property.
- Updated all default value factories to functions (e.g. StreamTypography)
- Re-organized all attachment factories and split up code in multiple packages
-

### ❌ Removed


## stream-chat-android-pushprovider-firebase
### 🐞 Fixed

### ⬆️ Improved

### ✅ Added

### ⚠️ Changed

### ❌ Removed
