package io.getstream.chat.ui.sample.feature.component_browser.reactions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.ui.common.ReactionType
import io.getstream.chat.ui.sample.databinding.FragmentComponentBrowserEditReactionsViewBinding
import io.getstream.chat.ui.sample.feature.component_browser.utils.randomMessage

@InternalStreamChatApi
class ComponentBrowserEditReactionsFragment : Fragment() {
    private var _binding: FragmentComponentBrowserEditReactionsViewBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentComponentBrowserEditReactionsViewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            editReactionsView1.setMessage(
                message = randomMessage().apply {
                    ownReactions = mutableListOf()
                },
                isMyMessage = true
            )
            editReactionsView2.setMessage(
                message = randomMessage().apply {
                    ownReactions = mutableListOf()
                },
                isMyMessage = false
            )
            editReactionsView3.setMessage(
                message = randomMessage().apply {
                    ownReactions = mutableListOf(
                        Reaction(type = ReactionType.LOVE.type)
                    )
                },
                isMyMessage = true
            )
            editReactionsView4.setMessage(
                message = randomMessage().apply {
                    ownReactions = mutableListOf(
                        Reaction(type = ReactionType.LOVE.type),
                        Reaction(type = ReactionType.WUT.type)
                    )
                },
                isMyMessage = false
            )
        }
    }
}
