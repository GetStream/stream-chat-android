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

package io.getstream.chat.ui.sample.feature.channel.add.header

import android.content.Context
import io.getstream.chat.android.models.User

interface AddChannelHeader {
    val viewContext: Context
    var membersInputListener: MembersInputChangedListener

    fun setMembers(members: List<User>)

    fun showInput()

    fun hideInput()

    fun showAddMemberButton()

    fun hideAddMemberButton()

    fun setAddMemberButtonClickListener(listener: AddMemberButtonClickListener)

    fun setMemberClickListener(listener: MemberClickListener?)
}

fun interface MembersInputChangedListener {
    fun onMembersInputChanged(query: String)
}

fun interface AddMemberButtonClickListener {
    fun onButtonClick()
}

fun interface MemberClickListener {
    fun onMemberClicked(user: User)
}
