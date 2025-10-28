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
import io.getstream.chat.android.ui.common.state.messages.MessageMode
import io.getstream.chat.android.ui.common.state.messages.composer.MessageComposerState
import io.getstream.chat.android.ui.databinding.StreamUiMessageComposerDefaultFooterContentBinding
import io.getstream.chat.android.ui.feature.messages.composer.MessageComposerContext
import io.getstream.chat.android.ui.feature.messages.composer.MessageComposerView
import io.getstream.chat.android.ui.feature.messages.composer.MessageComposerViewStyle
import io.getstream.chat.android.ui.font.setTextStyle
import io.getstream.chat.android.ui.utils.extensions.createStreamThemeWrapper
import io.getstream.chat.android.ui.utils.extensions.streamThemeInflater

/**
 * Represents the content shown at the bottom of [MessageComposerView].
 */
public interface MessageComposerFooterContent : MessageComposerContent {
    /**
     * Selection listener for the "also send to channel" checkbox.
     */
    public var alsoSendToChannelSelectionListener: ((Boolean) -> Unit)?
}

/**
 * Represents the default content shown at the bottom of [MessageComposerView].
 */
public open class DefaultMessageComposerFooterContent :
    FrameLayout,
    MessageComposerFooterContent {
    /**
     * Generated binding class for the XML layout.
     */
    protected lateinit var binding: StreamUiMessageComposerDefaultFooterContentBinding

    /**
     * The style for [MessageComposerView].
     */
    protected lateinit var style: MessageComposerViewStyle

    /**
     * Selection listener for the "also send to channel" checkbox.
     */
    public override var alsoSendToChannelSelectionListener: ((Boolean) -> Unit)? = null

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
        binding = StreamUiMessageComposerDefaultFooterContentBinding.inflate(streamThemeInflater, this)
        binding.alsoSendToChannelCheckBox.setOnCheckedChangeListener { _, _ ->
            alsoSendToChannelSelectionListener?.invoke(binding.alsoSendToChannelCheckBox.isChecked)
        }
    }

    /**
     * Initializes the content view with [MessageComposerContext].
     *
     * @param messageComposerContext The context of this [MessageComposerView].
     */
    override fun attachContext(messageComposerContext: MessageComposerContext) {
        this.style = messageComposerContext.style

        binding.alsoSendToChannelCheckBox.text = style.alsoSendToChannelCheckboxText
        binding.alsoSendToChannelCheckBox.setTextStyle(style.alsoSendToChannelCheckboxTextStyle)
        style.alsoSendToChannelCheckboxDrawable?.let { binding.alsoSendToChannelCheckBox.buttonDrawable = it }
    }

    /**
     * Invoked when the state has changed and the UI needs to be updated accordingly.
     *
     * @param state The state that will be used to render the updated UI.
     */
    override fun renderState(state: MessageComposerState) {
        val isThreadModeActive = state.messageMode is MessageMode.MessageThread
        binding.alsoSendToChannelCheckBox.isVisible = style.alsoSendToChannelCheckboxVisible && isThreadModeActive
        binding.alsoSendToChannelCheckBox.isChecked = state.alsoSendToChannel
    }
}
