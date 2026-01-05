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

package io.getstream.chat.ui.sample.feature.channel.add.group

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.getstream.chat.android.models.User

class AddGroupChannelMembersSharedViewModel : ViewModel() {

    private val _members: MutableLiveData<List<User>> = MutableLiveData(emptyList())
    val members: LiveData<List<User>> = _members

    fun setMembers(memberList: List<User>) {
        _members.value = memberList
    }

    fun removeMember(member: User) {
        val members = _members.value?.toMutableList()
        members?.let {
            it.remove(member)
            _members.value = it
        }
    }
}
