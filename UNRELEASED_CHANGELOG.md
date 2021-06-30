## Common changes for all artifacts
### 🐞 Fixed

### ⬆️ Improved
- Updated to Kotlin 1.5.20

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
- Added new attributes to `MessageInputView` allowing to customize the style of input field during command input:
    - `streamUiCommandInputBadgeTextSize`, `streamUiCommandInputBadgeTextColor`, `streamUiCommandInputBadgeFontAssets`, `streamUiCommandInputBadgeFont`, `streamUiCommandInputBadgeStyle` attributes to customize the text appearance of command name inside command badge
    - `streamUiCommandInputCancelIcon` attribute to customize the icon for cancel button
    - `streamUiCommandInputBadgeIcon` attribute to customize the icon inside command badge
    - `streamUiCommandInputBadgeBackgroundDrawable` attribute to customize the background shape of command badge
- Added possibility to customize `MessageListHeaderView` style via `streamUiMessageListHeaderStyle` theme attribute and via `TransformStyle.messageListHeaderStyleTransformer`.
- Added new attributes to `MessageInputView`:
    - `streamUiCommandIcon` attribute to customize the command icon displayed for each command item in the suggestion list popup
    - `streamUiLightningIcon` attribute to customize the lightning icon displayed in the top left corner of the suggestion list popup
  
### ⚠️ Changed
- 🚨 Breaking change: moved `commandsTitleTextStyle`, `commandsNameTextStyle`, `commandsDescriptionTextStyle`, `mentionsUsernameTextStyle`, `mentionsNameTextStyle`, `mentionsIcon`, `suggestionsBackground` fields from `MessageInputViewStyle` to `SuggestionListViewStyle`. Their values can be customized via `TransformStyle.suggestionListStyleTransformer`.

### ❌ Removed
