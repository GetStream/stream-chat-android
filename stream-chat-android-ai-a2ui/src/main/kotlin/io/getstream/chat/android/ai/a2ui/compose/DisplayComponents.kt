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

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.m3.markdownColor
import com.mikepenz.markdown.m3.markdownTypography
import com.mikepenz.markdown.model.rememberMarkdownState
import io.getstream.chat.android.ai.a2ui.compose.icons.A2UIIcons
import io.getstream.chat.android.ai.a2ui.compose.internal.UrlImageLoader

/**
 * `Text`: simple Markdown, in the `body` style or the smaller `caption` one. Markdown headings
 * (`#` to `####`) give it a hierarchy. Links show as text: the agent writes them, so a tap on one
 * opens nothing.
 */
@Composable
internal fun A2UIText(scope: A2UIComponentScope) {
    val text = scope.string("text") ?: return
    val caption = scope.hint("variant") == "caption"
    val typography = MaterialTheme.typography
    val style = if (caption) typography.bodySmall else typography.bodyMedium
    val color = if (caption) MaterialTheme.colorScheme.onSurfaceVariant else LocalContentColor.current
    val modifier = scope.modifier.padding(LeafMargin)
    if (text.none { it in MarkdownMarkers }) {
        Text(text = text, modifier = modifier, style = style, color = color)
        return
    }
    val state = key(text) { rememberMarkdownState(content = text, immediate = true) }
    CompositionLocalProvider(LocalUriHandler provides NoLinks) {
        Markdown(
            markdownState = state,
            modifier = modifier,
            colors = markdownColor(text = color),
            typography = markdownTypography(
                h1 = typography.headlineSmall.copy(color = color),
                h2 = typography.titleLarge.copy(color = color),
                h3 = typography.titleMedium.copy(color = color),
                h4 = style.copy(fontWeight = FontWeight.SemiBold, color = color),
                h5 = style.copy(fontWeight = FontWeight.SemiBold, color = color),
                h6 = style.copy(fontWeight = FontWeight.SemiBold, color = color),
                text = style.copy(color = color),
                paragraph = style.copy(color = color),
                ordered = style.copy(color = color),
                bullet = style.copy(color = color),
                list = style.copy(color = color),
                quote = style.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                code = typography.bodySmall.copy(color = color),
                inlineCode = typography.bodySmall.copy(color = color),
            ),
        )
    }
}

/**
 * `Image`: an image from the web, sized by its `variant` and fitted by its `fit`. A placeholder
 * shows until it loads, and stays when it can't.
 */
@Composable
internal fun A2UIImage(scope: A2UIComponentScope) {
    val url = scope.string("url")
    val variant = scope.hint("variant")
    val shape = if (variant == "avatar") CircleShape else RoundedCornerShape(ImageCorner)
    val size = when (variant) {
        "icon" -> Modifier.size(IconImageSize)
        "avatar" -> Modifier.size(AvatarSize)
        "smallFeature" -> Modifier.size(SmallFeatureSize)
        "largeFeature" -> Modifier.fillMaxWidth().height(LargeFeatureHeight)
        "header" -> Modifier.fillMaxWidth().height(HeaderHeight)
        else -> Modifier.fillMaxWidth().height(MediumFeatureHeight)
    }
    BoxWithConstraints(
        modifier = scope.modifier
            .padding(LeafMargin)
            .then(size)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        val targetPx = with(LocalDensity.current) { maxOf(maxWidth, maxHeight).roundToPx() }
        var bitmap by remember(url) { mutableStateOf<Bitmap?>(url?.let { UrlImageLoader.cached(it, targetPx) }) }
        if (url != null && bitmap == null) {
            LaunchedEffect(url, targetPx) { bitmap = UrlImageLoader.load(url, targetPx) }
        }
        val image = bitmap
        if (image == null) {
            Icon(
                imageVector = A2UIIcons.vector("photo"),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            Image(
                bitmap = image.asImageBitmap(),
                contentDescription = scope.string("description"),
                contentScale = when (scope.hint("fit")) {
                    "contain" -> ContentScale.Fit
                    "cover" -> ContentScale.Crop
                    "none" -> ContentScale.None
                    "scaleDown" -> ContentScale.Inside
                    else -> ContentScale.FillBounds
                },
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

/**
 * `Icon`: one of the basic catalog's icons by `name`, or a shape the agent draws with an SVG
 * `svgPath` in a 24 by 24 box. An unknown name shows a neutral icon.
 */
@Composable
internal fun A2UIIcon(scope: A2UIComponentScope) {
    val raw = scope.value("name")
    val svgPath = (raw as? Map<*, *>)?.get("svgPath")?.let(scope::resolve)
    val name = raw as? String
    Icon(
        imageVector = (svgPath as? String)?.let(A2UIIcons::fromSvgPath) ?: A2UIIcons.vector(name),
        contentDescription = name,
        modifier = scope.modifier.padding(LeafMargin).size(IconSize),
    )
}

// Characters that may make text Markdown; text without any is drawn as it is.
private const val MarkdownMarkers = "#*_`~>-+|[!\n"

// The agent writes the links of a Text, so a tap on one opens nothing.
private object NoLinks : UriHandler {
    override fun openUri(uri: String) = Unit
}

private val ImageCorner = 8.dp
private val IconImageSize = 24.dp
private val AvatarSize = 40.dp
private val SmallFeatureSize = 100.dp
private val MediumFeatureHeight = 180.dp
private val LargeFeatureHeight = 280.dp
private val HeaderHeight = 200.dp
private val IconSize = 24.dp

/** A placeholder box, for media that has nothing to show yet. */
@Composable
internal fun MediaPlaceholder(modifier: Modifier, content: @Composable () -> Unit) {
    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) { content() }
}

/** The style of a component's label, such as an input's. */
@Composable
internal fun labelStyle(): TextStyle = MaterialTheme.typography.labelLarge
