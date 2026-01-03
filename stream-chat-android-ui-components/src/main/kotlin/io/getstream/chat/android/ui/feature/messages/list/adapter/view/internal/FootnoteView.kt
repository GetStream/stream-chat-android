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

package io.getstream.chat.android.ui.feature.messages.list.adapter.view.internal

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import io.getstream.chat.android.models.User
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.databinding.StreamUiItemMessageFootnoteBinding
import io.getstream.chat.android.ui.databinding.StreamUiMessageThreadsFootnoteBinding
import io.getstream.chat.android.ui.feature.messages.list.MessageListItemStyle
import io.getstream.chat.android.ui.font.setTextStyle
import io.getstream.chat.android.ui.utils.extensions.createStreamThemeWrapper
import io.getstream.chat.android.ui.utils.extensions.streamThemeInflater
import io.getstream.chat.android.ui.widgets.avatar.UserAvatarView

internal class FootnoteView : LinearLayoutCompat {

    constructor(context: Context) : super(context.createStreamThemeWrapper())
    constructor(context: Context, attrs: AttributeSet?) : super(context.createStreamThemeWrapper(), attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context.createStreamThemeWrapper(),
        attrs,
        defStyleAttr,
    )

    private val footnote: StreamUiItemMessageFootnoteBinding =
        StreamUiItemMessageFootnoteBinding.inflate(streamThemeInflater).also { addView(it.root) }
    private val threadsFootnote: StreamUiMessageThreadsFootnoteBinding =
        StreamUiMessageThreadsFootnoteBinding.inflate(streamThemeInflater).also { addView(it.root) }

    private val translatedLabel = TextView(context).also {
        it.id = View.generateViewId()
        addView(it)
    }

    val footerTextLabel: TextView = footnote.messageFooterLabel

    init {
        orientation = VERTICAL
        footnote.root.isVisible = false
        threadsFootnote.root.isVisible = false
        translatedLabel.isVisible = false
        footnote.editedLabel.isVisible = false
    }

    fun applyGravity(isMine: Boolean) {
        footnote.messageFooterContainer.updateLayoutParams {
            gravity = if (isMine) Gravity.END else Gravity.START
        }
        footnote.messageFooterContainerInner.updateLayoutParams {
            gravity = if (isMine) Gravity.END else Gravity.START
        }
        threadsFootnote.root.updateLayoutParams {
            gravity = if (isMine) Gravity.END else Gravity.START
        }
        translatedLabel.updateLayoutParams {
            gravity = if (isMine) Gravity.END else Gravity.START
        }
    }

    fun showSimpleFootnote() {
        footnote.root.isVisible = true
        threadsFootnote.root.isVisible = false
    }

    /**
     * Hides the message footnote.
     */
    fun hideSimpleFootnote() {
        footnote.root.isVisible = false
        threadsFootnote.root.isVisible = false
    }

    /**
     * Shows the footnote for thread replies.
     *
     * @param isMine Whether this is the message if the current user.
     * @param replyCount How many replies this thread has.
     * @param threadParticipants How many participants this thread has.
     * @param style [MessageListItemStyle] How many participants this thread has.
     */
    fun showThreadRepliesFootnote(
        isMine: Boolean,
        replyCount: Int,
        threadParticipants: List<User>,
        style: MessageListItemStyle,
    ) {
        footnote.root.isVisible = false
        with(threadsFootnote) {
            root.isVisible = true
            threadsOrnamentLeft.isVisible = !isMine
            threadsOrnamentRight.isVisible = isMine
            threadRepliesButton.text = when (replyCount) {
                0 -> resources.getString(R.string.stream_ui_message_list_thread_footnote_thread_reply)
                else -> resources.getQuantityString(
                    R.plurals.stream_ui_message_list_thread_footnote,
                    replyCount,
                    replyCount,
                )
            }
            threadRepliesButton.setTextStyle(style.textStyleThreadCounter)
        }
        setupUserAvatars(isMine, threadParticipants)
    }

    private fun setupUserAvatars(isMine: Boolean, threadParticipants: List<User>) {
        fun applyUser(user: User?, userAvatarView: UserAvatarView) {
            if (user != null) {
                userAvatarView.setUser(user)
            } else {
                userAvatarView.isVisible = false
            }
        }

        with(threadsFootnote) {
            firstTheirUserImage.isVisible = !isMine
            secondTheirUserImage.isVisible = !isMine
            firstMineUserImage.isVisible = isMine
            secondMineUserImage.isVisible = isMine

            val (first, second) = getTwoLastUsers(threadParticipants)

            applyUser(first, if (isMine) firstMineUserImage else firstTheirUserImage)
            applyUser(second, if (isMine) secondMineUserImage else secondTheirUserImage)
        }
    }

    private fun getTwoLastUsers(threadParticipants: List<User>): Pair<User?, User?> {
        if (threadParticipants.isEmpty()) {
            return null to null
        }
        return threadParticipants.toSet().let { userSet ->
            when {
                userSet.size > 1 -> userSet.first() to userSet.elementAt(1)
                else -> userSet.first() to null
            }
        }
    }

    /**
     * Hides the the read counter.
     */
    fun hideReadCounter() {
        footnote.readCount.isVisible = false
    }

    /**
     * Shows the read counter.
     *
     * @param count The number of users that read the message.
     * @param style [MessageListItemStyle] The style of the MessageListItem and its items.
     */
    fun showReadCounter(
        count: Int,
        style: MessageListItemStyle,
    ) {
        footnote.readCount.apply {
            isVisible = true
            text = count.toString()
            setTextStyle(style.textStyleReadCounter)
        }
    }

    fun hideStatusIndicator() {
        footnote.deliveryStatusIcon.isVisible = false
    }

    internal fun showStatusIndicator(drawableRes: Drawable) {
        footnote.deliveryStatusIcon.isVisible = true
        footnote.deliveryStatusIcon.setImageDrawable(drawableRes)
    }

    fun showTime(time: String, editedAt: String?, style: MessageListItemStyle) {
        footnote.timeView.apply {
            isVisible = true
            text = time
            setTextStyle(style.textStyleMessageDate)
        }
        footnote.editedLabel.isVisible = editedAt != null
        footnote.editedInfo.text = context.getString(R.string.stream_ui_message_list_footnote_edited_at, editedAt)
        footnote.editedInfo.isVisible = false
        footnote.root.setOnClickListener {
            if (editedAt != null) {
                footnote.editedLabel.isVisible = !footnote.editedLabel.isVisible
                footnote.editedInfo.isVisible = !footnote.editedInfo.isVisible
                footnote.editedInfo.invalidate()
            }
        }
    }

    fun hideTimeLabel() {
        footnote.timeView.isVisible = false
    }

    fun setOnThreadClickListener(onClick: (View) -> Unit) {
        threadsFootnote.root.setOnClickListener(onClick)
    }

    /**
     * Sets the click listener for the "Translated to ..." label.
     *
     * @param onClick The click listener to be set.
     */
    fun setOnTranslatedLabelClickListener(onClick: (View) -> Unit) {
        translatedLabel.setOnClickListener(onClick)
    }

    /**
     * Shows the translated label.
     *
     * @param languageName The name of the language to be shown.
     * @param style [MessageListItemStyle] The style of the MessageListItem and its items.
     * @param showOriginalTranslationEnabled If the "Show original" option should be enabled.
     * @param showOriginalText If the original text of the message should be shown in the UI instead of its translation.
     * (if [showOriginalTranslationEnabled] is true).
     */
    fun showTranslatedLabel(
        languageName: String,
        style: MessageListItemStyle,
        showOriginalTranslationEnabled: Boolean = false,
        showOriginalText: Boolean = false,
    ) {
        translatedLabel.apply {
            isVisible = true
            setTextStyle(style.textStyleMessageLanguage)
        }
        if (showOriginalTranslationEnabled) {
            val label = if (showOriginalText) {
                context.getString(R.string.stream_ui_message_list_show_translation)
            } else {
                context.getString(R.string.stream_ui_message_list_translated, languageName) +
                    context.getString(R.string.stream_ui_message_list_show_original)
            }
            translatedLabel.text = label
        } else {
            translatedLabel.text = context.getString(R.string.stream_ui_message_list_translated, languageName)
        }
    }

    /**
     * Hides the translated label.
     */
    fun hideTranslatedLabel() {
        translatedLabel.isVisible = false
    }
}
