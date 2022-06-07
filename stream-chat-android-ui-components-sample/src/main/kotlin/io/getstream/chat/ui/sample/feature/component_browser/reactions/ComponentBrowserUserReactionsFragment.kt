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

package io.getstream.chat.ui.sample.feature.component_browser.reactions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.ui.SupportedReactions.DefaultReactionTypes.LOL
import io.getstream.chat.android.ui.SupportedReactions.DefaultReactionTypes.LOVE
import io.getstream.chat.android.ui.SupportedReactions.DefaultReactionTypes.THUMBS_DOWN
import io.getstream.chat.android.ui.SupportedReactions.DefaultReactionTypes.THUMBS_UP
import io.getstream.chat.android.ui.SupportedReactions.DefaultReactionTypes.WUT
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
                        Reaction(type = LOVE, user = currentUser),
                    )
                },
                currentUser = currentUser
            )
            userReactionsView2.setMessage(
                message = randomMessage().apply {
                    latestReactions = mutableListOf(
                        Reaction(type = LOVE, user = currentUser),
                        Reaction(type = LOVE, user = randomUser()),
                    )
                },
                currentUser = currentUser
            )
            userReactionsView3.setMessage(
                message = randomMessage().apply {
                    latestReactions = mutableListOf(
                        Reaction(type = LOVE, user = currentUser),
                        Reaction(type = WUT, user = randomUser()),
                        Reaction(type = LOL, user = randomUser()),
                    )
                },
                currentUser = currentUser
            )
            userReactionsView4.setMessage(
                message = randomMessage().apply {
                    latestReactions = mutableListOf(
                        Reaction(type = LOVE, user = currentUser),
                        Reaction(type = THUMBS_UP, user = randomUser()),
                        Reaction(type = THUMBS_DOWN, user = randomUser()),
                        Reaction(type = LOL, user = randomUser()),
                        Reaction(type = WUT, user = currentUser),
                        Reaction(type = LOVE, user = currentUser),
                        Reaction(type = THUMBS_UP, user = randomUser()),
                        Reaction(type = THUMBS_DOWN, user = randomUser()),
                        Reaction(type = LOL, user = randomUser()),
                        Reaction(type = WUT, user = randomUser()),
                        Reaction(type = LOVE, user = currentUser),
                        Reaction(type = THUMBS_UP, user = currentUser),
                        Reaction(type = THUMBS_DOWN, user = randomUser()),
                        Reaction(type = LOL, user = randomUser()),
                        Reaction(type = WUT, user = randomUser()),
                    )
                },
                currentUser = currentUser
            )
        }
    }
}
