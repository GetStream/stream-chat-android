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

package io.getstream.chat.android.ui.message.list

import android.content.Context
import androidx.annotation.Px
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.TransformStyle
import io.getstream.chat.android.ui.common.extensions.internal.getDimension

/**
 * Style to be applied to [DefaultQuotedAttachmentView].
 * Use [TransformStyle.defaultQuotedAttachmentViewStyleTransformer] to change the style programmatically.
 *
 * @param height The height of the quoted attachment.
 * @param width The width of the quoted attachment.
 * @param radius The radius of the quoted attachment corners.
 */
public class DefaultQuotedAttachmentViewStyle(
    @Px public val height: Int,
    @Px public val width: Int,
    @Px public val radius: Int,
) {

    internal companion object {
        operator fun invoke(context: Context): DefaultQuotedAttachmentViewStyle {
            val height: Int = context.getDimension(R.dimen.streamUiQuotedAttachmentViewHeight)
            val width: Int = context.getDimension(R.dimen.streamUiQuotedAttachmentViewWidth)
            val radius: Int = context.getDimension(R.dimen.streamUiQuotedAttachmentImageRadius)

            return DefaultQuotedAttachmentViewStyle(
                height = height,
                width = width,
                radius = radius
            ).let(TransformStyle.defaultQuotedAttachmentViewStyleTransformer::transform)
        }
    }
}
