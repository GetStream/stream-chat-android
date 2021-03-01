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
- Deprecated `ChatDomain::disconnect`, use disconnect on ChatClient instead, it will make the disconnection on ChatDomain too.
- Deprecated constructors for `ChatDomain.Builder` with the `User` type parameter, use constructor with `Context` and `ChatClient` instead.

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
- Updated PhotoView to version 2.3.0

### ✅ Added
- Introduced `AttachmentViewFactory` as a factory for custom attachment views/custom link view
- Introduced `TextAndAttachmentsViewHolder` for any combination of attachment content and text

### ⚠️ Changed

### ❌ Removed
- Deleted `OnlyFileAttachmentsViewHolder`, `OnlyMediaAttachmentsViewHolder`,
  `PlainTextWithMediaAttachmentsViewHolder` and `PlainTextWithFileAttachmentsViewHolder`

