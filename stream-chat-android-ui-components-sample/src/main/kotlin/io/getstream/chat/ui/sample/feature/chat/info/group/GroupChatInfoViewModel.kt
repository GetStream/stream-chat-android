/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.ui.sample.feature.chat.info.group

// import androidx.lifecycle.LiveData
// import androidx.lifecycle.MediatorLiveData
// import androidx.lifecycle.MutableLiveData
// import androidx.lifecycle.ViewModel
// import androidx.lifecycle.asLiveData
// import androidx.lifecycle.viewModelScope
// import io.getstream.chat.android.client.ChatClient
// import io.getstream.chat.android.client.channel.ChannelClient
// import io.getstream.chat.android.client.channel.state.ChannelState
// import io.getstream.chat.android.client.setup.state.ClientState
// import io.getstream.chat.android.models.ChannelMute
// import io.getstream.chat.android.models.Member
// import io.getstream.chat.android.models.Message
// import io.getstream.chat.android.models.User
// import io.getstream.chat.android.state.extensions.watchChannelAsState
// import io.getstream.chat.android.state.utils.Event
// import io.getstream.log.taggedLogger
// import io.getstream.result.Result
// import kotlinx.coroutines.flow.Flow
// import kotlinx.coroutines.flow.distinctUntilChanged
// import kotlinx.coroutines.flow.filterNotNull
// import kotlinx.coroutines.flow.flatMapLatest
// import kotlinx.coroutines.flow.take
// import kotlinx.coroutines.launch
//
// class GroupChatInfoViewModel(
//     private val cid: String,
//     private val chatClient: ChatClient = ChatClient.instance(),
//     private val clientState: ClientState = chatClient.clientState,
// ) : ViewModel() {
//
//     private val logger by taggedLogger("GroupChatInfo-VM")
//
//     /**
//      * Holds information about the current channel and is actively updated.
//      */
//     private val channelState: Flow<ChannelState> =
//         chatClient.watchChannelAsState(cid, 0, viewModelScope).filterNotNull()
//
//     private val channelClient: ChannelClient = chatClient.channel(cid)
//     private val _state = MediatorLiveData<State>()
//     private val _events = MutableLiveData<Event<UiEvent>>()
//     private val _errorEvents: MutableLiveData<Event<ErrorEvent>> = MutableLiveData()
//     val events: LiveData<Event<UiEvent>> = _events
//     val state: LiveData<State> = _state
//     val errorEvents: LiveData<Event<ErrorEvent>> = _errorEvents
//
//     init {
//         _state.value = INITIAL_STATE
//
//         // Update channel mute status
//         clientState.user.value?.channelMutes?.let(::updateChannelMuteStatus)
//
//         // Update members
//         _state.addSource(channelState.flatMapLatest { it.members }.asLiveData(), this::updateMembers)
//
//         _state.addSource(channelState.flatMapLatest { it.channelData }.asLiveData()) { channelData ->
//             _state.value = _state.value?.copy(
//                 channelName = channelData.name,
//                 ownCapabilities = channelData.ownCapabilities,
//                 createdBy = channelData.createdBy,
//             )
//         }
//
//         _state.addSource(
//             channelState.flatMapLatest { it.hidden }
//                 .distinctUntilChanged()
//                 .take(1) // TODO we use take(1), cause ChannelState.hidden seems to be not updated properly
//                 .asLiveData(),
//         ) { hidden ->
//             logger.v { "[onHiddenChanged] hidden: $hidden" }
//             _state.value = _state.value?.copy(
//                 channelHidden = hidden,
//             )
//         }
//     }
//
//     fun onAction(action: Action) {
//         logger.d { "[onAction] action: $action" }
//         viewModelScope.launch {
//             when (action) {
//                 is Action.NameChanged -> changeGroupName(action.name)
//                 is Action.MemberClicked -> handleMemberClick(action.member)
//                 is Action.MembersSeparatorClicked -> _state.value = _state.value!!.copy(shouldExpandMembers = true)
//                 is Action.MuteChannelClicked -> switchGroupMute(action.isEnabled)
//                 is Action.HideChannelClicked -> switchGroupHide(action.isHidden, action.clearHistory)
//                 is Action.ChannelMutesUpdated -> updateChannelMuteStatus(action.channelMutes)
//                 is Action.ChannelHiddenUpdated -> updateChannelHideStatus(action.cid, action.hidden)
//                 is Action.LeaveChannelClicked -> leaveChannel()
//             }
//         }
//     }
//
//     private fun handleMemberClick(member: Member) {
//         if (member.getUserId() != clientState.user.value?.id) {
//             val currentState = _state.value!!
//             _events.value = Event(UiEvent.ShowMemberOptions(member, currentState.channelName))
//         }
//     }
//
//     private fun changeGroupName(name: String) {
//         viewModelScope.launch {
//             val result = channelClient.update(message = null, mapOf("name" to name)).await()
//             if (result is Result.Failure) {
//                 _errorEvents.postValue(Event(ErrorEvent.ChangeGroupNameError))
//             }
//         }
//     }
//
//     private fun leaveChannel() {
//         viewModelScope.launch {
//             val result = clientState.user.value?.let { user ->
//                 val message = Message(text = "${user.name} left")
//                 chatClient.channel(channelClient.channelType, channelClient.channelId)
//                     .removeMembers(listOf(user.id), message)
//                     .await()
//             } ?: return@launch
//
//             when (result) {
//                 is Result.Success -> _events.value = Event(UiEvent.RedirectToHome)
//                 is Result.Failure -> _errorEvents.postValue(Event(ErrorEvent.LeaveChannelError))
//             }
//         }
//     }
//
//     private fun updateMembers(members: List<Member>) {
//         val currentState = _state.value!!
//         _state.value =
//             currentState.copy(
//                 members = members,
//                 shouldExpandMembers = currentState.shouldExpandMembers ?: false || members.size <= COLLAPSED_MEMBERS_COUNT,
//                 membersToShowCount = members.size - COLLAPSED_MEMBERS_COUNT,
//             )
//     }
//
//     private fun updateChannelMuteStatus(channelMutes: List<ChannelMute>) {
//         _state.value = _state.value!!.copy(channelMuted = channelMutes.any { it.channel?.cid == cid })
//     }
//
//     private fun updateChannelHideStatus(eventCid: String, hidden: Boolean) {
//         if (eventCid != cid) return
//         logger.v { "[updateChannelHideStatus] hidden: $hidden" }
//         _state.value = _state.value!!.copy(channelHidden = hidden)
//     }
//
//     private fun switchGroupMute(isEnabled: Boolean) {
//         viewModelScope.launch {
//             val result = if (isEnabled) {
//                 channelClient.mute().await()
//             } else {
//                 channelClient.unmute().await()
//             }
//             if (result is Result.Failure) {
//                 _errorEvents.postValue(Event(ErrorEvent.MuteChannelError))
//             }
//         }
//     }
//
//     private fun switchGroupHide(hide: Boolean, clearHistory: Boolean?) {
//         logger.v { "[switchGroupHide] hide: $hide, clearHistory: $clearHistory" }
//         viewModelScope.launch {
//             val result = if (hide) {
//                 channelClient.hide(clearHistory = clearHistory == true).await()
//             } else {
//                 channelClient.show().await()
//             }
//             if (result is Result.Failure) {
//                 _errorEvents.postValue(Event(ErrorEvent.HideChannelError))
//             }
//         }
//     }
//
//     data class State(
//         val members: List<Member>,
//         val createdBy: User,
//         val channelName: String,
//         val channelMuted: Boolean,
//         val channelHidden: Boolean,
//         val shouldExpandMembers: Boolean?,
//         val membersToShowCount: Int,
//         val ownCapabilities: Set<String>,
//     )
//
//     sealed class Action {
//         data class NameChanged(val name: String) : Action()
//         data class MemberClicked(val member: Member) : Action()
//         data object MembersSeparatorClicked : Action()
//         data class MuteChannelClicked(val isEnabled: Boolean) : Action()
//         data class HideChannelClicked(val isHidden: Boolean, val clearHistory: Boolean? = null) : Action()
//         data class ChannelMutesUpdated(val channelMutes: List<ChannelMute>) : Action()
//
//         data class ChannelHiddenUpdated(
//             val cid: String,
//             val hidden: Boolean,
//             val clearHistory: Boolean? = null,
//         ) : Action()
//         data object LeaveChannelClicked : Action()
//     }
//
//     sealed class UiEvent {
//         data class ShowMemberOptions(val member: Member, val channelName: String) : UiEvent()
//         data object RedirectToHome : UiEvent()
//     }
//
//     sealed class ErrorEvent {
//         data object ChangeGroupNameError : ErrorEvent()
//         data object MuteChannelError : ErrorEvent()
//         data object HideChannelError : ErrorEvent()
//         data object LeaveChannelError : ErrorEvent()
//     }
//
//     companion object {
//         const val COLLAPSED_MEMBERS_COUNT = 5
//
//         private val INITIAL_STATE = State(
//             members = emptyList(),
//             createdBy = User(),
//             channelName = "",
//             channelMuted = false,
//             channelHidden = false,
//             shouldExpandMembers = null,
//             membersToShowCount = 0,
//             emptySet(),
//         )
//     }
// }
