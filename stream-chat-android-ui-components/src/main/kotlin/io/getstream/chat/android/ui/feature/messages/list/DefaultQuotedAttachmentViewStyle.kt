/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.ui.feature.messages.list

import android.content.Context
import androidx.annotation.Px
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.helper.TransformStyle
import io.getstream.chat.android.ui.helper.ViewStyle
import io.getstream.chat.android.ui.utils.extensions.getDimension

/**
 * Style to be applied to [DefaultQuotedAttachmentView].
 * Use [TransformStyle.defaultQuotedAttachmentViewStyleTransformer] to change the style programmatically.
 *
 * @param fileAttachmentHeight The height of the quoted file attachment.
 * @param fileAttachmentWidth The width of the quoted file attachment.
 * @param imageAttachmentHeight The width of the quoted image attachment.
 * @param imageAttachmentWidth The height of the quoted image attachment.
 * @param quotedImageRadius The radius of the quoted attachment corners.
 */
public class DefaultQuotedAttachmentViewStyle(
    @Px public val fileAttachmentHeight: Int,
    @Px public val fileAttachmentWidth: Int,
    @Px public val imageAttachmentHeight: Int,
    @Px public val imageAttachmentWidth: Int,
    @Px public val quotedImageRadius: Int,
) : ViewStyle {

    internal companion object {
        operator fun invoke(context: Context): DefaultQuotedAttachmentViewStyle {
            val fileAttachmentHeight: Int = context.getDimension(R.dimen.streamUiQuotedFileAttachmentViewHeight)
            val fileAttachmentWidth: Int = context.getDimension(R.dimen.streamUiQuotedFileAttachmentViewWidth)
            val imageAttachmentHeight: Int = context.getDimension(R.dimen.streamUiQuotedImageAttachmentViewHeight)
            val imageAttachmentWidth: Int = context.getDimension(R.dimen.streamUiQuotedImageAttachmentViewWidth)
            val quotedImageRadius: Int = context.getDimension(R.dimen.streamUiQuotedImageAttachmentImageRadius)

            return DefaultQuotedAttachmentViewStyle(
                fileAttachmentHeight = fileAttachmentHeight,
                fileAttachmentWidth = fileAttachmentWidth,
                imageAttachmentHeight = imageAttachmentHeight,
                imageAttachmentWidth = imageAttachmentWidth,
                quotedImageRadius = quotedImageRadius,
            ).let(TransformStyle.defaultQuotedAttachmentViewStyleTransformer::transform)
        }
    }
}
