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

import android.content.res.Resources
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.ai.openui.R
import io.getstream.chat.android.ai.openui.icons.NorthEast
import io.getstream.chat.android.ai.openui.icons.SouthEast
import io.getstream.chat.android.ai.openui.lang.ElementNode
import io.getstream.chat.android.ai.openui.lang.JS
import io.getstream.chat.android.ai.openui.lang.OpenUIObject
import io.getstream.chat.android.ai.openui.lang.OpenUIValue
import io.getstream.chat.android.ai.openui.lang.arrayValue
import io.getstream.chat.android.ai.openui.lang.elementValue
import io.getstream.chat.android.ai.openui.lang.objectValue
import io.getstream.chat.android.ai.openui.lang.stringValue
import io.getstream.chat.android.ai.openui.rendering.ComponentProps
import io.getstream.chat.android.ai.openui.rendering.ComponentRenderProps
import io.getstream.chat.android.ai.openui.rendering.ComponentRenderer
import io.getstream.chat.android.ai.openui.rendering.LocalOpenUIFormName
import io.getstream.chat.android.ai.openui.rendering.OpenUIElement
import io.getstream.chat.android.ai.openui.rendering.OpenUIIcon
import io.getstream.chat.android.ai.openui.rendering.OpenUITheme
import io.getstream.chat.android.ai.openui.rendering.inlineMarkdown
import io.getstream.chat.android.ai.openui.utils.NumberFormatting
import kotlin.math.abs

private fun cardRenderer(block: ComponentRenderer): ComponentRenderer = block

internal val cardRenderers: Map<String, ComponentRenderer> = mapOf(
    "Text" to cardRenderer { TextBlockView(it, bold = false) },
    "BoldText" to cardRenderer { TextBlockView(it, bold = true) },
    "IconText" to cardRenderer { IconTextView(it) },
    "ImageText" to cardRenderer { ImageTextView(it) },
    "ImageTextLarge" to cardRenderer { ImageTextLargeView(it) },
    "MetricIndicatorInline" to cardRenderer { MetricInlineView(it.props) },
    "MetricIndicatorWithStrikethrough" to cardRenderer { MetricStrikethroughView(it.props) },
    "SnippetCardBlock" to cardRenderer { SnippetCardBlockView(it) },
    "OverviewCardBlock" to cardRenderer { OverviewCardBlockView(it) },
    "ContextCardBlock" to cardRenderer { ContextCardBlockView(it) },
    "CompositeCardBlock" to cardRenderer { CompositeCardBlockView(it) },
    "VisualCardBlock" to cardRenderer { VisualCardBlockView(it) },
)

/** Tabular (fixed-width) digits. */
private fun TextStyle.tabular(enabled: Boolean = true): TextStyle =
    if (enabled) copy(fontFeatureSettings = "tnum") else this

// Inline building blocks ----------------------------------------------------------------------

/** `Text(variant?, value, subtext?, subtextVariant?, size?)` and `BoldText(...)`. */
@Composable
private fun TextBlockView(context: ComponentRenderProps, bold: Boolean) {
    val theme = OpenUITheme.current
    val props = context.props
    val size = props.string("size") ?: "md"
    val primary = when (size) {
        "xs" -> theme.typography.caption
        "sm" -> theme.typography.bodySmall
        "lg" -> if (bold) theme.typography.title else theme.typography.bodyLarge
        else -> if (bold) theme.typography.heading else theme.typography.body
    }
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            inlineMarkdown(props.text("value").orEmpty(), theme.colors.accent),
            style = primary.copy(fontWeight = if (bold) FontWeight.SemiBold else FontWeight.Normal).tabular(
                props.string("variant") == "number",
            ),
            color = theme.colors.textPrimary,
        )
        props.text("subtext")?.takeIf { it.isNotEmpty() }?.let { subtext ->
            val variant = props.string("subtextVariant")
            val color = when {
                variant != "metric" -> theme.colors.textSecondary
                subtext.startsWith("+") -> theme.colors.success
                subtext.startsWith("-") || subtext.startsWith("−") -> theme.colors.danger
                else -> theme.colors.textSecondary
            }
            Text(subtext, style = theme.typography.caption.tabular(variant != "text"), color = color)
        }
    }
}

/** An icon in a tinted rounded badge. */
@Composable
private fun CardIconBadge(icon: ElementNode?, variant: String?, size: String?) {
    val theme = OpenUITheme.current
    val dimension = when (size) {
        "xs" -> 24.dp
        "s", "sm" -> 30.dp
        "l", "lg" -> 44.dp
        "xl" -> 52.dp
        else -> 36.dp
    }
    val tint = when (variant) {
        "inverted", "filled" -> theme.colors.onAccent
        "info", "success", "warning", "danger" -> theme.colors.variant(variant)
        "soft" -> theme.colors.accent
        else -> theme.colors.textPrimary
    }
    val background = when (variant) {
        "inverted" -> theme.colors.textPrimary
        "filled" -> theme.colors.accent
        "info", "success", "warning", "danger" -> theme.colors.variant(variant).copy(alpha = 0.14f)
        "soft" -> theme.colors.accent.copy(alpha = 0.12f)
        else -> theme.colors.sunkenSurface
    }
    Box(
        Modifier.size(dimension).background(background, RoundedCornerShape(dimension * 0.28f)),
        contentAlignment = Alignment.Center,
    ) {
        OpenUIIcon(icon, Modifier.size(dimension * 0.5f), tint = tint)
    }
}

/** `IconText(icon, iconVariant?, iconSize?, title, subtitle?, bold?, layout?)`. */
@Composable
private fun IconTextView(context: ComponentRenderProps) {
    val props = context.props
    CardHorizontalOrVertical(vertical = props.string("layout") == "vertical") {
        CardIconBadge(props.element("icon"), props.string("iconVariant"), props.string("iconSize"))
        CardTitleSubtitle(props.text("title").orEmpty(), props.text("subtitle"), props.bool("bold") ?: false)
    }
}

/** A row (centered, `m` spacing) or a column (`s` spacing). */
@Composable
private fun CardHorizontalOrVertical(vertical: Boolean, content: @Composable () -> Unit) {
    val theme = OpenUITheme.current
    if (vertical) {
        Column(verticalArrangement = Arrangement.spacedBy(theme.spacing.s)) { content() }
    } else {
        Row(
            horizontalArrangement = Arrangement.spacedBy(theme.spacing.m),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            content()
        }
    }
}

@Composable
private fun CardTitleSubtitle(title: String, subtitle: String?, bold: Boolean = false) {
    val theme = OpenUITheme.current
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            inlineMarkdown(title, theme.colors.accent),
            style = theme.typography.bodySmall.copy(fontWeight = if (bold) FontWeight.SemiBold else FontWeight.Medium),
            color = theme.colors.textPrimary,
        )
        subtitle?.takeIf { it.isNotEmpty() }?.let {
            Text(
                inlineMarkdown(it, theme.colors.accent),
                style = theme.typography.caption,
                color = theme.colors.textSecondary,
            )
        }
    }
}

/** An `ImageText`'s image size: the model's `imageSize`, kept between 8 and 200, or 40. */
internal fun imageTextSize(size: Double?): Double =
    size?.takeIf { it.isFinite() }?.coerceIn(MinImageSize, MaxImageSize) ?: DefaultImageSize

private const val MinImageSize = 8.0
private const val MaxImageSize = 200.0
private const val DefaultImageSize = 40.0

/** `ImageText(src, alt?, title, subtitle?, bold?, layout?, imageSize?)`. */
@Composable
private fun ImageTextView(context: ComponentRenderProps) {
    val theme = OpenUITheme.current
    val props = context.props
    val size = imageTextSize(props.number("imageSize")).dp
    CardHorizontalOrVertical(vertical = props.string("layout") == "vertical") {
        RemoteImage(
            url = props.string("src"),
            alt = props.text("alt"),
            modifier = Modifier.size(size).clip(RoundedCornerShape(theme.radius.small)),
        )
        CardTitleSubtitle(props.text("title").orEmpty(), props.text("subtitle"), props.bool("bold") ?: false)
    }
}

/** `ImageTextLarge(src, alt?, title, subtitle?, bold?)`. */
@Composable
private fun ImageTextLargeView(context: ComponentRenderProps) {
    val theme = OpenUITheme.current
    val props = context.props
    Column(verticalArrangement = Arrangement.spacedBy(theme.spacing.s)) {
        RemoteImage(
            url = props.string("src"),
            alt = props.text("alt"),
            modifier = Modifier.fillMaxWidth().height(140.dp).clip(RoundedCornerShape(theme.radius.medium)),
        )
        CardTitleSubtitle(props.text("title").orEmpty(), props.text("subtitle"), bold = true)
    }
}

/** A `{direction: "up" | "down", value: number}` trend badge. */
@Composable
private fun TrendBadge(trend: OpenUIObject?) {
    val theme = OpenUITheme.current
    val direction = trend?.get("direction").stringValue ?: return
    val up = direction == "up"
    val value = abs(JS.toNumber(trend?.get("value")))
    val color = if (up) theme.colors.success else theme.colors.danger
    val spoken = stringResource(
        if (up) R.string.stream_ai_openui_trend_up else R.string.stream_ai_openui_trend_down,
        NumberFormatting.decimal(value),
    )
    Row(
        Modifier
            .clearAndSetSemantics { contentDescription = spoken }
            .background(
                color.copy(alpha = 0.12f),
                RoundedCornerShape(theme.radius.pill),
            ).padding(horizontal = 6.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            if (up) Icons.Rounded.NorthEast else Icons.Rounded.SouthEast,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(12.dp),
        )
        Text(
            NumberFormatting.percent(value),
            style = theme.typography.caption
                .copy(fontWeight = FontWeight.SemiBold)
                .tabular(),
            color = color,
        )
    }
}

/** A metric value that shrinks (down to 60%) rather than wrapping. */
@Composable
private fun MetricValue(text: String) {
    val theme = OpenUITheme.current
    val style = theme.typography.metric.copy(color = theme.colors.textPrimary)
    val full = style.fontSize
    BasicText(
        text,
        style = style,
        maxLines = 1,
        autoSize = if (full.isSp) TextAutoSize.StepBased(minFontSize = full * 0.6f, maxFontSize = full) else null,
    )
}

/** `MetricIndicatorInline(value, subtext?, trend?)`. */
@Composable
private fun MetricInlineView(props: ComponentProps) {
    val theme = OpenUITheme.current
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(theme.spacing.s),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.weight(1f, fill = false)) { MetricValue(props.text("value").orEmpty()) }
            TrendBadge(props.obj("trend"))
        }
        props.text("subtext")?.takeIf { it.isNotEmpty() }?.let {
            Text(it, style = theme.typography.caption, color = theme.colors.textSecondary)
        }
    }
}

/** `MetricIndicatorWithStrikethrough(value, subtext?, previousValue?, trend?)`. */
@Composable
private fun MetricStrikethroughView(props: ComponentProps) {
    val theme = OpenUITheme.current
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(theme.spacing.s),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(props.text("value").orEmpty(), style = theme.typography.metric, color = theme.colors.textPrimary)
            props.text("previousValue")?.takeIf { it.isNotEmpty() }?.let {
                Text(
                    it,
                    style = theme.typography.bodySmall.copy(textDecoration = TextDecoration.LineThrough),
                    color = theme.colors.textTertiary,
                )
            }
            TrendBadge(props.obj("trend"))
        }
        props.text("subtext")?.takeIf { it.isNotEmpty() }?.let {
            Text(it, style = theme.typography.caption, color = theme.colors.textSecondary)
        }
    }
}

// Card blocks ---------------------------------------------------------------------------------

/** Shared layout for card blocks: a responsive grid or a horizontal carousel of tappable cards. */
@Composable
private fun CardBlockLayout(
    count: Int,
    layout: String,
    minItemWidth: Dp,
    carouselItemWidth: Dp,
    gap: Dp?,
    isClickable: Boolean,
    onTap: (Int) -> Unit,
    item: @Composable (Int) -> Unit,
) {
    val spacing = gap ?: OpenUITheme.current.spacing.m
    if (layout == "carousel") {
        val state = rememberLazyListState()
        LazyRow(
            Modifier.fillMaxWidth(),
            state = state,
            horizontalArrangement = Arrangement.spacedBy(spacing),
            verticalAlignment = Alignment.Top,
            flingBehavior = rememberSnapFlingBehavior(state),
        ) {
            items(
                count,
            ) { index ->
                CardTappable(
                    isClickable,
                    { onTap(index) },
                    Modifier.width(carouselItemWidth),
                ) { item(index) }
            }
        }
    } else {
        ResponsiveGrid(spacing = spacing, minItemWidth = minItemWidth, maxColumns = 3) {
            repeat(count) { index -> CardTappable(isClickable, { onTap(index) }) { item(index) } }
        }
    }
}

/** A card that shrinks and dims slightly while pressed. */
@Composable
private fun CardTappable(
    isClickable: Boolean,
    onTap: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    if (!isClickable) {
        Box(modifier) { content() }
        return
    }
    val theme = OpenUITheme.current
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    // The press shows as the card shrinking, so there's no ripple; keyboard and switch-access focus
    // shows as an outline.
    val focused by interaction.collectIsFocusedAsState()
    val shape = RoundedCornerShape(theme.radius.medium)
    Box(
        modifier
            .graphicsLayer {
                val scale = if (pressed) 0.98f else 1f
                scaleX = scale
                scaleY = scale
                alpha = if (pressed) 0.85f else 1f
            }.border(2.dp, if (focused) theme.colors.accent else Color.Transparent, shape)
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClickLabel = stringResource(R.string.stream_ai_openui_card_block_click_label),
                onClick = onTap,
            ),
    ) { content() }
}

/** A card block's numeric `gap` in dp: kept between 0 and 48, or `null` when not finite. */
internal fun cardBlockGap(gap: Double): Double? = gap.takeIf { it.isFinite() }?.coerceIn(0.0, MaxCardBlockGap)

private const val MaxCardBlockGap = 48.0

/** Common props of the five card blocks. */
private class CardBlockProps(context: ComponentRenderProps, theme: OpenUITheme) {
    val items: List<ElementNode> = context.props.elements("items")
    val layout: String = context.props.string("layout") ?: "grid"
    val action: OpenUIValue? = context.props["action"]?.takeUnless { it is OpenUIValue.Null }
    val gap: Dp? = when (val value = context.props["gap"]) {
        is OpenUIValue.Num -> cardBlockGap(value.value)?.dp
        is OpenUIValue.Str -> theme.spacing.value(value.value, theme.spacing.m)
        else -> null
    }
}

private fun ElementNode?.stringProp(key: String): String? = this?.props?.get(key).stringValue

private fun String?.str(): OpenUIValue? = this?.let(OpenUIValue::Str)

private fun cardTap(
    context: ComponentRenderProps,
    action: OpenUIValue?,
    label: String,
    item: OpenUIObject,
    formName: String?,
) {
    if (action == null || context.isStreaming) return
    context.triggerAction(label, formName, ItemActionContext.merge(action, item))
}

@Composable
private fun Modifier.cardSurface(): Modifier {
    val theme = OpenUITheme.current
    return fillMaxWidth()
        .background(theme.colors.sunkenSurface, RoundedCornerShape(theme.radius.medium)).padding(theme.spacing.m)
}

// The message sent for a tapped card without a title, which names it by its number.
private fun Resources.cardLabel(@StringRes label: Int, index: Int): String =
    getString(label, NumberFormatting.integer(index + 1))

/** `SnippetCardBlock(items, layout?, responsive?, action?, gap?)` with `SnippetCardItem(id?, lhs, rhs?)`. */
@Composable
private fun SnippetCardBlockView(context: ComponentRenderProps) {
    val theme = OpenUITheme.current
    val formName = LocalOpenUIFormName.current
    val resources = LocalContext.current.resources
    val block = CardBlockProps(context, theme)
    CardBlockLayout(
        count = block.items.size,
        layout = "grid",
        minItemWidth = 150.dp,
        carouselItemWidth = 220.dp,
        gap = block.gap,
        isClickable = block.action != null && !context.isStreaming,
        onTap = { index ->
            val item = block.items[index]
            val lhs = item.props["lhs"].elementValue
            val rhs = item.props["rhs"].elementValue
            val id = item.stringProp("id")
            cardTap(
                context,
                block.action,
                lhs.stringProp("title")
                    ?: id
                    ?: resources.cardLabel(R.string.stream_ai_openui_card_block_snippet, index),
                ItemActionContext.obj(
                    "itemIndex" to OpenUIValue.of(index),
                    "itemId" to id.str(),
                    "itemTitle" to lhs.stringProp("title").str(),
                    "itemSubtitle" to lhs.stringProp("subtitle").str(),
                    "itemValue" to rhs.stringProp("value").str(),
                ),
                formName,
            )
        },
    ) { index ->
        val item = block.items[index]
        val rhs = item.props["rhs"].elementValue?.let {
            ComponentRenderProps.overriding(it, mapOf("size" to OpenUIValue.Str("xs")))
        }
        Box(Modifier.cardSurface()) {
            CardFitsHorizontally(
                spacing = theme.spacing.s,
                leading = { context.RenderNode(item.props["lhs"]) },
                trailing = rhs?.let { { OpenUIElement(it) } },
            )
        }
    }
}

/**
 * Leading and trailing content side by side when they fit, the trailing one at the end, and wrapped
 * onto the next line otherwise. Each is composed once.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CardFitsHorizontally(spacing: Dp, leading: @Composable () -> Unit, trailing: (@Composable () -> Unit)?) {
    FlowRow(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalArrangement = Arrangement.spacedBy(spacing),
        itemVerticalAlignment = Alignment.CenterVertically,
    ) {
        // The end padding keeps the two apart when they share a line.
        Box(Modifier.padding(end = if (trailing != null) spacing else 0.dp)) { leading() }
        trailing?.invoke()
    }
}

/** `OverviewCardBlock(items, layout?, ...)` with `OverviewCardItem(id?, top, bottom?)`. */
@Composable
private fun OverviewCardBlockView(context: ComponentRenderProps) {
    val theme = OpenUITheme.current
    val formName = LocalOpenUIFormName.current
    val resources = LocalContext.current.resources
    val block = CardBlockProps(context, theme)
    CardBlockLayout(
        count = block.items.size,
        layout = block.layout,
        minItemWidth = 140.dp,
        carouselItemWidth = 180.dp,
        gap = block.gap,
        isClickable = block.action != null && !context.isStreaming,
        onTap = { index ->
            val item = block.items[index]
            val top = item.props["top"].elementValue
            val bottom = item.props["bottom"].elementValue
            val id = item.stringProp("id")
            val title = top.stringProp("title") ?: top.stringProp("value")
            cardTap(
                context,
                block.action,
                title ?: id ?: resources.cardLabel(R.string.stream_ai_openui_card_block_overview, index),
                ItemActionContext.obj(
                    "itemIndex" to OpenUIValue.of(index),
                    "itemId" to id.str(),
                    "itemTitle" to title.str(),
                    "itemSubtitle" to (top.stringProp("subtitle") ?: top.stringProp("subtext")).str(),
                    "itemMetricValue" to bottom?.props?.get("value"),
                ),
                formName,
            )
        },
    ) { index ->
        val item = block.items[index]
        Column(Modifier.cardSurface(), verticalArrangement = Arrangement.spacedBy(theme.spacing.m)) {
            context.RenderNode(item.props["top"])
            item.props["bottom"].elementValue?.let { MetricInlineView(ComponentProps(it.props)) }
        }
    }
}

/**
 * `ContextCardBlock(items, layout?, ...)` with
 * `ContextCardItem(id?, title, body?, bgColor?, bgImageSrc?, bgImageAlt?)`.
 */
@Composable
private fun ContextCardBlockView(context: ComponentRenderProps) {
    val theme = OpenUITheme.current
    val formName = LocalOpenUIFormName.current
    val resources = LocalContext.current.resources
    val block = CardBlockProps(context, theme)
    CardBlockLayout(
        count = block.items.size,
        layout = block.layout,
        minItemWidth = 150.dp,
        carouselItemWidth = 210.dp,
        gap = block.gap,
        isClickable = block.action != null && !context.isStreaming,
        onTap = { index ->
            val item = block.items[index]
            val props = ComponentProps(item.props)
            val title = contextCardTitle(item)
            cardTap(
                context,
                block.action,
                title.ifEmpty {
                    props.text("id")
                        ?: resources.cardLabel(R.string.stream_ai_openui_card_block_context, index)
                },
                ItemActionContext.obj(
                    "itemIndex" to OpenUIValue.of(index),
                    "itemId" to props["id"],
                    "itemTitle" to OpenUIValue.Str(title),
                    "itemBody" to props["body"],
                    "itemBgColor" to props["bgColor"],
                    "itemBgImageSrc" to props["bgImageSrc"],
                    "itemBgImageAlt" to props["bgImageAlt"],
                ),
                formName,
            )
        },
    ) { index -> ContextCard(block.items[index]) }
}

private fun contextCardTitle(item: ElementNode): String = when (val title = item.props["title"]) {
    is OpenUIValue.Str -> title.value
    is OpenUIValue.Element -> title.node.props["text"]
        .stringValue
        .orEmpty()
    else -> ""
}

@Composable
private fun ContextCard(item: ElementNode) {
    val theme = OpenUITheme.current
    val props = ComponentProps(item.props)
    val imageSource = props.string("bgImageSrc")
    val hasImage = !imageSource.isNullOrEmpty()
    val shape = RoundedCornerShape(theme.radius.medium)
    Box(
        Modifier
            .fillMaxWidth()
            .heightIn(min = if (hasImage) 150.dp else 100.dp)
            .clip(shape)
            .then(if (hasImage) Modifier else Modifier.background(theme.colors.sunkenSurface)),
    ) {
        if (hasImage) {
            RemoteImage(imageSource, Modifier.matchParentSize(), alt = props.text("bgImageAlt"))
            Box(
                Modifier.matchParentSize().background(
                    Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.15f), Color.Black.copy(alpha = 0.7f))),
                ),
            )
        }
        Column(
            Modifier.fillMaxWidth().heightIn(min = if (hasImage) 150.dp else 100.dp).padding(theme.spacing.m),
            verticalArrangement = Arrangement.spacedBy(theme.spacing.s),
        ) {
            when (val title = item.props["title"]) {
                is OpenUIValue.Element -> CardTagPill(title.node)
                null -> Unit
                else -> Text(
                    JS.toString(title),
                    style = theme.typography.caption.copy(fontWeight = FontWeight.SemiBold),
                    color = if (hasImage) Color.White.copy(alpha = 0.9f) else theme.colors.textSecondary,
                )
            }
            Spacer(Modifier.weight(1f).heightIn(min = if (hasImage) 40.dp else 0.dp))
            props.text("body")?.takeIf { it.isNotEmpty() }?.let {
                Text(
                    inlineMarkdown(it, theme.colors.accent),
                    style = theme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = if (hasImage) Color.White else theme.colors.textPrimary,
                )
            }
        }
    }
}

/** A capsule tag from a `Tag(text, icon?, size?, variant?)` element, at the small size cards use. */
@Composable
private fun CardTagPill(tag: ElementNode, modifier: Modifier = Modifier) {
    val theme = OpenUITheme.current
    val variant = tag.props["variant"].stringValue ?: "neutral"
    val neutral = variant == "neutral"
    val color = if (neutral) theme.colors.textSecondary else theme.colors.variant(variant)
    Row(
        modifier
            .background(
                if (neutral) theme.colors.subtleFill else color.copy(alpha = 0.14f),
                RoundedCornerShape(theme.radius.pill),
            ).padding(horizontal = 8.dp, vertical = 3.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        tag.props["icon"].elementValue?.let { OpenUIIcon(it, Modifier.size(12.dp), tint = color) }
        Text(
            tag.props["text"]?.let(JS::toString).orEmpty(),
            style = theme.typography.caption.copy(fontWeight = FontWeight.Medium),
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** `CompositeCardBlock(items, layout?, ...)` with `CompositeCardItem(id?, header?, body?, footer?)`. */
@Composable
private fun CompositeCardBlockView(context: ComponentRenderProps) {
    val theme = OpenUITheme.current
    val formName = LocalOpenUIFormName.current
    val resources = LocalContext.current.resources
    val block = CardBlockProps(context, theme)
    CardBlockLayout(
        count = block.items.size,
        layout = block.layout,
        minItemWidth = 250.dp,
        carouselItemWidth = 270.dp,
        gap = block.gap,
        isClickable = block.action != null && !context.isStreaming,
        onTap = { index ->
            val item = block.items[index]
            val header = item.props["header"].elementValue
            val title = header.stringProp("title") ?: header.stringProp("value")
            val subtitle = header.stringProp("subtitle") ?: header.stringProp("subtext")
            val alt = header.stringProp("alt")
            val footer = item.props["footer"].objectValue
            val id = item.stringProp("id")
            cardTap(
                context,
                block.action,
                title ?: alt ?: id ?: resources.cardLabel(R.string.stream_ai_openui_card_block_composite, index),
                ItemActionContext.obj(
                    "itemIndex" to OpenUIValue.of(index),
                    "itemId" to id.str(),
                    "itemHeaderTitle" to title.str(),
                    "itemHeaderSubtitle" to subtitle.str(),
                    "itemHeaderAlt" to alt.str(),
                    "itemBodyCount" to OpenUIValue.of(item.props["body"].arrayValue?.size ?: 0),
                    "itemFooterPrice" to footer
                        ?.get("price")
                        .elementValue
                        ?.props
                        ?.get("value"),
                    "itemFooterButtonLabel" to footer
                        ?.get("button")
                        .elementValue
                        ?.props
                        ?.get("label"),
                ),
                formName,
            )
        },
    ) { index -> CompositeCard(block.items[index], context) }
}

@Composable
private fun CompositeCard(item: ElementNode, context: ComponentRenderProps) {
    val theme = OpenUITheme.current
    val body = item.props["body"].arrayValue.orEmpty()
    val footer = item.props["footer"].objectValue
    Column(Modifier.cardSurface(), verticalArrangement = Arrangement.spacedBy(theme.spacing.m)) {
        item.props["header"]?.let { context.RenderNode(it) }
        body.forEach { node -> node.elementValue?.let { OpenUIElement(compositeNormalized(it, inBody = true)) } }
        if (footer != null) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(theme.spacing.m),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.weight(1f)) {
                    footer["price"].elementValue?.let { OpenUIElement(compositeNormalized(it, inBody = false)) }
                }
                footer["button"].elementValue?.let {
                    // The button fills its width elsewhere; here it takes only what its label needs.
                    Box(Modifier.width(IntrinsicSize.Max)) {
                        OpenUIElement(ComponentRenderProps.overriding(it, mapOf("size" to OpenUIValue.Str("small"))))
                    }
                }
            }
        }
    }
}

/** Keeps text size, chart height and list density consistent inside composite cards. */
private fun compositeNormalized(element: ElementNode, inBody: Boolean): ElementNode {
    fun override(vararg pairs: Pair<String, OpenUIValue>) = ComponentRenderProps.overriding(element, mapOf(*pairs))
    return when (element.typeName) {
        "Text", "BoldText" -> if (inBody) override("size" to OpenUIValue.Str("xs")) else element
        "AreaChart", "BarChart", "LineChart" -> override("height" to OpenUIValue.of(180))
        "ListBlock" -> override("size" to OpenUIValue.Str("small"))
        "EntityList" -> override("size" to OpenUIValue.Str("small")).let {
            it.copy(props = it.props.without("header").without("footer"))
        }
        "TagBlock" -> override("size" to OpenUIValue.Str("sm"))
        "Button" -> override("size" to OpenUIValue.Str("small"))
        else -> element
    }
}

/** `VisualCardBlock(items, layout?, ...)` with `VisualCardItem(body, id?, bgImageSrc?, tag?, bgImageAlt?)`. */
@Composable
private fun VisualCardBlockView(context: ComponentRenderProps) {
    val theme = OpenUITheme.current
    val formName = LocalOpenUIFormName.current
    val resources = LocalContext.current.resources
    val block = CardBlockProps(context, theme)
    CardBlockLayout(
        count = block.items.size,
        layout = block.layout,
        minItemWidth = 150.dp,
        carouselItemWidth = 200.dp,
        gap = block.gap,
        isClickable = block.action != null && !context.isStreaming,
        onTap = { index ->
            val item = block.items[index]
            val props = ComponentProps(item.props)
            val body = props.element("body")
            val bodyValue = body.stringProp("value")
            val tagText = props.element("tag").stringProp("text")
            cardTap(
                context,
                block.action,
                bodyValue
                    ?: tagText
                    ?: props.text("id")
                    ?: resources.cardLabel(R.string.stream_ai_openui_card_block_visual, index),
                ItemActionContext.obj(
                    "itemIndex" to OpenUIValue.of(index),
                    "itemId" to props["id"],
                    "itemTag" to tagText.str(),
                    "itemBody" to bodyValue.str(),
                    "itemBodySubtext" to body?.props?.get("subtext"),
                    "itemBgImageSrc" to props["bgImageSrc"],
                    "itemBgImageAlt" to props["bgImageAlt"],
                ),
                formName,
            )
        },
    ) { index -> VisualCard(block.items[index]) }
}

@Composable
private fun VisualCard(item: ElementNode) {
    val theme = OpenUITheme.current
    val props = ComponentProps(item.props)
    val body = props.element("body")?.let { ComponentProps(it.props) }
    Box(Modifier.fillMaxWidth().height(190.dp).clip(RoundedCornerShape(theme.radius.medium))) {
        RemoteImage(props.string("bgImageSrc"), Modifier.matchParentSize(), alt = props.text("bgImageAlt"))
        Box(
            Modifier.matchParentSize().background(
                Brush.verticalGradient(0.5f to Color.Transparent, 1f to Color.Black.copy(alpha = 0.75f)),
            ),
        )
        Column(
            Modifier.align(Alignment.BottomStart).padding(theme.spacing.m),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                body?.text("value").orEmpty(),
                style = theme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                color = Color.White,
            )
            body?.text("subtext")?.takeIf { it.isNotEmpty() }?.let {
                Text(it, style = theme.typography.caption, color = Color.White.copy(alpha = 0.85f))
            }
        }
        props.element("tag")?.let { tag ->
            CardTagPill(
                tag,
                Modifier
                    .align(Alignment.TopStart)
                    .padding(theme.spacing.s)
                    .background(theme.colors.surface.copy(alpha = 0.92f), RoundedCornerShape(theme.radius.pill)),
            )
        }
    }
}
