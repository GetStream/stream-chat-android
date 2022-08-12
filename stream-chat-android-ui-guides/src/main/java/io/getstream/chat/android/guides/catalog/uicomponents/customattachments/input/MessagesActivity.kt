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

package io.getstream.chat.android.guides.catalog.uicomponents.customattachments.input

import android.content.Context
import android.content.Intent
import com.google.android.material.datepicker.MaterialDatePicker
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.guides.R
import io.getstream.chat.android.guides.catalog.uicomponents.customattachments.input.factory.DateAttachmentPreviewFactory
import io.getstream.chat.android.ui.message.MessageListActivity
import io.getstream.chat.android.ui.message.MessageListFragment
import io.getstream.chat.android.ui.message.input.MessageInputView
import java.text.DateFormat
import java.util.Date

/**
 * An Activity representing a self-contained chat screen.
 */
class MessagesActivity : MessageListActivity() {

    override fun createMessageListFragment(cid: String, messageId: String?): MessageListFragment {
        return MessageListFragment.newInstance(cid) {
            setFragment(CustomMessageListFragment())
            customTheme(R.style.CustomStreamUiTheme)
            showHeader(true)
            messageId(messageId)
        }
    }

    /**
     * A Fragment representing a self-contained chat screen.
     */
    class CustomMessageListFragment : MessageListFragment() {

        override fun setupMessageInput(messageInputView: MessageInputView) {
            super.setupMessageInput(messageInputView)

            // Create an instance of a date picker dialog
            val datePickerDialog = MaterialDatePicker.Builder
                .datePicker()
                .build()

            // Add an attachment to the message input when the user selects a date
            datePickerDialog.addOnPositiveButtonClickListener {
                val date = DateFormat
                    .getDateInstance(DateFormat.LONG)
                    .format(Date(it))
                val attachment = Attachment(
                    type = "date",
                    extraData = mutableMapOf("payload" to date)
                )
                messageInputView.submitCustomAttachments(
                    attachments = listOf(attachment),
                    viewHolderFactory = DateAttachmentPreviewFactory()
                )
            }

            // Show the date picker dialog when the attachment button is clicked
            messageInputView.setAttachmentButtonClickListener {
                datePickerDialog.show(requireActivity().supportFragmentManager, null)
            }
        }
    }

    companion object {
        private const val EXTRA_CID: String = "extra_cid"
        private const val EXTRA_MESSAGE_ID: String = "extra_message_id"

        /**
         * Creates an [Intent] to start [MessagesActivity].
         *
         * @param context The context used to create the intent.
         * @param cid The id of the channel.
         * @return The [Intent] to start [MessagesActivity].
         */
        fun createIntent(context: Context, cid: String, messageId: String? = null): Intent {
            return Intent(context, MessagesActivity::class.java).apply {
                putExtra(EXTRA_CID, cid)
                putExtra(EXTRA_MESSAGE_ID, messageId)
            }
        }
    }
}
