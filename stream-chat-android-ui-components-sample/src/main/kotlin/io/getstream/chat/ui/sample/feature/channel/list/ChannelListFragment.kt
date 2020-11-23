package io.getstream.chat.ui.sample.feature.channel.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.getstream.sdk.chat.viewmodel.channels.ChannelsViewModel
import com.getstream.sdk.chat.viewmodel.factory.ChannelsViewModelFactory
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.name
import io.getstream.chat.android.ui.channel.list.viewmodel.bindView
import io.getstream.chat.ui.sample.R
import io.getstream.chat.ui.sample.databinding.FragmentChannelsBinding

class ChannelListFragment : Fragment() {

    private val viewModel: ChannelsViewModel by viewModels { ChannelsViewModelFactory() }

    private var _binding: FragmentChannelsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentChannelsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupOnClickListeners()
        binding.channelsView.apply {

            val loadingView = layoutInflater.inflate(
                R.layout.channels_loading_view,
                view as ViewGroup,
                false
            )

            setLoadingView(loadingView, FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT))

            setChannelClickListener {
                ChatLogger.instance.logD(it.name, "clicked!")
            }

            viewModel.bindView(this, viewLifecycleOwner)
        }
    }

    private fun setupOnClickListeners() {
        activity?.apply {
            onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
                finish()
            }
        }
    }
}
