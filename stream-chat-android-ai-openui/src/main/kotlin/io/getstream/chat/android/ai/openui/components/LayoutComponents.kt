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

package io.getstream.chat.android.ai.openui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.ai.openui.R
import io.getstream.chat.android.ai.openui.lang.ElementNode
import io.getstream.chat.android.ai.openui.lang.OpenUIJson
import io.getstream.chat.android.ai.openui.lang.OpenUIValue
import io.getstream.chat.android.ai.openui.lang.plainJson
import io.getstream.chat.android.ai.openui.rendering.ComponentProps
import io.getstream.chat.android.ai.openui.rendering.ComponentRenderProps
import io.getstream.chat.android.ai.openui.rendering.ComponentRenderer
import io.getstream.chat.android.ai.openui.rendering.OpenUIMarkdown
import io.getstream.chat.android.ai.openui.rendering.OpenUITheme
import io.getstream.chat.android.ai.openui.rendering.inlineMarkdownText
import io.getstream.chat.android.ai.openui.utils.NumberFormatting
import kotlinx.coroutines.launch

internal val layoutRenderers: Map<String, ComponentRenderer> = mapOf(
    "Tabs" to { TabsComponent(it) },
    "Accordion" to { AccordionComponent(it) },
    "Steps" to { StepsComponent(it) },
    "Carousel" to { CarouselComponent(it) },
)

internal val chatLayoutRenderers: Map<String, ComponentRenderer> = mapOf(
    "SectionBlock" to { SectionBlockComponent(it) },
)

internal val standardLayoutRenderers: Map<String, ComponentRenderer> = mapOf(
    "Modal" to { ModalComponent(it) },
)

/** Item props shared by `TabItem`, `AccordionItem` and `SectionItem`. */
private class ContainerItem(val value: String, val trigger: String, val content: OpenUIValue) {
    /** A cheap size measure used to follow content as it streams in. */
    val contentSize: Int get() = OpenUIJson.stringify(content.plainJson).encodeToByteArray().size

    companion object {
        fun from(element: ElementNode, index: Int, requireValue: Boolean = true): ContainerItem? {
            val props = ComponentProps(element.props)
            val value = props.text("value") ?: (if (requireValue) null else "$index") ?: return null
            return ContainerItem(
                value,
                props.text("trigger").orEmpty(),
                element.props["content"] ?: OpenUIValue.EmptyArray,
            )
        }

        fun list(elements: List<ElementNode>, requireValue: Boolean = true): List<ContainerItem> =
            elements.mapIndexedNotNull { index, element -> from(element, index, requireValue) }
    }
}

// Tabs ----------------------------------------------------------------------------------------

/** `Tabs(items)` with `TabItem(value, trigger, content)`. */
@Composable
private fun TabsComponent(context: ComponentRenderProps) {
    val theme = OpenUITheme.current
    val items = ContainerItem.list(context.props.elements("items"))
    var selection by rememberSaveable { mutableStateOf<String?>(null) }
    var userInteracted by rememberSaveable { mutableStateOf(false) }
    val previousSizes = remember { mutableMapOf<String, Int>() }
    if (items.isEmpty()) return

    // While the person hasn't picked a tab, follow the tab whose content is growing.
    val sizes = items.map { it.value to it.contentSize }
    LaunchedEffect(sizes, context.isStreaming) {
        if (userInteracted) return@LaunchedEffect
        var candidate: String? = null
        for ((value, size) in sizes) {
            if (size > (previousSizes[value] ?: 0)) candidate = value
        }
        previousSizes.clear()
        previousSizes.putAll(sizes)
        if (context.isStreaming && candidate != null && candidate != selection) {
            selection = candidate
        } else if (selection == null) {
            selection = sizes.firstOrNull()?.first
        }
    }

    val active = selection?.takeIf { value -> items.any { it.value == value } } ?: items[0].value
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(theme.spacing.m)) {
        Row(
            Modifier
                .horizontalScroll(rememberScrollState())
                .clip(RoundedCornerShape(theme.radius.pill))
                .background(theme.colors.sunkenSurface)
                .padding(3.dp),
            horizontalArrangement = Arrangement.spacedBy(theme.spacing.xs),
        ) {
            items.forEach { item ->
                val selected = item.value == active
                Text(
                    item.trigger,
                    style = theme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = if (selected) theme.colors.textPrimary else theme.colors.textSecondary,
                    modifier = Modifier
                        .then(if (selected) Modifier.shadow(1.dp, RoundedCornerShape(theme.radius.pill)) else Modifier)
                        .clip(RoundedCornerShape(theme.radius.pill))
                        .then(if (selected) Modifier.background(theme.colors.surface) else Modifier)
                        .selectable(selected = selected, role = Role.Tab) {
                            userInteracted = true
                            selection = item.value
                        }.padding(horizontal = 14.dp, vertical = 7.dp),
                )
            }
        }
        items.firstOrNull { it.value == active }?.let { current ->
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(theme.spacing.m)) {
                context.RenderNode(current.content)
            }
        }
    }
}

// Accordion -----------------------------------------------------------------------------------

/** `Accordion(items)` with `AccordionItem(value, trigger, content)`: one section open at a time. */
@Composable
private fun AccordionComponent(context: ComponentRenderProps) {
    val theme = OpenUITheme.current
    val items = ContainerItem.list(context.props.elements("items"))
    var openItem by rememberSaveable { mutableStateOf<String?>(null) }
    var userInteracted by rememberSaveable { mutableStateOf(false) }
    var previousCount by rememberSaveable { mutableIntStateOf(0) }
    if (items.isEmpty()) return

    // Auto-opens the newest item while it streams in (until the person interacts).
    val count = items.size
    LaunchedEffect(count) {
        if (context.isStreaming && !userInteracted && count > previousCount) openItem = items.last().value
        previousCount = count
    }

    Column(Modifier.fillMaxWidth()) {
        items.forEachIndexed { index, item ->
            OpenUIDisclosureRow(
                title = item.trigger,
                expanded = openItem == item.value,
                onExpandedChange = { expanded ->
                    userInteracted = true
                    openItem = if (expanded) item.value else null
                },
            ) { context.RenderNode(item.content) }
            if (index < items.lastIndex) HorizontalDivider(color = theme.colors.border)
        }
    }
}

/** A disclosure row with a chevron header. */
@Composable
internal fun OpenUIDisclosureRow(
    title: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    style: TextStyle? = null,
    content: @Composable () -> Unit,
) {
    val theme = OpenUITheme.current
    val rotation by animateFloatAsState(if (expanded) ExpandedChevronDegrees else 0f, label = "chevron")
    val state = stringResource(
        if (expanded) R.string.stream_ai_openui_disclosure_expanded else R.string.stream_ai_openui_disclosure_collapsed,
    )
    Column(Modifier.fillMaxWidth()) {
        Row(
            Modifier
                .fillMaxWidth()
                .clickable { onExpandedChange(!expanded) }
                .semantics {
                    heading()
                    stateDescription = state
                }.padding(vertical = theme.spacing.m),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                inlineMarkdownText(title, theme.colors.accent),
                style = style ?: theme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                color = theme.colors.textPrimary,
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(theme.spacing.s))
            Icon(
                Icons.Rounded.KeyboardArrowDown,
                contentDescription = null,
                tint = theme.colors.textSecondary,
                modifier = Modifier.size(18.dp).rotate(rotation),
            )
        }
        AnimatedVisibility(expanded, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
            Column(
                Modifier.fillMaxWidth().padding(bottom = theme.spacing.m),
                verticalArrangement = Arrangement.spacedBy(theme.spacing.m),
            ) { content() }
        }
    }
}

// Sections ------------------------------------------------------------------------------------

/** Saves the open sections as a list, which a `Bundle` can hold. */
private val StringSetSaver = listSaver<Set<String>, String>(save = { it.toList() }, restore = { it.toSet() })

/** `SectionBlock(sections, isFoldable?)` with `SectionItem(value, trigger, content)`. */
@Composable
private fun SectionBlockComponent(context: ComponentRenderProps) {
    val theme = OpenUITheme.current
    val sections = ContainerItem.list(context.props.elements("sections"), requireValue = false)
    val foldable = context.props.bool("isFoldable") ?: true
    var openItems by rememberSaveable(stateSaver = StringSetSaver) { mutableStateOf(emptySet<String>()) }
    var userInteracted by rememberSaveable { mutableStateOf(false) }
    var previousCount by rememberSaveable { mutableIntStateOf(0) }
    var wasStreaming by rememberSaveable { mutableStateOf(context.isStreaming) }

    if (!foldable) {
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(theme.spacing.l)) {
            sections.forEach { section ->
                Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(theme.spacing.s)) {
                    Text(
                        inlineMarkdownText(section.trigger, theme.colors.accent),
                        style = theme.typography.heading,
                        color = theme.colors.textPrimary,
                        modifier = Modifier.semantics { heading() },
                    )
                    context.RenderNode(section.content)
                }
            }
        }
        return
    }

    val count = sections.size
    LaunchedEffect(count) {
        if (sections.isNotEmpty()) {
            if (context.isStreaming && count > previousCount && !userInteracted) {
                openItems = openItems + sections.last().value
            } else if (openItems.isEmpty() && !userInteracted) {
                openItems = setOf(sections.first().value)
            }
        }
        previousCount = count
    }
    // When streaming ends, collapse back to the first section (unless the person interacted).
    LaunchedEffect(context.isStreaming) {
        if (wasStreaming && !context.isStreaming && !userInteracted) {
            sections.firstOrNull()?.let { openItems = setOf(it.value) }
        }
        wasStreaming = context.isStreaming
    }

    Column(Modifier.fillMaxWidth()) {
        sections.forEachIndexed { index, section ->
            OpenUIDisclosureRow(
                title = section.trigger,
                expanded = section.value in openItems,
                onExpandedChange = { expanded ->
                    userInteracted = true
                    openItems = if (expanded) openItems + section.value else openItems - section.value
                },
                style = theme.typography.heading,
            ) { context.RenderNode(section.content) }
            if (index < sections.lastIndex) HorizontalDivider(color = theme.colors.border)
        }
    }
}

// Steps ---------------------------------------------------------------------------------------

/** `Steps(items)` with `StepsItem(title, details)`. */
@Composable
private fun StepsComponent(context: ComponentRenderProps) {
    val theme = OpenUITheme.current
    val items = context.props.elements("items")
    val lineColor = theme.colors.border
    Column(Modifier.fillMaxWidth()) {
        items.forEachIndexed { index, item ->
            val props = ComponentProps(item.props)
            val isLast = index == items.lastIndex
            Row(
                Modifier
                    .fillMaxWidth()
                    // The connector to the next step, drawn so the row needs no intrinsic measurement.
                    .drawBehind {
                        if (!isLast) {
                            val x = 12.dp.toPx()
                            drawLine(
                                lineColor,
                                Offset(x, 28.dp.toPx()),
                                Offset(x, size.height - 4.dp.toPx()),
                                strokeWidth = 2.dp.toPx(),
                            )
                        }
                    },
                horizontalArrangement = Arrangement.spacedBy(theme.spacing.m),
            ) {
                Box(
                    Modifier.size(24.dp).background(theme.colors.accent, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "${index + 1}",
                        style = theme.typography.caption.copy(fontWeight = FontWeight.Bold),
                        color = theme.colors.onAccent,
                    )
                }
                Column(
                    Modifier.weight(1f).padding(bottom = if (isLast) 0.dp else theme.spacing.l),
                    verticalArrangement = Arrangement.spacedBy(theme.spacing.xs),
                ) {
                    Text(
                        inlineMarkdownText(props.text("title").orEmpty(), theme.colors.accent),
                        style = theme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = theme.colors.textPrimary,
                    )
                    when (val details = props["details"]) {
                        null -> Unit
                        is OpenUIValue.Str -> OpenUIMarkdown(
                            details.value,
                            style = theme.typography.bodySmall,
                            color = theme.colors.textSecondary,
                        )
                        else -> context.RenderNode(details)
                    }
                }
            }
        }
    }
}

// Carousel ------------------------------------------------------------------------------------

/** `Carousel(children, variant?)`: horizontally paged slides. */
// The pager, its page indicator and its arrows share state, so they stay in one composable.
@Suppress("LongMethod")
@Composable
private fun CarouselComponent(context: ComponentRenderProps) {
    val theme = OpenUITheme.current
    val slides = context.props.array("children")
    val variant = context.props.string("variant") ?: "card"
    if (slides.isEmpty()) return
    val pager = rememberPagerState { slides.size }
    val scope = rememberCoroutineScope()
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(theme.spacing.s)) {
        HorizontalPager(
            state = pager,
            modifier = Modifier.fillMaxWidth(),
            pageSpacing = theme.spacing.m,
            verticalAlignment = Alignment.Top,
        ) { index ->
            Column(
                Modifier.fillMaxWidth().openUISurface(
                    if (variant == "sunk") {
                        "sunk"
                    } else {
                        "card"
                    },
                    padding = theme.spacing.m,
                ),
                verticalArrangement = Arrangement.spacedBy(theme.spacing.s),
            ) {
                context.RenderNode(slides.getOrNull(index))
            }
        }
        if (slides.size > 1) {
            val current = pager.currentPage
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(theme.spacing.m, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FilledTonalIconButton(
                    onClick = { scope.launch { pager.animateScrollToPage(maxOf(0, current - 1)) } },
                    enabled = current > 0,
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(
                        Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                        contentDescription = stringResource(R.string.stream_ai_openui_carousel_previous),
                        modifier = Modifier.size(18.dp),
                    )
                }
                val spoken = stringResource(
                    R.string.stream_ai_openui_carousel_position_accessibility,
                    NumberFormatting.integer(current + 1),
                    NumberFormatting.integer(slides.size),
                )
                // Weighted so the buttons keep their size; the dots scroll when there are many.
                Box(
                    Modifier.weight(1f, fill = false).semantics { contentDescription = spoken },
                    contentAlignment = Alignment.Center,
                ) {
                    Row(
                        Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        repeat(slides.size) { index ->
                            Box(
                                Modifier.size(6.dp).background(
                                    if (index == current) {
                                        theme.colors.accent
                                    } else {
                                        theme.colors.border
                                    },
                                    CircleShape,
                                ),
                            )
                        }
                    }
                }
                FilledTonalIconButton(
                    onClick = { scope.launch { pager.animateScrollToPage(minOf(slides.lastIndex, current + 1)) } },
                    enabled = current < slides.lastIndex,
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(
                        Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                        contentDescription = stringResource(R.string.stream_ai_openui_carousel_next),
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}

// Modal ---------------------------------------------------------------------------------------

/** `Modal(title, open, children, size?)`: `open` is a `$boolean` binding. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModalComponent(context: ComponentRenderProps) {
    val theme = OpenUITheme.current
    val field = context.stateField("open", context.props["open"])
    val isOpen = field.value == OpenUIValue.True || field.value == OpenUIValue.Str("true")
    if (!isOpen) return
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = context.props.string("size") != "sm")
    val scope = rememberCoroutineScope()
    val close: () -> Unit = {
        scope.launch { sheetState.hide() }.invokeOnCompletion { field.setValue(OpenUIValue.False) }
    }
    ModalBottomSheet(onDismissRequest = { field.setValue(OpenUIValue.False) }, sheetState = sheetState) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = theme.spacing.s),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = close) {
                Icon(Icons.Rounded.Close, contentDescription = stringResource(R.string.stream_ai_openui_modal_close))
            }
            Text(
                context.props.text("title").orEmpty(),
                style = theme.typography.heading,
                color = theme.colors.textPrimary,
                modifier = Modifier.weight(1f).semantics { heading() },
            )
        }
        Column(
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(theme.spacing.l),
            verticalArrangement = Arrangement.spacedBy(theme.spacing.m),
        ) {
            context.RenderProp("children")
        }
    }
}

// How far a disclosure's chevron turns when it expands.
private const val ExpandedChevronDegrees = 180f
