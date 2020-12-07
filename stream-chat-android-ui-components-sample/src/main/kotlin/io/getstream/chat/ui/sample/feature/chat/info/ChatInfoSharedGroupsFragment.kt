package io.getstream.chat.ui.sample.feature.chat.info

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.getstream.sdk.chat.viewmodel.channels.ChannelsViewModel
import com.getstream.sdk.chat.viewmodel.factory.ChannelsViewModelFactory
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.livedata.ChatDomain
import io.getstream.chat.android.ui.channel.list.viewmodel.bindView
import io.getstream.chat.ui.sample.R
import io.getstream.chat.ui.sample.common.initToolbar
import io.getstream.chat.ui.sample.common.navigateSafely
import io.getstream.chat.ui.sample.databinding.FragmentChatInfoSharedGroupsBinding

class ChatInfoSharedGroupsFragment : Fragment() {

    private val args: ChatInfoSharedGroupsFragmentArgs by navArgs()
    private val viewModel: ChannelsViewModel by viewModels {
        ChannelsViewModelFactory(
            filter = Filters.and(
                Filters.eq("type", "messaging"),
                Filters.`in`("members", listOf(ChatDomain.instance().currentUser.id, args.memberId)),
                Filters.ne("draft", true)
            ),
        )
    }

    private var _binding: FragmentChatInfoSharedGroupsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatInfoSharedGroupsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initToolbar(binding.toolbar)
        binding.channelsView.apply {

            val loadingView = layoutInflater.inflate(
                R.layout.channels_loading_view,
                view as ViewGroup,
                false
            )

            setLoadingView(loadingView, FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT))

            setChannelClickListener {
                findNavController().navigateSafely(ChatInfoSharedGroupsFragmentDirections.actionOpenChat(it.cid, null))
            }

            viewModel.bindView(this, viewLifecycleOwner)
        }
    }
}
