package io.getstream.chat.android.compose.ui.components.messages

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.util.PatternsCompat
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * Default text element for messages, with extra styling and padding for the chat bubble.
 *
 * It detects if we have any annotations/links in the message, and if so, it uses the [ClickableText]
 * component to allow for clicks on said links, that will open the link.
 *
 * Alternatively, it just shows a basic [Text] element.
 *
 * @param message Message to show.
 * @param modifier Modifier for styling.
 * @param onLongItemClick Handler used for long pressing on the message text.
 */
@Composable
public fun MessageText(
    message: Message,
    modifier: Modifier = Modifier,
    onLongItemClick: (Message) -> Unit,
) {
    val context = LocalContext.current

    val styledText = buildAnnotatedMessageText(message)
    val annotations = styledText.getStringAnnotations(0, styledText.lastIndex)

    if (annotations.isNotEmpty()) {
        ClickableText(
            modifier = modifier
                .padding(
                    start = 12.dp,
                    end = 12.dp,
                    top = 8.dp,
                    bottom = 8.dp
                ),
            text = styledText,
            style = ChatTheme.typography.bodyBold,
            onLongPress = { onLongItemClick(message) }
        ) { position ->
            val targetUrl = annotations.firstOrNull {
                position in it.start..it.end
            }?.item

            if (targetUrl != null && targetUrl.isNotEmpty()) {
                context.startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(targetUrl)
                    )
                )
            }
        }
    } else {
        Text(
            modifier = modifier
                .padding(
                    start = 12.dp,
                    end = 12.dp,
                    top = 8.dp,
                    bottom = 8.dp
                ),
            text = styledText,
            style = ChatTheme.typography.bodyBold
        )
    }
}

private val URL_SCHEMES = listOf("http://", "https://")

/**
 * Takes the given message and builds an annotated message text that shows links and allows for clicks,
 * if there are any links available.
 *
 * @param message The message to extract the text from and style.
 *
 * @return The annotated String, with clickable links, if applicable.
 */
@Composable
private fun buildAnnotatedMessageText(message: Message): AnnotatedString {
    val text = message.text

    return buildAnnotatedString {
        // First we add the whole text to the [AnnotatedString] and style it as a regular text.
        append(text)
        addStyle(
            SpanStyle(
                fontStyle = ChatTheme.typography.body.fontStyle,
                color = ChatTheme.colors.textHighEmphasis
            ),
            start = 0,
            end = text.length
        )

        // Then for each available link in the text, we add a different style, to represent the links,
        // as well as add a String annotation to it. This gives us the ability to open the URL on click.
        @SuppressLint("RestrictedApi")
        val matcher = PatternsCompat.AUTOLINK_WEB_URL.matcher(text)
        while (matcher.find()) {
            val start = matcher.start()
            val end = matcher.end()

            addStyle(
                style = SpanStyle(
                    color = ChatTheme.colors.primaryAccent,
                    textDecoration = TextDecoration.Underline,
                ),
                start = start,
                end = end,
            )

            val linkText = requireNotNull(matcher.group(0)!!)

            // Add "http://" prefix if link has no scheme in it
            val url = if (URL_SCHEMES.none { scheme -> linkText.startsWith(scheme) }) {
                URL_SCHEMES[0] + linkText
            } else {
                linkText
            }

            addStringAnnotation(
                tag = "URL",
                annotation = url,
                start = start,
                end = end,
            )
        }
    }
}

/**
 * A spin-off of a Foundation component that allows calling long press handlers.
 * Contains only one additional parameter.
 *
 * @param onLongPress Handler called on long press.
 *
 * @see androidx.compose.foundation.text.ClickableText
 */
@Composable
private fun ClickableText(
    text: AnnotatedString,
    modifier: Modifier = Modifier,
    style: TextStyle = TextStyle.Default,
    softWrap: Boolean = true,
    overflow: TextOverflow = TextOverflow.Clip,
    maxLines: Int = Int.MAX_VALUE,
    onTextLayout: (TextLayoutResult) -> Unit = {},
    onLongPress: () -> Unit,
    onClick: (Int) -> Unit,
) {
    val layoutResult = remember { mutableStateOf<TextLayoutResult?>(null) }
    val pressIndicator = Modifier.pointerInput(onClick, onLongPress) {
        detectTapGestures(
            onLongPress = { onLongPress() },
            onTap = { pos ->
                layoutResult.value?.let { layoutResult ->
                    onClick(layoutResult.getOffsetForPosition(pos))
                }
            }
        )
    }

    BasicText(
        text = text,
        modifier = modifier.then(pressIndicator),
        style = style,
        softWrap = softWrap,
        overflow = overflow,
        maxLines = maxLines,
        onTextLayout = {
            layoutResult.value = it
            onTextLayout(it)
        }
    )
}
