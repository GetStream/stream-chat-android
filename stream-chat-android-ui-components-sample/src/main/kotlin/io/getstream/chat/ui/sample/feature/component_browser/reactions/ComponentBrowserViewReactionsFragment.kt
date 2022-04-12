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
import io.getstream.chat.android.ui.SupportedReactions.DefaultReactionTypes.THUMBS_UP
import io.getstream.chat.android.ui.SupportedReactions.DefaultReactionTypes.WUT
import io.getstream.chat.ui.sample.databinding.FragmentComponentBrowserViewReactionsViewBinding
import io.getstream.chat.ui.sample.feature.component_browser.utils.randomMessage

const val CUSTOM_REACTIONS = "CUSTOM_REACTIONS"

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
        val reactions = arguments?.getSerializable(CUSTOM_REACTIONS)

        binding.apply {
            viewReactionsView1.setMessage(
                message = randomMessage().apply {
                    reactionCounts = mutableMapOf(
                        LOVE to 1,
                    )
                    ownReactions = mutableListOf(
                        Reaction(type = LOVE),
                    )
                },
                isMyMessage = true
            )
            viewReactionsView2.setMessage(
                message = randomMessage().apply {
                    reactionCounts = mutableMapOf(
                        LOVE to 1,
                    )
                    ownReactions = mutableListOf(
                        Reaction(type = LOVE),
                    )
                },
                isMyMessage = false
            )
            viewReactionsView3.setMessage(
                message = randomMessage().apply {
                    reactionCounts = mutableMapOf(
                        LOVE to 1,
                    )
                    ownReactions = mutableListOf()
                },
                isMyMessage = true
            )
            viewReactionsView4.setMessage(
                message = randomMessage().apply {
                    reactionCounts = mutableMapOf(
                        LOVE to 1,
                    )
                    ownReactions = mutableListOf()
                },
                isMyMessage = false
            )
            viewReactionsView5.setMessage(
                message = randomMessage().apply {
                    reactionCounts = mutableMapOf(
                        LOVE to 10,
                        WUT to 20,
                        LOL to 20,
                        THUMBS_UP to 20
                    )
                    ownReactions = mutableListOf(
                        Reaction(type = LOVE),
                        Reaction(type = WUT)
                    )
                },
                isMyMessage = true
            )

            val customReactions = if (reactions != null) {
                reactions as Map<String, Int>
            } else {
                mutableMapOf(
                    LOVE to 10,
                    WUT to 20,
                    LOL to 20,
                    THUMBS_UP to 20
                )
            }

            viewReactionsView6.setMessage(
                message = randomMessage().apply {
                    reactionCounts = customReactions.toMutableMap()
                    ownReactions = mutableListOf(
                        Reaction(type = LOVE),
                        Reaction(type = WUT)
                    )
                },
                isMyMessage = false
            )
        }
    }
}
