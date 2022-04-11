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
 
package io.getstream.chat.android.ui.message.list.options.message.internal

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.view.isVisible
import io.getstream.chat.android.client.models.ChannelCapabilities
import io.getstream.chat.android.client.models.Config
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.extensions.internal.createStreamThemeWrapper
import io.getstream.chat.android.ui.common.extensions.internal.setStartDrawable
import io.getstream.chat.android.ui.common.extensions.internal.streamThemeInflater
import io.getstream.chat.android.ui.common.style.TextStyle
import io.getstream.chat.android.ui.common.style.setTextStyle
import io.getstream.chat.android.ui.databinding.StreamUiMessageOptionsViewBinding
import io.getstream.chat.android.ui.message.list.MessageListViewStyle
import java.io.Serializable

internal class MessageOptionsView : FrameLayout {

    private val binding = StreamUiMessageOptionsViewBinding.inflate(streamThemeInflater, this, true)

    constructor(context: Context) : this(context, null, 0)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context.createStreamThemeWrapper(),
        attrs,
        defStyleAttr
    )

    internal fun configure(
        configuration: Configuration,
        style: MessageListViewStyle,
        isMessageTheirs: Boolean,
        syncStatus: SyncStatus,
        isMessageAuthorMuted: Boolean,
        isMessagePinned: Boolean,
    ) {
        val isMessageMine = !isMessageTheirs
        val isMessageSynced = syncStatus == SyncStatus.COMPLETED
        val ownCapabilities = configuration.ownCapabilities

        configureMessageOption(
            isVisible = isMessageMine && syncStatus == SyncStatus.FAILED_PERMANENTLY,
            textView = binding.retryTV,
            textStyle = style.messageOptionsText,
            optionIcon = style.retryIcon,
        )

        configureMessageOption(
            isVisible = configuration.replyEnabled && isMessageSynced,
            textView = binding.replyTV,
            textStyle = style.messageOptionsText,
            optionIcon = style.replyIcon,
        )

        configureMessageOption(
            isVisible = configuration.threadsEnabled && isMessageSynced,
            textView = binding.threadReplyTV,
            textStyle = style.messageOptionsText,
            optionIcon = style.threadReplyIcon,
        )

        configureMessageOption(
            isVisible = configuration.copyTextEnabled,
            textView = binding.copyTV,
            textStyle = style.messageOptionsText,
            optionIcon = style.copyIcon,
        )

        val userCanEditOwnMessage = ownCapabilities.contains(ChannelCapabilities.UPDATE_OWN_MESSAGE)
        val userCalEditAnyMessage = ownCapabilities.contains(ChannelCapabilities.UPDATE_ANY_MESSAGE)

        configureMessageOption(
            isVisible = configuration.editMessageEnabled &&
                (userCalEditAnyMessage || (userCanEditOwnMessage && isMessageMine)),
            textView = binding.editTV,
            textStyle = style.messageOptionsText,
            optionIcon = style.editIcon,
        )

        configureMessageOption(
            isVisible = configuration.flagEnabled && isMessageTheirs,
            textView = binding.flagTV,
            textStyle = style.messageOptionsText,
            optionIcon = style.flagIcon,
        )

        val (pinText, pinIcon) = if (isMessagePinned) {
            R.string.stream_ui_message_list_unpin_message to style.unpinIcon
        } else {
            R.string.stream_ui_message_list_pin_message to style.pinIcon
        }

        configureMessageOption(
            isVisible = configuration.pinMessageEnabled && isMessageSynced,
            textView = binding.pinTV,
            textStyle = style.messageOptionsText,
            optionIcon = pinIcon,
            optionText = pinText,
        )

        val userCanDeleteOwnMessage = ownCapabilities.contains(ChannelCapabilities.DELETE_OWN_MESSAGE)
        val userCanDeleteAnyMessage = ownCapabilities.contains(ChannelCapabilities.DELETE_ANY_MESSAGE)

        configureMessageOption(
            isVisible = configuration.deleteMessageEnabled &&
                (userCanDeleteAnyMessage || (userCanDeleteOwnMessage && isMessageMine)),
            textView = binding.deleteTV,
            textStyle = style.warningMessageOptionsText,
            optionIcon = style.deleteIcon,
        )

        val (muteText, muteIcon) = if (isMessageAuthorMuted) {
            R.string.stream_ui_message_list_unmute_user to style.unmuteIcon
        } else {
            R.string.stream_ui_message_list_mute_user to style.muteIcon
        }
        configureMessageOption(
            isVisible = configuration.muteEnabled && isMessageTheirs,
            textView = binding.muteTV,
            textStyle = style.messageOptionsText,
            optionIcon = muteIcon,
            optionText = muteText,
        )

        configureMessageOption(
            isVisible = configuration.blockEnabled && isMessageTheirs,
            textView = binding.blockTV,
            textStyle = style.messageOptionsText,
            optionIcon = style.blockIcon,
        )

        binding.messageOptionsContainer.setCardBackgroundColor(style.messageOptionsBackgroundColor)
    }

    internal data class Configuration(
        val replyEnabled: Boolean,
        val threadsEnabled: Boolean,
        val editMessageEnabled: Boolean,
        val deleteMessageEnabled: Boolean,
        val copyTextEnabled: Boolean,
        val deleteConfirmationEnabled: Boolean,
        val reactionsEnabled: Boolean,
        val flagEnabled: Boolean,
        val pinMessageEnabled: Boolean,
        val muteEnabled: Boolean,
        val blockEnabled: Boolean,
        val retryMessageEnabled: Boolean,
        val ownCapabilities: Set<String>,
    ) : Serializable {
        internal companion object {
            operator fun invoke(
                viewStyle: MessageListViewStyle,
                channelConfig: Config,
                hasTextToCopy: Boolean,
                suppressThreads: Boolean,
                ownCapabilities: Set<String>,
            ): Configuration {
                val userCanReply = ownCapabilities.contains(ChannelCapabilities.SEND_REPLY)
                val userCanPinMessage = ownCapabilities.contains(ChannelCapabilities.PIN_MESSAGE)
                val userCanBlockMembers = ownCapabilities.contains(ChannelCapabilities.BAN_CHANNEL_MEMBERS)

                return Configuration(
                    replyEnabled = viewStyle.replyEnabled && userCanReply,
                    threadsEnabled = if (suppressThreads) false else viewStyle.threadsEnabled && channelConfig.isThreadEnabled && userCanReply,
                    editMessageEnabled = viewStyle.editMessageEnabled,
                    deleteMessageEnabled = viewStyle.deleteMessageEnabled,
                    copyTextEnabled = viewStyle.copyTextEnabled && hasTextToCopy,
                    deleteConfirmationEnabled = viewStyle.deleteConfirmationEnabled,
                    reactionsEnabled = viewStyle.reactionsEnabled && channelConfig.isReactionsEnabled && ownCapabilities.contains(
                        ChannelCapabilities.SEND_REACTION
                    ),
                    flagEnabled = viewStyle.flagEnabled,
                    pinMessageEnabled = viewStyle.pinMessageEnabled && userCanPinMessage,
                    muteEnabled = viewStyle.muteEnabled,
                    blockEnabled = viewStyle.blockEnabled && userCanBlockMembers,
                    retryMessageEnabled = viewStyle.retryMessageEnabled,
                    ownCapabilities = ownCapabilities
                )
            }
        }
    }

    fun setReplyListener(onReplyListener: () -> Unit) {
        binding.replyTV.setOnClickListener {
            onReplyListener()
        }
    }

    fun setThreadListener(onThreadReply: () -> Unit) {
        binding.threadReplyTV.setOnClickListener {
            onThreadReply()
        }
    }

    fun setRetryListener(onRetry: () -> Unit) {
        binding.retryTV.setOnClickListener {
            onRetry()
        }
    }

    fun setCopyListener(onCopy: () -> Unit) {
        binding.copyTV.setOnClickListener {
            onCopy()
        }
    }

    fun setEditMessageListener(onEdit: () -> Unit) {
        binding.editTV.setOnClickListener {
            onEdit()
        }
    }

    fun setFlagMessageListener(onFlag: () -> Unit) {
        binding.flagTV.setOnClickListener {
            onFlag()
        }
    }

    fun setPinMessageListener(onPin: () -> Unit) {
        binding.pinTV.setOnClickListener {
            onPin()
        }
    }

    fun setDeleteMessageListener(onDelete: () -> Unit) {
        binding.deleteTV.setOnClickListener {
            onDelete()
        }
    }

    fun setMuteUserListener(onMute: () -> Unit) {
        binding.muteTV.setOnClickListener {
            onMute()
        }
    }

    fun setBlockUserListener(onBlock: () -> Unit) {
        binding.blockTV.setOnClickListener {
            onBlock()
        }
    }

    private fun configureMessageOption(
        isVisible: Boolean,
        textView: TextView,
        textStyle: TextStyle,
        optionIcon: Int,
        optionText: Int? = null,
    ) {
        if (isVisible) {
            textView.isVisible = true
            textView.setStartDrawable(optionIcon)
            textView.setTextStyle(textStyle)
            optionText?.let { textView.text = context.getString(optionText) }
        } else {
            textView.isVisible = false
        }
    }
}
