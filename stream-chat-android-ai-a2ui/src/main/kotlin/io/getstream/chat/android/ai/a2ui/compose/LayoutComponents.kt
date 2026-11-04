/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getstream.chat.android.ai.a2ui.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.LookaheadScope
import androidx.compose.ui.layout.approachLayout
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.ai.a2ui.R
import io.getstream.chat.android.ai.a2ui.core.A2UIJson

/**
 * `Row`: children side by side, with `justify`, `align` and each child's `weight`, which shares
 * the free space like CSS `flex-grow`.
 *
 * A child without a weight gets at most an equal share of the width and keeps its own size in it,
 * so a child that fills the width, such as a text field, leaves room for the ones after it.
 *
 * With `align: stretch`, the default, every child gets the height of the tallest one.
 */
@Composable
internal fun A2UIRow(scope: A2UIComponentScope) {
    val justify = scope.hint("justify")
    val align = scope.hint("align") ?: "stretch"
    val stretch = align == "stretch"
    val row = @Composable {
        Row(
            modifier = scope.modifier
                .fillMaxWidth()
                .then(if (stretch) Modifier.heightOfLookahead() else Modifier),
            horizontalArrangement = horizontalArrangement(justify),
            verticalAlignment = when (align) {
                "center" -> Alignment.CenterVertically
                "end" -> Alignment.Bottom
                else -> Alignment.Top
            },
        ) {
            scope.children().forEach { child ->
                key(child.key) {
                    val weight = child.weight ?: if (justify == "stretch") 1f else null
                    scope.Child(
                        child = child,
                        modifier = Modifier
                            .weight(weight ?: 1f, fill = weight != null)
                            .then(if (stretch) Modifier.fillMaxHeight() else Modifier),
                    )
                }
            }
        }
    }
    // The lookahead pass measures the row with its children at their own heights; the main pass
    // then gives the row that height, which the children fill. Unlike intrinsic sizes, this
    // works with children that subcompose, such as tabs and lazy lists. The scope adds no
    // layout node, so the parent's weight still reaches the row.
    if (stretch) LookaheadScope { row() } else row()
}

// The row's height in the lookahead pass is the tallest child's, measured with no height to
// fill; the main pass fixes the row to that height.
private fun Modifier.heightOfLookahead(): Modifier = this
    .approachLayout(isMeasurementApproachInProgress = { true }) { measurable, constraints ->
        val height = lookaheadSize.height.coerceIn(constraints.minHeight, constraints.maxHeight)
        val placeable = measurable.measure(constraints.copy(minHeight = height, maxHeight = height))
        layout(placeable.width, placeable.height) { placeable.place(0, 0) }
    }
    .layout { measurable, constraints ->
        val placeable = measurable.measure(
            if (isLookingAhead) constraints.copy(maxHeight = Constraints.Infinity) else constraints,
        )
        val height = placeable.height.coerceIn(constraints.minHeight, constraints.maxHeight)
        layout(placeable.width, height) { placeable.place(0, 0) }
    }

/**
 * `Column`: children one below the other, with `justify` and `align`.
 *
 * A surface in a chat message has no height of its own, so a child's weight changes nothing.
 */
@Composable
internal fun A2UIColumn(scope: A2UIComponentScope) {
    val align = scope.hint("align") ?: "stretch"
    Column(
        modifier = scope.modifier.fillMaxWidth(),
        verticalArrangement = verticalArrangement(scope.hint("justify")),
        horizontalAlignment = horizontalAlignment(align),
    ) {
        scope.children().forEach { child ->
            key(child.key) {
                scope.Child(child, if (align == "stretch") Modifier.fillMaxWidth() else Modifier)
            }
        }
    }
}

/**
 * `List`: children in a column, or in a row that scrolls with the `horizontal` direction.
 *
 * The items are all composed at once: the surface is in a chat message, already in a lazy list,
 * and a surface's lists are short.
 */
@Composable
internal fun A2UIList(scope: A2UIComponentScope) {
    val align = scope.hint("align") ?: "stretch"
    val children = scope.children()
    if (scope.hint("direction") == "horizontal") {
        Row(
            modifier = scope.modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(ItemSpacing),
            verticalAlignment = when (align) {
                "center" -> Alignment.CenterVertically
                "end" -> Alignment.Bottom
                else -> Alignment.Top
            },
        ) {
            children.forEach { child ->
                key(child.key) { scope.Child(child, Modifier.widthIn(max = HorizontalItemMaxWidth)) }
            }
        }
    } else {
        Column(
            modifier = scope.modifier.fillMaxWidth(),
            horizontalAlignment = horizontalAlignment(align),
        ) {
            children.forEach { child ->
                key(child.key) { scope.Child(child, if (align == "stretch") Modifier.fillMaxWidth() else Modifier) }
            }
        }
    }
}

/** `Card`: its child in an outlined card. */
@Composable
internal fun A2UICard(scope: A2UIComponentScope) {
    OutlinedCard(modifier = scope.modifier.padding(LeafMargin)) {
        scope.child()?.let { scope.Child(it, Modifier.padding(CardPadding)) }
    }
}

/** `Divider`: a line across, or down with the `vertical` axis. */
@Composable
internal fun A2UIDivider(scope: A2UIComponentScope) {
    if (scope.hint("axis") == "vertical") {
        VerticalDivider(modifier = scope.modifier.padding(horizontal = LeafMargin))
    } else {
        HorizontalDivider(modifier = scope.modifier.padding(vertical = LeafMargin))
    }
}

/** `Tabs`: a tab row of the tabs' titles over the selected tab's child. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun A2UITabs(scope: A2UIComponentScope) {
    val tabs = A2UIJson.list(scope.raw("tabs")).orEmpty().mapIndexedNotNull { index, raw ->
        val tab = A2UIJson.map(raw) ?: return@mapIndexedNotNull null
        val title = scope.resolve(tab["title"])
        Triple(A2UIJson.stringify(title), tab["child"] as? String, index)
    }
    if (tabs.isEmpty()) return
    var selected by rememberSaveable { mutableIntStateOf(0) }
    val current = selected.coerceIn(0, tabs.lastIndex)
    Column(modifier = scope.modifier.fillMaxWidth()) {
        PrimaryScrollableTabRow(selectedTabIndex = current, edgePadding = 0.dp) {
            tabs.forEachIndexed { index, (title, _, _) ->
                Tab(selected = index == current, onClick = { selected = index }, text = { Text(title) })
            }
        }
        val (_, childId, slot) = tabs[current]
        childId?.let { scope.childWithId(it, "tab$slot") }?.let { child ->
            key(child.key) { scope.Child(child, Modifier.fillMaxWidth()) }
        }
    }
}

/**
 * `Modal`: its trigger, which opens a dialog with its content. A button trigger opens it when
 * tapped, and still does what it does; any other trigger opens it as a whole.
 */
@Composable
internal fun A2UIModal(scope: A2UIComponentScope) {
    var open by rememberSaveable { mutableStateOf(false) }
    val trigger = scope.child("trigger")
    val triggerIsButton = trigger?.let { scope.context.surface.component(it.id)?.type == "Button" } == true
    CompositionLocalProvider(LocalModalOpener provides { open = true }) {
        Box(
            modifier = if (triggerIsButton) {
                scope.modifier
            } else {
                scope.layout.interactive(scope) { open = true }.clickable(role = Role.Button) { open = true }
            },
        ) {
            trigger?.let { scope.Child(it) }
        }
    }
    if (open) {
        AlertDialog(
            onDismissRequest = { open = false },
            confirmButton = {
                TextButton(onClick = { open = false }) { Text(stringResource(R.string.stream_ai_a2ui_close)) }
            },
            text = {
                Box(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    scope.child("content")?.let { scope.Child(it, Modifier.fillMaxWidth()) }
                }
            },
        )
    }
}

private fun horizontalArrangement(justify: String?): Arrangement.Horizontal = when (justify) {
    "center" -> Arrangement.Center
    "end" -> Arrangement.End
    "spaceBetween" -> Arrangement.SpaceBetween
    "spaceAround" -> Arrangement.SpaceAround
    "spaceEvenly" -> Arrangement.SpaceEvenly
    else -> Arrangement.Start
}

private fun verticalArrangement(justify: String?): Arrangement.Vertical = when (justify) {
    "center" -> Arrangement.Center
    "end" -> Arrangement.Bottom
    "spaceBetween" -> Arrangement.SpaceBetween
    "spaceAround" -> Arrangement.SpaceAround
    "spaceEvenly" -> Arrangement.SpaceEvenly
    else -> Arrangement.Top
}

private fun horizontalAlignment(align: String?): Alignment.Horizontal = when (align) {
    "center" -> Alignment.CenterHorizontally
    "end" -> Alignment.End
    else -> Alignment.Start
}

private val ItemSpacing = 8.dp
private val CardPadding = 8.dp
private val HorizontalItemMaxWidth = 280.dp
