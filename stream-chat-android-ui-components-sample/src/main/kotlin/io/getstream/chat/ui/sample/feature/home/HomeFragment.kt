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
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import io.getstream.chat.android.client.plugins.requests.ApiRequestsAnalyser
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.state.utils.EventObserver
import io.getstream.chat.android.ui.viewmodel.channels.ChannelListHeaderViewModel
import io.getstream.chat.android.ui.viewmodel.channels.bindView
import io.getstream.chat.android.ui.widgets.avatar.UserAvatarView
import io.getstream.chat.ui.sample.BuildConfig
import io.getstream.chat.ui.sample.R
import io.getstream.chat.ui.sample.common.navigateSafely
import io.getstream.chat.ui.sample.common.setBadgeNumber
import io.getstream.chat.ui.sample.databinding.FragmentHomeBinding
import io.getstream.chat.ui.sample.feature.EXTRA_CHANNEL_ID
import io.getstream.chat.ui.sample.feature.EXTRA_CHANNEL_TYPE
import io.getstream.chat.ui.sample.feature.EXTRA_MESSAGE_ID
import io.getstream.chat.ui.sample.feature.EXTRA_PARENT_MESSAGE_ID
import io.getstream.chat.ui.sample.feature.userlogin.UserLoginViewModel
import io.getstream.chat.ui.sample.util.extensions.useAdjustNothing

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val homeViewModel: HomeViewModel by viewModels()
    private val channelListHeaderViewModel: ChannelListHeaderViewModel by viewModels()

    private lateinit var userAvatarView: UserAvatarView
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
            EventObserver(::handleHomeEvents),
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
                setOnTitleClickListener {
                    if (ApiRequestsAnalyser.isInitialized()) {
                        Log.d("ApiRequestsAnalyser", ApiRequestsAnalyser.get().dumpAll())
                        Toast.makeText(
                            requireContext(),
                            "ApiRequestsAnalyser dumped all requests",
                            Toast.LENGTH_SHORT,
                        ).show()
                    }
                }

                setOnTitleLongClickListener {
                    if (ApiRequestsAnalyser.isInitialized()) {
                        ApiRequestsAnalyser.get().clearAll()
                        Toast.makeText(requireContext(), "ApiRequestsAnalyser clean", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun handleHomeEvents(uiEvent: HomeViewModel.UiEvent) {
        when (uiEvent) {
            HomeViewModel.UiEvent.NavigateToLoginScreenLogout -> {
                navigateSafely(
                    R.id.action_to_userLoginFragment,
                    Bundle().apply {
                        this.putBoolean(UserLoginViewModel.EXTRA_SWITCH_USER, false)
                    },
                )
            }
            HomeViewModel.UiEvent.NavigateToLoginScreenSwitchUser -> {
                navigateSafely(
                    R.id.action_to_userLoginFragment,
                    Bundle().apply {
                        this.putBoolean(UserLoginViewModel.EXTRA_SWITCH_USER, true)
                    },
                )
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
                val parentMessageId = it.getStringExtra(EXTRA_PARENT_MESSAGE_ID)

                requireActivity().intent = null

                findNavController().navigateSafely(
                    HomeFragmentDirections.actionOpenChat(cid, messageId, parentMessageId),
                )
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
            getOrCreateBadge(R.id.threads_fragment).apply {
                backgroundColor = ContextCompat.getColor(requireContext(), R.color.stream_ui_accent_red)
                badgeTextColor = ContextCompat.getColor(requireContext(), R.color.stream_ui_literal_white)
            }
        }
    }

    private fun setupNavigationDrawer() {
        AppBarConfiguration(
            setOf(R.id.directChatFragment, R.id.groupChatFragment),
            binding.drawerLayout,
        )
        binding.navigationView.setupWithNavController(findNavController())

        val header = binding.navigationView.getHeaderView(0)
        userAvatarView = header.findViewById(R.id.userAvatarView)
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

        binding.switchUserTextView.setOnClickListener {
            homeViewModel.onUiAction(HomeViewModel.UiAction.SwitchUserClicked)
        }
        binding.signOutTextView.setOnClickListener {
            homeViewModel.onUiAction(HomeViewModel.UiAction.LogoutClicked)
        }
        binding.versionName.text = BuildConfig.VERSION_NAME
    }

    private fun renderState(state: HomeViewModel.UiState) {
        binding.bottomNavigationView.apply {
            setBadgeNumber(R.id.channels_fragment, state.totalUnreadCount)
            setBadgeNumber(R.id.mentions_fragment, state.mentionsUnreadCount)
            setBadgeNumber(R.id.threads_fragment, state.unreadThreadsCount)
        }

        nameTextView.text = state.user.name
        userAvatarView.setUser(state.user)
    }
}
