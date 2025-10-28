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

package io.getstream.chat.android.ui.feature.messages.list.background

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat.setTint
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.utils.extensions.dpToPxPrecise
import io.getstream.chat.android.ui.utils.extensions.isRtlLayout

/**
 * Class that creates the default version of ShapeAppearanceModel used in the background of messages, attachments, pictures...
 */
internal object ShapeAppearanceModelFactory {

    private val CORNER_SIZE_FILE_PX = 16.dpToPxPrecise()
    private val CORNER_SIZE_AUDIO_PX = 14.dpToPxPrecise()
    private val STROKE_WIDTH_PX = 1.dpToPxPrecise()

    /**
     * Creates the ShapeAppearanceModel.
     *
     * @param context [Context].
     * @param defaultCornerRadius The corner radius of all corners with the exception of one.
     * @param differentCornerRadius The corner radius of one of the corners accordingly with the logic on the method.
     * @param isMine Whether the message is from the current user or not. Used to position the differentCorner.
     * @param isBottomPosition Whether the message the bottom position or not. Used to position the differentCorner.
     */
    fun create(
        context: Context,
        defaultCornerRadius: Float,
        differentCornerRadius: Float,
        isMine: Boolean,
        isBottomPosition: Boolean,
    ): ShapeAppearanceModel = ShapeAppearanceModel.builder()
        .setAllCornerSizes(defaultCornerRadius)
        .apply {
            if (isBottomPosition) {
                val isRtl = context.isRtlLayout

                when {
                    !isRtl && isMine -> setBottomRightCornerSize(differentCornerRadius)

                    !isRtl && !isMine -> setBottomLeftCornerSize(differentCornerRadius)

                    isRtl && isMine -> setBottomLeftCornerSize(differentCornerRadius)

                    isRtl && !isMine -> setBottomRightCornerSize(differentCornerRadius)
                }
            }
        }
        .build()

    fun fileBackground(context: Context): MaterialShapeDrawable = ShapeAppearanceModel.builder()
        .setAllCornerSizes(CORNER_SIZE_FILE_PX)
        .build()
        .let(::MaterialShapeDrawable)
        .apply {
            setStroke(
                STROKE_WIDTH_PX,
                ContextCompat.getColor(context, R.color.stream_ui_grey_whisper),
            )
            setTint(ContextCompat.getColor(context, R.color.stream_ui_white))
        }

    fun audioBackground(context: Context): MaterialShapeDrawable = ShapeAppearanceModel.builder()
        .setAllCornerSizes(CORNER_SIZE_AUDIO_PX)
        .build()
        .let(::MaterialShapeDrawable)
        .apply {
            setStroke(
                STROKE_WIDTH_PX,
                ContextCompat.getColor(context, R.color.stream_ui_grey_whisper),
            )
            setTint(ContextCompat.getColor(context, R.color.stream_ui_white))
        }
}
