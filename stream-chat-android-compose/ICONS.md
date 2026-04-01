# Icon System

This document describes the icon naming conventions, placement rules, and update workflow for the Compose SDK design system icons.

## Figma Source

The icons originate from the internal Chat SDK Design System Figma file.

## Naming Convention

Icons use the `stream_design_ic_` prefix with the **Figma component name** converted from kebab-case to snake_case:

```
Figma: arrow-down-circle  →  stream_design_ic_arrow_down_circle.xml
Figma: message-bubbles    →  stream_design_ic_message_bubbles.xml
Figma: filetype-pdf       →  stream_design_ic_filetype_pdf.xml
```

This provides direct traceability: any developer can look up `stream_design_ic_send` in Figma by searching for `send`.

### Prefixes by module

| Module | Prefix | Purpose |
|---|---|---|
| `stream-chat-android-compose` | `stream_design_ic_*` | Design system icons (new Figma art) |
| `stream-chat-android-ui-common` | `stream_design_ic_*` | Shared icons referenced by common code (legacy art, overridden by Compose via resource merging) |
| `stream-chat-android-ui-components` | `stream_ui_ic_*` | XML SDK icons (legacy design) |

## Icon Variants

The Figma export provides three categories:

- **Line** — stroke-based SVGs (preferred for Android). Stroke width can be adjusted programmatically.
- **Flat** — flattened vector shapes with baked-in strokes. Used mainly for Flutter.
- **Filetype** — polychromatic file type icons (colored backgrounds with white content).

**Android uses the Line variant** for all standard icons. Filetype icons use the `lg` size (32x40dp).

## Size Convention

All `stream_design_ic_*` standard icons use **20dp** as the intrinsic size. When a different size is needed at the call site, it is set explicitly via `Modifier.size()`.

Filetype icons (`stream_design_ic_filetype_*`) use **32x40dp** (non-square, document-shaped).

## Resource Merging (Compose overrides)

Some icons exist in both `ui-common` (legacy art) and `compose` (new Figma art) with the **same name**. Android resource merging ensures:

- **Compose apps** pick up the new Figma art from the compose module
- **XML apps** pick up the legacy art from ui-common

This allows shared code in `ui-common` (e.g., `ChannelAction.kt`, `MemberAction.kt`) to reference icons by a single name while each SDK gets the appropriate visual.

### Override icons (exist in both ui-common and compose)

| Resource name | Figma icon |
|---|---|
| `stream_design_ic_archive` | `archive` |
| `stream_design_ic_audio` | `audio` |
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

## How to Update Icons from Figma

1. Export icons from Figma as SVG (use the **Line** folder for standard icons, **Filetype** for file types)
2. Pre-process SVGs if needed: replace `fill="white"` and `stroke="white"` with `fill="#FFFFFF"` / `stroke="#FFFFFF"` (the converter mishandles named colors)
3. Convert SVGs to Android vector drawables:
   ```bash
   npx svg2vectordrawable -i icon-name.svg
   ```
4. Add the license header
5. Add `android:autoMirrored="true"` for unsymmetric (arrows, chevrons, reply, share, send, leave, export, quote)
6. Name the file `stream_design_ic_{figma_name_snake_case}.xml`
7. Place in `stream-chat-android-compose/src/main/res/drawable/`
8. If the icon is referenced by ui-common shared code, also place legacy art in `stream-chat-android-ui-common/src/main/res/drawable/` with the same name
9. Standard icons: colors should be `#FF000000` (tinted at use site). Polychromatic icons (giphy, imgur, filetype) keep their original colors.
