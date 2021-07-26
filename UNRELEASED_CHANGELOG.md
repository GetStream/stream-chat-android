## Common changes for all artifacts
### 🐞 Fixed

### ⬆️ Improved
- ⚠ Downgraded Kotlin version to 1.5.10 to support Jetpack Compose
- Removed AndroidX Media dependency
- Updated dependency versions
  - Coil 1.3.0
  - AndroidX Activity 1.2.4
  - AndroidX AppCompat 1.3.1
  - Android Ktx 1.6.0
  - AndroidX RecyclerView 1.2.1
  - Kotlin Coroutines 1.5.1
  - Dexter 6.2.3
  - Lottie 3.7.2

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

### ⚠️ Changed

### ❌ Removed


## stream-chat-android-compose
### 🐞 Fixed

### ⬆️ Improved

### ✅ Added

### ⚠️ Changed

### ❌ Removed

