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
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import io.getstream.chat.android.ai.openui.R
import io.getstream.chat.android.ai.openui.icons.Circle
import io.getstream.chat.android.ai.openui.icons.Dangerous
import io.getstream.chat.android.ai.openui.lang.OpenUIObject
import io.getstream.chat.android.ai.openui.lang.OpenUIValue
import io.getstream.chat.android.ai.openui.lang.ReactiveBinding
import io.getstream.chat.android.ai.openui.lang.objectValue
import io.getstream.chat.android.ai.openui.lang.stringValue
import io.getstream.chat.android.ai.openui.rendering.ComponentRenderProps
import io.getstream.chat.android.ai.openui.rendering.ComponentRenderer
import io.getstream.chat.android.ai.openui.rendering.LocalOpenUICanOpenUrl
import io.getstream.chat.android.ai.openui.rendering.OpenUICodeBlock
import io.getstream.chat.android.ai.openui.rendering.OpenUIMarkdown
import io.getstream.chat.android.ai.openui.rendering.OpenUITheme
import io.getstream.chat.android.ai.openui.rendering.inlineMarkdownText
import kotlinx.coroutines.delay

private fun contentRenderer(block: ComponentRenderer): ComponentRenderer = block

internal val contentRenderers: Map<String, ComponentRenderer> = mapOf(
    "CardHeader" to contentRenderer { CardHeaderView(it) },
    "TextContent" to contentRenderer { TextContentView(it) },
    "MarkDownRenderer" to contentRenderer { MarkdownRendererView(it) },
    "Callout" to contentRenderer { CalloutView(it) },
    "TextCallout" to contentRenderer { TextCalloutView(it) },
    "Image" to contentRenderer { ImageComponentView(it) },
    "ImageBlock" to contentRenderer { ImageBlockView(it) },
    "ImageGallery" to contentRenderer { ImageGalleryView(it) },
    "CodeBlock" to contentRenderer { CodeBlockComponentView(it) },
    "Separator" to contentRenderer { SeparatorView(it) },
    "InlineHeader" to contentRenderer { InlineHeaderView(it) },
    "Label" to contentRenderer { LabelView(it) },
)

internal val chatContentRenderers: Map<String, ComponentRenderer> = mapOf(
    "Card" to contentRenderer { ChatCardView(it) },
)

internal val standardContentRenderers: Map<String, ComponentRenderer> = mapOf(
    "Card" to contentRenderer { StandardCardView(it) },
    "Stack" to contentRenderer { StackView(it) },
)

// Citations -----------------------------------------------------------------------------------

/** Card sources keyed by their 1-based citation index, used to make `[n]` citations tappable. */
internal val LocalCitationSources = compositionLocalOf<Map<Int, String>> { emptyMap() }

internal object Citations {
    private val pattern = Regex("""\[(\d+)](?!\()""")
    private val fence = Regex("""^ {0,3}(`{3,}|~{3,})""")
    private val codeSpan = Regex("""(`+)[\s\S]*?(?<!`)\1(?!`)""")

    /**
     * Turns `[1]` citations into Markdown links when a source URL exists for them. Code spans and
     * fenced code show their text as written, so citations in them stay as they are.
     */
    fun link(text: String, sources: Map<Int, String>): String {
        if (sources.isEmpty() || '[' !in text) return text
        var openFence: String? = null
        return text.split('\n').joinToString("\n") { line ->
            val marker = fence.find(line)?.groupValues?.get(1)
            val inFence = openFence
            when {
                inFence != null -> {
                    val closes = marker != null && marker[0] == inFence[0] && marker.length >= inFence.length &&
                        line.trim().all { it == inFence[0] }
                    if (closes) openFence = null
                    line
                }
                marker != null -> {
                    openFence = marker
                    line
                }
                else -> linkOutsideCodeSpans(line, sources)
            }
        }
    }

    private fun linkOutsideCodeSpans(line: String, sources: Map<Int, String>): String {
        if ('[' !in line) return line
        var start = 0
        return buildString {
            codeSpan.findAll(line).forEach { span ->
                append(linkCitations(line.substring(start, span.range.first), sources))
                append(span.value)
                start = span.range.last + 1
            }
            append(linkCitations(line.substring(start), sources))
        }
    }

    private fun linkCitations(text: String, sources: Map<Int, String>): String = pattern.replace(text) { match ->
        val number = match.groupValues[1].toIntOrNull() ?: 0
        val url = sources[number] ?: return@replace match.value
        "[[$number]](${encode(url)})"
    }

    // Keeps the URL one Markdown link destination (like `urlFragmentAllowed` percent-encoding).
    private fun encode(url: String): String = buildString {
        for (char in url) {
            when (char) {
                ' ' -> append("%20")
                '(' -> append("%28")
                ')' -> append("%29")
                '<' -> append("%3C")
                '>' -> append("%3E")
                '"' -> append("%22")
                else -> append(char)
            }
        }
    }
}

// Cards and stacks ----------------------------------------------------------------------------

/** Chat `Card(children, sources?)`: the vertical container for a whole response. */
@Composable
private fun ChatCardView(context: ComponentRenderProps) {
    val theme = OpenUITheme.current
    val sources = context.props.array("sources").mapNotNull { it.objectValue }
    val citations = sources
        .withIndex()
        .mapNotNull { (index, source) ->
            source["url"].stringValue?.takeIf { it.isNotEmpty() }?.let { index + 1 to it }
        }.toMap()
    CompositionLocalProvider(LocalCitationSources provides citations) {
        Column(
            Modifier.fillMaxWidth().openUISurface("card"),
            verticalArrangement = Arrangement.spacedBy(theme.spacing.m),
        ) {
            context.RenderProp("children")
            if (sources.isNotEmpty()) SourcesStrip(sources, context)
        }
    }
}

// The strip, its overflow sheet and their shared row read best as one composable.
@Suppress("LongMethod")
@Composable
private fun SourcesStrip(sources: List<OpenUIObject>, context: ComponentRenderProps) {
    val theme = OpenUITheme.current
    val canOpen = LocalOpenUICanOpenUrl.current
    Column(verticalArrangement = Arrangement.spacedBy(theme.spacing.s)) {
        HorizontalDivider(color = theme.colors.border)
        Text(
            stringResource(R.string.stream_ai_openui_sources_title),
            style = theme.typography.caption.copy(fontWeight = FontWeight.SemiBold),
            color = theme.colors.textSecondary,
        )
        Row(
            Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(theme.spacing.s),
        ) {
            sources.forEachIndexed { index, source ->
                val url = source["url"].stringValue
                Row(
                    Modifier
                        .width(180.dp)
                        .clip(RoundedCornerShape(theme.radius.small))
                        .background(theme.colors.sunkenSurface)
                        .clickable(enabled = canOpen(url)) {
                            context.triggerAction(
                                label = "",
                                action = OpenUIValue.obj(
                                    "type" to OpenUIValue.Str("open_url"),
                                    "url" to OpenUIValue.Str(url.orEmpty()),
                                ),
                            )
                        }.padding(theme.spacing.s),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Box(
                        Modifier.size(18.dp).background(theme.colors.accent, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            "${index + 1}",
                            style = theme.typography.caption.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                            color = theme.colors.onAccent,
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            source["title"].stringValue ?: stringResource(R.string.stream_ai_openui_sources_untitled),
                            style = theme.typography.caption.copy(fontWeight = FontWeight.Medium),
                            color = theme.colors.textPrimary,
                            maxLines = 2,
                        )
                        Text(
                            source["sourceName"].stringValue.orEmpty(),
                            style = theme.typography.caption.copy(fontSize = 11.sp),
                            color = theme.colors.textSecondary,
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    }
}

/** Standard `Card(children, variant?, direction?, gap?, align?, justify?, wrap?)`. */
@Composable
private fun StandardCardView(context: ComponentRenderProps) {
    val theme = OpenUITheme.current
    val props = context.props
    FlexContainer(
        direction = props.string("direction") ?: "column",
        gap = theme.spacing.value(props.string("gap"), theme.spacing.m),
        align = props.string("align"),
        justify = props.string("justify"),
        wrap = props.bool("wrap") ?: false,
        flexibleChildren = true,
        itemCount = props.array("children").size,
        modifier = Modifier.fillMaxWidth().openUISurface(props.string("variant") ?: "card"),
    ) {
        context.RenderProp("children")
    }
}

private val stackFlexibleTypes = setOf("Card", "Stack", "MarkDownRenderer", "Callout", "TextCallout", "Form", "Table")

/** `Stack(children, direction?, gap?, align?, justify?, wrap?)`. */
@Composable
private fun StackView(context: ComponentRenderProps) {
    val theme = OpenUITheme.current
    val props = context.props
    val children = props.elements("children")
    val wrap = props.bool("wrap") ?: false
    val justify = if (wrap && props.string("justify") == "between") "start" else props.string("justify")
    FlexContainer(
        direction = props.string("direction") ?: "column",
        gap = theme.spacing.value(props.string("gap"), theme.spacing.m),
        align = props.string("align"),
        justify = justify,
        wrap = wrap,
        flexibleChildren = children.isNotEmpty() && children.all { it.typeName in stackFlexibleTypes },
        itemCount = children.size,
    ) {
        context.RenderProp("children")
    }
}

// Text ----------------------------------------------------------------------------------------

/** `TextContent(text, size?)`: Markdown text. */
@Composable
private fun TextContentView(context: ComponentRenderProps) {
    val theme = OpenUITheme.current
    val style = when (context.props.string("size")) {
        "small" -> theme.typography.bodySmall
        "large" -> theme.typography.bodyLarge
        "small-heavy" -> theme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
        "large-heavy" -> theme.typography.title.copy(fontWeight = FontWeight.Bold)
        else -> theme.typography.body
    }
    SelectionContainer {
        OpenUIMarkdown(
            Citations.link(context.props.text("text").orEmpty(), LocalCitationSources.current),
            style = style,
        )
    }
}

/** `MarkDownRenderer(textMarkdown, variant?)`. */
@Composable
private fun MarkdownRendererView(context: ComponentRenderProps) {
    Box(Modifier.fillMaxWidth().openUISurface(context.props.string("variant") ?: "clear")) {
        SelectionContainer {
            OpenUIMarkdown(Citations.link(context.props.text("textMarkdown").orEmpty(), LocalCitationSources.current))
        }
    }
}

/** `CardHeader(title?, subtitle?)`. */
@Composable
private fun CardHeaderView(context: ComponentRenderProps) {
    val theme = OpenUITheme.current
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(theme.spacing.xs)) {
        context.props.text("title")?.takeIf { it.isNotEmpty() }?.let {
            Text(
                inlineMarkdownText(it, theme.colors.accent),
                style = theme.typography.title,
                color = theme.colors.textPrimary,
            )
        }
        context.props.text("subtitle")?.takeIf { it.isNotEmpty() }?.let {
            Text(
                inlineMarkdownText(it, theme.colors.accent),
                style = theme.typography.bodySmall,
                color = theme.colors.textSecondary,
            )
        }
    }
}

/** `InlineHeader(heading, description?)`. */
@Composable
private fun InlineHeaderView(context: ComponentRenderProps) {
    val theme = OpenUITheme.current
    Column(Modifier.fillMaxWidth().padding(top = theme.spacing.xs), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            inlineMarkdownText(context.props.text("heading").orEmpty(), theme.colors.accent),
            style = theme.typography.heading,
            color = theme.colors.textPrimary,
        )
        context.props.text("description")?.takeIf { it.isNotEmpty() }?.let {
            Text(
                inlineMarkdownText(it, theme.colors.accent),
                style = theme.typography.caption,
                color = theme.colors.textSecondary,
            )
        }
    }
}

/** `Label(text)`. */
@Composable
private fun LabelView(context: ComponentRenderProps) {
    val theme = OpenUITheme.current
    Text(
        context.props.text("text").orEmpty(),
        style = theme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
        color = theme.colors.textPrimary,
    )
}

// Callouts ------------------------------------------------------------------------------------

/** `Callout(variant, title, description, visible?)`: auto-dismisses after 3s when `visible` is bound. */
@Composable
private fun CalloutView(context: ComponentRenderProps) {
    val theme = OpenUITheme.current
    val binding = context.props["visible"]
    val field = context.stateField("visible", binding)
    val isVisible = if (field.isReactive) {
        field.value == OpenUIValue.True ||
            field.value == OpenUIValue.Str("true")
    } else {
        // A literal `false` hides it; without `visible` it shows.
        field.value != OpenUIValue.False && field.value != OpenUIValue.Str("false")
    }
    val currentField by rememberUpdatedState(field)
    // Keyed on the binding too, so a callout bound to another `$variable` waits its own 3s.
    LaunchedEffect(ReactiveBinding.target(binding), field.isReactive && isVisible) {
        if (!(field.isReactive && isVisible)) return@LaunchedEffect
        delay(CalloutDismissMillis)
        currentField.setValue(OpenUIValue.False)
    }
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn() + slideInVertically { -it / 2 },
        exit = fadeOut() + slideOutVertically { -it / 2 },
    ) {
        val variant = context.props.string("variant") ?: "info"
        val color = theme.colors.variant(variant)
        Row(
            Modifier.fillMaxWidth().tinted(color, theme.radius.medium).padding(theme.spacing.m),
            horizontalArrangement = Arrangement.spacedBy(theme.spacing.m),
        ) {
            Icon(
                when (variant) {
                    "success" -> Icons.Rounded.CheckCircle
                    "warning" -> Icons.Rounded.Warning
                    "error", "danger" -> Icons.Rounded.Dangerous
                    "neutral" -> Icons.Rounded.Circle
                    else -> Icons.Rounded.Info
                },
                contentDescription = calloutDescription(variant),
                tint = color,
                modifier = Modifier.size(20.dp),
            )
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(theme.spacing.xs)) {
                context.props.text("title")?.takeIf { it.isNotEmpty() }?.let {
                    Text(
                        it,
                        style = theme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = theme.colors.textPrimary,
                    )
                }
                context.props.text("description")?.takeIf { it.isNotEmpty() }?.let {
                    OpenUIMarkdown(it, style = theme.typography.bodySmall, color = theme.colors.textSecondary)
                }
            }
        }
    }
}

/** What a callout's icon means, for TalkBack; `null` for a neutral or unknown variant. */
@Composable
private fun calloutDescription(variant: String): String? = when (variant) {
    "info" -> stringResource(R.string.stream_ai_openui_callout_info)
    "success" -> stringResource(R.string.stream_ai_openui_callout_success)
    "warning" -> stringResource(R.string.stream_ai_openui_callout_warning)
    "error", "danger" -> stringResource(R.string.stream_ai_openui_callout_error)
    else -> null
}

/** `TextCallout(variant?, title?, description?)`. */
@Composable
private fun TextCalloutView(context: ComponentRenderProps) {
    val theme = OpenUITheme.current
    val color = theme.colors.variant(context.props.string("variant") ?: "neutral")
    Row(
        Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clip(RoundedCornerShape(theme.radius.small))
            .background(color.copy(alpha = 0.08f)),
    ) {
        Box(Modifier.width(3.dp).fillMaxHeight().background(color))
        Column(
            Modifier.weight(1f).padding(theme.spacing.m),
            verticalArrangement = Arrangement.spacedBy(theme.spacing.xs),
        ) {
            context.props.text("title")?.takeIf { it.isNotEmpty() }?.let {
                Text(
                    it,
                    style = theme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = theme.colors.textPrimary,
                )
            }
            context.props.text("description")?.takeIf { it.isNotEmpty() }?.let {
                Text(
                    inlineMarkdownText(it, theme.colors.accent),
                    style = theme.typography.bodySmall,
                    color = theme.colors.textSecondary,
                )
            }
        }
    }
}

// Images --------------------------------------------------------------------------------------

/** `Image(alt, src?)`. */
@Composable
private fun ImageComponentView(context: ComponentRenderProps) {
    val theme = OpenUITheme.current
    RemoteImage(
        url = context.props.string("src"),
        alt = context.props.text("alt"),
        modifier = Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(theme.radius.medium)),
    )
}

/** `ImageBlock(src, alt?)`. */
@Composable
private fun ImageBlockView(context: ComponentRenderProps) {
    val theme = OpenUITheme.current
    RemoteImage(
        url = context.props.string("src"),
        alt = context.props.text("alt"),
        modifier = Modifier.fillMaxWidth().aspectRatio(ImageAspectRatio).clip(RoundedCornerShape(theme.radius.medium)),
    )
}

/** `ImageGallery(images)`: thumbnails with a full-screen preview. */
@Composable
private fun ImageGalleryView(context: ComponentRenderProps) {
    val theme = OpenUITheme.current
    val images = context.props.array("images").mapNotNull { it.objectValue }
    if (images.isEmpty()) return
    var selection by remember { mutableStateOf<Int?>(null) }
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val spacing = theme.spacing.s
        val columns = maxOf(1, ((maxWidth + spacing) / (100.dp + spacing)).toInt())
        Column(verticalArrangement = Arrangement.spacedBy(spacing)) {
            images.withIndex().chunked(columns).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(spacing)) {
                    row.forEach { (index, image) ->
                        RemoteImage(
                            url = image["src"].stringValue,
                            alt = image["alt"].stringValue,
                            modifier = Modifier
                                .weight(1f)
                                .height(100.dp)
                                .clip(RoundedCornerShape(theme.radius.small))
                                .clickable { selection = index },
                        )
                    }
                    repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
    selection?.let { initial -> GalleryPreview(images, initial) { selection = null } }
}

@Composable
private fun GalleryPreview(images: List<OpenUIObject>, initial: Int, onDismiss: () -> Unit) {
    val theme = OpenUITheme.current
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(Modifier.fillMaxSize(), color = theme.colors.surface) {
            Column {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text(stringResource(R.string.stream_ai_openui_gallery_done)) }
                }
                val pager = rememberPagerState(initialPage = initial) { images.size }
                HorizontalPager(pager, Modifier.weight(1f)) { page ->
                    val image = images[page]
                    Column(
                        Modifier.fillMaxSize().padding(bottom = theme.spacing.l),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(theme.spacing.m),
                    ) {
                        RemoteImage(
                            url = image["src"].stringValue,
                            alt = image["alt"].stringValue,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxWidth().weight(1f),
                        )
                        (image["details"].stringValue ?: image["alt"].stringValue)?.let {
                            Text(
                                it,
                                style = theme.typography.bodySmall,
                                color = theme.colors.textSecondary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = theme.spacing.l),
                            )
                        }
                    }
                }
            }
        }
    }
}

// Misc ----------------------------------------------------------------------------------------

/** `CodeBlock(language, codeString)`. */
@Composable
private fun CodeBlockComponentView(context: ComponentRenderProps) {
    OpenUICodeBlock(context.props.text("language"), context.props.text("codeString").orEmpty())
}

/** `Separator(orientation?, decorative?)`. */
@Composable
private fun SeparatorView(context: ComponentRenderProps) {
    val theme = OpenUITheme.current
    if (context.props.string("orientation") == "vertical") {
        VerticalDivider(Modifier.height(24.dp), color = theme.colors.border)
    } else {
        HorizontalDivider(color = theme.colors.border)
    }
}

// How long a callout bound to a `$variable` shows before it dismisses itself.
private const val CalloutDismissMillis = 3_000L

// The width-to-height ratio of an image block.
private const val ImageAspectRatio = 16f / 9f
