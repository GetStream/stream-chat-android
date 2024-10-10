/*
 * Copyright (c) 2014-2024 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.ui.sample.feature.chat.messagelist.details

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.navArgs
import io.getstream.chat.android.client.extensions.getCreatedAtOrNull
import io.getstream.chat.android.models.ChannelUserRead
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListItem
import io.getstream.chat.android.ui.viewmodel.messages.MessageListViewModel
import io.getstream.chat.android.ui.viewmodel.messages.MessageListViewModelFactory
import io.getstream.chat.ui.sample.R
import io.getstream.chat.ui.sample.databinding.FragmentMessageDetailsBinding
import io.getstream.log.taggedLogger
import kotlinx.coroutines.launch

class MessageDetailsFragment : Fragment() {

    private val logger by taggedLogger("ChatFragment")

    private val args: MessageDetailsFragmentArgs by navArgs()

    private val viewModel: MessageDetailsViewModel by viewModels {
        MessageDetailsViewModelFactory(args.cid, args.messageId)
    }

    private val messageListViewModel: MessageListViewModel by viewModels {
        MessageListViewModelFactory(
            context = requireContext().applicationContext,
            cid = args.cid,
            messageId = args.messageId,
        )
    }

    private var _binding: FragmentMessageDetailsBinding? = null
    private val binding get() = _binding!!

    private val adapter: MessageDetailsAdapter = MessageDetailsAdapter()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        logger.i { "[onAttach] context: $context, targetFragment: $targetFragment" }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        logger.i { "[onCreate] args: $args" }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentMessageDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.initToolbar()

        binding.readByValue.itemAnimator = null
        binding.readByValue.adapter = adapter

        observeMessageList()
        // observeMessage()
    }

    private fun observeMessageList() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                messageListViewModel.state.observe(viewLifecycleOwner) { state ->
                    logger.i { "[onViewCreated] state: $state" }
                    when (state) {
                        is MessageListViewModel.State.Loading -> {
                            binding.progressBar.isVisible = true
                        }
                        is MessageListViewModel.State.Result -> {
                            binding.progressBar.isVisible = false
                            val messageItem = state.messageListItem.items.filterIsInstance<MessageListItem.MessageItem>()
                                .firstOrNull { it.message.id == args.messageId }
                            if (messageItem == null) {
                                logger.w { "[observeMessageList] was unable to find messageId: ${args.messageId}" }
                                return@observe
                            }
                            binding.sentByValue.text = messageItem.message.user.name
                            binding.createAtValue.text = messageItem.message.getCreatedAtOrNull()?.toString() ?: "N/A"
                            binding.readByValueEmpty.isVisible = messageItem.messageReadBy.isEmpty()
                            binding.readByValue.isVisible = messageItem.messageReadBy.isNotEmpty()

                            val items = messageItem.messageReadBy.toItems()
                            adapter.submitList(items)
                        }
                        MessageListViewModel.State.NavigateUp -> {
                            // TODO: Navigate up
                        }
                    }
                }
            }
        }
    }

    private fun observeMessage() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    logger.i { "[onViewCreated] state: $state" }
                    when (state) {
                        MessageDetailsViewState.Empty -> {
                            binding.progressBar.isVisible = false
                        }

                        MessageDetailsViewState.Loading -> {
                            binding.progressBar.isVisible = true
                        }

                        is MessageDetailsViewState.Failed -> {
                            binding.progressBar.isVisible = false
                            // TODO: Show error
                        }

                        is MessageDetailsViewState.Loaded -> {
                            binding.progressBar.isVisible = false
                            binding.sentByValue.text = state.sentBy
                            binding.createAtValue.text = state.createdAt
                            binding.readByValueEmpty.isVisible = state.readBy.isEmpty()
                            binding.readByValue.isVisible = state.readBy.isNotEmpty()
                            adapter.submitList(state.readBy.toItems())
                        }
                    }
                }
            }
        }
    }

    private fun FragmentMessageDetailsBinding.initToolbar() {
        (requireActivity() as AppCompatActivity).run {
            setSupportActionBar(toolbar)
            supportActionBar?.run {
                setDisplayShowTitleEnabled(false)
                setDisplayShowHomeEnabled(true)
                setDisplayHomeAsUpEnabled(true)

                ContextCompat.getDrawable(requireContext(), R.drawable.ic_icon_left)?.apply {
                    setTint(ContextCompat.getColor(requireContext(), R.color.stream_ui_black))
                }?.let(toolbar::setNavigationIcon)

                toolbar.setNavigationOnClickListener {
                    onBackPressed()
                }
            }
        }
        toolbar.setTitle(R.string.message_details)
    }
}

private fun List<ChannelUserRead>.toItems(): List<MessageDetailsItem> {
    return map(ChannelUserRead::toItem)
}

private fun ChannelUserRead.toItem(): MessageDetailsItem {
    return ReadByItem(
        user = user,
        lastReadAt = lastRead,
        lastReadMessageId = lastReadMessageId.orEmpty(),
    )
}
