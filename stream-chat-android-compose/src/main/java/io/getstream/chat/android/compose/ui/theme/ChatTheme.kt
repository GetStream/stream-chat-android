package io.getstream.chat.android.compose.ui.theme

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import io.getstream.chat.android.client.models.Message

/**
 * Local providers for various properties we connect to our components, for styling.
 * */
private val LocalColors = compositionLocalOf<StreamColors> { error("No colors provided!") }
private val LocalTypography = compositionLocalOf<StreamTypography> { error("No typography provided!") }
private val LocalShapes = compositionLocalOf<StreamShapes> { error("No shapes provided!") }
private val LocalAttachmentFactories =
    compositionLocalOf<List<AttachmentFactory>> { error("No attachment factories provided!") }

/**
 * Our theme that provides all the important properties for styling to the user.
 *
 * @param isInDarkMode - If we're currently in the dark mode or not.
 * @param colors - The set of colors we provide, wrapped in [StreamColors].
 * @param typography - The set of typography styles we provide, wrapped in [StreamTypography].
 * @param shapes - The set of shapes we provide, wrapped in [StreamShapes].
 * @param attachmentFactories - Attachment factories that we provide. By default, images and files.
 * @param content - The content shown within the theme wrapper.
 * */
@Composable
fun ChatTheme(
    isInDarkMode: Boolean = isSystemInDarkTheme(),
    colors: StreamColors = if (isInDarkMode) StreamColors.defaultDarkColors() else StreamColors.defaultColors(),
    typography: StreamTypography = StreamTypography.default,
    shapes: StreamShapes = StreamShapes.default,
    attachmentFactories: List<AttachmentFactory> = StreamAttachmentFactories.defaultFactories,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalColors provides colors,
        LocalTypography provides typography,
        LocalShapes provides shapes,
        LocalAttachmentFactories provides attachmentFactories
    ) {
        content()
    }
}

object ChatTheme {

    /**
     * These represent the default ease-of-use accessors for colors, typography, shapes and attachment
     * factories.
     * */
    val colors: StreamColors
        @Composable
        get() = LocalColors.current

    val typography: StreamTypography
        @Composable
        get() = LocalTypography.current

    val shapes: StreamShapes
        @Composable
        get() = LocalShapes.current

    val attachmentFactories: List<AttachmentFactory>
        @Composable
        get() = LocalAttachmentFactories.current
}