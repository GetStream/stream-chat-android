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

package io.getstream.chat.ui.sample.feature.chat.info.group.member

// class GroupChatInfoMemberOptionsViewModel(
//     private val cid: String,
//     private val memberId: String,
//     private val chatClient: ChatClient = ChatClient.instance(),
// ) : ViewModel() {
//
//     private companion object {
//         const val DEFAULT_BAN_TIMEOUT = 60 // 1 hour
//     }
//
//     private val _events = MutableLiveData<Event<UiEvent>>()
//     private val _state: MediatorLiveData<State> = MediatorLiveData()
//     private val _errorEvents: MutableLiveData<Event<ErrorEvent>> = MutableLiveData()
//     val events: LiveData<Event<UiEvent>> = _events
//     val state: LiveData<State> = _state
//     val errorEvents: LiveData<Event<ErrorEvent>> = _errorEvents
//
//     init {
//         viewModelScope.launch {
//             val currentUser = chatClient.clientState.user.value!!
//
//             val result = chatClient.queryChannels(
//                 request = QueryChannelsRequest(
//                     filter = Filters.and(
//                         Filters.eq("type", "messaging"),
//                         Filters.distinct(listOf(memberId, currentUser.id)),
//                     ),
//                     querySort = QuerySortByField.descByName("last_updated"),
//                     messageLimit = 0,
//                     limit = 1,
//                 ),
//             ).await()
//
//             val directChannelCid = when (result) {
//                 is Result.Success -> if (result.value.isNotEmpty()) result.value.first().cid else null
//                 is Result.Failure -> null
//             }
//
//             _state.value = State(directChannelCid = directChannelCid, loading = false)
//         }
//     }
//
//     fun onAction(action: Action) {
//         when (action) {
//             is Action.MessageClicked -> handleMessageClicked()
//             is Action.RemoveFromChannel -> removeFromChannel(action.username)
//             is Action.BanMember -> banMember(action.timeout)
//             is Action.UnbanMember -> unbanMember()
//         }
//     }
//
//     private fun handleMessageClicked() {
//         val state = state.value!!
//         _events.value = Event(
//             if (state.directChannelCid != null) {
//                 UiEvent.RedirectToChat(state.directChannelCid)
//             } else {
//                 UiEvent.RedirectToChatPreview
//             },
//         )
//     }
//
//     private fun removeFromChannel(username: String) {
//         viewModelScope.launch {
//             val message = Message(text = "$username has been removed from this channel")
//             when (chatClient.channel(cid).removeMembers(listOf(memberId), message).await()) {
//                 is Result.Success -> _events.value = Event(UiEvent.Dismiss)
//                 is Result.Failure -> _errorEvents.postValue(Event(ErrorEvent.RemoveMemberError))
//             }
//         }
//     }
//
//     private fun banMember(timeout: Int? = null) {
//         viewModelScope.launch {
//             when (chatClient.channel(cid).banUser(memberId, reason = null, timeout = timeout).await()) {
//                 is Result.Success -> _events.value = Event(UiEvent.Dismiss)
//                 is Result.Failure -> _errorEvents.postValue(Event(ErrorEvent.BanMemberError))
//             }
//         }
//     }
//
//     private fun unbanMember() {
//         viewModelScope.launch {
//             when (chatClient.channel(cid).unbanUser(memberId).await()) {
//                 is Result.Success -> _events.value = Event(UiEvent.Dismiss)
//                 is Result.Failure -> _errorEvents.postValue(Event(ErrorEvent.UnbanMemberError))
//             }
//         }
//     }
//
//     data class State(val directChannelCid: String?, val loading: Boolean)
//
//     sealed class Action {
//         data object MessageClicked : Action()
//         data class RemoveFromChannel(val username: String) : Action()
//         data class BanMember(val timeout: Int? = DEFAULT_BAN_TIMEOUT) : Action()
//         data object UnbanMember : Action()
//     }
//
//     sealed class UiEvent {
//         data object Dismiss : UiEvent()
//         data class RedirectToChat(val cid: String) : UiEvent()
//         data object RedirectToChatPreview : UiEvent()
//     }
//
//     sealed class ErrorEvent {
//         data object RemoveMemberError : ErrorEvent()
//         data object BanMemberError : ErrorEvent()
//         data object UnbanMemberError : ErrorEvent()
//     }
// }
//
// class GroupChatInfoMemberOptionsViewModelFactory(private val cid: String, private val memberId: String) :
//     ViewModelProvider.Factory {
//     override fun <T : ViewModel> create(modelClass: Class<T>): T {
//         require(modelClass == GroupChatInfoMemberOptionsViewModel::class.java) {
//             "GroupChatInfoMemberOptionsViewModelFactory can only create instances of GroupChatInfoMemberOptionsViewModel"
//         }
//
//         @Suppress("UNCHECKED_CAST")
//         return GroupChatInfoMemberOptionsViewModel(cid, memberId) as T
//     }
// }
