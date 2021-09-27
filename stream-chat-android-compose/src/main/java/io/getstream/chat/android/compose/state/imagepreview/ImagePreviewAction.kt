package io.getstream.chat.android.compose.state.imagepreview

import io.getstream.chat.android.client.models.Message

/**
 * Represents the actions the user can take with images in the Image Preview feature.
 *
 * @param message The message that the action is being performed on.
 */
internal sealed class ImagePreviewAction(internal val message: Message)

internal class Reply(message: Message) : ImagePreviewAction(message)

internal class ShowInChat(message: Message) : ImagePreviewAction(message)

internal class SaveImage(message: Message) : ImagePreviewAction(message)

internal class Delete(message: Message) : ImagePreviewAction(message)
