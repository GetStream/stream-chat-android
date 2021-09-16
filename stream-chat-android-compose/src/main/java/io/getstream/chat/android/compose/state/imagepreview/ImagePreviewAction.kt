package io.getstream.chat.android.compose.state.imagepreview

import io.getstream.chat.android.client.models.Message

/**
 * Represents the actions the user can take with images in the Image Preview.
 */
public sealed class ImagePreviewAction(public val message: Message)

public class Reply(message: Message) : ImagePreviewAction(message)

public class ShowInChat(message: Message) : ImagePreviewAction(message)

public class SaveImage(message: Message) : ImagePreviewAction(message)

public class Delete(message: Message) : ImagePreviewAction(message)
