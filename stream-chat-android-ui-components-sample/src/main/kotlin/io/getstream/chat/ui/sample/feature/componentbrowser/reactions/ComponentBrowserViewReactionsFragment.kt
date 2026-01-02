/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.ui.sample.feature.componentbrowser.reactions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.models.Reaction
import io.getstream.chat.android.models.ReactionGroup
import io.getstream.chat.android.ui.helper.SupportedReactions.DefaultReactionTypes.LOL
import io.getstream.chat.android.ui.helper.SupportedReactions.DefaultReactionTypes.LOVE
import io.getstream.chat.android.ui.helper.SupportedReactions.DefaultReactionTypes.THUMBS_UP
import io.getstream.chat.android.ui.helper.SupportedReactions.DefaultReactionTypes.WUT
import io.getstream.chat.ui.sample.databinding.FragmentComponentBrowserViewReactionsViewBinding
import io.getstream.chat.ui.sample.feature.componentbrowser.utils.randomMessage
import java.util.Date

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
                message = randomMessage().copy(
                    reactionGroups = mutableMapOf(
                        LOVE to ReactionGroup(
                            type = LOVE,
                            count = 1,
                            sumScore = 1,
                            firstReactionAt = Date(),
                            lastReactionAt = Date(),
                        ),
                    ),
                    ownReactions = mutableListOf(
                        Reaction(type = LOVE),
                    ),
                ),
                isMyMessage = true,
            )
            viewReactionsView2.setMessage(
                message = randomMessage().copy(
                    reactionGroups = mutableMapOf(
                        LOVE to ReactionGroup(
                            type = LOVE,
                            count = 1,
                            sumScore = 1,
                            firstReactionAt = Date(),
                            lastReactionAt = Date(),
                        ),
                    ),
                    ownReactions = mutableListOf(
                        Reaction(type = LOVE),
                    ),
                ),
                isMyMessage = false,
            )
            viewReactionsView3.setMessage(
                message = randomMessage().copy(
                    reactionGroups = mutableMapOf(
                        LOVE to ReactionGroup(
                            type = LOVE,
                            count = 1,
                            sumScore = 1,
                            firstReactionAt = Date(),
                            lastReactionAt = Date(),
                        ),
                    ),
                    ownReactions = mutableListOf(),
                ),
                isMyMessage = true,
            )
            viewReactionsView4.setMessage(
                message = randomMessage().copy(
                    reactionGroups = mutableMapOf(
                        LOVE to ReactionGroup(
                            type = LOVE,
                            count = 1,
                            sumScore = 1,
                            firstReactionAt = Date(),
                            lastReactionAt = Date(),
                        ),
                    ),
                    ownReactions = mutableListOf(),
                ),
                isMyMessage = false,
            )
            viewReactionsView5.setMessage(
                message = randomMessage().copy(
                    reactionGroups = mutableMapOf(
                        LOVE to ReactionGroup(
                            type = LOVE,
                            count = 10,
                            sumScore = 10,
                            firstReactionAt = Date(),
                            lastReactionAt = Date(),
                        ),
                        WUT to ReactionGroup(
                            type = WUT,
                            count = 20,
                            sumScore = 20,
                            firstReactionAt = Date(),
                            lastReactionAt = Date(),
                        ),
                        LOL to ReactionGroup(
                            type = LOL,
                            count = 20,
                            sumScore = 20,
                            firstReactionAt = Date(),
                            lastReactionAt = Date(),
                        ),
                        THUMBS_UP to ReactionGroup(
                            type = THUMBS_UP,
                            count = 20,
                            sumScore = 20,
                            firstReactionAt = Date(),
                            lastReactionAt = Date(),
                        ),
                    ),
                    ownReactions = mutableListOf(
                        Reaction(type = LOVE),
                        Reaction(type = WUT),
                    ),
                ),
                isMyMessage = true,
            )

            val customReactions = if (reactions != null) {
                reactions as Map<String, ReactionGroup>
            } else {
                mutableMapOf(
                    LOVE to ReactionGroup(
                        type = LOVE,
                        count = 10,
                        sumScore = 10,
                        firstReactionAt = Date(),
                        lastReactionAt = Date(),
                    ),
                    WUT to ReactionGroup(
                        type = WUT,
                        count = 20,
                        sumScore = 20,
                        firstReactionAt = Date(),
                        lastReactionAt = Date(),
                    ),
                    LOL to ReactionGroup(
                        type = LOL,
                        count = 20,
                        sumScore = 20,
                        firstReactionAt = Date(),
                        lastReactionAt = Date(),
                    ),
                    THUMBS_UP to ReactionGroup(
                        type = THUMBS_UP,
                        count = 20,
                        sumScore = 20,
                        firstReactionAt = Date(),
                        lastReactionAt = Date(),
                    ),
                )
            }

            viewReactionsView6.setMessage(
                message = randomMessage().copy(
                    reactionGroups = customReactions.toMutableMap(),
                    ownReactions = mutableListOf(
                        Reaction(type = LOVE),
                        Reaction(type = WUT),
                    ),
                ),
                isMyMessage = false,
            )
        }
    }
}
