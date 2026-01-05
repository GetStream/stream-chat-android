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

package io.getstream.chat.android.uitests.snapshot.uicomponents.reactions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.models.Reaction
import io.getstream.chat.android.ui.helper.SupportedReactions.DefaultReactionTypes.LOL
import io.getstream.chat.android.ui.helper.SupportedReactions.DefaultReactionTypes.LOVE
import io.getstream.chat.android.ui.helper.SupportedReactions.DefaultReactionTypes.THUMBS_DOWN
import io.getstream.chat.android.ui.helper.SupportedReactions.DefaultReactionTypes.THUMBS_UP
import io.getstream.chat.android.ui.helper.SupportedReactions.DefaultReactionTypes.WUT
import io.getstream.chat.android.uitests.databinding.FragmentComponentBrowserUserReactionsViewBinding
import io.getstream.chat.android.uitests.snapshot.utils.randomMessage
import io.getstream.chat.android.uitests.snapshot.utils.randomUser

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
                message = randomMessage().copy(
                    latestReactions = mutableListOf(
                        Reaction(type = LOVE, user = currentUser),
                    ),
                ),
                currentUser = currentUser,
            )
            userReactionsView2.setMessage(
                message = randomMessage().copy(
                    latestReactions = mutableListOf(
                        Reaction(type = LOVE, user = currentUser),
                        Reaction(type = LOVE, user = randomUser()),
                    ),
                ),
                currentUser = currentUser,
            )
            userReactionsView3.setMessage(
                message = randomMessage().copy(
                    latestReactions = mutableListOf(
                        Reaction(type = LOVE, user = currentUser),
                        Reaction(type = WUT, user = randomUser()),
                        Reaction(type = LOL, user = randomUser()),
                    ),
                ),
                currentUser = currentUser,
            )
            userReactionsView4.setMessage(
                message = randomMessage().copy(
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
                    ),
                ),
                currentUser = currentUser,
            )
        }
    }
}
