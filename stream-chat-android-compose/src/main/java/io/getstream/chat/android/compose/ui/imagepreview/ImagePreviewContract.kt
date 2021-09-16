package io.getstream.chat.android.compose.ui.imagepreview

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import io.getstream.chat.android.compose.state.imagepreview.ImagePreviewResult

/**
 * The contract used to start the [ImagePreviewActivity] given a message ID and the position of the clicked attachment.
 */
public class ImagePreviewContract : ActivityResultContract<Pair<String, Int>, ImagePreviewResult?>() {

    /**
     * Creates the intent to start the [ImagePreviewActivity]. It receives a data pair of a [String] and an [Int] that
     * represent the messageId and the attachmentPosition.
     *
     * @return The [Intent] to
     */
    override fun createIntent(context: Context, data: Pair<String, Int>): Intent {
        return ImagePreviewActivity.getIntent(context, messageId = data.first, attachmentPosition = data.second)
    }

    /**
     * We parse the result as [ImagePreviewResult], which can be null in case there is no result to return.
     */
    override fun parseResult(resultCode: Int, intent: Intent?): ImagePreviewResult? {
        return intent?.getParcelableExtra(ImagePreviewActivity.KEY_IMAGE_PREVIEW_RESULT)
    }
}
