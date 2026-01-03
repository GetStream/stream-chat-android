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

package io.getstream.chat.ui.sample.feature.channel.add

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import io.getstream.chat.android.models.User
import io.getstream.chat.android.ui.feature.messages.composer.MessageComposerView
import io.getstream.chat.android.ui.feature.messages.list.MessageListView
import io.getstream.chat.ui.sample.R
import io.getstream.chat.ui.sample.feature.channel.add.header.AddChannelHeader
import io.getstream.chat.ui.sample.feature.channel.add.header.MembersInputChangedListener

class AddChannelViewController(
    private val headerView: AddChannelHeader,
    private val usersTitle: TextView,
    private val usersRecyclerView: RecyclerView,
    private val createGroupContainer: ViewGroup,
    private val messageListView: MessageListView,
    private val messageComposerView: MessageComposerView,
    private val emptyStateView: View,
    private val loadingView: View,
    private val isAddGroupChannel: Boolean,
) {

    private val usersAdapter = AddChannelUsersAdapter()
    private val members: MutableList<User> = mutableListOf()
    private val userInfoList: MutableList<UserInfo> = mutableListOf()
    private var isSearching = false

    var membersChangedListener = AddChannelView.MembersChangedListener {}
    var searchInputChangedListener = AddChannelView.SearchInputChangedListener { }

    init {
        usersRecyclerView.adapter = usersAdapter
        usersAdapter.userClickListener = AddChannelUsersAdapter.UserClickListener {
            onUserClicked(it)
        }
        headerView.apply {
            membersInputListener = MembersInputChangedListener { query ->
                usersTitle.text = if (query.isEmpty()) {
                    isSearching = false
                    viewContext.getString(R.string.add_channel_user_list_title)
                } else {
                    isSearching = true
                    viewContext.getString(R.string.add_channel_user_list_search_title, query)
                }
                searchInputChangedListener.onInputChanged(query)
            }
            setAddMemberButtonClickListener {
                this@AddChannelViewController.showInput()
                showUsersView()
            }
            setMemberClickListener {
                onUserClicked(UserInfo(it, true))
            }
        }
    }

    fun setUsers(users: List<User>) {
        userInfoList.clear()
        addMoreUsers(users) {
            showUsersView()
        }
    }

    fun addMoreUsers(users: List<User>, usersSubmittedCallback: () -> Unit = {}) {
        userInfoList.addAll(users.map { UserInfo(it, members.contains(it)) })
        showUsers(userInfoList, usersSubmittedCallback)
    }

    fun setMembers(members: List<User>) {
        this.members.clear()
        this.members.addAll(members)
        headerView.setMembers(members.toList())

        showUsers(userInfoList.map { UserInfo(it.user, members.contains(it.user)) })
    }

    fun messageComposerViewClicked() {
        if (members.isNotEmpty()) {
            showMessageListView()
        }
    }

    private fun showUsersView() {
        usersRecyclerView.isVisible = userInfoList.isNotEmpty()
        emptyStateView.isVisible = userInfoList.isEmpty()
        usersTitle.isVisible = true
        messageListView.isVisible = false
        loadingView.isVisible = false
    }

    private fun showMessageListView() {
        usersRecyclerView.isVisible = false
        emptyStateView.isVisible = false
        usersTitle.isVisible = false
        messageListView.isVisible = true
        loadingView.isVisible = false
        hideInput()
    }

    private fun showUsers(users: List<UserInfo>, usersSubmittedCallback: () -> Unit = {}) {
        if (isSearching) {
            usersAdapter.submitList(users.map { UserListItem.UserItem(it) }, usersSubmittedCallback)
        } else {
            showSectionedUsers(users, usersSubmittedCallback)
        }
    }

    private fun showSectionedUsers(userInfoList: List<UserInfo>, usersSubmittedCallback: () -> Unit) {
        val sectionedUsers = userInfoList
            .groupBy { it.user.name.firstOrNull()?.uppercaseChar() ?: EMPTY_NAME_SYMBOL }
            .toSortedMap()
            .flatMap { (letter, users) ->
                mutableListOf(UserListItem.Separator(letter)) + users.map { UserListItem.UserItem(it) }
            }
        usersAdapter.submitList(sectionedUsers, usersSubmittedCallback)
    }

    private fun hideInput() {
        headerView.hideInput()
        headerView.showAddMemberButton()
    }

    private fun showInput() {
        headerView.showInput()
        headerView.hideAddMemberButton()
    }

    private fun onUserClicked(userInfo: UserInfo) {
        // Update members
        if (userInfo.isSelected) {
            members.remove(userInfo.user)
        } else {
            members.add(userInfo.user)
        }
        if (members.isEmpty()) {
            if (!isAddGroupChannel) {
                messageComposerView.isVisible = false
                createGroupContainer.isVisible = true
            }
            showUsersView()
            showInput()
        } else {
            if (!isAddGroupChannel) {
                messageComposerView.isVisible = true
                createGroupContainer.isVisible = false
            }
            hideInput()
        }
        headerView.setMembers(members.toList())
        membersChangedListener.onMembersChanged(members)

        // Update user list
        val index = userInfoList.indexOf(userInfo)
        if (index != -1) {
            userInfoList[index] = userInfoList[index].copy(isSelected = !userInfo.isSelected)
            showUsers(userInfoList)
        }
    }

    companion object {
        const val EMPTY_NAME_SYMBOL = Char.MAX_VALUE
    }
}
