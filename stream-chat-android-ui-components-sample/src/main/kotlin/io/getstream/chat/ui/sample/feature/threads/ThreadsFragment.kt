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

package io.getstream.chat.ui.sample.feature.threads

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import io.getstream.chat.android.client.api.models.QueryThreadsRequest
import io.getstream.chat.android.ui.viewmodel.threads.ThreadListViewModel
import io.getstream.chat.android.ui.viewmodel.threads.ThreadsViewModelFactory
import io.getstream.chat.android.ui.viewmodel.threads.bindView
import io.getstream.chat.ui.sample.R
import io.getstream.chat.ui.sample.common.navigateSafely
import io.getstream.chat.ui.sample.databinding.FragmentThreadsBinding
import io.getstream.chat.ui.sample.feature.home.HomeFragmentDirections

/**
 * Fragment displaying the list of threads for the currently logged in user.
 */
class ThreadsFragment : Fragment() {

    private var _binding: FragmentThreadsBinding? = null
    private val binding: FragmentThreadsBinding
        get() = _binding!!

    private val viewModel: ThreadListViewModel by viewModels {
        val query = QueryThreadsRequest()
        ThreadsViewModelFactory(query)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentThreadsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.bindView(binding.threadListView, viewLifecycleOwner)
        binding.threadListView.setThreadClickListener { thread ->
            requireActivity().findNavController(R.id.hostFragmentContainer)
                .navigateSafely(
                    HomeFragmentDirections.actionOpenChat(
                        cid = thread.parentMessage.cid,
                        parentMessageId = thread.parentMessageId,
                    ),
                )
        }
        setupBackHandler()
    }

    private fun setupBackHandler() {
        activity?.apply {
            onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
                finish()
            }
        }
    }
}
