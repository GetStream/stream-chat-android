# Accessibility playbook — Compose SDK

This document is a decision tree for adding or changing accessibility semantics inside `stream-chat-android-compose`. It builds on the two principles stated below. Keep it short.

## The two principles to apply

1. **Fix at the leaves; mirror visual order.** Each visual cell (`Icon`, `Text`, badge, button) owns its own `contentDescription` / `text` semantic. Compose's natural merge from clickables already composes them in tree order — which usually matches the visual reading order (top-to-bottom, left-to-right in LTR; mirrored in RTL). When tree order and reading order disagree (reversed lists, `zIndex` overlaps), see the Traversal order section — that disagreement is the root cause, and fixing it beats patching the symptoms.

2. **Trust framework defaults; reach for overrides only when defaults fail at the leaves.** Before adding `clearAndSetSemantics`, a parent `contentDescription`, `mergeDescendants = true`, or any other override, check whether Compose's defaults — perhaps after small leaf fixes — already produce the right result.

## Decision tree

```
What are you trying to express?
│
├─ "This Icon / Text / badge has a non-obvious meaning"
│      → contentDescription on the LEAF. Done.
│
├─ "This decorative leaf should be hidden from TalkBack"
│      → Modifier.semantics { hideFromAccessibility() } on the leaf.
│      (Same effect as contentDescription = null for Icons, but explicit.)
│
├─ "This element is a heading / section landmark"
│      → Modifier.semantics { heading() } on the heading composable.
│
├─ "Clicking this element triggers an action; TalkBack should announce
│   what that action does (e.g. 'opens thread', 'opens actions')"
│      → Pass onClickLabel / onLongClickLabel into clickable / combinedClickable.
│        Expose as paired onXxxClickLabel parameters on the public composable
│        with null defaults — the integrator owns the label, the SDK doesn't
│        ship one.
│
├─ "This element has a state TalkBack should announce on top of the role
│   (selected, checked, on/off, expanded)"
│      → role + state on the leaf via clickable(role = ...) /
│        toggleable / Modifier.semantics { toggleableState = ... }.
│      → For Tab role with selected: clearAndSetSemantics IS appropriate
│        on the leaf button (Tab semantics are a parent-level a11y concept).
│
├─ "Content changes dynamically and TalkBack should announce the change"
│      → Modifier.semantics { liveRegion = LiveRegionMode.Polite } on the
│        smallest container that owns the changing text.
│
├─ "This is a tappable container (Row) whose children should be read as
│   one focus stop instead of separate stops"
│      → If the Row has clickable / combinedClickable, the merge boundary is
│        IMPLICIT — do not add semantics(mergeDescendants = true) {}.
│      → If the Row has NO click handler (e.g. an edit-indicator banner),
│        Modifier.semantics(mergeDescendants = true) {} on the Row is the
│        right way to force a single focus stop. Add a brief comment
│        explaining why merging is needed.
│
├─ "I need to expose extra actions for TalkBack (move-up, move-down, etc.
│   that have no visible button)"
│      → Modifier.semantics { customActions = listOf(...) } on the smallest
│        container that represents the actionable item.
│
├─ "TalkBack reads elements in the wrong order / focus jumps between
│   elements unpredictably"
│      → Read the Traversal order section below. The fix is making the
│        semantics tree order match the reading order, not adding more
│        traversalIndex values.
│
└─ "An interactive descendant (toggle / button) is consuming the long-press
   so the message row's actions menu never opens"
      → Thread the message's long-click handler (onLongItemClick) down to the
        descendant and pass it as onLongClick to its own combinedClickable, so
        the descendant opens the same actions menu the cell would.
```

## Traversal order (TalkBack)

Verified against Compose UI 1.9.0 (`SemanticsSort.kt`) and TalkBack 16 sources, plus on-device TalkBack verbose logs.

**How the pipeline actually works.** Compose computes a reading order — a chain of `traversalBefore` links — from geometry plus `traversalIndex`, scoped by `isTraversalGroup`. TalkBack does **not** follow that chain directly. It rebuilds its own traversal tree by physically relocating each link's source node next to its target, and it **silently drops any link whose target is a descendant of its source** — which is exactly what a merged message cell linked before its own inner button produces. The rebuild is only reliable when it has nothing to do.

Rules that follow from this:

1. **The semantics tree order must match the intended reading order.** `traversalIndex` / `isTraversalGroup` should only *confirm* the natural order, never fight it. When they fight it, TalkBack's rebuild strands focusable containers (message cells) and navigation skips or jumps.
2. **Compose orders sibling semantics nodes by `zIndex`.** When composition order cannot match reading order (a `reverseLayout` lazy list composes newest-first but reads oldest-first), give each sibling a `zIndex` that mirrors its `traversalIndex`. See `Messages.kt` (`itemTraversalOrder`) for the pattern, and mind the draw-order change where siblings overlap.
3. **Only three kinds of nodes get a slot in Compose's chain**: merging nodes (real `clickable` / `toggleable` / `semantics(mergeDescendants = true)`), unmerged speaking leaves, and traversal groups (non-focusable groups are replaced by their children). A node that TalkBack focuses but Compose never hinted — for example a plain container with `contentDescription` plus `semantics { onClick(...) }` — has no position, and TalkBack degrades navigation around it. If a node should be a focus stop, make it a real `clickable`.
4. **A focusable container is always read before its focusable descendants**, and backward navigation is the exact reverse. You cannot reorder a parent against its own children with any traversal API.

## Diagnosing traversal issues

Two tools that turn guessing into reading:

- **Compose's computed chain**: an instrumented test can read each node's before-link from `node.extras.getInt("android.view.accessibility.extra.EXTRA_DATA_TEST_TRAVERSALBEFORE_VAL")` and reconstruct the full chain (run with `am instrument --no-hidden-api-checks`; node ids come from the hidden `getSourceNodeId`). Compose only computes the chain while a real accessibility service is enabled — UiAutomation alone does not count, and acquiring UiAutomation without `FLAG_DONT_SUPPRESS_ACCESSIBILITY_SERVICES` kills TalkBack.
- **TalkBack's decisions**: TalkBack settings → Advanced settings → Developer settings → Log output level → VERBOSE. Logcat then shows every focus verdict with its reason (`shouldFocusNode`) and the navigation pipeline (search, auto-scroll, focus). One real swipe explains a jump precisely. Injected input (UiAutomation, `adb shell input`) never reaches TalkBack's gesture detector, so the swipe must be a real gesture; restore the log level to ERROR afterwards.

## Don't do these

| Anti-pattern                                                                                                                       | Why it breaks                                                                                                                                                                                                      | What to do instead                                                                                                                                  |
|------------------------------------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------|
| `Modifier.semantics(mergeDescendants = true) {}` on a Row that already has `clickable`/`combinedClickable`/`toggleable`            | Redundant. The clickable already creates a merge boundary. The empty block reads like ritual.                                                                                                                      | Delete it.                                                                                                                                          |
| `Modifier.semantics { contentDescription = "..." }` on a container that wraps `Text` / `Icon` children with their own descriptions | The parent description composes with the children's descriptions, producing a verbose announce that breaks the moment a child is added/removed/swapped. Also breaks consumer overrides via `ChatComponentFactory`. | Let the children own their content. If you need a parent-level label (e.g. role), put it in `stateDescription` or `role`, not `contentDescription`. |
| `Modifier.clearAndSetSemantics { ... }` to "clean up" a noisy announce                                                             | You re-implement what the leaves already do, and drift the moment a leaf changes. Consumer overrides via the SDK's public extension API stop reaching the announce.                                                | Fix the offending leaf. Reserve `clearAndSetSemantics` for genuine parent-level a11y models (Tab, custom widget).                                   |
| `Role.Button` on a clickable list/grid item (channel row, message row, attachment tile)                                            | The item isn't visually a button. TalkBack will mis-announce it.                                                                                                                                                   | Don't set a role. Compose's clickable produces a usable focus stop without one.                                                                     |
| `stateDescription = ...` on a disabled element whose tap already shows a snackbar / dialog / live region with the same info        | TalkBack announces the state on focus, the snackbar announces on tap — the user hears the same string twice.                                                                                                       | Drop the `stateDescription`; trust the existing announce path.                                                                                      |
| New string resource named `stream_compose_cd_*` for a contentDescription                                                           | Convention was dropped in PRs #6404, #6433. The `_cd_` marker adds no value — a string used in `contentDescription` is still just a localized string.                                                              | Name by feature + intent: `stream_compose_<feature>_<intent>` (e.g. `stream_compose_channel_item_muted`).                                           |
| `Modifier.semantics { onClick(...) }` on a non-merging container to make it a screen-reader stop                                   | TalkBack focuses it (it is actionable) but Compose's traversal sorter never gives it a position, and TalkBack navigation degrades around the unhinted stop.                                                        | Use a real `clickable` — it makes the node merging, which also earns it a slot in the traversal order.                                              |
| Stacking `traversalIndex` / `isTraversalGroup` to force an order the semantics tree disagrees with                                 | TalkBack enforces the links by relocating nodes and drops ancestor→descendant links, so a fought-over order breaks in ways the Compose-side APIs cannot fix.                                                       | Align the tree first (composition order, or `zIndex` mirroring the index — see Traversal order), then let the indices confirm it.                   |
| Shipping a default `onClickLabel` string baked into the SDK for an integrator-defined action                                       | The integrator may swap the click handler; the default label becomes a lie.                                                                                                                                        | Expose an `onXxxClickLabel: String? = null` paired with `onXxxClick: () -> Unit`. Apply only when the integrator provides a label.                  |
| Manually inventing a formatter that already exists                                                                                 | Drift.                                                                                                                                                                                                             | Reuse: `rememberSpokenDurationFormatter()` (spoken durations), `typingUsersDescription(...)` (typing announce).                                     |

## Verifying changes

For any PR touching a11y:

1. Build and run on a real device.
2. Enable TalkBack: Settings → Accessibility → TalkBack → On.
3. Sweep right (next focus) and left (previous focus) through the affected screen — does the order match the visual order?
4. Double-tap on the focused element — does it activate the right action?
5. Double-tap-and-hold — does it open the message actions menu (where applicable)?
6. Check Paparazzi snapshots — semantic changes don't change pixels, so visual snapshots should not regress.
7. Add a unit test asserting the merged `SemanticsNode` if the announcement is non-trivial.

## When in doubt

- Read the offending leaf first, not the parent — that's almost always where the fix is.
- If you find yourself re-implementing logic that already lives in a subcomponent, your abstraction is wrong.
- For ordering problems, check the tree order before reaching for `traversalIndex` — the index can only confirm an order the tree already supports.
- A `// reason: …` comment next to any rule-bending override is cheap insurance for the next reader.
