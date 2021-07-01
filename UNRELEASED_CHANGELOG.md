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

### ❌ Removed


## stream-chat-android-ui-components
### 🐞 Fixed

### ⬆️ Improved

### ✅ Added

### ⚠️ Changed
- 🚨 Breaking change: moved `commandsTitleTextStyle`, `commandsNameTextStyle`, `commandsDescriptionTextStyle`, `mentionsUsernameTextStyle`, `mentionsNameTextStyle`, `mentionsIcon`, `suggestionsBackground` fields from `MessageInputViewStyle` to `SuggestionListViewStyle`. Their values can be customized via `TransformStyle.suggestionListStyleTransformer`.
- Made `SuggestionListController` and `SuggestionListUi` public. Note that both of these are _experimental_, which means that the API might change at any time in the future (even without a deprecation cycle).
- Made `AttachmentSelectionDialogFragment` _experimental_ which means that the API might change at any time in the future (even without a deprecation cycle).
- 🚨 Breaking change: removed `MessageListItemStyle.threadsEnabled` property. You should use only the `MessageListViewStyle.threadsEnabled` instead. E.g. The following code will disable both _Thread reply_ message option and _Thread reply_ footnote view visible below the message list item:
```kotlin
        TransformStyle.messageListStyleTransformer = StyleTransformer {
            it.copy(threadsEnabled = false)
        }
```

### ❌ Removed
