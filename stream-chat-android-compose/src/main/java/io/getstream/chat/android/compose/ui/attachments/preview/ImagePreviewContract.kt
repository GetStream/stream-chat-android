/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getstream.chat.android.compose.ui.attachments.preview

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
    override fun createIntent(context: Context, input: Input): Intent {
        return ImagePreviewActivity.getIntent(
            context,
            messageId = input.messageId,
            attachmentPosition = input.initialPosition
        )
    }

    /**
     * We parse the result as [ImagePreviewResult], which can be null in case there is no result to return.
     *
     * @return The [ImagePreviewResult] or null if it doesn't exist.
     */
    override fun parseResult(resultCode: Int, intent: Intent?): ImagePreviewResult? {
        return intent?.getParcelableExtra(ImagePreviewActivity.KeyImagePreviewResult)
    }

    /**
     * Defines the input for the [ImagePreviewContract].
     *
     * @param messageId The ID of the message.
     * @param initialPosition The initial position of the Image gallery, based on the clicked item.
     * @param skipEnrichUrl If set to true will skip enriching URLs when you update the message
     * by deleting an attachment contained within it. Set to false by default.
     */
    public class Input(
        public val messageId: String,
        public val initialPosition: Int = 0,
        public val skipEnrichUrl: Boolean = false,
    )
}
