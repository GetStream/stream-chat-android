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

package io.getstream.chat.ui.sample.feature.chat.info.group.member

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.getstream.chat.android.models.ChannelCapabilities
import io.getstream.chat.android.models.User
import io.getstream.chat.android.state.utils.EventObserver
import io.getstream.chat.android.ui.utils.extensions.getLastSeenText
import io.getstream.chat.ui.sample.R
import io.getstream.chat.ui.sample.common.navigateSafely
import io.getstream.chat.ui.sample.common.showToast
import io.getstream.chat.ui.sample.databinding.ChatInfoGroupMemberOptionsFragmentBinding
import io.getstream.chat.ui.sample.feature.chat.info.UserData
import io.getstream.chat.ui.sample.feature.chat.info.group.GroupChatInfoFragmentDirections
import io.getstream.chat.ui.sample.feature.chat.info.toUser
import io.getstream.chat.ui.sample.feature.chat.info.toUserData
import io.getstream.chat.ui.sample.feature.common.ConfirmationDialogFragment

class GroupChatInfoMemberOptionsDialogFragment : BottomSheetDialogFragment() {

    private val cid: String by lazy {
        requireArguments().getString(ARG_CID)!!
    }
    private val userData: UserData by lazy {
        requireArguments().getSerializable(ARG_USER_DATA) as UserData
    }
    private val channelName: String by lazy {
        requireArguments().getString(ARG_CHANNEL_NAME)!!
    }
    private val user: User by lazy {
        userData.toUser()
    }
    private val ownCapabilities: Set<String> by lazy {
        requireArguments().getStringArrayList(ARG_OWN_CAPABILITIES)?.toSet() ?: setOf()
    }

    private val viewModel: GroupChatInfoMemberOptionsViewModel by viewModels {
        GroupChatInfoMemberOptionsViewModelFactory(cid, user.id)
    }
    private var _binding: ChatInfoGroupMemberOptionsFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = ChatInfoGroupMemberOptionsFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun getTheme(): Int = R.style.StreamUiBottomSheetDialogTheme

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModel()
        binding.apply {
            userNameTextView.text = user.name
            lastSeenTextView.text = user.getLastSeenText(requireContext())
            userAvatarView.setUser(user)
            optionViewInfo.setOnOptionClickListener {
                findNavController().navigateSafely(
                    GroupChatInfoFragmentDirections.actionOpenChatInfo(
                        userData = userData,
                        cid = viewModel.state.value!!.directChannelCid,
                    ),
                )
                dismiss()
            }
            optionMessage.setOnClickListener {
                viewModel.onAction(GroupChatInfoMemberOptionsViewModel.Action.MessageClicked)
            }

            if (isAnonymousChannel(cid) || !ownCapabilities.contains(ChannelCapabilities.UPDATE_CHANNEL_MEMBERS)) {
                optionRemove.isVisible = false
            } else {
                optionRemove.setOnClickListener {
                    ConfirmationDialogFragment.newInstance(
                        iconResId = R.drawable.ic_delete,
                        iconTintResId = R.color.red,
                        title = getString(R.string.chat_group_info_user_remove_title, user.name),
                        description = getString(
                            R.string.chat_group_info_user_remove_description,
                            user.name,
                            channelName,
                        ),
                        confirmText = getString(R.string.remove),
                        cancelText = getString(R.string.cancel),
                    ).apply {
                        confirmClickListener = ConfirmationDialogFragment.ConfirmClickListener {
                            viewModel.onAction(GroupChatInfoMemberOptionsViewModel.Action.RemoveFromChannel(user.name))
                        }
                    }.show(parentFragmentManager, ConfirmationDialogFragment.TAG)
                }
            }
            optionCancel.setOnOptionClickListener {
                dismiss()
            }
        }
    }

    private fun isAnonymousChannel(cid: String): Boolean = cid.contains("!members")

    private fun initViewModel() {
        viewModel.state.observe(viewLifecycleOwner) { state ->
            if (!state.loading) {
                binding.apply {
                    optionMessage.isVisible = true
                    optionViewInfo.isVisible = state.directChannelCid != null
                }
            }
        }
        viewModel.events.observe(
            viewLifecycleOwner,
            EventObserver {
                when (it) {
                    GroupChatInfoMemberOptionsViewModel.UiEvent.Dismiss -> dismiss()
                    is GroupChatInfoMemberOptionsViewModel.UiEvent.RedirectToChat -> {
                        findNavController().navigateSafely(
                            GroupChatInfoFragmentDirections.actionOpenChat(cid = it.cid),
                        )
                        dismiss()
                    }
                    GroupChatInfoMemberOptionsViewModel.UiEvent.RedirectToChatPreview -> {
                        findNavController().navigateSafely(
                            GroupChatInfoFragmentDirections.actionOpenChatPreview(userData),
                        )
                        dismiss()
                    }
                }
            },
        )
        viewModel.errorEvents.observe(
            viewLifecycleOwner,
            EventObserver {
                when (it) {
                    is GroupChatInfoMemberOptionsViewModel.ErrorEvent.RemoveMemberError -> R.string.chat_group_info_error_remove_member
                }.let(::showToast)
            },
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "GroupChatInfoMemberOptionsDialogFragment"
        private const val ARG_CID = "cid"
        private const val ARG_CHANNEL_NAME = "channel_name"
        private const val ARG_USER_DATA = "user_data"
        private const val ARG_OWN_CAPABILITIES = "own_capabilities"

        fun newInstance(cid: String, channelName: String, user: User, ownCapabilities: Set<String>) =
            GroupChatInfoMemberOptionsDialogFragment().apply {
                arguments =
                    bundleOf(
                        ARG_CID to cid,
                        ARG_CHANNEL_NAME to channelName,
                        ARG_USER_DATA to user.toUserData(),
                        ARG_OWN_CAPABILITIES to ownCapabilities.toList(),
                    )
            }
    }
}
