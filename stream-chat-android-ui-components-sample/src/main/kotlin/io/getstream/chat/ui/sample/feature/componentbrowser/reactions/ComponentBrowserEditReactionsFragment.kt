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
import io.getstream.chat.android.ui.helper.SupportedReactions.DefaultReactionTypes.LOVE
import io.getstream.chat.android.ui.helper.SupportedReactions.DefaultReactionTypes.WUT
import io.getstream.chat.ui.sample.databinding.FragmentComponentBrowserEditReactionsViewBinding
import io.getstream.chat.ui.sample.feature.componentbrowser.utils.randomMessage

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
                message = randomMessage().copy(
                    ownReactions = mutableListOf(),
                ),
                isMyMessage = true,
            )
            editReactionsView2.setMessage(
                message = randomMessage().copy(
                    ownReactions = mutableListOf(),
                ),
                isMyMessage = false,
            )
            editReactionsView3.setMessage(
                message = randomMessage().copy(
                    ownReactions = mutableListOf(
                        Reaction(type = LOVE),
                    ),
                ),
                isMyMessage = true,
            )
            editReactionsView4.setMessage(
                message = randomMessage().copy(
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
