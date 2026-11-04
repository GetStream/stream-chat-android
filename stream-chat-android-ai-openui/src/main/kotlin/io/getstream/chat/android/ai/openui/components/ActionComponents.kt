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

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.getstream.chat.android.ai.openui.icons.SubdirectoryArrowRight
import io.getstream.chat.android.ai.openui.lang.ActionStepType
import io.getstream.chat.android.ai.openui.lang.ElementNode
import io.getstream.chat.android.ai.openui.lang.JS
import io.getstream.chat.android.ai.openui.lang.OpenUIObject
import io.getstream.chat.android.ai.openui.lang.OpenUIValue
import io.getstream.chat.android.ai.openui.lang.arrayValue
import io.getstream.chat.android.ai.openui.lang.get
import io.getstream.chat.android.ai.openui.lang.objectValue
import io.getstream.chat.android.ai.openui.lang.stringValue
import io.getstream.chat.android.ai.openui.rendering.ComponentProps
import io.getstream.chat.android.ai.openui.rendering.ComponentRenderProps
import io.getstream.chat.android.ai.openui.rendering.ComponentRenderer
import io.getstream.chat.android.ai.openui.rendering.LocalOpenUIFormName
import io.getstream.chat.android.ai.openui.rendering.OpenUIIcon
import io.getstream.chat.android.ai.openui.rendering.OpenUITheme
import io.getstream.chat.android.ai.openui.rendering.inlineMarkdown

internal val actionRenderers: Map<String, ComponentRenderer> = mapOf(
    "Button" to { ButtonComponent(it) },
    "Buttons" to { ButtonsComponent(it) },
    "IconButton" to { IconButtonComponent(it) },
    "ListBlock" to { ListBlockComponent(it) },
    "Icon" to { IconComponent(it) },
    "Tag" to { TagComponent(it) },
    "TagBlock" to { TagBlockComponent(it) },
    "EntityList" to { EntityListComponent(it) },
)

internal val chatActionRenderers: Map<String, ComponentRenderer> = mapOf(
    "FollowUpBlock" to { FollowUpBlockComponent(it) },
)

private val tabularNumbers = TextStyle(fontFeatureSettings = "tnum")

// Buttons ---------------------------------------------------------------------------------------

/** Padding per button `size`. */
private fun buttonPadding(size: String): PaddingValues = when (size) {
    "extra-small" -> PaddingValues(horizontal = 10.dp, vertical = 2.dp)
    "small" -> PaddingValues(horizontal = 12.dp, vertical = 4.dp)
    "large" -> PaddingValues(horizontal = 20.dp, vertical = 12.dp)
    else -> PaddingValues(horizontal = 16.dp, vertical = 8.dp)
}

private fun buttonMinHeight(size: String): Dp = when (size) {
    "extra-small" -> 28.dp
    "small" -> 34.dp
    "large" -> 50.dp
    else -> 40.dp
}

/** A Material button in an OpenUI variant: primary (filled), secondary (tonal), tertiary/ghost (text). */
// Each parameter is one of the button's OpenUI props or its Material slots.
@Suppress("LongParameterList")
@Composable
private fun OpenUIVariantButton(
    variant: String,
    destructive: Boolean,
    size: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape? = null,
    content: @Composable () -> Unit,
) {
    val theme = OpenUITheme.current
    val padding = buttonPadding(size)
    val sized = modifier.heightIn(min = buttonMinHeight(size))
    val buttonShape = shape ?: ButtonDefaults.shape
    when (variant) {
        "secondary" -> FilledTonalButton(
            onClick = onClick,
            enabled = enabled,
            modifier = sized,
            shape = buttonShape,
            contentPadding = padding,
            colors = if (destructive) {
                ButtonDefaults.filledTonalButtonColors(
                    containerColor = theme.colors.danger.copy(alpha = 0.14f),
                    contentColor = theme.colors.danger,
                )
            } else {
                ButtonDefaults.filledTonalButtonColors()
            },
        ) { content() }
        "tertiary", "ghost" -> TextButton(
            onClick = onClick,
            enabled = enabled,
            modifier = sized,
            shape = buttonShape,
            contentPadding = padding,
            colors = if (destructive) {
                ButtonDefaults.textButtonColors(contentColor = theme.colors.danger)
            } else {
                ButtonDefaults.textButtonColors()
            },
        ) { content() }
        else -> Button(
            onClick = onClick,
            enabled = enabled,
            modifier = sized,
            shape = buttonShape,
            contentPadding = padding,
            colors = primaryButtonColors(destructive),
        ) { content() }
    }
}

@Composable
private fun primaryButtonColors(destructive: Boolean): ButtonColors {
    val theme = OpenUITheme.current
    return if (destructive) {
        ButtonDefaults.buttonColors(containerColor = theme.colors.danger, contentColor = theme.colors.onAccent)
    } else {
        ButtonDefaults.buttonColors(containerColor = theme.colors.accent, contentColor = theme.colors.onAccent)
    }
}

/**
 * `Button(label, action?, variant?, type?, size?)`. A primary button validates the enclosing form
 * first (when its action talks to the model or runs a mutation), then fires its action.
 */
@Composable
internal fun ButtonComponent(ctx: ComponentRenderProps, sizeOverride: String? = null) {
    val theme = OpenUITheme.current
    val props = ctx.props
    val label = props.text("label").orEmpty()
    val variant = props.string("variant") ?: "primary"
    val destructive = props.string("type") == "destructive"
    val size = sizeOverride ?: props.string("size") ?: "medium"
    val formName = LocalOpenUIFormName.current
    val validation = LocalFormValidation.current

    OpenUIVariantButton(
        variant = variant,
        destructive = destructive,
        size = size,
        enabled = !ctx.isStreaming,
        onClick = {
            val action = props["action"]
            val validates = validation != null && variant == "primary" && actionNeedsValidation(action)
            if (validates && !validation.validateForm()) return@OpenUIVariantButton
            ctx.triggerAction(label, formName, action)
        },
        modifier = if (variant == "tertiary" || variant == "ghost") Modifier else Modifier.fillMaxWidth(),
    ) {
        Text(
            label,
            style = (
                if (size == "large") {
                    theme.typography.body
                } else {
                    theme.typography.bodySmall
                }
                ).copy(fontWeight = FontWeight.SemiBold),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
    }
}

/**
 * `Buttons(buttons, direction?)`. Rows fall back to a column when the buttons don't fit side by
 * side.
 */
@Composable
internal fun ButtonsComponent(ctx: ComponentRenderProps) {
    val theme = OpenUITheme.current
    val buttons = OpenUIValue.Arr(ctx.props.array("buttons"))
    if (ctx.props.string("direction") == "column") {
        Column(
            Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(theme.spacing.s),
        ) { ctx.RenderNode(buttons) }
    } else {
        ButtonsFitRow(theme.spacing.s) { ctx.RenderNode(buttons) }
    }
}

/** Places children in an equal-width row when their natural widths fit, else stacks them full width. */
@Composable
private fun ButtonsFitRow(spacing: Dp, content: @Composable () -> Unit) {
    Layout(content, Modifier.fillMaxWidth()) { measurables, constraints ->
        val gap = spacing.roundToPx()
        val width = constraints.maxWidth
        if (measurables.isEmpty()) return@Layout layout(width, 0) {}
        val natural = measurables.sumOf { it.maxIntrinsicWidth(Constraints.Infinity) } + gap * (measurables.size - 1)
        if (natural <= width) {
            val itemWidth = ((width - gap * (measurables.size - 1)) / measurables.size).coerceAtLeast(0)
            val placeables = measurables.map { it.measure(Constraints(minWidth = 0, maxWidth = itemWidth)) }
            val height = placeables.maxOf { it.height }
            layout(width, height) {
                placeables.forEachIndexed { index, placeable ->
                    val x = index * (itemWidth + gap) + (itemWidth - placeable.width) / 2
                    placeable.placeRelative(x, (height - placeable.height) / 2)
                }
            }
        } else {
            val placeables = measurables.map { it.measure(Constraints(maxWidth = width)) }
            val height = placeables.sumOf { it.height } + gap * (placeables.size - 1)
            layout(width, height) {
                var y = 0
                placeables.forEach { placeable ->
                    placeable.placeRelative((width - placeable.width) / 2, y)
                    y += placeable.height + gap
                }
            }
        }
    }
}

/** `IconButton(name, icon, action?, variant?, size?, shape?)`. */
@Composable
internal fun IconButtonComponent(ctx: ComponentRenderProps) {
    val props = ctx.props
    val name = props.text("name").orEmpty()
    val variant = props.string("variant") ?: "secondary"
    val formName = LocalOpenUIFormName.current
    val shape: Shape = if (props.string("shape") == "circle") {
        CircleShape
    } else {
        RoundedCornerShape(OpenUITheme.current.radius.small)
    }
    val dimension = when (props.string("size")) {
        "extra-small" -> 28.dp
        "small" -> 34.dp
        "large" -> 52.dp
        else -> 42.dp
    }
    val onClick = { ctx.triggerAction(name, formName, props["action"]) }
    val modifier = Modifier.size(dimension).semantics { contentDescription = name }
    val icon: @Composable () -> Unit = {
        OpenUIIcon(props.element("icon"), Modifier.size(20.dp), decorative = name.isNotEmpty())
    }
    when (variant) {
        "primary" -> FilledIconButton(
            onClick,
            modifier,
            enabled = !ctx.isStreaming,
            shape = shape,
            colors = IconButtonDefaults.filledIconButtonColors(containerColor = OpenUITheme.current.colors.accent),
        ) { icon() }
        // This Material 3 version's IconButton has no shape parameter, so the clip shapes its ripple.
        "tertiary", "ghost" -> IconButton(onClick, modifier.clip(shape), enabled = !ctx.isStreaming) { icon() }
        else -> FilledTonalIconButton(onClick, modifier, enabled = !ctx.isStreaming, shape = shape) { icon() }
    }
}

// Lists & follow-ups -----------------------------------------------------------------------------

/** `FollowUpBlock(items)`: suggestions that send their text as the next user message. */
@Composable
internal fun FollowUpBlockComponent(ctx: ComponentRenderProps) {
    val theme = OpenUITheme.current
    val items = ctx.props.elements("items")
    Column(Modifier.fillMaxWidth()) {
        items.forEachIndexed { index, item ->
            val text = ComponentProps(item.props).text("text").orEmpty()
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable(enabled = !ctx.isStreaming) { ctx.triggerAction(text) }
                    .padding(vertical = theme.spacing.s + 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(theme.spacing.s),
            ) {
                Icon(
                    Icons.Rounded.SubdirectoryArrowRight,
                    contentDescription = null,
                    tint = theme.colors.accent,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text,
                    style = theme.typography.bodySmall,
                    color = theme.colors.textPrimary,
                    modifier = Modifier.weight(1f),
                )
            }
            if (index < items.lastIndex) HorizontalDivider(Modifier.padding(start = 26.dp), color = theme.colors.border)
        }
    }
}

/** `ListBlock(items, variant?, size?)` with `ListItem(title, subtitle?, image?, actionLabel?, action?)`. */
// The list, its rows and their actions share the variant and size they are drawn in.
@Suppress("LongMethod")
@Composable
internal fun ListBlockComponent(ctx: ComponentRenderProps, sizeOverride: String? = null) {
    val theme = OpenUITheme.current
    val items = ctx.props.elements("items")
    val variant = ctx.props.string("variant") ?: "number"
    val small = (sizeOverride ?: ctx.props.string("size")) == "small"
    val hasSubtitle = items.any { ComponentProps(it.props).text("subtitle") != null }

    Column(Modifier.fillMaxWidth()) {
        items.forEachIndexed { index, item ->
            val props = ComponentProps(item.props)
            val title = props.text("title").orEmpty()
            val action = props["action"]
            val hasAction = action != null && action !is OpenUIValue.Null
            Row(
                Modifier
                    .fillMaxWidth()
                    .then(
                        if (hasAction) {
                            Modifier.clickable(enabled = !ctx.isStreaming) {
                                ctx.triggerAction(title, action = action)
                            }
                        } else {
                            Modifier
                        },
                    ).padding(vertical = if (small) theme.spacing.xs + 2.dp else theme.spacing.s + 2.dp),
                verticalAlignment = if (hasSubtitle) Alignment.Top else Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(theme.spacing.m),
            ) {
                val image = props.obj("image")
                if (variant == "image" && image != null) {
                    RemoteImage(
                        url = image["src"].stringValue,
                        alt = image["alt"].stringValue,
                        modifier = Modifier
                            .size(if (small) 36.dp else 48.dp).clip(RoundedCornerShape(theme.radius.small)),
                    )
                } else {
                    val dimension = if (small) 20.dp else 24.dp
                    Box(
                        Modifier.size(dimension).background(theme.colors.accent.copy(alpha = 0.12f), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            "${index + 1}",
                            style = theme.typography.caption
                                .merge(tabularNumbers).copy(fontWeight = FontWeight.SemiBold),
                            color = theme.colors.accent,
                        )
                    }
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        inlineMarkdown(title, theme.colors.accent),
                        style = (if (small) theme.typography.caption else theme.typography.bodySmall).copy(
                            fontWeight = FontWeight.Medium,
                        ),
                        color = theme.colors.textPrimary,
                    )
                    props.text("subtitle")?.let { subtitle ->
                        Text(
                            inlineMarkdown(subtitle, theme.colors.accent),
                            style = if (small) {
                                theme.typography.caption.copy(fontSize = 11.sp)
                            } else {
                                theme.typography.caption
                            },
                            color = theme.colors.textSecondary,
                        )
                    }
                }
                if (hasAction) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        props.text("actionLabel")?.let {
                            Text(
                                it,
                                style = theme.typography.caption.copy(fontWeight = FontWeight.Medium),
                                color = theme.colors.textSecondary,
                            )
                        }
                        Icon(
                            Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                            null,
                            tint = theme.colors.textSecondary,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
            }
            if (index < items.lastIndex) {
                val inset = if (variant == "image") (if (small) 48.dp else 60.dp) else 36.dp
                HorizontalDivider(Modifier.padding(start = inset), color = theme.colors.border)
            }
        }
    }
}

// Tags & icons -----------------------------------------------------------------------------------

/** `Icon(name, category?)`. */
@Composable
internal fun IconComponent(ctx: ComponentRenderProps) {
    OpenUIIcon(ctx.element, Modifier.size(20.dp), tint = OpenUITheme.current.colors.textSecondary)
}

/** A capsule tag (`Tag(text, icon?, size?, variant?)`). */
@Composable
internal fun TagPill(text: String, icon: ElementNode? = null, size: String? = null, variant: String? = null) {
    val theme = OpenUITheme.current
    val neutral = variant == null || variant == "neutral"
    val color = if (neutral) theme.colors.textSecondary else theme.colors.variant(variant)
    val style = when (size) {
        "lg" -> theme.typography.bodySmall
        "md" -> theme.typography.caption.copy(fontSize = 13.sp)
        else -> theme.typography.caption
    }
    Row(
        Modifier
            .background(
                if (neutral) theme.colors.subtleFill else color.copy(alpha = 0.14f),
                RoundedCornerShape(theme.radius.pill),
            ).padding(
                horizontal = when (size) {
                    "lg" -> 12.dp
                    "md" -> 10.dp
                    else -> 8.dp
                },
                vertical = when (size) {
                    "lg" -> 6.dp
                    "md" -> 4.dp
                    else -> 3.dp
                },
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        if (icon != null) {
            OpenUIIcon(
                icon,
                Modifier.size(
                    when (size) {
                        "lg" -> 16.dp
                        "md" -> 14.dp
                        else -> 12.dp
                    },
                ),
                tint = color,
                decorative = true,
            )
        }
        Text(
            text,
            style = style.copy(fontWeight = FontWeight.Medium),
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
internal fun TagComponent(ctx: ComponentRenderProps, sizeOverride: String? = null) {
    val props = ctx.props
    TagPill(
        props.text("text").orEmpty(),
        props.element("icon"),
        sizeOverride ?: props.string("size"),
        props.string("variant"),
    )
}

/** `TagBlock(tags, size?)`. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun TagBlockComponent(ctx: ComponentRenderProps, sizeOverride: String? = null) {
    val theme = OpenUITheme.current
    val tags = ctx.props.strings("tags")
    val gap = theme.spacing.xs + 2.dp
    FlowRow(horizontalArrangement = Arrangement.spacedBy(gap), verticalArrangement = Arrangement.spacedBy(gap)) {
        tags.forEach { TagPill(it, size = sizeOverride ?: ctx.props.string("size")) }
    }
}

/** `EntityList(rows?, size?, header?, footer?)`: two-column key/value rows. */
@Composable
internal fun EntityListComponent(ctx: ComponentRenderProps, compact: Boolean = false) {
    val theme = OpenUITheme.current
    val props = ctx.props
    val small = compact || props.string("size") == "small"
    val rows = props.array("rows").mapNotNull { it.objectValue }
    Column(Modifier.fillMaxWidth()) {
        if (!small) {
            props.obj("header")?.let {
                EntityRow(it, emphasized = true, small = false)
                HorizontalDivider(color = theme.colors.border)
            }
        }
        rows.forEachIndexed { index, row ->
            EntityRow(row, emphasized = false, small = small)
            if (index < rows.lastIndex) HorizontalDivider(color = theme.colors.border.copy(alpha = 0.6f))
        }
        if (!small) {
            props.obj("footer")?.let {
                HorizontalDivider(color = theme.colors.border)
                EntityRow(it, emphasized = true, small = false)
            }
        }
    }
}

@Composable
private fun EntityRow(item: OpenUIObject, emphasized: Boolean, small: Boolean) {
    val theme = OpenUITheme.current
    val isNumber = item["rightVariant"] == OpenUIValue.Str("number")
    val base = if (small) theme.typography.caption else theme.typography.bodySmall
    Row(Modifier.fillMaxWidth().padding(vertical = if (small) 5.dp else 8.dp), verticalAlignment = Alignment.Top) {
        Text(
            JS.toStringOrEmpty(item["left"]),
            style = if (emphasized) base.copy(fontWeight = FontWeight.SemiBold) else base,
            color = if (emphasized) theme.colors.textPrimary else theme.colors.textSecondary,
        )
        Spacer(Modifier.width(theme.spacing.s).weight(1f))
        Text(
            JS.toStringOrEmpty(item["right"]),
            style = base
                .copy(
                    fontWeight = if (emphasized || !small) {
                        FontWeight.SemiBold
                    } else {
                        FontWeight.Medium
                    },
                ).let { if (isNumber) it.merge(tabularNumbers) else it },
            color = theme.colors.textPrimary,
            textAlign = TextAlign.End,
        )
    }
}

/** Whether an action needs the enclosing form validated before it runs (primary buttons). */
internal fun actionNeedsValidation(action: OpenUIValue?): Boolean {
    val steps = action["steps"].arrayValue ?: return true
    return steps.any { step ->
        step["type"] == OpenUIValue.Str(ActionStepType.CONTINUE_CONVERSATION) ||
            (step["type"] == OpenUIValue.Str(ActionStepType.RUN) && step["refType"] == OpenUIValue.Str("mutation"))
    }
}
