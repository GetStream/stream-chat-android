package io.getstream.chat.android.ui.channel.add

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.models.name

internal class AddChannelViewController(
    private val headerView: AddChannelHeaderView,
    private val usersRecyclerView: RecyclerView,
    private val createGroupContainer: ViewGroup
) {

    private val usersAdapter = AddChannelUsersAdapter()
    private val members: MutableList<User> = mutableListOf()
    private var userInfoList: MutableList<UserInfo> = mutableListOf()

    init {
        usersRecyclerView.adapter = usersAdapter
        usersAdapter.userClickListener = AddChannelUsersAdapter.UserClickListener {
            onUserClicked(it)
        }
        headerView.membersInputListener = object : AddChannelHeaderView.Listener {
            override fun onInputChanged(query: String) {
                // Filter users list
            }
        }

        headerView.setAddMemberButtonClickListener {
            addMemberButtonClicked()
        }
    }

    fun setUsers(users: List<User>) {
        userInfoList.clear()
        userInfoList.addAll(users.map { UserInfo(it, false) })
        showSectionedUsers(userInfoList)
    }

    private fun showSectionedUsers(userInfoList: List<UserInfo>) {
        val sectionedUsers = userInfoList
            .groupBy { it.user.name.first() }
            .toSortedMap()
            .flatMap { (letter, users) ->
                mutableListOf(UserListItem.Separator(letter)) + users.map { UserListItem.UserItem(it) }
            }
        usersAdapter.submitList(sectionedUsers)
    }

    private fun hideInput() {
        headerView.hideInput()
        headerView.showAddMemberButton()
    }

    private fun showInput() {
        headerView.showInput()
        headerView.hideAddMemberButton()
    }

    private fun addMemberButtonClicked() {
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
            showInput()
        } else {
            hideInput()
        }
        headerView.setMembers(members.toList())

        // Update user list
        val index = userInfoList.indexOf(userInfo)
        if (index != -1) {
            userInfoList[index] = userInfoList[index].copy(isSelected = !userInfo.isSelected)
            showSectionedUsers(userInfoList)
        }
    }
}
