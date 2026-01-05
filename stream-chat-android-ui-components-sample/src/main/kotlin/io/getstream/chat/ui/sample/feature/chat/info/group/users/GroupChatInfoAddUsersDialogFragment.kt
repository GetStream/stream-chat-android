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

package io.getstream.chat.ui.sample.feature.chat.info.group.users

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import io.getstream.chat.android.models.User
import io.getstream.chat.android.state.utils.EventObserver
import io.getstream.chat.android.ui.widgets.EndlessScrollListener
import io.getstream.chat.ui.sample.R
import io.getstream.chat.ui.sample.common.showToast
import io.getstream.chat.ui.sample.databinding.ChatInfoGroupAddUsersDialogFragmentBinding
import io.getstream.chat.ui.sample.feature.chat.ChatViewModelFactory
import io.getstream.chat.ui.sample.feature.chat.info.group.users.GroupChatInfoAddUsersDialogFragment.LoadMoreListener

class GroupChatInfoAddUsersDialogFragment : DialogFragment() {

    private val cid: String by lazy { requireArguments().getString(ARG_CID)!! }
    private val adapter: GroupChatInfoAddUsersAdapter = GroupChatInfoAddUsersAdapter()
    private val viewModel: GroupChatInfoAddUsersViewModel by viewModels { ChatViewModelFactory(cid) }
    private var loadMoreListener: LoadMoreListener? = null
    private val scrollListener = EndlessScrollListener(LOAD_MORE_THRESHOLD) {
        loadMoreListener?.loadMore()
    }

    private var _binding: ChatInfoGroupAddUsersDialogFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            window?.requestFeature(Window.FEATURE_NO_TITLE)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = ChatInfoGroupAddUsersDialogFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerView.adapter = adapter
        binding.recyclerView.addOnScrollListener(scrollListener)
        loadMoreListener = LoadMoreListener {
            viewModel.onAction(GroupChatInfoAddUsersViewModel.Action.LoadMoreRequested)
        }
        viewModel.state.observe(viewLifecycleOwner) { state ->
            if (state.isLoading) {
                showLoading()
            } else {
                if (state.results.isEmpty()) {
                    showEmptyState()
                } else {
                    showResults(state.results)
                }
            }
        }
        viewModel.userAddedState.observe(viewLifecycleOwner) { isUserAdded ->
            if (isUserAdded) {
                dismiss()
            }
        }
        viewModel.errorEvents.observe(
            viewLifecycleOwner,
            EventObserver {
                when (it) {
                    is GroupChatInfoAddUsersViewModel.ErrorEvent.AddMemberError -> R.string.chat_group_info_error_add_member
                }.let(::showToast)
            },
        )

        adapter.setUserClickListener { user ->
            viewModel.onAction(GroupChatInfoAddUsersViewModel.Action.UserClicked(user))
        }
        binding.searchInputView.setDebouncedInputChangedListener { query ->
            viewModel.onAction(GroupChatInfoAddUsersViewModel.Action.SearchQueryChanged(query))
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.let {
            it.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            it.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showLoading() {
        binding.progressBar.isVisible = true
        binding.recyclerView.isVisible = false
        binding.emptyView.isVisible = false
        scrollListener.disablePagination()
    }

    private fun showResults(users: List<User>) {
        binding.progressBar.isVisible = false
        binding.recyclerView.isVisible = true
        binding.emptyView.isVisible = false
        scrollListener.enablePagination()
        adapter.submitList(users)
    }

    private fun showEmptyState() {
        binding.progressBar.isVisible = false
        binding.recyclerView.isVisible = false
        scrollListener.disablePagination()
        binding.emptyView.isVisible = true
    }

    fun interface LoadMoreListener {
        fun loadMore()
    }

    companion object {
        const val TAG = "GroupChatInfoAddMembersDialogFragment"
        private const val ARG_CID = "cid"
        private const val LOAD_MORE_THRESHOLD = 5

        fun newInstance(cid: String): GroupChatInfoAddUsersDialogFragment {
            return GroupChatInfoAddUsersDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_CID, cid)
                }
            }
        }
    }
}
