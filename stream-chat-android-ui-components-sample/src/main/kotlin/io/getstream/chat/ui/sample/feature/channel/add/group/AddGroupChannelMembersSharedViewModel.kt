package io.getstream.chat.ui.sample.feature.channel.add.group

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.getstream.chat.android.client.models.User

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
