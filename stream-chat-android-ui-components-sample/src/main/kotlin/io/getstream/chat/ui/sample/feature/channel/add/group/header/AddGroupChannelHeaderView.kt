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

package io.getstream.chat.ui.sample.feature.channel.add.group.header

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.view.isVisible
import io.getstream.chat.android.models.User
import io.getstream.chat.ui.sample.databinding.AddGroupChannelHeaderViewBinding
import io.getstream.chat.ui.sample.feature.channel.add.header.AddChannelHeader
import io.getstream.chat.ui.sample.feature.channel.add.header.AddMemberButtonClickListener
import io.getstream.chat.ui.sample.feature.channel.add.header.MemberClickListener
import io.getstream.chat.ui.sample.feature.channel.add.header.MembersInputChangedListener

class AddGroupChannelHeaderView : FrameLayout, AddChannelHeader {

    override val viewContext: Context
        get() = context
    override var membersInputListener: MembersInputChangedListener = MembersInputChangedListener { }
    private val binding = AddGroupChannelHeaderViewBinding.inflate(LayoutInflater.from(context), this, true)
    private val adapter = AddGroupChannelMembersAdapter()

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr,
    ) {
        init()
    }

    private fun init() {
        binding.membersRecyclerView.adapter = adapter
        binding.usersSearchView.setDebouncedInputChangedListener {
            membersInputListener.onMembersInputChanged(it)
        }
    }

    override fun setMembers(members: List<User>) {
        adapter.submitList(members)
        binding.membersRecyclerView.isVisible = members.isNotEmpty()
    }

    override fun showInput() = Unit

    override fun hideInput() = Unit

    override fun showAddMemberButton() = Unit

    override fun hideAddMemberButton() = Unit

    override fun setAddMemberButtonClickListener(listener: AddMemberButtonClickListener) = Unit

    override fun setMemberClickListener(listener: MemberClickListener?) {
        adapter.memberClickListener = listener
    }
}
