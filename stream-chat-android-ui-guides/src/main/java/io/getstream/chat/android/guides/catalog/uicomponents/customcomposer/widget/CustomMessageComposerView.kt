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

package io.getstream.chat.android.guides.catalog.uicomponents.customcomposer.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import io.getstream.chat.android.client.channel.ChannelClient
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.guides.databinding.ViewCustomMessageComposerBinding
import io.getstream.chat.android.models.Message

@OptIn(InternalStreamChatApi::class)
class CustomMessageComposerView : ConstraintLayout {

    private val binding = ViewCustomMessageComposerBinding.inflate(LayoutInflater.from(context), this)

    private lateinit var channelClient: ChannelClient

    private var messageToEdit: Message? = null
    private var parentMessage: Message? = null

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        binding.sendButton.setOnClickListener {
            val text = binding.inputField.text.toString()

            val messageToEdit = messageToEdit
            if (messageToEdit != null) {
                channelClient.updateMessage(messageToEdit.copy(text = text)).enqueue()
            } else {
                channelClient.sendMessage(Message(text = text, parentId = parentMessage?.id)).enqueue()
            }

            this.messageToEdit = null
            binding.inputField.setText("")
        }
    }

    fun setChannelClient(channelClient: ChannelClient) {
        this.channelClient = channelClient
    }

    fun editMessage(message: Message) {
        this.messageToEdit = message
        binding.inputField.setText(message.text)
    }

    fun setActiveThread(parentMessage: Message) {
        this.parentMessage = parentMessage
        this.messageToEdit = null
        binding.inputField.setText("")
    }

    fun resetThread() {
        this.parentMessage = null
        this.messageToEdit = null
        binding.inputField.setText("")
    }
}
