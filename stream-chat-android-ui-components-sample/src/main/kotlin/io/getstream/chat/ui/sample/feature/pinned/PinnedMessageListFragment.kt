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

package io.getstream.chat.ui.sample.feature.pinned

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import io.getstream.chat.android.ui.viewmodel.pinned.PinnedMessageListViewModel
import io.getstream.chat.android.ui.viewmodel.pinned.PinnedMessageListViewModelFactory
import io.getstream.chat.android.ui.viewmodel.pinned.bindView
import io.getstream.chat.ui.sample.R
import io.getstream.chat.ui.sample.common.initToolbar
import io.getstream.chat.ui.sample.common.navigateSafely
import io.getstream.chat.ui.sample.databinding.FragmentPinnedMessageListBinding

class PinnedMessageListFragment : Fragment() {

    private val args: PinnedMessageListFragmentArgs by navArgs()

    private val viewModel: PinnedMessageListViewModel by viewModels { PinnedMessageListViewModelFactory(args.cid) }

    private var _binding: FragmentPinnedMessageListBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentPinnedMessageListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initToolbar(binding.toolbar)

        viewModel.bindView(binding.pinnedMessageListView, viewLifecycleOwner)
        binding.pinnedMessageListView.setPinnedMessageSelectedListener { message ->
            requireActivity().findNavController(R.id.hostFragmentContainer)
                .navigateSafely(PinnedMessageListFragmentDirections.actionOpenChat(message.cid, message.id))
        }
    }
}
