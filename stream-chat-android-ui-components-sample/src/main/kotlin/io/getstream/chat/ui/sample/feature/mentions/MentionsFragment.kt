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

package io.getstream.chat.ui.sample.feature.mentions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import io.getstream.chat.android.ui.viewmodel.mentions.MentionListViewModel
import io.getstream.chat.android.ui.viewmodel.mentions.MentionListViewModelFactory
import io.getstream.chat.android.ui.viewmodel.mentions.bindView
import io.getstream.chat.ui.sample.R
import io.getstream.chat.ui.sample.common.navigateSafely
import io.getstream.chat.ui.sample.databinding.FragmentMentionsBinding
import io.getstream.chat.ui.sample.feature.home.HomeFragmentDirections

class MentionsFragment : Fragment() {

    private val viewModel: MentionListViewModel by viewModels { MentionListViewModelFactory() }

    private var _binding: FragmentMentionsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentMentionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.bindView(binding.mentionsListView, viewLifecycleOwner)
        binding.mentionsListView.setMentionSelectedListener { message ->
            requireActivity().findNavController(R.id.hostFragmentContainer)
                .navigateSafely(HomeFragmentDirections.actionOpenChat(message.cid, message.id, message.parentId))
        }
        setupOnClickListeners()
    }

    private fun setupOnClickListeners() {
        activity?.apply {
            onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
                finish()
            }
        }
    }
}
