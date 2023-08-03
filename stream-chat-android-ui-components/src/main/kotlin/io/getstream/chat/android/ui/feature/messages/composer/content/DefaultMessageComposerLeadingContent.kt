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

package io.getstream.chat.android.ui.feature.messages.composer.content

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.core.view.isVisible
import io.getstream.chat.android.models.ChannelCapabilities
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.internal.getColorList
import io.getstream.chat.android.ui.common.state.messages.Edit
import io.getstream.chat.android.ui.common.state.messages.composer.MessageComposerState
import io.getstream.chat.android.ui.common.state.messages.composer.RecordingState
import io.getstream.chat.android.ui.databinding.StreamUiMessageComposerDefaultLeadingContentBinding
import io.getstream.chat.android.ui.feature.messages.composer.MessageComposerContext
import io.getstream.chat.android.ui.feature.messages.composer.MessageComposerView
import io.getstream.chat.android.ui.feature.messages.composer.MessageComposerViewStyle
import io.getstream.chat.android.ui.utils.extensions.createStreamThemeWrapper
import io.getstream.chat.android.ui.utils.extensions.getColorCompat
import io.getstream.chat.android.ui.utils.extensions.setBorderlessRipple
import io.getstream.chat.android.ui.utils.extensions.streamThemeInflater

/**
 * Represents the default content shown at the start of [MessageComposerView].
 */
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
        defStyleAttr,
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
     * Initializes the content view with [MessageComposerContext].
     *
     * @param messageComposerContext The context of this [MessageComposerView].
     */
    override fun attachContext(messageComposerContext: MessageComposerContext) {
        this.style = messageComposerContext.style

        binding.commandsButton.isVisible = style.commandsButtonVisible
        binding.attachmentsButton.setImageDrawable(style.attachmentsButtonIconDrawable)
        binding.attachmentsButton.setBorderlessRipple(style.attachmentsButtonRippleColor)

        binding.attachmentsButton.isVisible = style.attachmentsButtonVisible
        binding.commandsButton.setImageDrawable(style.commandsButtonIconDrawable)
        binding.commandsButton.setBorderlessRipple(style.commandsButtonRippleColor)

        style.buttonIconDrawableTintColor?.let { tintColor ->
            val colorStateList = getColorList(
                normalColor = context.getColorCompat(R.color.stream_ui_grey),
                selectedColor = tintColor,
                disabledColor = context.getColorCompat(R.color.stream_ui_grey_gainsboro),
            )
            binding.attachmentsButton.imageTintList = colorStateList
            binding.commandsButton.imageTintList = colorStateList
        }
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
        val isInEditMode = state.action is Edit
        val hasCommands = state.hasCommands
        val noRecording = state.recording is RecordingState.Idle

        binding.root.isVisible = noRecording
        // isVisible = noRecording
        binding.attachmentsButton.isEnabled = !hasCommandInput && !hasCommandSuggestions && !hasMentionSuggestions
        binding.attachmentsButton.isVisible = style.attachmentsButtonVisible && canSendMessage && canUploadFile && !isInEditMode

        binding.commandsButton.isEnabled = !hasTextInput && !hasAttachments
        binding.commandsButton.isVisible = style.commandsButtonVisible && canSendMessage && !isInEditMode && hasCommands
        binding.commandsButton.isSelected = hasCommandSuggestions
    }
}
