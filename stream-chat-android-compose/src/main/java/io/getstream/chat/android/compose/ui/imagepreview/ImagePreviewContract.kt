package io.getstream.chat.android.compose.ui.imagepreview

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import io.getstream.chat.android.compose.state.imagepreview.ImagePreviewResult

/**
 * The contract used to start the [ImagePreviewActivity] given a message ID and the position of the clicked attachment.
 */
public class ImagePreviewContract : ActivityResultContract<ImagePreviewContract.Input, ImagePreviewResult?>() {

    /**
     * Creates the intent to start the [ImagePreviewActivity]. It receives a data pair of a [String] and an [Int] that
     * represent the messageId and the attachmentPosition.
     *
     * @return The [Intent] to start the [ImagePreviewActivity].
     */
    override fun createIntent(context: Context, data: Input): Intent {
        return ImagePreviewActivity.getIntent(
            context,
            messageId = data.messageId,
            attachmentPosition = data.initialPosition
        )
    }

    /**
     * We parse the result as [ImagePreviewResult], which can be null in case there is no result to return.
     *
     * @return The [ImagePreviewResult] or null if it doesn't exist.
     */
    override fun parseResult(resultCode: Int, intent: Intent?): ImagePreviewResult? {
        return intent?.getParcelableExtra(ImagePreviewActivity.KEY_IMAGE_PREVIEW_RESULT)
    }

    /**
     * Defines the input for the [ImagePreviewContract].
     *
     * @param messageId The ID of the message.
     * @param initialPosition The initial position of the Image gallery, based on the clicked item.
     */
    public class Input(
        public val messageId: String,
        public val initialPosition: Int = 0,
    )
}
