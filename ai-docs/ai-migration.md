# Stream Chat Android SDK — v6 to v7 AI Migration Guide

## §0 — Agent Instructions

> **Your training data is stale.** The SDK underwent a major rewrite between v6 and v7. Do NOT guess API names, imports, or patterns from memory. Follow this guide exactly.

### Trust hierarchy

1. This guide (machine-verified rename lists, structural patterns)
2. Source files listed in §8 (read them — they are the ground truth)
3. The human migration guide in the repo (prose, may lag behind code)
4. Your training data (LAST resort — assume it is wrong for v7)

### Work order

Execute sections in order. Each section assumes the previous one is complete.

1. **§1 Detection** — scan the codebase to determine which sections apply
2. **§2 Prerequisites** — update dependencies (fail-fast — if this breaks, nothing else matters)
3. **§3 Big structural migrations** — plugin system, theming, ChatComponentFactory
4. **§4 Package relocations** — bulk find-and-replace for moved classes
5. **§5 Rename lists** — class, method, and component renames
6. **§6 Removed with structural replacement** — things that can't be simple-renamed
7. **§7 Behavior changes** — runtime semantics that changed (no compile error, but different behavior)
8. **§8 Machine-readable rename block** — JSON blob for automated tools
9. **§9 Source paths to read** — files to consult when this guide is insufficient
10. **§10 Verification** — grep checks + build + smoke test

### Key facts

- v6 latest: check Maven Central for the latest `6.x` release
- v7 latest: check Maven Central for the latest `7.x` release
- v6 branch: `v6` (maintenance only)
- v7 branch: `develop` (default branch)
- Module count: v6 had 10 modules, v7 has 6 (4 removed/merged)
- The compose module (`stream-chat-android-compose`) is the primary UI layer
- **Kotlin 2.2.0 required** — v7 SDK is compiled with Kotlin 2.2.0; you must update your project's Kotlin version

### Platform note

Shell commands in this guide use Unix syntax (bash). On Windows, use `gradlew.bat` instead of `./gradlew`, and adapt `find`/`rg`/`perl` commands to PowerShell equivalents or run via WSL/Git Bash.

---

## §1 — Detection

Run these commands from the project root. Each match maps to a section you must apply.

```bash
# §2 — Dependencies (ALWAYS apply)
rg "stream-chat-android-offline|stream-chat-android-state|stream-chat-android-ui-utils|stream-chat-android-ai-assistant" build.gradle* --type gradle

# §3.1 — Plugin system
rg "StreamOfflinePluginFactory|StreamStatePluginFactory|StatePluginConfig|withPlugins" --type kotlin

# §3.2 — Theming (StreamColors/StreamTypography/StreamDimens/StreamShapes)
rg "StreamColors|StreamTypography|StreamDimens|StreamShapes|StreamRippleConfiguration" --type kotlin

# §3.3 — ChatTheme parameters
rg "reactionIconFactory|messageContentFactory|autoTranslationEnabled|isComposerLinkPreviewEnabled|attachmentsPickerTabFactories|attachmentPickerTheme|dimens\s*=|shapes\s*=" --type kotlin

# §3.4 — ChatComponentFactory / MessageContentFactory
rg "ChatComponentFactory|MessageContentFactory\b" --type kotlin

# §4 — Package relocations
rg "io\.getstream\.chat\.android\.state\.|io\.getstream\.chat\.android\.offline\.|io\.getstream\.chat\.android\.uiutils\." --type kotlin

# §5.1 — Compose component renames
rg "MessagesScreen|MessageListHeader|MessagesViewModelFactory|MessageListViewModelFactory" --type kotlin
# Also check for channel list factory (v6 name collides with v7 messages factory name)
rg "import.*viewmodel\.channels\.ChannelViewModelFactory" --type kotlin

# §5.2 — Attachment picker
rg "AttachmentsPicker|AttachmentsPickerMode|AttachmentsPickerTabFactory|AttachmentsPickerImagesTabFactory|AttachmentsPickerFilesTabFactory|AttachmentsPickerMediaCaptureTabFactory" --type kotlin

# §5.3 — Reactions
rg "ReactionIconFactory|ReactionPushEmojiFactory|SelectedReactionsMenu|ReactionOptions\b|ReactionOptionItem\b|AdaptiveMessageReactions" --type kotlin

# §5.4 — Channel list actions
rg "ChannelOptionState|ChannelOptionItemVisibility" --type kotlin

# §5.5 — Avatars
rg "GroupAvatar|ImageAvatar|InitialsAvatar|UserAvatarRow" --type kotlin

# §5.6 — CDN / Headers
rg "ImageHeadersProvider|AsyncImageHeadersProvider|VideoHeadersProvider|DownloadAttachmentUriGenerator|DownloadRequestInterceptor|ImageAssetTransformer|AttachmentDocumentActivity|useDocumentGView" --type kotlin

# §5.7 — Composer changes + ChatComponentFactory overrides + removed APIs
rg "mentionPopupContent|commandPopupContent|MentionSuggestionItem|MentionSuggestionList|SuggestionList\b" --type kotlin
rg "MessageMenu\b|MessageItemCenterContent|MessageItemHeaderContent|MessageItemFooterContent|SelectedMessageMenu" --type kotlin
rg "mirrorRtl|ChatClient\.config\.apiKey|\.integrations\s*=" --type kotlin
rg "isDraftMessageEnabled|isComposerDraftMessageEnabled|titleColor\s*=.*messageOption|iconColor\s*=.*messageOption" --type kotlin

# §5.8 — Drawable resource renames
rg "stream_compose_ic_" --type kotlin --type xml

# §5.9 — Removed color properties
rg "\.highlight\b|\.overlayDark\b|\.inputBackground\b|\.infoAccent\b" --type kotlin

# §5.10 — Removed dimens/shapes
rg "ChatTheme\.dimens\.|ChatTheme\.shapes\." --type kotlin

# §5.11 — Component parameter changes
rg "channelContent\s*=|divider\s*=.*\{|isMuted\s*=|onChannelOptionClick\s*=|showOnlineIndicator\s*=" --type kotlin
rg "channelViewModelFactory\s*=|messagesViewModelFactoryProvider\s*=" --type kotlin
rg "allowUIAutomationTest|messageComposerTheme|reactionOptionsTheme|channelOptionsTheme" --type kotlin

# §5.12 — ChannelInfoViewEvent changes
rg "HideChannelError|UnhideChannelError|stream_ui_channel_info_hide" --type kotlin

# §5.13 — Deleted message visibility
rg "DeletedMessageVisibility|deletedMessageVisibility" --type kotlin

# §6 — Removed models
rg "PollConfig\b|AgoraChannel\b|HMSRoom\b|VideoCallInfo\b|VideoCallToken\b" --type kotlin
```

If a command returns no matches, skip the corresponding section.

---

## §2 — Prerequisites

### Step 0: Update Kotlin version

v7 requires **Kotlin 2.2.0**. Update your root `build.gradle.kts`:

```kotlin
plugins {
    id("org.jetbrains.kotlin.android") version "2.2.0" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.2.0" apply false
}
```

If using a version catalog, update `kotlin = "2.2.0"` in `libs.versions.toml`.

### Step 1: Update dependency versions

In your `build.gradle.kts` (or `build.gradle`):

```kotlin
// REMOVE these — they no longer exist in v7
implementation("io.getstream:stream-chat-android-offline:$version")       // REMOVE
implementation("io.getstream:stream-chat-android-state:$version")         // REMOVE
implementation("io.getstream:stream-chat-android-ui-utils:$version")      // REMOVE
implementation("io.getstream:stream-chat-android-ai-assistant:$version")  // REMOVE

// UPDATE version to 7.0.0
implementation("io.getstream:stream-chat-android-compose:7.0.0")
// The client module is included transitively via compose.
// Only add it explicitly if you use it without a UI module:
// implementation("io.getstream:stream-chat-android-client:7.0.0")
```

### Module merge map

| Removed Module | Merged Into |
|---|---|
| `stream-chat-android-offline` | `stream-chat-android-client` |
| `stream-chat-android-state` | `stream-chat-android-client` |
| `stream-chat-android-ui-utils` | `stream-chat-android-ui-common` |
| `stream-chat-android-ai-assistant` | Removed (use separate `stream-chat-android-ai` repo) |

### Step 2: Remove stale imports

After updating deps, do a project-wide search for these package prefixes and delete the import lines (they will be re-added in §4 with correct v7 paths):

```
io.getstream.chat.android.offline.
io.getstream.chat.android.state.
io.getstream.chat.android.uiutils.
```

Do NOT try to build yet. Continue to §3.

---

## §3 — Big Structural Migrations

### §3.1 — Plugin System → ChatClientConfig

**v6 pattern (remove):**
```kotlin
import io.getstream.chat.android.offline.plugin.factory.StreamOfflinePluginFactory
import io.getstream.chat.android.state.plugin.config.StatePluginConfig
import io.getstream.chat.android.state.plugin.factory.StreamStatePluginFactory

val offlinePlugin = StreamOfflinePluginFactory(context)
val statePluginFactory = StreamStatePluginFactory(
    config = StatePluginConfig(
        backgroundSyncEnabled = false,
        userPresence = true,
    ),
    appContext = context,
)

ChatClient.Builder(apiKey, context)
    .withPlugins(offlinePlugin, statePluginFactory)
    .build()
```

**v7 pattern (replace with):**
```kotlin
import io.getstream.chat.android.client.api.ChatClientConfig

ChatClient.Builder(apiKey, context)
    .config(ChatClientConfig(userPresence = true))
    .build()
```

#### ChatClientConfig mapping

| v6 (StatePluginConfig / OfflinePluginFactory) | v7 (ChatClientConfig) | Default |
|---|---|---|
| `StatePluginConfig.userPresence` | `userPresence` | `true` |
| `StreamOfflinePluginFactory` present in `withPlugins()` | `offlineEnabled` | `true` |
| `StreamOfflinePluginFactory.ignoredOfflineChannelTypes` | `ignoredOfflineChannelTypes` | `emptySet()` |
| `StatePluginConfig.isAutomaticSyncOnReconnectEnabled` | `isAutomaticSyncOnReconnectEnabled` | `true` |
| _(none)_ | `useLegacyChannelLogic` | `false` |
| `StatePluginConfig.syncMaxThreshold` | `syncMaxThreshold` | `TimeDuration.hours(12)` |
| `StatePluginConfig.messageLimitConfig` | `messageLimitConfig` | `MessageLimitConfig()` |

> **Note:** v6's `backgroundSyncEnabled` (which triggered `/sync` after push notifications) was deprecated in v6 and **removed in v7**. It does NOT map to `isAutomaticSyncOnReconnectEnabled` — that controls `/sync` + channel re-watch after a WebSocket reconnect, which is a different mechanism.

#### Removed classes

Delete all references to:
- `StreamOfflinePluginFactory`
- `StreamStatePluginFactory`
- `StatePluginConfig`
- `PluginFactory` (interface)
- `Plugin` (interface)
- `ChatClient.Builder.withPlugins()`
- `ChatClient.Builder.withRepositoryFactoryProvider()`
- `RepositoryFactory`, `RepositoryFactory.Provider`

### §3.2 — Theming: StreamDesign

#### Colors

```kotlin
// v6
val colors = StreamColors.defaultColors()
ChatTheme(colors = colors) { ... }

// v7
val colors = StreamDesign.Colors.default()        // light
val darkColors = StreamDesign.Colors.defaultDark() // dark
ChatTheme(colors = colors) { ... }
```

**Color property renames:**

| v6 | v7 |
|---|---|
| `primaryAccent` | `accentPrimary` |
| `errorAccent` | `accentError` |
| `textHighEmphasis` | `textPrimary` |
| `textLowEmphasis` | `textSecondary` |
| `disabled` | `textDisabled` |
| `barsBackground` | `backgroundCoreSurfaceDefault` |
| `appBackground` | `backgroundCoreApp` |
| `borders` | `borderCoreDefault` |

> `inputBackground`, `ownMessagesBackground`, `otherMessagesBackground` no longer map to single tokens. Customize via `brand` and `chrome` color scales on `StreamDesign.Colors`.

#### Typography

```kotlin
// v6
val typography = StreamTypography.defaultTypography(fontFamily = myFont)

// v7
val typography = StreamDesign.Typography.default(fontFamily = myFont)
```

**Typography renames:**

| v6 | v7 |
|---|---|
| `title1` | `headingLarge` |
| `title3` | `headingMedium` |
| `title3Bold` | `headingSmall` |
| `body` | `bodyDefault` |
| `bodyBold` | `bodyEmphasis` |
| `footnote` | `captionDefault` |
| `footnoteBold` | `captionEmphasis` |
| `captionBold` | `metadataEmphasis` |

#### Removed theme classes

Delete all references to:
- `StreamDimens` → internal `StreamTokens`
- `StreamShapes` → internal `StreamTokens.radius*`
- `StreamRippleConfiguration`
- `AttachmentPickerTheme`
- `MessageComposerTheme`
- `MessageOptionsTheme`
- `ChannelOptionsTheme`
- `ComposerActionsTheme`, `ComposerInputFieldTheme`, `ComposerLinkPreviewTheme`
- `MessageDateSeparatorTheme`
- `ComponentOffset`, `ComponentPadding`, `ComponentSize`
- `IconStyle`, `IconContainerStyle`
- `MessageBackgroundShapes`

#### CompositionLocals

```kotlin
// v6
LocalStreamColors.current
LocalStreamDimens.current
LocalStreamTypography.current
LocalStreamShapes.current

// v7 — use ChatTheme object instead
ChatTheme.colors
ChatTheme.typography
ChatTheme.config
ChatTheme.componentFactory
ChatTheme.reactionResolver
```

### §3.3 — ChatTheme Parameter Changes

**v7 ChatTheme signature:**
```kotlin
ChatTheme(
    isInDarkMode = isSystemInDarkTheme(),
    colors = StreamDesign.Colors.default(),
    typography = StreamDesign.Typography.default(),
    config = ChatUiConfig(),
    componentFactory = object : ChatComponentFactory {},
    reactionResolver = ReactionResolver.defaultResolver(),
) { content() }
```

| Removed Parameter | Replacement |
|---|---|
| `dimens` | Removed (internal `StreamTokens`) |
| `shapes` | Removed (internal `StreamTokens`) |
| `reactionIconFactory` | `reactionResolver` |
| `messageContentFactory` | `componentFactory` (ChatComponentFactory) |
| `autoTranslationEnabled` | `config.translation.enabled` |
| `isComposerLinkPreviewEnabled` | `config.composer.linkPreviewEnabled` |
| `videoThumbnailsEnabled` | `config.messageList.videoThumbnailsEnabled` |
| `readCountEnabled` | Removed |
| `userPresence` | Removed (online status always shown) |
| `ownMessageTheme`, `otherMessageTheme` | Removed |
| `messageDateSeparatorTheme`, `messageUnreadSeparatorTheme` | Removed |
| `attachmentPickerTheme` | Removed |
| `attachmentsPickerTabFactories` | `config.attachmentPicker.modes` |

### §3.4 — ChatUiConfig

Feature flags scattered across `ChatTheme` params are now in `ChatUiConfig`:

```kotlin
ChatTheme(
    config = ChatUiConfig(
        translation = TranslationConfig(enabled = true),
        messageList = MessageListConfig(videoThumbnailsEnabled = true),
        composer = ComposerConfig(
            audioRecordingEnabled = true,
            linkPreviewEnabled = false,
        ),
        channelList = ChannelListConfig(swipeActionsEnabled = true),
        attachmentPicker = AttachmentPickerConfig(
            useSystemPicker = true,
            modes = listOf(
                GalleryPickerMode(),
                FilePickerMode(),
                CameraPickerMode(),
                PollPickerMode(),
                CommandPickerMode,
            ),
        ),
    ),
) { content() }
```

#### Changed defaults (v6 → v7)

These features **now default to `true`** (were `false` in v6):
- `TranslationConfig.enabled`
- `TranslationConfig.showOriginalEnabled`
- `ChatUI.autoTranslationEnabled`
- `ComposerConfig.audioRecordingEnabled`
- `ChatUI.draftMessagesEnabled`

Additionally, `ComposerConfig.audioRecordingSendOnComplete` now defaults to `false`.

If you relied on these being disabled, explicitly set them to `false`.

### §3.5 — ChatComponentFactory

`ChatComponentFactory` replaces `MessageContentFactory`, composable slot parameters (`itemContent`, `loadingContent`, `emptyContent`, `channelContent`, etc.), and factory classes.

```kotlin
// v6
ChatTheme(messageContentFactory = MyContentFactory()) { ... }

// v7
ChatTheme(componentFactory = MyComponentFactory()) { ... }
```

**v6 slot-based customization → v7 factory override:**
```kotlin
// v6
ChannelList(
    channelContent = { channelItem ->
        Row {
            ChannelAvatar(...)
            Text(channelName)
        }
    }
)

// v7
object MyFactory : ChatComponentFactory {
    @Composable
    override fun LazyItemScope.ChannelListItemContent(params: ChannelListItemContentParams) {
        Row {
            ChannelAvatar(channel = params.channelItem.channel, currentUser = params.currentUser)
            Text(ChatTheme.channelNameFormatter.formatChannelName(params.channelItem.channel, params.currentUser))
        }
    }
}

ChatTheme(componentFactory = MyFactory) {
    ChannelList(viewModel = listViewModel)
}
```

Implement the interface and override only the methods you need — all methods have default implementations.

---

## §4 — Package Relocations

Apply these find-and-replace operations **in order** (most specific first to avoid partial matches):

### State module → Client module

```text
io.getstream.chat.android.state.plugin.config.StatePluginConfig → io.getstream.chat.android.client.api.ChatClientConfig
io.getstream.chat.android.state.plugin.state.global → io.getstream.chat.android.client.api.state
io.getstream.chat.android.state.plugin.state.querychannels → io.getstream.chat.android.client.api.state
io.getstream.chat.android.state.plugin.state.querythreads → io.getstream.chat.android.client.api.state
io.getstream.chat.android.state.plugin.state.channel.thread → io.getstream.chat.android.client.api.state
io.getstream.chat.android.state.plugin.state.channel → io.getstream.chat.android.client.channel.state
io.getstream.chat.android.state.plugin.state → io.getstream.chat.android.client.api.state
io.getstream.chat.android.state.event.handler.chat.factory → io.getstream.chat.android.client.api.event
io.getstream.chat.android.state.event.handler.chat → io.getstream.chat.android.client.api.event
io.getstream.chat.android.state.extensions → io.getstream.chat.android.client.api.state
```

### UI-Utils module → UI-Common module

```text
io.getstream.chat.android.uiutils.model → io.getstream.chat.android.ui.common.model
io.getstream.chat.android.uiutils.util → io.getstream.chat.android.ui.common.utils
io.getstream.chat.android.uiutils.extension → io.getstream.chat.android.ui.common.utils.extensions
io.getstream.chat.android.uiutils → io.getstream.chat.android.ui.common
```

### Specific class relocations

| v6 Class | v7 Class |
|---|---|
| `StatePluginConfig` | `ChatClientConfig` (different class — see §3.1) |
| `GlobalState` | same name, new package: `io.getstream.chat.android.client.api.state.GlobalState` |
| `StateRegistry` | same name, new package: `io.getstream.chat.android.client.api.state.StateRegistry` |
| `QueryChannelsState` | same name, new package: `io.getstream.chat.android.client.api.state.QueryChannelsState` |
| `QueryThreadsState` | same name, new package: `io.getstream.chat.android.client.api.state.QueryThreadsState` |
| `ThreadState` | same name, new package: `io.getstream.chat.android.client.api.state.ThreadState` |
| `ChannelState` | same name, new package: `io.getstream.chat.android.client.channel.state.ChannelState` |
| `ChannelsStateData` | same name, new package: `io.getstream.chat.android.client.api.state.ChannelsStateData` |
| `ChatEventHandler` | same name, new package: `io.getstream.chat.android.client.api.event.ChatEventHandler` |
| `ChatEventHandlerFactory` | same name, new package: `io.getstream.chat.android.client.api.event.ChatEventHandlerFactory` |
| `EventHandlingResult` | same name, new package: `io.getstream.chat.android.client.api.event.EventHandlingResult` |
| `BaseChatEventHandler` | same name, new package: `io.getstream.chat.android.client.api.event.BaseChatEventHandler` |
| `DefaultChatEventHandler` | same name, new package: `io.getstream.chat.android.client.api.event.DefaultChatEventHandler` |
| `MimeType` | same name, new package: `io.getstream.chat.android.ui.common.model.MimeType` |
| `EmojiUtil` | same name, new package: `io.getstream.chat.android.ui.common.utils.EmojiUtil` |
| `ColorUtils` | renamed to `ColorUtil`: `io.getstream.chat.android.ui.common.utils.ColorUtil` (now `@InternalStreamChatApi`) |
| `IntentUtils` | renamed to `ContextUtils`: `io.getstream.chat.android.ui.common.utils.ContextUtils` (now `@InternalStreamChatApi`) |

---

## §5 — Rename Lists

### §5.1 — Screen & ViewModel renames

| v6 | v7 |
|---|---|
| `MessagesScreen` | `ChannelScreen` |
| `MessageListHeader` | `ChannelHeader` |
| `MessagesViewModelFactory` (Compose, messages) | `ChannelViewModelFactory` (in `viewmodel.messages` package) |
| `MessageListViewModelFactory` (XML) | `ChannelViewModelFactory` (in `viewmodel.messages` package) |
| `ChannelViewModelFactory` (Compose, channel list — in `viewmodel.channels`) | `ChannelListViewModelFactory` |

> **TRAP:** v6 had `ChannelViewModelFactory` in the `viewmodel.channels` package for the channel list. In v7, this was renamed to `ChannelListViewModelFactory`. Meanwhile, the *messages* factory (`MessagesViewModelFactory`) was renamed TO `ChannelViewModelFactory` (in `viewmodel.messages`). Do NOT confuse the two. Check the import package to determine which one you have.

### §5.2 — Attachment picker renames

| v6 | v7 |
|---|---|
| `AttachmentsPicker` | `AttachmentPicker` |
| `AttachmentsPickerMode` (sealed class) | `AttachmentPickerMode` (interface) |
| `AttachmentsPickerMode.Images` | `GalleryPickerMode()` |
| `AttachmentsPickerMode.Files` | `FilePickerMode()` |
| `AttachmentsPickerMode.MediaCapture` | `CameraPickerMode()` |
| `AttachmentsPickerMode.Poll(...)` | `PollPickerMode(...)` |

### §5.3 — Reaction renames

| v6 | v7 |
|---|---|
| `ReactionIconFactory` | `ReactionResolver` |
| `ReactionPushEmojiFactory` | `ReactionResolver` |
| `ReactionOptionItemState(painter: Painter)` | `ReactionOptionItemState(emojiCode: String?)` |
| `UserReactionItemState(painter: Painter)` | `UserReactionItemState(emojiCode: String?)` |
| `SelectedReactionsMenu` | `ReactionsMenu` |

### §5.4 — Channel action renames

| v6 | v7 |
|---|---|
| `ChannelOptionState` | `ChannelAction` (interface, self-describing) |
| `ChannelOptionItemVisibility` | `ChannelOptionsVisibility` |
| `MessageOptionItemVisibility` | `MessageActionsOptionsVisibility` |

### §5.5 — Avatar renames

| v6 | v7 |
|---|---|
| `GroupAvatar` | `ChannelAvatar` (handles groups internally) |
| `ImageAvatar` | `Avatar` (internal) |
| `InitialsAvatar` | `UserAvatarPlaceholder` (internal) |
| `UserAvatarRow` | `UserAvatarStack` (internal) |

`UserAvatar` and `ChannelAvatar` remain the public API.

### §5.6 — Composer renames

| v6 | v7 |
|---|---|
| `MentionSuggestionItem` | `UserSuggestionItem` (internal) |
| `MentionSuggestionList` | `UserSuggestionList` (internal) |
| `SuggestionList` | `SuggestionsMenu` (internal) |
| `CancelIcon` | `ComposerCancelIcon` |

### §5.7 — Other renames

| v6 | v7 |
|---|---|
| `DefaultQueryFilter` | `DefaultUserQueryFilter` |
| `UserPresence` composable | Removed (online status always shown) |
| `MentionStyleFactory` | Removed |
| `ClipboardHandler` / `ClipboardHandlerImpl` | Removed from public API |
| `PollConfig` (model) | `CreatePollParams` |
| `MessageMenu` (composable) | `MessageActions` |
| `MessageItemCenterContent` | `MessageContent` |
| `MessageItemHeaderContent` | `MessageTop` |
| `MessageItemFooterContent` | `MessageFooterContent` |
| `SelectedMessageMenu` | Removed (use `MessageActions` via `ChatComponentFactory`) |
| `mirrorRtl` (Modifier extension) | Removed — use `Modifier.then(if (isRtl) Modifier.scale(-1f, 1f) else Modifier)` |
| `Avatar` (composable) | Internal — use `UserAvatar` or `ChannelAvatar` instead |

### §5.7a — ChatComponentFactory method renames

All `ChatComponentFactory` override methods changed from individual parameters to `*Params` data classes. If you override any factory method, update the signature:

| v6 Method Signature | v7 Method Signature |
|---|---|
| `ChannelListItemContent(channelItem, currentUser, onChannelClick, onChannelLongClick)` | `LazyItemScope.ChannelListItemContent(params: ChannelListItemContentParams)` |
| `MessageMenu(modifier, message, messageOptions, ...)` | `MessageActions(params: MessageActionsParams)` |
| `MessageItemCenterContent(message, ...)` | `MessageContent(params: MessageContentParams)` |
| `MessageItemHeaderContent(message, ...)` | `MessageTop(params: MessageTopParams)` |
| `MessageItemFooterContent(message, ...)` | `MessageFooterContent(params: MessageFooterContentParams)` |
| `MessageListEmptyContent(modifier)` | `MessageListEmptyContent(params: MessageListEmptyContentParams)` |
| `DirectChannelInfoTopBar(...)` | `DirectChannelInfoTopBar(params: DirectChannelInfoTopBarParams)` |
| `GroupChannelInfoTopBar(...)` | `GroupChannelInfoTopBar(params: GroupChannelInfoTopBarParams)` |

> **Pattern:** Every `ChatComponentFactory` method now takes a single `*Params` data class. Access the old individual parameters via the params object (e.g., `params.message`, `params.modifier`, `params.channelItem`).

### §5.7b — ViewModel factory parameter removals

These parameters were removed from `ChannelViewModelFactory` (messages) and `ChannelListViewModelFactory`:

| Factory | Removed Parameter | Notes |
|---|---|---|
| `ChannelViewModelFactory` | `deletedMessageVisibility` | Always visible now |
| `ChannelViewModelFactory` | `autoTranslationEnabled` | Now in `ChatUiConfig` |
| `ChannelViewModelFactory` | `isComposerLinkPreviewEnabled` | Now in `ChatUiConfig` |
| `ChannelListViewModelFactory` | `isDraftMessageEnabled` | Drafts always enabled in v7 |

### §5.7c — Other removed APIs

| Removed | Notes |
|---|---|
| `ChatClient.config.apiKey` | Now internal — if you need the API key, store it yourself before passing to `ChatClient.Builder` |
| `MessageComposer` `integrations` parameter | Removed — use `onAttachmentsClick` callback instead of a custom integrations composable |
| `MessageOptionItemState` `titleColor`/`iconColor` params | Removed — `destructive: Boolean` is now a **required** constructor parameter (not optional). Set `true` for delete/flag actions, `false` for others |
| `openSystemSettings()` | Now `@InternalStreamChatApi` — copy the implementation or use `context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null)))` |

### §5.8 — Drawable resource renames

All `stream_compose_ic_*` drawables are renamed to `stream_design_ic_*` in v7. Apply a bulk rename:

```bash
# Find all references
rg "stream_compose_ic_" --type kotlin --type xml

# Bulk rename (portable — works on macOS and Linux)
find . \( -name "*.kt" -o -name "*.xml" \) -print0 | xargs -0 perl -i -pe 's/stream_compose_ic_/stream_design_ic_/g'
```

**Key renames (not 1:1 — name also changed):**

| v6 | v7 |
|---|---|
| `stream_compose_ic_arrow_back` | `stream_design_ic_arrow_left` |
| `stream_compose_ic_attachments` | `stream_design_ic_attachment` |
| `stream_compose_ic_close` | `stream_design_ic_xmark` |
| `stream_compose_ic_clear` | `stream_design_ic_xmark` |
| `stream_compose_ic_error` | `stream_design_ic_exclamation_circle` |
| `stream_compose_ic_check_circle` | `stream_design_ic_checkmark` |
| `stream_compose_ic_mute` | `stream_design_ic_bell_slash` |
| `stream_compose_ic_mentions` | `stream_design_ic_mention` (singular!) |
| `stream_compose_ic_mention` | `stream_design_ic_at` |
| `stream_compose_ic_new_chat` | Removed — use your own drawable or Material icon |
| `stream_compose_ic_person` | Removed — use `stream_design_ic_account` or Material icon |
| `stream_compose_ic_cancel` | `stream_design_ic_xmark` |

> After bulk rename, verify each drawable exists. Run: `rg "stream_design_ic_" --type kotlin -o | sort -u` and check against files in the SDK's `res/drawable/` directory. For drawables that no longer exist, replace with your own or use Material icons.

### §5.9 — Removed color properties

These `ChatTheme.colors.*` properties are removed with no direct replacement:

| Removed | Guidance |
|---|---|
| `highlight` | Use `accentPrimary` with alpha, or a custom color |
| `overlayDark` | Use `backgroundCoreScrim` |
| `inputBackground` | Derived internally from color scales — customize via `StreamDesign.Colors` brand/chrome scales |
| `infoAccent` | Use `accentPrimary` or a custom color |
| `ownMessagesBackground` | Derived internally |
| `otherMessagesBackground` | Derived internally |

### §5.10 — Removed StreamDimens/StreamShapes properties

`ChatTheme.dimens.*` and `ChatTheme.shapes.*` are entirely removed. Replace with hardcoded values:

| Removed | Replacement |
|---|---|
| `ChatTheme.dimens.messageItemMaxWidth` | Use a hardcoded `Dp` value (e.g., `300.dp`) |
| `ChatTheme.dimens.attachmentsPickerHeight` | Hardcoded value or `Modifier.fillMaxHeight(0.5f)` |
| `ChatTheme.dimens.headerElevation` | `0.dp` (v7 headers have no elevation) |
| `ChatTheme.shapes.bottomSheet` | `RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)` |
| `ChatTheme.shapes.attachment` | `RoundedCornerShape(12.dp)` |
| `ChatTheme.shapes.avatar` | `CircleShape` |
| `ChatTheme.shapes.inputField` | `RoundedCornerShape(24.dp)` |

### §5.11 — Component parameter changes

These composable parameters were removed or renamed in v7:

#### ChannelList
| Removed Parameter | Replacement |
|---|---|
| `channelContent` | Override `ChatComponentFactory.ChannelListItemContent(params)` |
| `divider` | Removed — no dividers in v7 channel list |

#### SelectedChannelMenu
| Removed Parameter | Replacement |
|---|---|
| `isMuted` | Removed (actions are self-describing) |
| `onChannelOptionClick` | `onChannelOptionConfirm: (ChannelAction) -> Unit` |
| _(new required)_ | `channelActions: List<ChannelAction>` — use `buildDefaultChannelActions()` |

```kotlin
// v7 SelectedChannelMenu
import io.getstream.chat.android.compose.ui.components.channels.buildDefaultChannelActions

SelectedChannelMenu(
    selectedChannel = channel,
    currentUser = user,
    channelActions = buildDefaultChannelActions(
        channel = channel,
        isMuted = isMuted,
        ownCapabilities = channel.ownCapabilities,
        viewModel = listViewModel,
    ),
    onChannelOptionConfirm = { action -> /* handle action */ },
    onDismiss = { listViewModel.dismissChannelAction() },
)
```

> `buildDefaultChannelActions` is a composable helper that returns the standard list of channel actions. Import it from `io.getstream.chat.android.compose.ui.components.channels`.

#### UserAvatar
| Removed Parameter | Replacement |
|---|---|
| `showOnlineIndicator` | `showIndicator` |
| `onClick` | Wrap in `Modifier.clickable { }` |
| `textStyle` | Removed (internal styling) |

#### ChannelInfoViewModelFactory
| Removed Parameter | Replacement |
|---|---|
| `context` | Removed — no longer needed |

```kotlin
// v6
ChannelInfoViewModelFactory(context = context, cid = cid)
// v7
ChannelInfoViewModelFactory(cid = cid)
```

#### GroupChannelInfoScreen
| Removed Parameter | Replacement |
|---|---|
| `onAddMembersClick` | Removed — handled internally with navigation |

#### DirectChannelInfoScreen / GroupChannelInfoScreen
Both now take `(viewModelFactory, modifier, onNavigationIconClick)`. GroupChannelInfoScreen also takes `currentUser`.

#### ChatsScreen (adaptive layout)
| v6 Parameter | v7 Parameter |
|---|---|
| `channelViewModelFactory` | `channelListViewModelFactory: ChannelListViewModelFactory` |
| `messagesViewModelFactoryProvider` | `channelViewModelFactoryProvider: ChannelViewModelFactoryProvider` |

#### ChannelScreen (was MessagesScreen)
| Removed Parameter | Notes |
|---|---|
| `reactionSorting` | Moved to `ChatUiConfig.messageList.reactionSorting` |
| `onMessageLinkClick` | Removed — override via `ChatComponentFactory` |
| `onUserAvatarClick` | Removed — override via `ChatComponentFactory` |
| `onUserMentionClick` | Removed — override via `ChatComponentFactory` |
| `showDateSeparators` | Removed |
| `showSystemMessages` | Removed |
| Poll-related callbacks (`onPollUpdated`, etc.) | Removed — override via `ChatComponentFactory` |

#### ChannelList
| Removed Parameter | Notes |
|---|---|
| `itemContent` / `channelContent` | Override `ChatComponentFactory.ChannelListItemContent(params)` |
| `loadingContent` | Override `ChatComponentFactory.ChannelListLoadingContent(params)` |
| `emptyContent` | Override `ChatComponentFactory.ChannelListEmptyContent(params)` |
| `divider` | Removed — no dividers in v7 |

#### MessageList
| Removed Parameter | Notes |
|---|---|
| `itemContent` | Override `ChatComponentFactory.MessageContent(params)` |
| `messageContent` | Override `ChatComponentFactory.MessageContent(params)` |
| `emptyContent` | Override `ChatComponentFactory.MessageListEmptyContent(params)` |
| `loadingContent` | Override `ChatComponentFactory.MessageListLoadingContent(params)` |

#### ChannelListHeader / ChannelHeader
| Removed Parameter | Notes |
|---|---|
| `color` | Removed — uses theme colors |
| `shape` | Removed — uses theme tokens |
| `elevation` | Removed — v7 headers have no elevation |

> **Lambda customization pattern in v7:** All composable slot parameters (`itemContent`, `loadingContent`, `emptyContent`, etc.) and forwarded callbacks (`onMessageLinkClick`, `onUserAvatarClick`, etc.) have been removed from public composables. Customization now goes through `ChatComponentFactory`. See the [Lambda Customization via ChatComponentFactory](https://getstream.io/chat/docs/sdk/android/migration-guides/migrating-from-v6-to-v7/#lambda-customization-via-chatcomponentfactory) documentation for the full migration pattern.

#### ChatTheme (additional removed params)
| Removed Parameter | Notes |
|---|---|
| `allowUIAutomationTest` | Removed |
| `messageComposerTheme` | Removed |
| `reactionOptionsTheme` | Removed |
| `messageOptionsTheme` | Removed |
| `channelOptionsTheme` | Removed |

### §5.12 — ChannelInfoViewEvent changes

The `ChannelInfoViewEvent` sealed hierarchy changed significantly:

**Removed events:**
- `HideChannelError` → removed (hide channel feature removed)
- `UnhideChannelError` → removed

**Added events (handle in `when` expressions):**
- `AddMembersError`
- `BlockUserError`
- `MuteUserError`
- `UnblockUserError`
- `UnmuteUserError`

**Action events (added):**
- `BlockUser(member)`
- `MuteUser(member)`
- `UnblockUser(member)`
- `UnmuteUser(member)`

> When you have `when` expressions over `ChannelInfoViewEvent` or `ChannelAction`, add an `else` branch or add the new cases. The compiler will flag exhaustive `when` errors.

### §5.13 — Removed string resources

| Removed | Notes |
|---|---|
| `stream_ui_channel_info_hide_conversation_error` | Hide channel removed |
| `stream_ui_channel_info_hide_group_error` | Hide channel removed |
| `stream_compose_grant_permission` | Permission UI redesigned |

Replace references with your own strings or remove the error handling code.

---

## §6 — Removed with Structural Replacement

### §6.1 — Attachment factory system

The entire `AttachmentFactory` class hierarchy is removed:
- `AttachmentFactory`, `AttachmentFactory.Type`, `StreamAttachmentFactories`
- All individual factories: `AudioRecordAttachmentFactory`, `FileAttachmentFactory`, `GiphyAttachmentFactory`, `LinkAttachmentFactory`, `MediaAttachmentFactory`, `QuotedAttachmentFactory`, `UnsupportedAttachmentFactory`, `UploadAttachmentFactory`
- All `AttachmentsPickerTabFactory` implementations

**Replacement:** Override the corresponding methods in `ChatComponentFactory`:
- `AttachmentPicker()`, `AttachmentMediaPicker()`, `AttachmentFilePicker()`, `AttachmentCameraPicker()`, `AttachmentPollPicker()`, `AttachmentCommandPicker()`, `AttachmentSystemPicker()`

### §6.2 — CDN / Headers system

All removed:
- `ImageHeadersProvider`, `AsyncImageHeadersProvider`, `VideoHeadersProvider`
- `DownloadAttachmentUriGenerator`, `DefaultDownloadAttachmentUriGenerator`
- `DownloadRequestInterceptor`
- `ImageAssetTransformer`, `DefaultImageAssetTransformer`
- `AttachmentDocumentActivity`
- `useDocumentGView` (on `ChatUI` and `ChatTheme`)
- `ChatClient.Builder.shareFileDownloadRequestInterceptor()`

**Replacement:** Use the unified `CDN` interface on `ChatClient.Builder`:

```kotlin
ChatClient.Builder("apiKey", context)
    .cdn(object : CDN {
        override suspend fun imageRequest(url: String) = CDNRequest(
            url = signingService.sign(url),
            headers = mapOf("Authorization" to "Bearer $token"),
        )
        override suspend fun fileRequest(url: String) = CDNRequest(
            url = signingService.sign(url),
            headers = mapOf("Authorization" to "Bearer $token"),
        )
    })
    .build()
```

### §6.3 — ChannelOptionState → ChannelAction

`ChannelOptionState(title, iconPainter, action)` is removed. Replace with `ChannelAction` interface implementations:

Built-in actions: `ViewInfo`, `MuteChannel`, `UnmuteChannel`, `PinChannel`, `UnpinChannel`, `ArchiveChannel`, `UnarchiveChannel`, `LeaveGroup`, `DeleteConversation`, `MuteUser`, `UnmuteUser`, `BlockUser`, `UnblockUser`.

```kotlin
// v6
val options = listOf(
    ChannelOptionState(
        title = R.string.mute,
        iconPainter = painterResource(R.drawable.ic_mute),
        action = Mute(channel),
    ),
)

// v7
val actions = listOf(
    MuteChannel(
        channel = channel,
        label = stringResource(R.string.mute),
        onAction = { viewModel.muteChannel(channel) },
    ),
)
```

### §6.4 — DeletedMessageVisibility

`DeletedMessageVisibility` enum and the `deletedMessageVisibility` parameter on ViewModel factories are removed. Deleted messages are now always visible as placeholders. Remove the parameter from any factory constructor call.

### §6.5 — Removed models

| Removed | Notes |
|---|---|
| `AgoraChannel` | Video call integration removed |
| `HMSRoom` | Video call integration removed |
| `VideoCallInfo` | Video call integration removed |
| `VideoCallToken` | Video call integration removed |
| `PollConfig` | Use `CreatePollParams` |
| `AttachmentType.IMGUR` | Removed constant |
| `AttachmentType.LINK` | Removed constant |
| `AttachmentType.PRODUCT` | Removed constant |

---

## §7 — Behavior Changes

These changes do NOT cause compile errors but change runtime behavior:

### §7.1 — Active-window channel state

v6 eagerly loaded all messages between a jumped-to message and the latest (gap-filling). v7 loads only the requested page (active-window model).

**Impact:** If you use `loadMessagesAroundId` with a custom UI and send a message while viewing a non-latest page, the message won't appear until you load the latest page.

**Fix:** Before sending, check and load:
```kotlin
if (!endOfNewerMessages) {
    chatClient.loadNewestMessages(cid, messageLimit = 30).enqueue()
}
chatClient.sendMessage(channelType, channelId, message).enqueue()
```

**Opt out:** `ChatClientConfig(useLegacyChannelLogic = true)` restores v6 behavior (deprecated, may be removed).

> If you use `ChannelScreen` / Stream UI components, no action needed — they handle this automatically.

### §7.2 — Default feature flags changed

See also §3.4 for the `ChatUiConfig` migration. Summary of changed defaults:

| Feature | v6 Default | v7 Default |
|---|---|---|
| Auto-translation | `false` | `true` |
| Show original translation | `false` | `true` |
| Audio recording enabled | `false` | `true` |
| Audio recording send-on-complete | `true` | `false` |
| Draft messages | `false` | `true` |

### §7.3 — Poll.maxVotesAllowed nullable

`Poll.maxVotesAllowed` changed from `Int` to `Int?`. Add null checks where used.

### §7.4 — Typing indicator moved inline

The typing indicator is now rendered as an inline item in the message list, not a separate overlay. Remove any manual `TypingIndicator` component usage.

---

## §8 — Machine-Readable Rename Block

```json
{
  "dependencies": {
    "remove": [
      "io.getstream:stream-chat-android-offline",
      "io.getstream:stream-chat-android-state",
      "io.getstream:stream-chat-android-ui-utils",
      "io.getstream:stream-chat-android-ai-assistant"
    ],
    "update": {
      "io.getstream:stream-chat-android-compose": "7.0.0",
      "io.getstream:stream-chat-android-client": "7.0.0",
      "io.getstream:stream-chat-android-ui-common": "7.0.0",
      "io.getstream:stream-chat-android-ui-components": "7.0.0"
    }
  },
  "kotlinVersion": "2.2.0",
  "classRenames": {
    "StreamColors": "StreamDesign.Colors",
    "StreamTypography": "StreamDesign.Typography",
    "StreamDimens": null,
    "StreamShapes": null,
    "MessagesScreen": "ChannelScreen",
    "MessageListHeader": "ChannelHeader",
    "MessagesViewModelFactory": "ChannelViewModelFactory (viewmodel.messages)",
    "MessageListViewModelFactory": "ChannelViewModelFactory (viewmodel.messages)",
    "ChannelViewModelFactory (viewmodel.channels)": "ChannelListViewModelFactory",
    "AttachmentsPicker": "AttachmentPicker",
    "ReactionIconFactory": "ReactionResolver",
    "ReactionPushEmojiFactory": "ReactionResolver",
    "SelectedReactionsMenu": "ReactionsMenu",
    "ChannelOptionState": null,
    "ChannelOptionItemVisibility": "ChannelOptionsVisibility",
    "MessageOptionItemVisibility": "MessageActionsOptionsVisibility",
    "GroupAvatar": "ChannelAvatar",
    "ImageAvatar": null,
    "InitialsAvatar": null,
    "UserAvatarRow": null,
    "MentionSuggestionItem": "UserSuggestionItem",
    "MentionSuggestionList": "UserSuggestionList",
    "CancelIcon": "ComposerCancelIcon",
    "DefaultQueryFilter": "DefaultUserQueryFilter",
    "PollConfig": "CreatePollParams",
    "StreamOfflinePluginFactory": null,
    "StreamStatePluginFactory": null,
    "StatePluginConfig": "ChatClientConfig",
    "MessageContentFactory": "ChatComponentFactory",
    "AttachmentFactory": null,
    "AttachmentsPickerTabFactory": null,
    "DeletedMessageVisibility": null,
    "ClipboardHandler": null,
    "ClipboardHandlerImpl": null,
    "MentionStyleFactory": null,
    "MessageMenu": "MessageActions",
    "MessageItemCenterContent": "MessageContent",
    "MessageItemHeaderContent": "MessageTop",
    "MessageItemFooterContent": "MessageFooterContent",
    "SelectedMessageMenu": null,
    "Avatar": null
  },
  "removedParameters": {
    "ChannelList.channelContent": "ChatComponentFactory.ChannelListItemContent",
    "ChannelList.divider": null,
    "SelectedChannelMenu.isMuted": null,
    "SelectedChannelMenu.onChannelOptionClick": "onChannelOptionConfirm",
    "UserAvatar.showOnlineIndicator": "showIndicator",
    "UserAvatar.onClick": "Modifier.clickable",
    "UserAvatar.textStyle": null,
    "ChannelInfoViewModelFactory.context": null,
    "GroupChannelInfoScreen.onAddMembersClick": null,
    "ChatsScreen.channelViewModelFactory": "channelListViewModelFactory",
    "ChatsScreen.messagesViewModelFactoryProvider": "channelViewModelFactoryProvider",
    "ChannelScreen.reactionSorting": null,
    "ChannelScreen.onMessageLinkClick": null,
    "ChatTheme.allowUIAutomationTest": null,
    "ChatTheme.messageComposerTheme": null,
    "ChatTheme.reactionOptionsTheme": null,
    "ChatTheme.messageOptionsTheme": null,
    "ChatTheme.channelOptionsTheme": null,
    "ChannelViewModelFactory.deletedMessageVisibility": null,
    "ChannelViewModelFactory.autoTranslationEnabled": "ChatUiConfig",
    "ChannelViewModelFactory.isComposerLinkPreviewEnabled": "ChatUiConfig",
    "ChannelListViewModelFactory.isDraftMessageEnabled": null,
    "MessageComposer.integrations": "onAttachmentsClick",
    "MessageOptionItemState.titleColor": null,
    "MessageOptionItemState.iconColor": null
  },
  "packageRelocations": {
    "io.getstream.chat.android.state.plugin.config": "io.getstream.chat.android.client.api",
    "io.getstream.chat.android.state.plugin.state.global": "io.getstream.chat.android.client.api.state",
    "io.getstream.chat.android.state.plugin.state.querychannels": "io.getstream.chat.android.client.api.state",
    "io.getstream.chat.android.state.plugin.state.querythreads": "io.getstream.chat.android.client.api.state",
    "io.getstream.chat.android.state.plugin.state.channel.thread": "io.getstream.chat.android.client.api.state",
    "io.getstream.chat.android.state.plugin.state.channel": "io.getstream.chat.android.client.channel.state",
    "io.getstream.chat.android.state.plugin.state": "io.getstream.chat.android.client.api.state",
    "io.getstream.chat.android.state.event.handler.chat.factory": "io.getstream.chat.android.client.api.event",
    "io.getstream.chat.android.state.event.handler.chat": "io.getstream.chat.android.client.api.event",
    "io.getstream.chat.android.state.extensions": "io.getstream.chat.android.client.api.state",
    "io.getstream.chat.android.uiutils.model": "io.getstream.chat.android.ui.common.model",
    "io.getstream.chat.android.uiutils.util": "io.getstream.chat.android.ui.common.utils",
    "io.getstream.chat.android.uiutils.extension": "io.getstream.chat.android.ui.common.utils.extensions",
    "io.getstream.chat.android.uiutils": "io.getstream.chat.android.ui.common"
  },
  "colorRenames": {
    "primaryAccent": "accentPrimary",
    "errorAccent": "accentError",
    "textHighEmphasis": "textPrimary",
    "textLowEmphasis": "textSecondary",
    "disabled": "textDisabled",
    "barsBackground": "backgroundCoreSurfaceDefault",
    "appBackground": "backgroundCoreApp",
    "borders": "borderCoreDefault",
    "highlight": null,
    "overlayDark": "backgroundCoreScrim",
    "inputBackground": null,
    "infoAccent": null,
    "ownMessagesBackground": null,
    "otherMessagesBackground": null
  },
  "drawableRenames": {
    "stream_compose_ic_": "stream_design_ic_",
    "stream_compose_ic_arrow_back": "stream_design_ic_arrow_left",
    "stream_compose_ic_attachments": "stream_design_ic_attachment",
    "stream_compose_ic_close": "stream_design_ic_xmark",
    "stream_compose_ic_clear": "stream_design_ic_xmark",
    "stream_compose_ic_error": "stream_design_ic_exclamation_circle",
    "stream_compose_ic_check_circle": "stream_design_ic_checkmark",
    "stream_compose_ic_mute": "stream_design_ic_bell_slash",
    "stream_compose_ic_mentions": "stream_design_ic_mention",
    "stream_compose_ic_mention": "stream_design_ic_at",
    "stream_compose_ic_cancel": "stream_design_ic_xmark",
    "stream_compose_ic_new_chat": null,
    "stream_compose_ic_person": null
  },
  "removedEvents": {
    "ChannelInfoViewEvent.HideChannelError": null,
    "ChannelInfoViewEvent.UnhideChannelError": null
  },
  "addedEvents": [
    "ChannelInfoViewEvent.AddMembersError",
    "ChannelInfoViewEvent.BlockUserError",
    "ChannelInfoViewEvent.MuteUserError",
    "ChannelInfoViewEvent.UnblockUserError",
    "ChannelInfoViewEvent.UnmuteUserError",
    "ChannelInfoViewEvent.BlockUser",
    "ChannelInfoViewEvent.MuteUser",
    "ChannelInfoViewEvent.UnblockUser",
    "ChannelInfoViewEvent.UnmuteUser"
  ],
  "typographyRenames": {
    "title1": "headingLarge",
    "title3": "headingMedium",
    "title3Bold": "headingSmall",
    "body": "bodyDefault",
    "bodyBold": "bodyEmphasis",
    "footnote": "captionDefault",
    "footnoteBold": "captionEmphasis",
    "captionBold": "metadataEmphasis"
  },
  "attachmentPickerModes": {
    "AttachmentsPickerMode.Images": "GalleryPickerMode()",
    "AttachmentsPickerMode.Files": "FilePickerMode()",
    "AttachmentsPickerMode.MediaCapture": "CameraPickerMode()",
    "AttachmentsPickerMode.Poll": "PollPickerMode()"
  },
  "chatThemeParamRenames": {
    "reactionIconFactory": "reactionResolver",
    "messageContentFactory": "componentFactory",
    "autoTranslationEnabled": "config.translation.enabled",
    "isComposerLinkPreviewEnabled": "config.composer.linkPreviewEnabled",
    "videoThumbnailsEnabled": "config.messageList.videoThumbnailsEnabled",
    "attachmentsPickerTabFactories": "config.attachmentPicker.modes",
    "dimens": null,
    "shapes": null,
    "readCountEnabled": null,
    "userPresence": null,
    "ownMessageTheme": null,
    "otherMessageTheme": null,
    "attachmentPickerTheme": null
  }
}
```

---

## §9 — Source Paths to Read

When this guide is insufficient, read these files from the v7 codebase (`develop` branch):

### Public API surface (ground truth for what exists)
```
stream-chat-android-client/api/stream-chat-android-client.api
stream-chat-android-compose/api/stream-chat-android-compose.api
stream-chat-android-ui-common/api/stream-chat-android-ui-common.api
stream-chat-android-ui-components/api/stream-chat-android-ui-components.api
```

### Key v7 classes
```
stream-chat-android-client/src/main/java/io/getstream/chat/android/client/api/ChatClientConfig.kt
stream-chat-android-compose/src/main/java/io/getstream/chat/android/compose/ui/theme/ChatTheme.kt
stream-chat-android-compose/src/main/java/io/getstream/chat/android/compose/ui/theme/StreamDesign.kt
stream-chat-android-compose/src/main/java/io/getstream/chat/android/compose/ui/theme/ChatUiConfig.kt
stream-chat-android-compose/src/main/java/io/getstream/chat/android/compose/ui/components/ChatComponentFactory.kt
stream-chat-android-compose/src/main/java/io/getstream/chat/android/compose/ui/components/ReactionResolver.kt
stream-chat-android-compose/src/main/java/io/getstream/chat/android/compose/ui/channels/ChannelScreen.kt
```

### Human migration guide
```
docs-content/chat-sdk/android/v7-latest/07-migration-guides/01-migrating-from-v6-to-v7.md
```

### v7 sample app (reference implementation)
```
stream-chat-android-compose-sample/src/main/java/io/getstream/chat/android/compose/sample/ChatHelper.kt
stream-chat-android-compose-sample/src/main/java/io/getstream/chat/android/compose/sample/ChatApp.kt
```

---

## §10 — Verification

After applying all changes, run these checks. ALL must pass.

### Step 1: No v6 symbols remain

```bash
# Must return 0 matches each
rg "StreamOfflinePluginFactory|StreamStatePluginFactory|StatePluginConfig" --type kotlin
rg "\.withPlugins\(" --type kotlin
rg "io\.getstream\.chat\.android\.offline\." --type kotlin
rg "io\.getstream\.chat\.android\.state\." --type kotlin
rg "io\.getstream\.chat\.android\.uiutils\." --type kotlin
rg "stream-chat-android-offline|stream-chat-android-state|stream-chat-android-ui-utils" build.gradle*
rg "AttachmentsPicker\b|AttachmentsPickerMode\b|AttachmentsPickerTabFactory" --type kotlin
rg "ReactionIconFactory|ReactionPushEmojiFactory" --type kotlin
rg "MessagesScreen\b|MessageListHeader\b|MessagesViewModelFactory\b" --type kotlin
rg "ChannelOptionState\b" --type kotlin
rg "GroupAvatar\b|ImageAvatar\b|InitialsAvatar\b" --type kotlin
rg "StreamColors\b|StreamTypography\b|StreamDimens\b|StreamShapes\b" --type kotlin
rg "DeletedMessageVisibility" --type kotlin
rg "ImageHeadersProvider|VideoHeadersProvider|DownloadAttachmentUriGenerator|DownloadRequestInterceptor|ImageAssetTransformer" --type kotlin
```

### Step 2: Build

```bash
./gradlew assembleDebug
```

Must compile with 0 errors. Warnings are acceptable.

### Step 3: Smoke test (manual)

1. Launch app on emulator
2. Log in / connect user
3. Open channel list — channels load
4. Open a channel — messages display
5. Send a message — appears in list
6. Send a reaction — emoji renders
7. Open attachment picker — modes display

If any step fails, re-read §9 source files and fix.
