package io.getstream.chat.ui.sample.feature.component_browser.reactions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.ui.common.ReactionType
import io.getstream.chat.ui.sample.databinding.FragmentComponentBrowserUserReactionsViewBinding
import io.getstream.chat.ui.sample.feature.component_browser.utils.randomMessage
import io.getstream.chat.ui.sample.feature.component_browser.utils.randomUser

@InternalStreamChatApi
class ComponentBrowserUserReactionsFragment : Fragment() {
    private var _binding: FragmentComponentBrowserUserReactionsViewBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentComponentBrowserUserReactionsViewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            val currentUser = randomUser()
            userReactionsView1.setMessage(
                message = randomMessage().apply {
                    latestReactions = mutableListOf(
                        Reaction(type = ReactionType.LOVE.type, user = currentUser),
                    )
                },
                currentUser = currentUser
            )
            userReactionsView2.setMessage(
                message = randomMessage().apply {
                    latestReactions = mutableListOf(
                        Reaction(type = ReactionType.LOVE.type, user = currentUser),
                        Reaction(type = ReactionType.LOVE.type, user = randomUser()),
                    )
                },
                currentUser = currentUser
            )
            userReactionsView3.setMessage(
                message = randomMessage().apply {
                    latestReactions = mutableListOf(
                        Reaction(type = ReactionType.LOVE.type, user = currentUser),
                        Reaction(type = ReactionType.WUT.type, user = randomUser()),
                        Reaction(type = ReactionType.LOL.type, user = randomUser()),
                    )
                },
                currentUser = currentUser
            )
            userReactionsView4.setMessage(
                message = randomMessage().apply {
                    latestReactions = mutableListOf(
                        Reaction(type = ReactionType.LOVE.type, user = currentUser),
                        Reaction(type = ReactionType.THUMBS_UP.type, user = randomUser()),
                        Reaction(type = ReactionType.THUMBS_DOWN.type, user = randomUser()),
                        Reaction(type = ReactionType.LOL.type, user = randomUser()),
                        Reaction(type = ReactionType.WUT.type, user = currentUser),
                        Reaction(type = ReactionType.LOVE.type, user = currentUser),
                        Reaction(type = ReactionType.THUMBS_UP.type, user = randomUser()),
                        Reaction(type = ReactionType.THUMBS_DOWN.type, user = randomUser()),
                        Reaction(type = ReactionType.LOL.type, user = randomUser()),
                        Reaction(type = ReactionType.WUT.type, user = randomUser()),
                        Reaction(type = ReactionType.LOVE.type, user = currentUser),
                        Reaction(type = ReactionType.THUMBS_UP.type, user = currentUser),
                        Reaction(type = ReactionType.THUMBS_DOWN.type, user = randomUser()),
                        Reaction(type = ReactionType.LOL.type, user = randomUser()),
                        Reaction(type = ReactionType.WUT.type, user = randomUser()),
                    )
                },
                currentUser = currentUser
            )
        }
    }
}
