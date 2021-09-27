package io.getstream.chat.android.compose.state.imagepreview

import android.os.Parcelable
import io.getstream.chat.android.compose.state.imagepreview.ImagePreviewResultType.QUOTE
import io.getstream.chat.android.compose.state.imagepreview.ImagePreviewResultType.SHOW_IN_CHAT
import kotlinx.parcelize.Parcelize

/**
 * Represents the Image Preview screen result that we propagate to the Messages screen.
 *
 * @param messageId The ID of the message that we've selected.
 */
@Parcelize
public class ImagePreviewResult(
    public val messageId: String,
    public val resultType: ImagePreviewResultType,
) : Parcelable

/**
 * Represents the types of actions that result in different behavior in the message list.
 *
 * @property SHOW_IN_CHAT The action when the user wants to scroll to and focus a given image.
 * @property QUOTE The action when the user wants to quote and reply to a message.
 */
public enum class ImagePreviewResultType {
    SHOW_IN_CHAT,
    QUOTE,
}
