## Common changes for all artifacts
### 🐞 Fixed
- Fix scroll bug in the `MessageListView` that produces an exception related to index out of bounds.

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
- Improved `ChatClient::enableSlowMode`, `ChatClient::disableSlowMode`, `ChannelClient::enableSlowMode`, `ChannelClient::disableSlowMode` methods. Now the methods do partial channel updates so that other channel fields are not affected.

### ✅ Added
- Added `ChatClient::partialUpdateUser` method for user partial updates.

### ⚠️ Changed

### ❌ Removed


## stream-chat-android-offline
### 🐞 Fixed
- Fixed bug related to editing message in offline mode. The bug was causing message to reset to the previous one after connection was recovered.
- Fixed violation of comparison contract for nullable fields in `QuerySort::comparator`

### ⬆️ Improved

### ✅ Added

### ⚠️ Changed

### ❌ Removed


## stream-chat-android-ui-common
### 🐞 Fixed

### ⬆️ Improved
Now it is possible to customize all avatar using themes. Create
```
<style name="StreamTheme" parent="@style/StreamUiTheme">
```

and customize all the avatars that you would like. All options are available here:
https://github.com/GetStream/stream-chat-android/blob/main/stream-chat-android-ui-components/src/main/res/values/attrs.xml

### ✅ Added

### ⚠️ Changed

### ❌ Removed


## stream-chat-android-ui-components
### 🐞 Fixed

### ⬆️ Improved
Now you can use the style `streamUiChannelListHeaderStyle` to customize ChannelListHeaderView. 
### ✅ Added
Added `MessageListView::requireStyle` which expose `MessageListViewStyle`. Be sure invoke it when view is initialized already.

### ⚠️ Changed
- 🚨 Breaking change: removed `MessageListItemStyle.threadsEnabled` property. You should use only the `MessageListViewStyle.threadsEnabled` instead. E.g. The following code will disable both _Thread reply_ message option and _Thread reply_ footnote view visible below the message list item:
```kotlin
        TransformStyle.messageListStyleTransformer = StyleTransformer {
            it.copy(threadsEnabled = false)
        }
```
- `MentionListView` can be customisable with XML parameters and with a theme.

### ❌ Removed
