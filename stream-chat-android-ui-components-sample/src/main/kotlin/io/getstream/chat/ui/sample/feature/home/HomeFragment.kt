package io.getstream.chat.ui.sample.feature.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
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
import io.getstream.chat.ui.sample.R
import io.getstream.chat.ui.sample.common.navigateSafely
import io.getstream.chat.ui.sample.common.setBadgeNumber
import io.getstream.chat.ui.sample.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeFragmentViewModel by viewModels()

    private lateinit var avatarView: AvatarView
    private lateinit var nameTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupBottomNavigation()
        setupNavigationDrawer()
        viewModel.state.observe(viewLifecycleOwner, ::renderState)
        viewModel.events.observe(
            viewLifecycleOwner,
            EventObserver {
                navigateToLoginScreen()
            }
        )
        binding.channelListHeaderView.apply {
            setOnAddChannelButtonClickListener {
                findNavController().navigateSafely(R.id.action_homeFragment_to_addChannelFragment)
            }
            viewModel.online.observe(viewLifecycleOwner) { isOnline ->
                if (isOnline) {
                    showOnlineTitle()
                } else {
                    showOfflineTitle()
                }
            }
            setUser(viewModel.currentUser)
            setOnUserAvatarClickListener {
                binding.drawerLayout.openDrawer(GravityCompat.START)
            }
        }
        binding.navigationView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.directChatFragment -> {
                    findNavController().navigateSafely(R.id.action_homeFragment_to_addChannelFragment)
                    true
                }
                R.id.groupChatFragment -> {
                    findNavController().navigateSafely(R.id.action_homeFragment_to_addGroupChannelFragment)
                    true
                }
                else -> {
                    false
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupBottomNavigation() {
        val navHostFragment =
            childFragmentManager.findFragmentById(R.id.hostFragmentContainer) as NavHostFragment
        binding.bottomNavigationView.setupWithNavController(navHostFragment.navController)
        // disable reloading fragment when clicking again on the same tab
        binding.bottomNavigationView.setOnNavigationItemReselectedListener {}
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

        binding.signOutTextView.setOnClickListener {
            viewModel.onUiAction(HomeFragmentViewModel.UiAction.LogoutClicked)
        }
    }

    private fun renderState(state: HomeFragmentViewModel.State) {
        binding.bottomNavigationView.setBadgeNumber(R.id.channels_fragment, state.totalUnreadCount)
        binding.bottomNavigationView.setBadgeNumber(R.id.mentions_fragment, state.mentionsUnreadCount)

        nameTextView.text = state.user.name
        avatarView.setUserData(state.user)
    }

    private fun navigateToLoginScreen() {
        findNavController().navigateSafely(R.id.action_to_userLoginFragment)
    }
}
