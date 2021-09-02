package io.getstream.chat.android.compose.ui.attachments.factory

import androidx.compose.runtime.Composable
import com.getstream.sdk.chat.model.ModelType
import io.getstream.chat.android.compose.ui.attachments.AttachmentFactory
import io.getstream.chat.android.compose.ui.attachments.content.GiphyAttachmentContent

/**
 * An extensions of the [AttachmentFactory] that validates and shows Giphy attachments using [GiphyAttachmentContent].
 * */
public class GiphyAttachmentFactory : AttachmentFactory(
    canHandle = { attachments -> attachments.any { it.type == ModelType.attach_giphy } },
    content = @Composable { GiphyAttachmentContent(it) }
)
