package io.getstream.chat.android.compose.ui.theme.messages.composer.attachments

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import io.getstream.chat.android.compose.ui.theme.StreamColors
import io.getstream.chat.android.compose.ui.theme.StreamTypography

/**
 * Represents the theming for the attachments preview.
 *
 * @param audioRecording The theming for the audio recording attachment preview.
 */
public data class AttachmentsPreviewTheme(
    val audioRecording: AudioRecordingAttachmentPreviewTheme,
) {

    public companion object {

        /**
         * Builds the default theming for the attachments preview.
         */
        @Composable
        public fun defaultTheme (
            isInDarkMode: Boolean = isSystemInDarkTheme(),
            typography: StreamTypography = StreamTypography.defaultTypography(),
            colors: StreamColors = when (isInDarkMode) {
                true -> StreamColors.defaultDarkColors()
                else -> StreamColors.defaultColors()
            },
        ): AttachmentsPreviewTheme {
            return AttachmentsPreviewTheme(
                audioRecording = AudioRecordingAttachmentPreviewTheme.defaultTheme(isInDarkMode, typography, colors),
            )
        }
    }

}