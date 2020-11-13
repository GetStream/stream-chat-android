package io.getstream.chat.ui.sample.feature.channels

import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.name
import io.getstream.chat.android.ui.channel.list.ChannelsView
import io.getstream.chat.android.ui.channel.list.adapter.diff.ChannelItemDiff
import io.getstream.chat.android.ui.channel.list.adapter.viewholder.BaseChannelListItemViewHolder
import io.getstream.chat.android.ui.channel.list.adapter.viewholder.BaseChannelViewHolderFactory
import io.getstream.chat.android.ui.channel.list.viewmodel.ChannelsViewModel
import io.getstream.chat.android.ui.channel.list.viewmodel.ChannelsViewModelFactory
import io.getstream.chat.android.ui.channel.list.viewmodel.bindView
import io.getstream.chat.ui.sample.R
import io.getstream.chat.ui.sample.databinding.FragmentChannelsBinding
import io.getstream.chat.ui.sample.databinding.SampleViewHolderBinding

class ChannelsFragment : Fragment() {

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

//            testViewHolderAPI()

            viewModel.bindView(this, viewLifecycleOwner)
        }
    }

    private fun ChannelsView.testViewHolderAPI() {
        class TestViewHolder(itemView: View) : BaseChannelListItemViewHolder(itemView) {
            override fun bind(channel: Channel, position: Int, diff: ChannelItemDiff?) {
                itemView.setOnClickListener {
                    channelClickListener?.onClick(channel)
                }

                SampleViewHolderBinding.bind(itemView).apply {
                    textView.text = channel.name
                    style?.let {
                        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, it.channelTitleText.size.toFloat())
                    }
                }
            }
        }

        val vhFactory = object : BaseChannelViewHolderFactory<TestViewHolder> {
            override fun createChannelViewHolder(layout: Int, parent: ViewGroup, viewType: Int): TestViewHolder {
                val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
                return TestViewHolder(view)
            }
        }

        setViewHolderFactory(vhFactory)
    }

    private fun setupOnClickListeners() {
        binding.channelsView.setOnChannelClickListener {
            ChatLogger.get("ChannelsFragment").logD("${it.name} clicked")
        }

        activity?.apply {
            onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
                finish()
            }
        }
    }
}
