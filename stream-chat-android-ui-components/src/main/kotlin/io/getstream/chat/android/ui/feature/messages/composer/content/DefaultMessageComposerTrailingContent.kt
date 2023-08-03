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

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.isVisible
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.models.AttachmentType
import io.getstream.chat.android.models.ChannelCapabilities
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.internal.getColorList
import io.getstream.chat.android.ui.common.state.messages.Edit
import io.getstream.chat.android.ui.common.state.messages.composer.MessageComposerState
import io.getstream.chat.android.ui.common.state.messages.composer.RecordingState
import io.getstream.chat.android.ui.databinding.StreamUiMessageComposerDefaultTrailingContentBinding
import io.getstream.chat.android.ui.feature.messages.composer.MessageComposerContext
import io.getstream.chat.android.ui.feature.messages.composer.MessageComposerView
import io.getstream.chat.android.ui.feature.messages.composer.MessageComposerViewStyle
import io.getstream.chat.android.ui.font.setTextStyle
import io.getstream.chat.android.ui.utils.extensions.createStreamThemeWrapper
import io.getstream.chat.android.ui.utils.extensions.getColorCompat
import io.getstream.chat.android.ui.utils.extensions.streamThemeInflater

/**
 * Represents the default content shown at the end of [MessageComposerView].
 */
public class DefaultMessageComposerTrailingContent : FrameLayout, MessageComposerContent {
    /**
     * Generated binding class for the XML layout.
     */
    internal lateinit var binding: StreamUiMessageComposerDefaultTrailingContentBinding

    /**
     * The style for [MessageComposerView].
     */
    private lateinit var style: MessageComposerViewStyle

    /**
     * Click listener for the send message button.
     */
    public var sendMessageButtonClickListener: () -> Unit = {}

    /**
     * Touch listener for the mic button.
     */
    public var recordAudioButtonTouchListener: (event: MotionEvent) -> Boolean = { false }

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
    @SuppressLint("ClickableViewAccessibility")
    private fun init() {
        binding = StreamUiMessageComposerDefaultTrailingContentBinding.inflate(streamThemeInflater, this)
        binding.sendMessageButton.setOnClickListener { sendMessageButtonClickListener() }
        binding.recordAudioButton.setOnTouchListener { _, event -> recordAudioButtonTouchListener(event) }
        binding.recordAudioButton.tag = RECORD_AUDIO_TAG
    }

    /**
     * Initializes the content view with [MessageComposerContext].
     *
     * @param messageComposerContext The context of this [MessageComposerView].
     */
    override fun attachContext(messageComposerContext: MessageComposerContext) {
        this.style = messageComposerContext.style

        binding.sendMessageButton.setImageDrawable(style.sendMessageButtonIconDrawable)
        style.buttonIconDrawableTintColor?.let { tintColor ->
            binding.sendMessageButton.imageTintList = getColorList(
                normalColor = tintColor,
                selectedColor = tintColor,
                disabledColor = context.getColorCompat(R.color.stream_ui_grey_gainsboro),
            )
        }
        binding.recordAudioButton.setImageDrawable(style.audioRecordingButtonIconDrawable)
        style.buttonIconDrawableTintColor?.let { tintColor ->
            binding.recordAudioButton.imageTintList = getColorList(
                normalColor = tintColor,
                selectedColor = tintColor,
                disabledColor = context.getColorCompat(R.color.stream_ui_grey_gainsboro),
            )
        }

        binding.cooldownBadgeTextView.setTextStyle(style.cooldownTimerTextStyle)
        binding.cooldownBadgeTextView.background = style.cooldownTimerBackgroundDrawable
    }

    /**
     * Invoked when the state has changed and the UI needs to be updated accordingly.
     *
     * @param state The state that will be used to render the updated UI.
     */
    override fun renderState(state: MessageComposerState) {
        val appSettings = ChatClient.instance().getAppSettings()
        val blockedMimeTypes = appSettings.app.fileUploadConfig.blockedMimeTypes
        val blockedFileExtensions = appSettings.app.fileUploadConfig.blockedFileExtensions
        val canSendMessage = state.ownCapabilities.contains(ChannelCapabilities.SEND_MESSAGE)
        val canUploadFile = state.ownCapabilities.contains(ChannelCapabilities.UPLOAD_FILE)
        val canUploadRecording = !blockedMimeTypes.contains(AttachmentType.AUDIO) &&
            !blockedFileExtensions.contains(AAC_EXTENSION)
        val hasTextInput = state.inputValue.isNotEmpty()
        val hasAttachments = state.attachments.isNotEmpty()
        val isInputValid = state.validationErrors.isEmpty()
        val isInEditMode = state.action is Edit

        val coolDownTime = state.coolDownTime
        val hasValidContent = (hasTextInput || hasAttachments) && isInputValid
        val noRecording = state.recording is RecordingState.Idle

        binding.root.isVisible = noRecording
        binding.apply {
            if (coolDownTime > 0 && !isInEditMode) {
                cooldownBadgeTextView.isVisible = true
                cooldownBadgeTextView.text = coolDownTime.toString()
                sendMessageButton.isVisible = false
                recordAudioButton.isVisible = false
            } else {
                cooldownBadgeTextView.isVisible = false
                sendMessageButton.isVisible = when (style.audioRecordingButtonPreferred) {
                    true -> hasTextInput || isInEditMode
                    else -> true
                }
                sendMessageButton.isEnabled = style.sendMessageButtonEnabled && canSendMessage && hasValidContent
                recordAudioButton.isEnabled = style.sendMessageButtonEnabled &&
                    canSendMessage &&
                    canUploadRecording &&
                    canUploadFile
                recordAudioButton.isVisible = when (style.audioRecordingButtonVisible) {
                    true -> when (style.audioRecordingButtonPreferred) {
                        true -> canUploadFile && canUploadRecording && canSendMessage && !isInEditMode && !hasTextInput
                        else -> canUploadFile && canUploadRecording && canSendMessage && !isInEditMode
                    }
                    else -> false
                }
            }
        }
    }

    override fun findViewByKey(key: String): View? {
        return when (key) {
            MessageComposerContent.RECORD_AUDIO_BUTTON -> binding.recordAudioButton
            else -> null
        }
    }

    internal companion object {

        private const val RECORD_AUDIO_TAG = "record_audio"
        internal fun recordAudioButton(container: ViewGroup): View {
            return container.findViewById(R.id.recordAudioButton)
                ?: container.findViewWithTag(RECORD_AUDIO_TAG)
                ?: error("recordAudioButton not found in $container")
        }

        private const val AAC_EXTENSION = "aac"
    }
}
