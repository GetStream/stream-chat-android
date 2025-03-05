package io.getstream.chat.android.compose.ui.chats

import androidx.compose.runtime.Composable

/**
 * The mode for displaying the list content in the chat screen.
 */
public enum class ListContentMode {
    /**
     * Display the list of channels.
     */
    Channels,

    /**
     * Display the list of threads.
     */
    Threads,
}

/**
 * The mode for displaying extra content in the chat screen.
 */
public sealed class ExtraContentMode {
    /**
     * No extra content is displayed.
     */
    public data object Hidden : ExtraContentMode()

    /**
     * Display extra custom content.
     *
     * @param content The composable content to display.
     */
    public data class Display(val content: @Composable () -> Unit) : ExtraContentMode()
}
