# Channel List Redesign — Gaps

## 1. Trailing content slot is empty (ChannelItem.kt)
- **Issue:** Timestamp, badge, and mute icon (TRAILING_BOTTOM) are all baked into center content. The `trailingContent` slot is just a 16dp spacer.
- **Problem:** Figma put everything in center, but this kills the trailing slot for customizers and doesn't match the standard channel list pattern.
- **Fix:** Move timestamp + badge into trailing (top row), mute icon (TRAILING_BOTTOM) into trailing (bottom row). Keep delivery status + message preview in center.
- **Priority:** Post-merge cleanup
