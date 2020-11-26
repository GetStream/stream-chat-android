package io.getstream.chat.ui.sample.feature.channel.add

import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.models.name
import io.getstream.chat.ui.sample.R
import io.getstream.chat.ui.sample.feature.channel.add.header.AddChannelHeaderView

class AddChannelViewController(
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
    var searchInputChangedListener = AddChannelView.SearchInputChangedListener { }

    init {
        usersRecyclerView.adapter = usersAdapter
        usersAdapter.userClickListener = AddChannelUsersAdapter.UserClickListener {
            onUserClicked(it)
        }
        headerView.apply {
            membersInputListener = object : AddChannelHeaderView.Listener {
                override fun onInputChanged(query: String) {
                    usersTitle.text = if (query.isEmpty()) {
                        context.getString(R.string.add_channel_user_list_title)
                    } else {
                        context.getString(R.string.add_channel_user_list_search_title, query)
                    }
                    searchInputChangedListener.onInputChanged(query)
                }
            }
            setAddMemberButtonClickListener {
                this@AddChannelViewController.showInput()
                addMemberButtonClickListener.onAddMemberButtonClicked()
                usersRecyclerView.isVisible = true
            }
            setMemberClickListener {
                onUserClicked(UserInfo(it, true))
            }
        }
    }

    fun setUsers(users: List<User>, shouldShowUserSections: Boolean) {
        userInfoList.clear()
        addMoreUsers(users, shouldShowUserSections)
    }

    fun addMoreUsers(users: List<User>, shouldShowUserSections: Boolean = true) {
        userInfoList.addAll(users.map { UserInfo(it, false) })
        if (shouldShowUserSections) {
            showSectionedUsers(userInfoList)
        } else {
            showUsers(userInfoList)
        }
    }

    private fun showUsers(users: List<UserInfo>) {
        usersAdapter.submitList(users.map { UserListItem.UserItem(it) })
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
