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

package io.getstream.chat.android.guides.catalog.uicomponents.customcomposer

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.guides.databinding.ActivityCustomMessageComposerBinding
import io.getstream.chat.android.ui.common.state.messages.MessageMode
import io.getstream.chat.android.ui.viewmodel.messages.MessageListViewModel
import io.getstream.chat.android.ui.viewmodel.messages.MessageListViewModelFactory
import io.getstream.chat.android.ui.viewmodel.messages.bindView

/**
 * An Activity representing a self-contained chat screen with custom message composer.
 */
class MessagesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCustomMessageComposerBinding

    private val cid: String by lazy { requireNotNull(intent.getStringExtra(EXTRA_CID)) }
    private val factory: MessageListViewModelFactory by lazy {
        MessageListViewModelFactory(
            context = applicationContext,
            cid = cid,
            threadLoadOlderToNewer = false,
        )
    }
    private val messageListViewModel: MessageListViewModel by viewModels { factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCustomMessageComposerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        messageListViewModel.bindView(binding.messageListView, this)

        messageListViewModel.state.observe(this) { state ->
            if (state is MessageListViewModel.State.NavigateUp) {
                finish()
            }
        }

        onBackPressedDispatcher.addCallback(this) {
            messageListViewModel.onEvent(MessageListViewModel.Event.BackButtonPressed)
        }

        binding.customMessageComposerView.setChannelClient(ChatClient.instance().channel(cid))

        binding.messageListView.setMessageEditHandler(binding.customMessageComposerView::editMessage)

        messageListViewModel.mode.observe(this) {
            when (it) {
                is MessageMode.MessageThread -> {
                    binding.customMessageComposerView.setActiveThread(it.parentMessage)
                }
                is MessageMode.Normal -> {
                    binding.customMessageComposerView.resetThread()
                }
            }
        }
    }

    companion object {
        private const val EXTRA_CID: String = "extra_cid"

        /**
         * Creates an [Intent] to start [MessagesActivity].
         *
         * @param context The context used to create the intent.
         * @param cid The id of the channel.
         * @return The [Intent] to start [MessagesActivity].
         */
        fun createIntent(context: Context, cid: String): Intent = Intent(context, MessagesActivity::class.java).apply {
            putExtra(EXTRA_CID, cid)
        }
    }
}
