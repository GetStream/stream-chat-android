package io.getstream.chat.ui.sample.feature.channel.add

import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.models.name
import io.getstream.chat.ui.sample.R
import io.getstream.chat.ui.sample.feature.channel.add.header.AddChannelHeader
import io.getstream.chat.ui.sample.feature.channel.add.header.MembersInputChangedListener

class AddChannelViewController(
    private val headerView: AddChannelHeader,
    private val usersTitle: TextView,
    private val usersRecyclerView: RecyclerView,
    private val createGroupContainer: ViewGroup,
    private val isAddGroupChannel: Boolean,
) {

    private val usersAdapter = AddChannelUsersAdapter()
    private val members: MutableList<User> = mutableListOf()
    private val userInfoList: MutableList<UserInfo> = mutableListOf()
    private var isSearching = false

    var membersChangedListener = AddChannelView.MembersChangedListener {}
    var addMemberButtonClickedListener = AddChannelView.AddMemberButtonClickedListener {}
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
                addMemberButtonClickedListener.onAddMemberButtonClicked()
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
        userInfoList.addAll(users.map { UserInfo(it, members.contains(it)) })
        showUsers(userInfoList)
    }

    fun setMembers(members: List<User>) {
        this.members.clear()
        this.members.addAll(members)
        headerView.setMembers(members.toList())

        showUsers(userInfoList.map { UserInfo(it.user, members.contains(it.user)) })
    }

    fun getMembers(): List<User> = members

    private fun showUsers(users: List<UserInfo>) {
        if (isSearching) {
            usersAdapter.submitList(users.map { UserListItem.UserItem(it) })
        } else {
            showSectionedUsers(users)
        }
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
        usersTitle.isVisible = isAddGroupChannel || false
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
            if (!isAddGroupChannel) {
                createGroupContainer.isVisible = true
            }
            usersRecyclerView.isVisible = true
            showInput()
        } else {
            if (!isAddGroupChannel) {
                createGroupContainer.isVisible = false
                usersRecyclerView.isVisible = false
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
}
