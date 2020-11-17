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
import com.getstream.sdk.chat.viewmodel.channels.ChannelsViewModel
import com.getstream.sdk.chat.viewmodel.factory.ChannelsViewModelFactory
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.name
import io.getstream.chat.android.ui.channel.list.ChannelListView
import io.getstream.chat.android.ui.channel.list.ChannelListViewStyle
import io.getstream.chat.android.ui.channel.list.ChannelsView
import io.getstream.chat.android.ui.channel.list.adapter.diff.ChannelDiff
import io.getstream.chat.android.ui.channel.list.adapter.viewholder.BaseChannelListItemViewHolder
import io.getstream.chat.android.ui.channel.list.adapter.viewholder.BaseChannelViewHolderFactory
import io.getstream.chat.android.ui.channel.list.viewmodel.bindView
import io.getstream.chat.ui.sample.R
import io.getstream.chat.ui.sample.databinding.FragmentChannelsBinding
import io.getstream.chat.ui.sample.databinding.SampleViewHolderBinding

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

            // testCustomViewHolderAPI()

            viewModel.bindView(this, viewLifecycleOwner)
        }
    }

    private fun ChannelsView.testCustomViewHolderAPI() {
        class TestViewHolder(itemView: View) : BaseChannelListItemViewHolder(itemView) {
            override fun bind(
                channel: Channel,
                diff: ChannelDiff?,
                channelClickListener: ChannelListView.ChannelClickListener,
                channelLongClickListener: ChannelListView.ChannelClickListener,
                userClickListener: ChannelListView.UserClickListener,
                style: ChannelListViewStyle?
            ) {
                itemView.setOnClickListener {
                    channelClickListener.onClick(channel)
                }

                SampleViewHolderBinding.bind(itemView).apply {
                    textView.text = channel.name
                    style?.let {
                        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, it.channelTitleText.size.toFloat())
                    }
                }
            }
        }

        setViewHolderFactory(
            object : BaseChannelViewHolderFactory<TestViewHolder>(R.layout.sample_view_holder) {
                override fun createChannelViewHolder(itemView: View): TestViewHolder = TestViewHolder(itemView)
            }
        )
    }

    private fun setupOnClickListeners() {
        activity?.apply {
            onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
                finish()
            }
        }
    }
}
