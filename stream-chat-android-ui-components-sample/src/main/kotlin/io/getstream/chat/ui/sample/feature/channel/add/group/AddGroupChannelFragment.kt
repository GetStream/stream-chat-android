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

package io.getstream.chat.ui.sample.feature.channel.add.group

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import io.getstream.chat.ui.sample.R
import io.getstream.chat.ui.sample.common.initToolbar
import io.getstream.chat.ui.sample.databinding.FragmentAddGroupChannelBinding
import io.getstream.chat.ui.sample.feature.channel.add.AddChannelViewModel
import io.getstream.chat.ui.sample.feature.channel.add.bindView
import io.getstream.chat.ui.sample.util.extensions.useAdjustResize

class AddGroupChannelFragment : Fragment() {

    private var _binding: FragmentAddGroupChannelBinding? = null
    private val binding get() = _binding!!
    private val addChannelViewModel: AddChannelViewModel by viewModels()
    private val sharedMembersViewModel: AddGroupChannelMembersSharedViewModel by activityViewModels()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentAddGroupChannelBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initToolbar(binding.toolbar)
        bindAddChannelView()

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    sharedMembersViewModel.setMembers(emptyList())
                    findNavController().navigateUp()
                }
            },
        )
    }

    override fun onResume() {
        super.onResume()
        useAdjustResize()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_add_group_channel, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == R.id.addGroupChannelNextButton) {
            findNavController().navigate(R.id.action_addGroupChannelFragment_to_addGroupChannelSelectNameFragment)
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    private fun bindAddChannelView() {
        addChannelViewModel.bindView(binding.addChannelView, viewLifecycleOwner)
        binding.addChannelView.apply {
            setMembersChangedListener {
                sharedMembersViewModel.setMembers(it)
            }
            sharedMembersViewModel.members.value?.let {
                setMembers(it)
            }
            sharedMembersViewModel.members.observe(viewLifecycleOwner) {
                setMenuVisibility(it.isNotEmpty())
            }
        }
    }
}
