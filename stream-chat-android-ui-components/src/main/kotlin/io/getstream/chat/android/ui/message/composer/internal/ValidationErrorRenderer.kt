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

package io.getstream.chat.android.ui.message.composer.internal

import android.content.Context
import android.widget.Toast
import com.getstream.sdk.chat.utils.MediaStringUtil
import io.getstream.chat.android.common.state.ValidationError
import io.getstream.chat.android.ui.R

/**
 * A helper class that can be used to display a validation error for the
 * current user input.
 *
 * @param context The [Context] used to display a [Toast].
 */
internal class ValidationErrorRenderer(private val context: Context) {

    /**
     * The validation error from the previous state update.
     */
    private var previousValidationError: ValidationError? = null

    /**
     * Displays the first validation error from the list of errors using a [Toast].
     *
     * @param validationErrors The list of validation errors based on the current user input.
     */
    fun renderValidationErrors(validationErrors: List<ValidationError>) {
        val currentValidationError = validationErrors.firstOrNull()

        // Don't display the validation error if it hasn't changed since the last state update.
        if (currentValidationError != null && currentValidationError != previousValidationError) {
            val errorMessage = when (currentValidationError) {
                is ValidationError.MessageLengthExceeded -> {
                    context.getString(
                        R.string.stream_ui_message_composer_error_message_length,
                        currentValidationError.maxMessageLength
                    )
                }
                is ValidationError.AttachmentCountExceeded -> {
                    context.getString(
                        R.string.stream_ui_message_composer_error_attachment_count,
                        currentValidationError.maxAttachmentCount
                    )
                }
                is ValidationError.AttachmentSizeExceeded -> {
                    context.getString(
                        R.string.stream_ui_message_composer_error_file_size,
                        MediaStringUtil.convertFileSizeByteCount(currentValidationError.maxAttachmentSize)
                    )
                }
                is ValidationError.ContainsLinksWhenNotAllowed -> {
                    context.getString(
                        R.string.stream_ui_message_composer_sending_links_not_allowed,
                    )
                }
            }
            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
        }

        this.previousValidationError = currentValidationError
    }
}
