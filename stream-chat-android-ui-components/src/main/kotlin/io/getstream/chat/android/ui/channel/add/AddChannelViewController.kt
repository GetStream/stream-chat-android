package io.getstream.chat.android.ui.channel.add

import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.models.name

internal class AddChannelViewController(
    private val headerView: AddChannelHeaderView,
    private val usersTitle: TextView,
    private val usersRecyclerView: RecyclerView,
    private val createGroupContainer: ViewGroup
) {

    private val usersAdapter = AddChannelUsersAdapter()
    private val members: MutableList<User> = mutableListOf()
    private val userInfoList: MutableList<UserInfo> = mutableListOf()

    var membersChangedListener = AddChannelView.MembersChangedListener {}
    var addMemberButtonClickListener = AddChannelView.AddMemberButtonClickListener {}

    init {
        usersRecyclerView.adapter = usersAdapter
        usersAdapter.userClickListener = AddChannelUsersAdapter.UserClickListener {
            onUserClicked(it)
        }
        headerView.apply {
            membersInputListener = object : AddChannelHeaderView.Listener {
                override fun onInputChanged(query: String) {
                    // Filter users list
                }
            }
            setAddMemberButtonClickListener {
                showInput()
                addMemberButtonClickListener.onAddMemberButtonClicked()
                usersRecyclerView.isVisible = true
            }
            setMemberClickListener {
                onUserClicked(UserInfo(it, true))
            }
        }
    }

    fun setUsers(users: List<User>) {
        userInfoList.clear()
        addMoreUsers(users)
    }

    fun addMoreUsers(users: List<User>) {
        userInfoList.addAll(users.map { UserInfo(it, false) })
        showSectionedUsers(userInfoList)
    }

    private fun showSectionedUsers(userInfoList: List<UserInfo>) {
        val sectionedUsers = userInfoList
            .groupBy { it.user.name.first().toUpperCase() }
            .toSortedMap()
            .flatMap { (letter, users) ->
                mutableListOf(UserListItem.Separator(letter)) + users.map { UserListItem.UserItem(it) }
            }
        usersAdapter.submitList(sectionedUsers)
    }

    private fun hideInput() {
        headerView.hideInput()
        headerView.showAddMemberButton()
        usersTitle.isVisible = false
    }

    private fun showInput() {
        headerView.showInput()
        headerView.hideAddMemberButton()
        usersTitle.isVisible = true
    }

    private fun onUserClicked(userInfo: UserInfo) {
        // Update members
        if (userInfo.isSelected) {
            members.remove(userInfo.user)
        } else {
            members.add(userInfo.user)
        }
        if (members.isEmpty()) {
            createGroupContainer.isVisible = true
            usersRecyclerView.isVisible = true
            showInput()
        } else {
            createGroupContainer.isVisible = false
            usersRecyclerView.isVisible = false
            hideInput()
        }
        headerView.setMembers(members.toList())
        membersChangedListener.onMembersChanged(members)

        // Update user list
        val index = userInfoList.indexOf(userInfo)
        if (index != -1) {
            userInfoList[index] = userInfoList[index].copy(isSelected = !userInfo.isSelected)
            showSectionedUsers(userInfoList)
        }
    }
}
