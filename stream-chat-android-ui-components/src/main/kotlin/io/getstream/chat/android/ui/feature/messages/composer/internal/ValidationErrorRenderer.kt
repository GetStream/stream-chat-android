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

package io.getstream.chat.android.ui.feature.messages.composer.internal

import android.content.Context
import android.view.View
import android.widget.Toast
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.state.messages.composer.ValidationError
import io.getstream.chat.android.ui.common.utils.MediaStringUtil

/**
 * A helper class that can be used to display a validation error for the
 * current user input.
 *
 * @param context The [Context] used to display a [Toast].
 * @param view The View that will be used to show a snackbar.
 */
internal class ValidationErrorRenderer(
    private val context: Context,
    private val view: View,
) : BaseTransientBottomBar.BaseCallback<Snackbar>() {

    /**
     * The validation error from the previous state update.
     */
    private var previousValidationError: ValidationError? = null

    /**
     * The currently visible snackbar or null if it is hidden.
     */
    private var currentlyVisibleSnackbar: Snackbar? = null

    /**
     * Displays the first validation error from the list of errors using a [Toast].
     *
     * @param validationErrors The list of validation errors based on the current user input.
     */
    fun renderValidationErrors(validationErrors: List<ValidationError>) {
        if (validationErrors.isEmpty()) {
            dismissValidationErrors()
            return
        }

        val currentValidationError = validationErrors.firstOrNull()

        // Don't display the validation error if it hasn't changed since the last state update.
        if (currentlyVisibleSnackbar == null &&
            currentValidationError != null &&
            currentValidationError != previousValidationError
        ) {
            val errorMessage = when (currentValidationError) {
                is ValidationError.MessageLengthExceeded -> {
                    context.getString(
                        R.string.stream_ui_message_composer_error_message_length,
                        currentValidationError.maxMessageLength,
                    )
                }
                is ValidationError.AttachmentCountExceeded -> {
                    context.getString(
                        R.string.stream_ui_message_composer_error_attachment_count,
                        currentValidationError.maxAttachmentCount,
                    )
                }
                is ValidationError.AttachmentSizeExceeded -> {
                    context.getString(
                        R.string.stream_ui_message_composer_error_file_size,
                        MediaStringUtil.convertFileSizeByteCount(currentValidationError.maxAttachmentSize),
                    )
                }
                is ValidationError.ContainsLinksWhenNotAllowed -> {
                    context.getString(
                        R.string.stream_ui_message_composer_sending_links_not_allowed,
                    )
                }
            }

            Snackbar.make(view, errorMessage, Snackbar.LENGTH_INDEFINITE).apply {
                anchorView = this@ValidationErrorRenderer.view
                addCallback(this@ValidationErrorRenderer)
                setAction(R.string.stream_ui_ok) { dismiss() }
            }.show()
        }

        this.previousValidationError = currentValidationError
    }

    /**
     * Dismisses the currently visible snackbar.
     */
    fun dismissValidationErrors() {
        currentlyVisibleSnackbar?.dismiss()
    }

    /**
     * Called when the snackbar is visible.
     *
     * @param snackbar The snackbar which is now visible.
     */
    override fun onShown(snackbar: Snackbar?) {
        super.onShown(snackbar)
        this.currentlyVisibleSnackbar = snackbar
    }

    /**
     * Called when the given snackbar has been dismissed.
     *
     * @param snackbar The snackbar which has been dismissed.
     * @param event The event which caused the dismissal.
     */
    override fun onDismissed(snackbar: Snackbar?, event: Int) {
        super.onDismissed(snackbar, event)
        this.currentlyVisibleSnackbar = null
    }
}
