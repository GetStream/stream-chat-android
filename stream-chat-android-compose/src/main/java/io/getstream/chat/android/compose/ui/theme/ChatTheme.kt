package io.getstream.chat.android.compose.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.header.VersionPrefixHeader
import io.getstream.chat.android.compose.ui.util.DefaultReactionTypes.defaultReactionTypes

/**
 * Local providers for various properties we connect to our components, for styling.
 * */
private val LocalColors = compositionLocalOf<StreamColors> {
    error("No colors provided! Make sure to wrap all usages of Stream components in a ChatTheme.")
}
private val LocalTypography = compositionLocalOf<StreamTypography> {
    error("No typography provided! Make sure to wrap all usages of Stream components in a ChatTheme.")
}
private val LocalShapes = compositionLocalOf<StreamShapes> {
    error("No shapes provided! Make sure to wrap all usages of Stream components in a ChatTheme.")
}
private val LocalAttachmentFactories = compositionLocalOf<List<AttachmentFactory>> {
    error("No attachment factories provided! Make sure to wrap all usages of Stream components in a ChatTheme.")
}
private val LocalReactionTypes = compositionLocalOf<Map<String, Int>> {
    error("No reactions provided! Make sure to wrap all usages of Stream components in a ChatTheme.")
}

/**
 * Our theme that provides all the important properties for styling to the user.
 *
 * @param isInDarkMode - If we're currently in the dark mode or not.
 * @param colors - The set of colors we provide, wrapped in [StreamColors].
 * @param typography - The set of typography styles we provide, wrapped in [StreamTypography].
 * @param shapes - The set of shapes we provide, wrapped in [StreamShapes].
 * @param attachmentFactories - Attachment factories that we provide. By default, images and files.
 * @param reactionTypes - The reaction types supported in the Messaging screen.
 * @param content - The content shown within the theme wrapper.
 * */
@Composable
public fun ChatTheme(
    isInDarkMode: Boolean = isSystemInDarkTheme(),
    colors: StreamColors = if (isInDarkMode) StreamColors.defaultDarkColors() else StreamColors.defaultColors(),
    typography: StreamTypography = StreamTypography.default,
    shapes: StreamShapes = StreamShapes.default,
    attachmentFactories: List<AttachmentFactory> = StreamAttachmentFactories.defaultFactories,
    reactionTypes: Map<String, Int> = defaultReactionTypes,
    content: @Composable () -> Unit,
) {
    LaunchedEffect(Unit) {
        ChatClient.VERSION_PREFIX_HEADER = VersionPrefixHeader.COMPOSE
    }

    CompositionLocalProvider(
        LocalColors provides colors,
        LocalTypography provides typography,
        LocalShapes provides shapes,
        LocalAttachmentFactories provides attachmentFactories,
        LocalReactionTypes provides reactionTypes
    ) {
        content()
    }
}

public object ChatTheme {

    /**
     * These represent the default ease-of-use accessors for colors, typography, shapes, attachment factories and
     * reaction types.
     * */
    public val colors: StreamColors
        @Composable
        @ReadOnlyComposable
        get() = LocalColors.current

    public val typography: StreamTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalTypography.current

    public val shapes: StreamShapes
        @Composable
        @ReadOnlyComposable
        get() = LocalShapes.current

    public val attachmentFactories: List<AttachmentFactory>
        @Composable
        @ReadOnlyComposable
        get() = LocalAttachmentFactories.current

    public val reactionTypes: Map<String, Int>
        @Composable
        @ReadOnlyComposable
        get() = LocalReactionTypes.current
}
