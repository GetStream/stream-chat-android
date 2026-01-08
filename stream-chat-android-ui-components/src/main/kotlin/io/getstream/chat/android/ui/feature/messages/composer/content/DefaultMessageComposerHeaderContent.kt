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

package io.getstream.chat.android.ui.feature.messages.composer.content

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.core.view.isVisible
import io.getstream.chat.android.ui.common.state.messages.Edit
import io.getstream.chat.android.ui.common.state.messages.Reply
import io.getstream.chat.android.ui.common.state.messages.composer.MessageComposerState
import io.getstream.chat.android.ui.common.state.messages.composer.RecordingState
import io.getstream.chat.android.ui.databinding.StreamUiMessageComposerDefaultHeaderContentBinding
import io.getstream.chat.android.ui.feature.messages.composer.MessageComposerContext
import io.getstream.chat.android.ui.feature.messages.composer.MessageComposerView
import io.getstream.chat.android.ui.feature.messages.composer.MessageComposerViewStyle
import io.getstream.chat.android.ui.utils.extensions.createStreamThemeWrapper
import io.getstream.chat.android.ui.utils.extensions.streamThemeInflater

/**
 * Represents the content shown at the top of [MessageComposerView].
 */
public interface MessageComposerHeaderContent : MessageComposerContent {
    /**
     * Click listener for the dismiss action button.
     */
    public var dismissActionClickListener: (() -> Unit)?
}

/**
 * Represents the default content shown at the top of [MessageComposerView].
 */
public open class DefaultMessageComposerHeaderContent : FrameLayout, MessageComposerHeaderContent {
    /**
     * Generated binding class for the XML layout.
     */
    protected lateinit var binding: StreamUiMessageComposerDefaultHeaderContentBinding

    /**
     * The style for [MessageComposerView].
     */
    protected lateinit var style: MessageComposerViewStyle

    /**
     * Click listener for the dismiss action button.
     */
    public override var dismissActionClickListener: (() -> Unit)? = null

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
        binding = StreamUiMessageComposerDefaultHeaderContentBinding.inflate(streamThemeInflater, this)
        binding.dismissInputModeButton.setOnClickListener { dismissActionClickListener?.invoke() }
    }

    /**
     * Initializes the content view with [MessageComposerContext].
     *
     * @param messageComposerContext The context of this [MessageComposerView].
     */
    override fun attachContext(messageComposerContext: MessageComposerContext) {
        this.style = messageComposerContext.style

        binding.dismissInputModeButton.setImageDrawable(style.dismissModeIconDrawable)
    }

    /**
     * Invoked when the state has changed and the UI needs to be updated accordingly.
     *
     * @param state The state that will be used to render the updated UI.
     */
    override fun renderState(state: MessageComposerState) {
        val noRecording = state.recording is RecordingState.Idle
        binding.root.isVisible = noRecording
        when (state.action) {
            is Reply -> {
                binding.inputModeHeaderContainer.isVisible = true
                binding.inputModeTextView.text = style.replyModeText
                binding.inputModeImageView.setImageDrawable(style.replyModeIconDrawable)
            }
            is Edit -> {
                binding.inputModeHeaderContainer.isVisible = true
                binding.inputModeTextView.text = style.editModeText
                binding.inputModeImageView.setImageDrawable(style.editModeIconDrawable)
            }
            else -> {
                binding.inputModeHeaderContainer.isVisible = false
            }
        }
    }
}
