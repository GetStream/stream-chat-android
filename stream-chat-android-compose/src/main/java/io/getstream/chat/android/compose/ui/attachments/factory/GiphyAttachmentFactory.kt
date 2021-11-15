package io.getstream.chat.android.compose.ui.attachments.factory

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import com.getstream.sdk.chat.model.ModelType
import io.getstream.chat.android.compose.ui.attachments.AttachmentFactory
import io.getstream.chat.android.compose.ui.attachments.content.GiphyAttachmentContent
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * An [AttachmentFactory] that validates and shows Giphy attachments using [GiphyAttachmentContent].
 *
 * Has no "preview content", given that this attachment only exists after being sent.
 */
@Suppress("FunctionName")
public fun GiphyAttachmentFactory(): AttachmentFactory = AttachmentFactory(
    canHandle = { attachments -> attachments.any { it.type == ModelType.attach_giphy } },
    previewContent = @Composable { _, _, _ -> },
    content = @Composable { modifier, state ->
        GiphyAttachmentContent(
            modifier = modifier.size(
                width = ChatTheme.dimens.attachmentsContentGiphyWidth,
                height = ChatTheme.dimens.attachmentsContentGiphyHeight
            ),
            attachmentState = state
        )
    },
)
