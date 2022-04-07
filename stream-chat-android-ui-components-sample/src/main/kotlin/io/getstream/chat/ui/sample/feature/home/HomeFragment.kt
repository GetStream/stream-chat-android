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

package io.getstream.chat.ui.sample.feature.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import io.getstream.chat.android.client.utils.internal.toggle.dialog.ToggleDialogFragment
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.livedata.utils.EventObserver
import io.getstream.chat.android.ui.avatar.AvatarView
import io.getstream.chat.android.ui.channel.list.header.viewmodel.ChannelListHeaderViewModel
import io.getstream.chat.android.ui.channel.list.header.viewmodel.bindView
import io.getstream.chat.ui.sample.BuildConfig
import io.getstream.chat.ui.sample.R
import io.getstream.chat.ui.sample.common.navigateSafely
import io.getstream.chat.ui.sample.common.setBadgeNumber
import io.getstream.chat.ui.sample.databinding.FragmentHomeBinding
import io.getstream.chat.ui.sample.feature.EXTRA_CHANNEL_ID
import io.getstream.chat.ui.sample.feature.EXTRA_CHANNEL_TYPE
import io.getstream.chat.ui.sample.feature.EXTRA_MESSAGE_ID
import io.getstream.chat.ui.sample.util.extensions.useAdjustNothing

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val homeViewModel: HomeFragmentViewModel by viewModels()
    private val channelListHeaderViewModel: ChannelListHeaderViewModel by viewModels()

    private lateinit var avatarView: AvatarView
    private lateinit var nameTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    @OptIn(InternalStreamChatApi::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        parseNotificationData()
        setupBottomNavigation()
        setupNavigationDrawer()
        homeViewModel.state.observe(viewLifecycleOwner, ::renderState)
        homeViewModel.events.observe(
            viewLifecycleOwner,
            EventObserver {
                navigateSafely(R.id.action_to_userLoginFragment)
            }
        )
        binding.channelListHeaderView.apply {
            channelListHeaderViewModel.bindView(this, viewLifecycleOwner)
            setOnActionButtonClickListener {
                navigateSafely(R.id.action_homeFragment_to_addChannelFragment)
            }
            setOnUserAvatarClickListener {
                binding.drawerLayout.openDrawer(GravityCompat.START)
            }
            if (BuildConfig.DEBUG) {
                setOnUserAvatarLongClickListener {
                    ToggleDialogFragment().apply {
                        togglesChangesCommittedListener = { changedToggles ->
                            if (changedToggles.isNotEmpty()) {
                                activity?.recreate()
                            }
                        }
                    }.show(parentFragmentManager, null)
                }
            }
        }
    }

    private fun parseNotificationData() {
        requireActivity().intent?.let {
            if (it.hasExtra(EXTRA_CHANNEL_ID) && it.hasExtra(EXTRA_MESSAGE_ID) && it.hasExtra(EXTRA_CHANNEL_TYPE)) {
                val channelType = it.getStringExtra(EXTRA_CHANNEL_TYPE)
                val channelId = it.getStringExtra(EXTRA_CHANNEL_ID)
                val cid = "$channelType:$channelId"
                val messageId = it.getStringExtra(EXTRA_MESSAGE_ID)

                requireActivity().intent = null

                findNavController().navigateSafely(HomeFragmentDirections.actionOpenChat(cid, messageId))
            }
        }
    }

    override fun onResume() {
        super.onResume()
        useAdjustNothing()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupBottomNavigation() {
        val navHostFragment =
            childFragmentManager.findFragmentById(R.id.hostFragmentContainer) as NavHostFragment
        binding.bottomNavigationView.apply {
            setupWithNavController(navHostFragment.navController)
            // disable reloading fragment when clicking again on the same tab
            setOnNavigationItemReselectedListener {}
            setBackgroundResource(R.drawable.shape_bottom_navigation_background)
            getOrCreateBadge(R.id.channels_fragment).apply {
                backgroundColor = ContextCompat.getColor(requireContext(), R.color.stream_ui_accent_red)
                badgeTextColor = ContextCompat.getColor(requireContext(), R.color.stream_ui_literal_white)
            }
            getOrCreateBadge(R.id.mentions_fragment).apply {
                backgroundColor = ContextCompat.getColor(requireContext(), R.color.stream_ui_accent_red)
                badgeTextColor = ContextCompat.getColor(requireContext(), R.color.stream_ui_literal_white)
            }
        }
    }

    private fun setupNavigationDrawer() {
        AppBarConfiguration(
            setOf(R.id.directChatFragment, R.id.groupChatFragment),
            binding.drawerLayout
        )
        binding.navigationView.setupWithNavController(findNavController())

        val header = binding.navigationView.getHeaderView(0)
        avatarView = header.findViewById(R.id.avatarView)
        nameTextView = header.findViewById(R.id.nameTextView)

        binding.navigationView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.directChatFragment -> {
                    navigateSafely(R.id.action_homeFragment_to_addChannelFragment)
                    binding.drawerLayout.close()
                    true
                }
                R.id.groupChatFragment -> {
                    navigateSafely(R.id.action_homeFragment_to_addGroupChannelFragment)
                    binding.drawerLayout.close()
                    true
                }
                else -> false
            }
        }

        binding.signOutTextView.setOnClickListener {
            homeViewModel.onUiAction(HomeFragmentViewModel.UiAction.LogoutClicked)
        }
        binding.versionName.text = BuildConfig.VERSION_NAME
    }

    private fun renderState(state: HomeFragmentViewModel.State) {
        binding.bottomNavigationView.apply {
            setBadgeNumber(R.id.channels_fragment, state.totalUnreadCount)
            setBadgeNumber(R.id.mentions_fragment, state.mentionsUnreadCount)
        }

        nameTextView.text = state.user.name
        avatarView.setUserData(state.user)
    }
}
