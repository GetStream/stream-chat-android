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
import io.getstream.chat.android.client.models.name
import io.getstream.chat.android.livedata.utils.EventObserver
import io.getstream.chat.android.ui.avatar.AvatarView
import io.getstream.chat.android.ui.channel.list.header.viewmodel.ChannelListHeaderViewModel
import io.getstream.chat.android.ui.channel.list.header.viewmodel.bindView
import io.getstream.chat.ui.sample.R
import io.getstream.chat.ui.sample.application.EXTRA_CHANNEL_ID
import io.getstream.chat.ui.sample.application.EXTRA_CHANNEL_TYPE
import io.getstream.chat.ui.sample.application.EXTRA_MESSAGE_ID
import io.getstream.chat.ui.sample.common.navigateSafely
import io.getstream.chat.ui.sample.common.setBadgeNumber
import io.getstream.chat.ui.sample.databinding.FragmentHomeBinding
import io.getstream.chat.ui.sample.util.extensions.useAdjustNothing

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeFragmentViewModel by viewModels()
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        parseNotificationData()
        setupBottomNavigation()
        setupNavigationDrawer()
        viewModel.state.observe(viewLifecycleOwner, ::renderState)
        viewModel.events.observe(
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
        }
    }

    private fun parseNotificationData() {
        requireActivity().intent?.let {
            if (it.hasExtra(EXTRA_CHANNEL_ID) && it.hasExtra(EXTRA_MESSAGE_ID) && it.hasExtra(EXTRA_CHANNEL_TYPE)) {
                val channelType = it.getStringExtra(EXTRA_CHANNEL_TYPE)
                val channelId = it.getStringExtra(EXTRA_CHANNEL_ID)
                val cid = "$channelType:$channelId"
                val messageId = it.getStringExtra(EXTRA_MESSAGE_ID)
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
            getOrCreateBadge(R.id.channels_fragment)?.apply {
                backgroundColor = ContextCompat.getColor(requireContext(), R.color.stream_ui_accent_red)
                badgeTextColor = ContextCompat.getColor(requireContext(), R.color.stream_ui_literal_white)
            }
            getOrCreateBadge(R.id.mentions_fragment)?.apply {
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
            viewModel.onUiAction(HomeFragmentViewModel.UiAction.LogoutClicked)
        }
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
