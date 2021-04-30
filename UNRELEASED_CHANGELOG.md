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
- Added `ChatDomain::user`, a new property that provide the current user into a LiveData/StateFlow container

### ⚠️ Changed
- `ChatDomain::currentUser` has been warning-deprecated because it is an unsafe property that could be null, you should subscribe to `ChatDomain::user` instead

### ❌ Removed

## stream-chat-android-ui-common
### 🐞 Fixed

### ⬆️ Improved

### ✅ Added

### ⚠️ Changed

### ❌ Removed

## stream-chat-android-ui-components
### 🐞 Fixed
- Fixed NPE on MessageInputViewModel when the it was initialized before the user was set

### ⬆️ Improved

### ✅ Added

### ⚠️ Changed

### ❌ Removed
