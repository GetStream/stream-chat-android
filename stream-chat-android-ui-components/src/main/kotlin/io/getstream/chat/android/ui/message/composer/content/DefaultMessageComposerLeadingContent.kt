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

package io.getstream.chat.android.ui.message.composer.content

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.core.view.isVisible
import io.getstream.chat.android.client.models.ChannelCapabilities
import io.getstream.chat.android.common.composer.MessageComposerState
import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.ui.common.extensions.internal.createStreamThemeWrapper
import io.getstream.chat.android.ui.common.extensions.internal.streamThemeInflater
import io.getstream.chat.android.ui.databinding.StreamUiMessageComposerDefaultLeadingContentBinding
import io.getstream.chat.android.ui.message.composer.MessageComposerContext
import io.getstream.chat.android.ui.message.composer.MessageComposerView
import io.getstream.chat.android.ui.message.composer.MessageComposerViewStyle
import io.getstream.chat.android.ui.utils.extensions.setBorderlessRipple

/**
 * Represents the default content shown at the start of [MessageComposerView].
 */
@ExperimentalStreamChatApi
public class DefaultMessageComposerLeadingContent : FrameLayout, MessageComposerContent {
    /**
     * Generated binding class for the XML layout.
     */
    private lateinit var binding: StreamUiMessageComposerDefaultLeadingContentBinding

    /**
     * The style for [MessageComposerView].
     */
    private lateinit var style: MessageComposerViewStyle

    /**
     * Click listener for the pick attachments button.
     */
    public var attachmentsButtonClickListener: () -> Unit = {}

    /**
     * Click listener for the pick commands button.
     */
    public var commandsButtonClickListener: () -> Unit = {}

    public constructor(context: Context) : this(context, null)

    public constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    public constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context.createStreamThemeWrapper(),
        attrs,
        defStyleAttr
    ) {
        init()
    }

    /**
     * Initializes the initial layout of the view.
     */
    private fun init() {
        binding = StreamUiMessageComposerDefaultLeadingContentBinding.inflate(streamThemeInflater, this)
        binding.attachmentsButton.setOnClickListener { attachmentsButtonClickListener() }
        binding.commandsButton.setOnClickListener { commandsButtonClickListener() }
    }

    /**
     * Initializes the content view with with [MessageComposerContext].
     *
     * @param messageComposerContext The context of this [MessageComposerView].
     */
    override fun attachContext(messageComposerContext: MessageComposerContext) {
        this.style = messageComposerContext.style

        binding.attachmentsButton.setImageDrawable(style.attachmentsButtonIconDrawable)
        binding.attachmentsButton.setBorderlessRipple(style.attachmentsButtonRippleColor)

        binding.commandsButton.setImageDrawable(style.commandsButtonIconDrawable)
        binding.commandsButton.setBorderlessRipple(style.commandsButtonRippleColor)
    }

    /**
     * Invoked when the state has changed and the UI needs to be updated accordingly.
     *
     * @param state The state that will be used to render the updated UI.
     */
    override fun renderState(state: MessageComposerState) {
        val canSendMessage = state.ownCapabilities.contains(ChannelCapabilities.SEND_MESSAGE)
        val canUploadFile = state.ownCapabilities.contains(ChannelCapabilities.UPLOAD_FILE)
        val hasTextInput = state.inputValue.isNotEmpty()
        val hasAttachments = state.attachments.isNotEmpty()
        val hasCommandInput = state.inputValue.startsWith("/")
        val hasCommandSuggestions = state.commandSuggestions.isNotEmpty()
        val hasMentionSuggestions = state.mentionSuggestions.isNotEmpty()

        val isAttachmentsButtonEnabled = !hasCommandInput && !hasCommandSuggestions && !hasMentionSuggestions
        val isCommandsButtonEnabled = !hasTextInput && !hasAttachments

        binding.attachmentsButton.isEnabled = style.attachmentsButtonVisible && isAttachmentsButtonEnabled
        binding.commandsButton.isEnabled = style.commandsButtonVisible && isCommandsButtonEnabled

        binding.attachmentsButton.isVisible = canSendMessage && canUploadFile
        binding.commandsButton.isVisible = canSendMessage
    }
}
