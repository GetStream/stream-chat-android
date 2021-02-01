package io.getstream.chat.ui.sample.feature.component_browser.reactions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.ui.utils.ReactionType
import io.getstream.chat.ui.sample.databinding.FragmentComponentBrowserViewReactionsViewBinding
import io.getstream.chat.ui.sample.feature.component_browser.utils.randomMessage

@InternalStreamChatApi
class ComponentBrowserViewReactionsFragment : Fragment() {
    private var _binding: FragmentComponentBrowserViewReactionsViewBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentComponentBrowserViewReactionsViewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            viewReactionsView1.setMessage(
                message = randomMessage().apply {
                    reactionCounts = mutableMapOf(
                        ReactionType.LOVE.type to 1,
                    )
                    ownReactions = mutableListOf(
                        Reaction(type = ReactionType.LOVE.type),
                    )
                },
                isMyMessage = true
            )
            viewReactionsView2.setMessage(
                message = randomMessage().apply {
                    reactionCounts = mutableMapOf(
                        ReactionType.LOVE.type to 1,
                    )
                    ownReactions = mutableListOf(
                        Reaction(type = ReactionType.LOVE.type),
                    )
                },
                isMyMessage = false
            )
            viewReactionsView3.setMessage(
                message = randomMessage().apply {
                    reactionCounts = mutableMapOf(
                        ReactionType.LOVE.type to 1,
                    )
                    ownReactions = mutableListOf()
                },
                isMyMessage = true
            )
            viewReactionsView4.setMessage(
                message = randomMessage().apply {
                    reactionCounts = mutableMapOf(
                        ReactionType.LOVE.type to 1,
                    )
                    ownReactions = mutableListOf()
                },
                isMyMessage = false
            )
            viewReactionsView5.setMessage(
                message = randomMessage().apply {
                    reactionCounts = mutableMapOf(
                        ReactionType.LOVE.type to 10,
                        ReactionType.WUT.type to 20,
                        ReactionType.LOL.type to 20,
                        ReactionType.THUMBS_UP.type to 20
                    )
                    ownReactions = mutableListOf(
                        Reaction(type = ReactionType.LOVE.type),
                        Reaction(type = ReactionType.WUT.type)
                    )
                },
                isMyMessage = true
            )
            viewReactionsView6.setMessage(
                message = randomMessage().apply {
                    reactionCounts = mutableMapOf(
                        ReactionType.LOVE.type to 10,
                        ReactionType.WUT.type to 20,
                        ReactionType.LOL.type to 20,
                        ReactionType.THUMBS_UP.type to 20
                    )
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
