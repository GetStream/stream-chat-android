package io.getstream.chat.android.compose.ui.attachments

import androidx.compose.runtime.Composable
import com.getstream.sdk.chat.model.ModelType
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.compose.state.messages.attachments.AttachmentState
import io.getstream.chat.android.compose.ui.attachments.content.FileAttachmentContent
import io.getstream.chat.android.compose.ui.attachments.content.GiphyAttachmentContent
import io.getstream.chat.android.compose.ui.attachments.content.ImageAttachmentContent
import io.getstream.chat.android.compose.ui.attachments.content.LinkAttachmentContent
import io.getstream.chat.android.compose.ui.util.hasLink
import io.getstream.chat.android.compose.ui.util.isMedia

/**
 * Provides different attachment factories that build custom message content based on a given attachment.
 *
 * TODO - Migrate back to abstract [AttachmentFactory] and concrete implementations once https://issuetracker.google.com/issues/197727783 is fixed.
 */
public object StreamAttachmentFactories {

    /**
     * The default max length of the link attachments description. We limit this, because for some links the description
     * can be too long.
     */
    private const val DEFAULT_LINK_DESCRIPTION_MAX_LINES = 5

    /**
     * Default attachment factories we provide, which can transform image, file and link attachments.
     *
     * @param linkDescriptionMaxLines - The limit of how long the link attachment descriptions can be.
     * @return A [List] of various [AttachmentFactory] instances that provide different attachments support.
     */
    public fun defaultFactories(
        linkDescriptionMaxLines: Int = DEFAULT_LINK_DESCRIPTION_MAX_LINES,
    ): List<AttachmentFactory> = listOf(
        AttachmentFactory(
            canHandle = { links -> links.any { it.hasLink() && it.type != ModelType.attach_giphy } },
            content = @Composable { LinkAttachmentContent(it, linkDescriptionMaxLines) }
        ),
        AttachmentFactory(
            canHandle = { attachments -> attachments.any { it.type == ModelType.attach_giphy } },
            content = @Composable { GiphyAttachmentContent(it) }
        ),
        AttachmentFactory(
            canHandle = { attachments -> attachments.all { it.isMedia() } },
            content = @Composable { ImageAttachmentContent(it) }
        ),
        AttachmentFactory(
            canHandle = { attachments -> attachments.isNotEmpty() },
            content = @Composable { FileAttachmentContent(it) }
        )
    )
}

/**
 * Holds the information required to build an attachment message.
 *
 * @param canHandle - Checks the message and returns if the factory can consume it or not.
 * @param content - Composable function that allows users to define the content the [AttachmentFactory] will build using any given
 * [AttachmentState].
 */
public open class AttachmentFactory(
    public val canHandle: (attachments: List<Attachment>) -> Boolean,
    public val content: @Composable (AttachmentState) -> Unit,
)
