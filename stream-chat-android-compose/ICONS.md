# Icon System

This document describes the icon naming conventions, placement rules, and mapping between the Figma design system and Android drawable resources.

## Figma Source

The icons originate from the internal Chat SDK Design System Figma file, under the **01 - FOUNDATIONS** page (Icons table).

## Naming Convention

New icons use the `stream_design_ic_` prefix with the **Figma component name** converted from kebab-case to snake_case:

```
Figma: arrow-down-circle  ->  stream_design_ic_arrow_down_circle.xml
Figma: message-bubbles     ->  stream_design_ic_message_bubbles.xml
```

This provides direct traceability: any developer can look up `stream_design_ic_send` in Figma by searching for `send`.

### Prefixes by module

| Module | Prefix | Purpose |
|---|---|---|
| `stream-chat-android-compose` | `stream_design_ic_*` | New design system icons (Figma art) |
| `stream-chat-android-compose` | `stream_compose_ic_*` | Legacy icons with no Figma replacement yet |
| `stream-chat-android-ui-common` | `stream_design_ic_*` | Shared icons referenced by common code (legacy art, overridden by Compose) |
| `stream-chat-android-ui-components` | `stream_ui_ic_*` | XML SDK icons (legacy design) |

## Size Convention

All `stream_design_ic_*` icons use **20dp** as the intrinsic size baseline. Figma exports each icon in 4 sizes (12, 16, 20, 32), but we use the 20dp variant. When a different size is needed at the call site, it is set explicitly via `Modifier.size()` or `android:layout_width/height`.

## Resource Merging (Compose overrides)

Some icons exist in both `ui-common` (legacy art) and `compose` (new Figma art) with the **same name**. Android resource merging ensures:

- **Compose apps** pick up the new Figma art from the compose module
- **XML apps** pick up the legacy art from ui-common

This allows shared code in `ui-common` (e.g., `ChannelAction.kt`, `MemberAction.kt`) to reference icons by a single name while each SDK gets the appropriate visual.

### Override icons (exist in both ui-common and compose)

| Resource name | Figma icon |
|---|---|
| `stream_design_ic_archive` | `archive` |
| `stream_design_ic_bell` | `bell` |
| `stream_design_ic_delete` | `delete` |
| `stream_design_ic_info` | `info` |
| `stream_design_ic_leave` | `leave` |
| `stream_design_ic_message_bubble` | `message-bubble` |
| `stream_design_ic_minus_circle` | `minus-circle` |
| `stream_design_ic_mute` | `mute` |
| `stream_design_ic_no_sign` | `no-sign` |
| `stream_design_ic_pin` | `pin` |
| `stream_design_ic_unpin` | `unpin` |

Note: `stream_design_ic_unarchive` exists only in ui-common with no Figma equivalent.

## Old-to-new Name Mapping

For traceability when reading git history or migrating code:

| Old resource name | New resource name | Figma icon |
|---|---|---|
| `stream_compose_ic_add` | `stream_design_ic_plus` | `plus` |
| `stream_compose_ic_annotation_arrow_up_right` | `stream_design_ic_arrow_up_right` | `arrow-up-right` |
| `stream_compose_ic_annotation_bookmark` | `stream_design_ic_save` | `save` |
| `stream_compose_ic_annotation_pin` | `stream_design_ic_pin` | `pin` |
| `stream_compose_ic_annotation_translated` | `stream_design_ic_translate` | `translate` |
| `stream_compose_ic_arrow_back` | `stream_design_ic_arrow_left` | `arrow-left` |
| `stream_compose_ic_arrow_down` | `stream_design_ic_arrow_down` | `arrow-down` |
| `stream_compose_ic_block` | `stream_design_ic_no_sign` | `no-sign` |
| `stream_compose_ic_bubbles` | `stream_design_ic_message_bubbles` | `message-bubbles` |
| `stream_compose_ic_camera` | `stream_design_ic_camera` | `camera` |
| `stream_compose_ic_chart` | `stream_design_ic_poll` | `poll` |
| `stream_compose_ic_checkmark` | `stream_design_ic_checkmark` | `checkmark` |
| `stream_compose_ic_chevron_left` | `stream_design_ic_chevron_left` | `chevron-left` |
| `stream_compose_ic_chevron_right` | `stream_design_ic_chevron_right` | `chevron-right` |
| `stream_compose_ic_chevron_top` | `stream_design_ic_chevron_up` | `chevron-up` |
| `stream_compose_ic_circle_minus` | `stream_design_ic_minus_circle` | `minus-circle` |
| `stream_compose_ic_clear` | `stream_design_ic_x_circle` | `x-circle` |
| `stream_compose_ic_close` | `stream_design_ic_xmark` | `xmark` |
| `stream_compose_ic_copy` | `stream_design_ic_copy` | `copy` |
| `stream_compose_ic_cross` | `stream_design_ic_xmark` | `xmark` |
| `stream_compose_ic_delete` | `stream_design_ic_delete` | `delete` |
| `stream_compose_ic_download` | `stream_design_ic_download` | `download` |
| `stream_compose_ic_drag_handle` | `stream_design_ic_reorder` | `reorder` |
| `stream_compose_ic_edit` | `stream_design_ic_edit` | `edit` |
| `stream_compose_ic_error` | `stream_design_ic_exclamation_circle_fill` | `exclamation-circle-fill` |
| `stream_compose_ic_exclamation_circle` | `stream_design_ic_exclamation_circle_fill` | `exclamation-circle-fill` |
| `stream_compose_ic_eye_open` | `stream_design_ic_eye_fill` | `eye-fill` |
| `stream_compose_ic_file` | `stream_design_ic_file` | `file` |
| `stream_compose_ic_files` | `stream_design_ic_folder` | `folder` |
| `stream_compose_ic_flag` | `stream_design_ic_flag` | `flag` |
| `stream_compose_ic_gallery` | `stream_design_ic_gallery` | `gallery` |
| `stream_compose_ic_image_picker` | `stream_design_ic_image` | `image` |
| `stream_compose_ic_link` | `stream_design_ic_link` | `link` |
| `stream_compose_ic_lock_closed` | `stream_design_ic_lock` | `lock` |
| `stream_compose_ic_lock_open` | `stream_design_ic_unlock` | `unlock` |
| `stream_compose_ic_map_pin` | `stream_design_ic_location` | `location` |
| `stream_compose_ic_media` | `stream_design_ic_gallery` | `gallery` |
| `stream_compose_ic_menu_vertical` | `stream_design_ic_more` | `more` |
| `stream_compose_ic_mic` | `stream_design_ic_voice` | `voice` |
| `stream_compose_ic_mic_solid` | `stream_design_ic_voice_fill` | `voice-fill` |
| `stream_compose_ic_microphone` | `stream_design_ic_voice` | `voice` |
| `stream_compose_ic_minus` | `stream_design_ic_minus` | `minus` |
| `stream_compose_ic_more_options` | `stream_design_ic_more` | `more` |
| `stream_compose_ic_mute` | `stream_design_ic_mute` | `mute` |
| `stream_compose_ic_muted` | `stream_design_ic_bell_off` | `bell-off` |
| `stream_compose_ic_pause` | `stream_design_ic_pause_fill` | `pause-fill` |
| `stream_compose_ic_person` | `stream_design_ic_account` | `account` |
| `stream_compose_ic_pin` | `stream_design_ic_pin` | `pin` |
| `stream_compose_ic_play` | `stream_design_ic_play_fill` | `play-fill` |
| `stream_compose_ic_plus` | `stream_design_ic_plus` | `plus` |
| `stream_compose_ic_poll` | `stream_design_ic_poll` | `poll` |
| `stream_compose_ic_reaction_add` | `stream_design_ic_emoji_add` | `emoji-add` |
| `stream_compose_ic_reply` | `stream_design_ic_reply` | `reply` |
| `stream_compose_ic_resend` | `stream_design_ic_refresh` | `refresh` |
| `stream_compose_ic_search` | `stream_design_ic_search` | `search` |
| `stream_compose_ic_send` | `stream_design_ic_send` | `send` |
| `stream_compose_ic_share` | `stream_design_ic_share` | `share` |
| `stream_compose_ic_stop` | `stream_design_ic_stop_fill` | `stop-fill` |
| `stream_compose_ic_team` | `stream_design_ic_users` | `users` |
| `stream_compose_ic_thread` | `stream_design_ic_thread` | `thread` |
| `stream_compose_ic_trophy` | `stream_design_ic_trophy` | `trophy` |
| `stream_compose_ic_unpin` | `stream_design_ic_unpin` | `unpin` |
| `stream_compose_ic_user` | `stream_design_ic_user` | `user` |
| `stream_compose_ic_video` | `stream_design_ic_video` | `video` |
| `stream_compose_ic_video_outline` | `stream_design_ic_video` | `video` |

### ui-common renames

| Old resource name | New resource name | Figma icon |
|---|---|---|
| `stream_ic_action_archive` | `stream_design_ic_archive` | `archive` |
| `stream_ic_action_block` | `stream_design_ic_no_sign` | `no-sign` |
| `stream_ic_action_delete` | `stream_design_ic_delete` | `delete` |
| `stream_ic_action_leave` | `stream_design_ic_leave` | `leave` |
| `stream_ic_action_mute` | `stream_design_ic_mute` | `mute` |
| `stream_ic_action_pin` | `stream_design_ic_pin` | `pin` |
| `stream_ic_action_unarchive` | `stream_design_ic_unarchive` | (no Figma equivalent) |
| `stream_ic_action_unmute` | `stream_design_ic_bell` | `bell` |
| `stream_ic_action_unpin` | `stream_design_ic_unpin` | `unpin` |
| `stream_ic_action_view_info` | `stream_design_ic_info` | `info` |
| `stream_ic_ban` | `stream_design_ic_minus_circle` | `minus-circle` |
| `stream_ic_block` | (consolidated into `stream_design_ic_no_sign`) | `no-sign` |
| `stream_ic_chat_bubble` | `stream_design_ic_message_bubble` | `message-bubble` |

## Legacy Icons (no Figma replacement)

These icons remain with `stream_compose_ic_*` prefix because the features they serve are not yet part of the new design system:

| Resource | Usage |
|---|---|
| `stream_compose_ic_annotation_reminder` | Message annotation badge |
| `stream_compose_ic_attachment_camera_picker` | Attachment picker option |
| `stream_compose_ic_attachment_commands_picker` | Attachment picker option |
| `stream_compose_ic_attachment_polls_picker` | Attachment picker option |
| `stream_compose_ic_clock` | Pending message status (awaiting `clock` Figma icon) |
| `stream_compose_ic_command_chip` | Command chip badge |
| `stream_compose_ic_command_chip_cancel` | Command chip cancel |
| `stream_compose_ic_file_audio` | File type icon (40dp) |
| `stream_compose_ic_file_code` | File type icon (40dp) |
| `stream_compose_ic_file_compression` | File type icon (40dp) |
| `stream_compose_ic_file_doc` | File type icon (40dp) |
| `stream_compose_ic_file_generic` | File type icon (40dp) |
| `stream_compose_ic_file_pdf` | File type icon (40dp) |
| `stream_compose_ic_file_presentation` | File type icon (40dp) |
| `stream_compose_ic_file_spreadsheet` | File type icon (40dp) |
| `stream_compose_ic_file_video` | File type icon (40dp) |
| `stream_compose_ic_giphy` | Giphy badge (polychromatic, legacy) |
| `stream_compose_ic_hide` | Hide channel option |
| `stream_compose_ic_mark_as_unread` | Mark as unread action |
| `stream_compose_ic_show_in_chat` | Show in chat action |
| `stream_compose_ic_union` | Thread list banner |
| `stream_compose_ic_visible_to_you` | Ephemeral message indicator |

## Icons Missing from Figma Export

These icons exist in the Figma design system but were not available in the export. They should be added when re-exported:

| Figma name | Purpose |
|---|---|
| `clock` | Clock icon (pending state) - different from `loading` |
| `code` | Code brackets `</>` |
| `spreadsheet` | Table/spreadsheet (Figjam component) |
| `presentation` | Layers/presentation (Figjam component) |

## Figma Export Naming Issues

Some Figma exports produce mangled filenames. The resource name should always match the **Figma component name**, not the exported filename:

| Figma component name | Exported as | Resource name |
|---|---|---|
| `message-bubbles` | `message-bubble--3.svg` (20dp) | `stream_design_ic_message_bubbles` |
| `emoji` | `emoji--3.svg` (20dp) | `stream_design_ic_emoji` |
| `bolt-fill` | `bolt-20.svg` | `stream_design_ic_bolt_fill` |

## How to Update Icons from Figma

1. Export updated icons from Figma as SVG
2. Convert SVGs to Android vector drawables using `svg2vectordrawable`:
   ```bash
   npx svg2vectordrawable -i icon-name-20.svg
   ```
3. Add license header and `android:autoMirrored="true"` for directional icons (arrows, chevrons, reply, share, send, leave, export, quote)
4. Name the file `stream_design_ic_{figma_name_snake_case}.xml`
5. Place in `stream-chat-android-compose/src/main/res/drawable/`
6. If the icon is referenced by ui-common shared code, also place legacy art in `stream-chat-android-ui-common/src/main/res/drawable/` with the same name
7. Normalize fill colors to `#000000` (except polychromatic brand icons like giphy, imgur)
