# Components Alignment Report

Source: [Notion - Components Alignment](https://www.notion.so/stream-wiki/Components-Alignment-2e76a5d7f9f680a8afaecf0c716338b2)

> This report compares the product component names defined in Notion with their current
> Compose implementation in `stream-chat-android-compose`. The public API surface is
> defined by `ChatComponentFactory` (the factory that exposes all customizable components).
>
> **Suffix convention:** The "View" suffix is iOS, "Widget" is Flutter. Android Compose
> needs no suffix — composable slot names follow Compose idiom (like `topBar` in `Scaffold`).
> The `Content` suffix used on some slots is an internal convention, not a cross-platform concern.

---

## Summary

| Product Name | Current `ChatComponentFactory` Method | Status |
|---|---|---|
| Avatar | `Avatar(params)` | Aligned |
| User Avatar | `UserAvatar(params)` | Aligned |
| Channel Avatar | `ChannelAvatar(params)` | Aligned |
| Message Item | `MessageItem(params)` | Aligned |
| Message Container | `MessageContainer(params)` | Aligned |
| Message Spacer | `MessageSpacer(params)` | Aligned |
| Message Content | `MessageContent(params)` | Aligned |
| Message Top | `MessageTop(params)` | Aligned |
| Message Bottom | `MessageBottom(params)` | Aligned |
| Message Author | `MessageAuthor(params)` | Aligned |
| Message Reactions | `MessageReactions(params)` | Aligned |
| Message Actions | **`MessageMenuOptions(params)`** | Name mismatch |
| Message List | *(top-level composable)* | Aligned |
| Message Composer | `MessageComposer(params)` | Aligned |
| Message Composer Leading | `MessageComposerLeadingContent(params)` | Aligned |
| Message Composer Trailing | `MessageComposerTrailingContent(params)` | Aligned |
| Message Composer Input | `MessageComposerInput(params)` | Aligned |
| Message Composer Input Leading | `MessageComposerInputLeadingContent(params)` | Aligned |
| Message Composer Input Center | `MessageComposerInputCenterContent(params)` | Aligned |
| Message Composer Input Trailing | `MessageComposerInputTrailingContent(params)` | Aligned |
| Message Composer Input Top | *(split into multiple methods)* | Aligned (more granular) |
| Attachment Picker | `AttachmentPicker(params)` | Aligned |
| Attachment Type Picker | `AttachmentTypePicker(params)` | Aligned |
| Attachment Picker Content | `AttachmentPickerContent(params)` | Aligned |
| Attachment Media Picker | `AttachmentMediaPicker(params)` | Aligned |
| Attachment File Picker | `AttachmentFilePicker(params)` | Aligned |
| Attachment Camera Picker | `AttachmentCameraPicker(params)` | Aligned |
| Attachment Polls Picker | `AttachmentPollPicker(params)` | Minor: `Poll` vs `Polls` |
| Attachment Commands Picker | `AttachmentCommandPicker(params)` | Minor: `Command` vs `Commands` |
| Channel | `MessageList` *(name conflict — `Channel` is the data model)* | Aligned (can't rename) |
| Channel Header | **`MessageListHeader(params)`** | Name mismatch |
| Thread | *(missing — `ThreadList` is a different concept)* | Missing |
| Thread Header | **`MessageListHeader(params)` (shared)** | Name mismatch + shared |
| Channel List | *(top-level composable)* | Aligned |
| Poll | **`PollMessageContent` (not in factory)** | Name mismatch + not in factory |

---

## Mismatches

### 1. Channel Header - NAME MISMATCH

**Notion name:** `Channel Header`

**ChatComponentFactory methods:**
- `MessageListHeader(params)`
- `MessageListHeaderLeadingContent(params)`
- `MessageListHeaderCenterContent(params)`
- `MessageListHeaderTrailingContent(params)`

This header is shared between channel and thread contexts.

**Action needed:**
- Rename `MessageListHeader` -> `ChannelHeader`
- Rename sub-components accordingly (`ChannelHeaderLeadingContent`, etc.)
- Create separate `ThreadHeader` + sub-components (see below)

---

### 2. Thread Header - NAME MISMATCH + MISSING

**Notion name:** `Thread Header`
**Current code:** Shares `MessageListHeader` with Channel Header — no dedicated component.

**Action needed:**
- Create dedicated `ThreadHeader(params)` in `ChatComponentFactory`
- Create sub-components: `ThreadHeaderLeadingContent`, `ThreadHeaderCenterContent`, `ThreadHeaderTrailingContent`

---

### 3. Message Actions - NAME MISMATCH

**Notion name:** `Message Actions`

**ChatComponentFactory methods:**
- `MessageMenu(params)` — the full selected message menu (reactions + options)
- `MessageMenuHeaderContent(params)` — header with reactions
- `MessageMenuOptions(params)` — the action options list
- `MessageMenuOptionsItem(params)` — individual action item

The standalone composable is also named `MessageOptions` (not `MessageActions`).

**Notion also defines a sync table** of required actions:

Own user actions: Thread Reply, Quote Message, Pin, Copy Message, Edit Message, Mark as Unread, Remind Me, Delete Message, Resend

Other user actions: Thread Reply, Quote Message, Pin, Copy Message, Mark as Unread, Remind Me, Flag Message, Block User

Notes:
- "Save for Later" is marked "Should be removed"
- "Mute User" is marked as missing on Android

**Action needed:**
- Evaluate renaming `MessageMenuOptions` -> `MessageActions`
- Add "Mute User" action for other user's messages
- Remove "Save for Later" if present

---

### 4. Poll - NAME MISMATCH + NOT IN FACTORY

**Notion name:** `Poll View` (Android: `Poll`)
**Current code:** `PollMessageContent` composable (standalone, not in `ChatComponentFactory`)

**Notion spec** defines sub-components:

| Notion Name | Expected Android Name | In Factory? |
|---|---|---|
| Poll View | `Poll` | No |
| Poll Option List View | `PollOptionList` | No |
| Poll Option List Item View | `PollOptionListItem` | No |
| Poll Results View | `PollResults` | No |
| Poll Results Main Header View | `PollResultsMainHeader` | No |
| Poll Results Option Header View | `PollResultsOptionHeader` | No |
| Poll Results Option Vote Item View | `PollResultsOptionVoteItem` | No |
| Poll Results Option Footer View | `PollResultsOptionFooter` | No |

**Action needed:**
- Rename `PollMessageContent` -> `Poll`
- Add `Poll` and sub-components to `ChatComponentFactory`
- Rename/create sub-components to match spec

---

### 5. AvatarSize Values

| Size | Notion Spec | Current Code |
|---|---|---|
| ExtraSmall | 20.dp | 20.dp |
| Small | 24.dp | 24.dp |
| Medium | 32.dp | 32.dp |
| Large | 40.dp | 40.dp |
| ExtraLarge | **64.dp** | **48.dp** |
| ExtraExtraLarge | *not defined* | 80.dp |

**Action needed:**
- Align `AvatarSize.ExtraLarge` to 64.dp
- Evaluate whether `ExtraExtraLarge` should exist (not in spec)

---

### 6. Minor: Attachment Picker Plurals

- `AttachmentPollPicker` -> `AttachmentPollsPicker`
- `AttachmentCommandPicker` -> `AttachmentCommandsPicker`

---

## Needs Clarification

1. **Channel** — Maps to `MessageList` in practice. Can't rename to `Channel` because that's already the data model class (`io.getstream.chat.android.models.Channel`). Not actionable.
2. **Thread** vs **ThreadList** — `ThreadList` is a threads *inbox* (list of thread previews). The Notion "Thread" component likely refers to a single thread conversation screen. These are different concepts. Notion subpage is blank.

These items have naming differences with the Notion spec but may be intentional:

3. **`MessageBubble`** — Notion says "Message (+ suffix)" for the bubble. `MessageBubble` is more descriptive.
4. **`MessageModifier`**, **`MessageLeading`**, **`MessageTrailing`** — In Notion spec but missing from factory. May not be needed given the current architecture.

---

## Priority Actions

### High Priority (semantic name mismatches in public API)
1. `MessageListHeader` -> `ChannelHeader` (+ sub-components in factory)
2. Create `ThreadHeader` (+ sub-components in factory)
3. `PollMessageContent` -> `Poll` + add to `ChatComponentFactory`

### Medium Priority
4. Evaluate `MessageMenuOptions` / `MessageOptions` -> `MessageActions`
5. `AvatarSize.ExtraLarge`: 48.dp -> 64.dp
6. `AttachmentPollPicker` -> `AttachmentPollsPicker` (plural)
7. `AttachmentCommandPicker` -> `AttachmentCommandsPicker` (plural)

### Needs Clarification
8. `Thread` vs `ThreadList` — different concepts, Notion subpage blank
