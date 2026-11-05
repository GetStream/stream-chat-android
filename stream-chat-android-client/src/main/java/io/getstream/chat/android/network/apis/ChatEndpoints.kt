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

@file:Suppress(
    "ArrayInDataClass",
    "EnumEntryName",
    "RemoveRedundantQualifierName",
    "UnusedImport",
)

package io.getstream.chat.android.network.apis

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

internal interface ChatEndpoints {

    /**
     * Get App Settings
     * This Method returns the application settings
     */
    @GET("/api/v2/app")
    suspend fun getApp(): io.getstream.chat.android.network.models.GetApplicationResponse

    /**
     * Delete draft
     * Deletes a draft
     */
    @DELETE("/api/v2/chat/channels/{type}/{id}/draft")
    suspend fun deleteDraft(
        @Path("type") type: String,
        @Path("id") id: String,
        @Query("parent_id") parentId: String? = null,
    ): io.getstream.chat.android.network.models.Response

    /**
     * Create a draft
     * Creates a draft
     */
    @POST("/api/v2/chat/channels/{type}/{id}/draft")
    suspend fun createDraft(
        @Path("type") type: String,
        @Path("id") id: String,
        @Body createDraftRequest: io.getstream.chat.android.network.models.CreateDraftRequest,
    ): io.getstream.chat.android.network.models.CreateDraftResponse

    /**
     * Query draft messages
     * Queries draft messages for a user
     */
    @POST("/api/v2/chat/drafts/query")
    suspend fun queryDrafts(
        @Body queryDraftsRequest: io.getstream.chat.android.network.models.QueryDraftsRequest,
    ): io.getstream.chat.android.network.models.QueryDraftsResponse

    /**
     * Cast vote
     * Cast a vote on a poll
     */
    @POST("/api/v2/chat/messages/{message_id}/polls/{poll_id}/vote")
    suspend fun castPollVote(
        @Path("message_id") messageId: String,
        @Path("poll_id") pollId: String,
        @Body castPollVoteRequest: io.getstream.chat.android.network.models.CastPollVoteRequest,
    ): io.getstream.chat.android.network.models.PollVoteResponse

    /**
     * Delete vote
     * Delete a vote from a poll
     */
    @DELETE("/api/v2/chat/messages/{message_id}/polls/{poll_id}/vote/{vote_id}")
    suspend fun deletePollVote(
        @Path("message_id") messageId: String,
        @Path("poll_id") pollId: String,
        @Path("vote_id") voteId: String,
    ): io.getstream.chat.android.network.models.PollVoteResponse

    /**
     * Delete reminder
     * Deletes a user's created reminder
     */
    @DELETE("/api/v2/chat/messages/{message_id}/reminders")
    suspend fun deleteReminder(
        @Path("message_id") messageId: String,
    ): io.getstream.chat.android.network.models.DeleteReminderResponse

    /**
     * Updates Reminder
     * Updates an existing reminder. remind_at and expires_at are both replaced on every call: omitting either one clears it
     */
    @PATCH("/api/v2/chat/messages/{message_id}/reminders")
    suspend fun updateReminder(
        @Path("message_id") messageId: String,
        @Body updateReminderRequest: io.getstream.chat.android.network.models.UpdateReminderRequest,
    ): io.getstream.chat.android.network.models.UpdateReminderResponse

    /**
     * Create reminder
     * Creates a new reminder. An optional expires_at hides the reminder from every read once it passes and stops it counting against the per-user reminder cap
     */
    @POST("/api/v2/chat/messages/{message_id}/reminders")
    suspend fun createReminder(
        @Path("message_id") messageId: String,
        @Body createReminderRequest: io.getstream.chat.android.network.models.CreateReminderRequest,
    ): io.getstream.chat.android.network.models.CreateReminderResponse

    /**
     * Query reminders
     * Queries reminders
     */
    @POST("/api/v2/chat/reminders/query")
    suspend fun queryReminders(
        @Body queryRemindersRequest: io.getstream.chat.android.network.models.QueryRemindersRequest,
    ): io.getstream.chat.android.network.models.QueryRemindersResponse

    /**
     * Unread counts
     * Fetch unread counts for a single user
     */
    @GET("/api/v2/chat/unread")
    suspend fun unreadCounts(
        @Query("include_large_channels") includeLargeChannels: Boolean? = null,
    ): io.getstream.chat.android.network.models.WrappedUnreadCountsResponse

    /**
     * Delete device
     * Deletes one device
     */
    @DELETE("/api/v2/devices")
    suspend fun deleteDevice(
        @Query("id") id: String,
    ): io.getstream.chat.android.network.models.Response

    /**
     * List devices
     * Returns all available devices
     */
    @GET("/api/v2/devices")
    suspend fun listDevices(): io.getstream.chat.android.network.models.ListDevicesResponse

    /**
     * Create device
     * Adds a new device to a user, if the same device already exists the call will have no effect
     */
    @POST("/api/v2/devices")
    suspend fun createDevice(
        @Body createDeviceRequest: io.getstream.chat.android.network.models.CreateDeviceRequest,
    ): io.getstream.chat.android.network.models.Response

    /**
     * Get OG
     * Get an OpenGraph attachment for a link
     */
    @GET("/api/v2/og")
    suspend fun getOG(
        @Query("url") url: String,
    ): io.getstream.chat.android.network.models.GetOGResponse

    /**
     * Create poll
     * Creates a new poll
     */
    @POST("/api/v2/polls")
    suspend fun createPoll(
        @Body createPollRequest: io.getstream.chat.android.network.models.CreatePollRequest,
    ): io.getstream.chat.android.network.models.PollResponse

    /**
     * Update poll
     * Updates a poll
     */
    @PUT("/api/v2/polls")
    suspend fun updatePoll(
        @Body updatePollRequest: io.getstream.chat.android.network.models.UpdatePollRequest,
    ): io.getstream.chat.android.network.models.PollResponse

    /**
     * Query polls
     * Queries polls
     */
    @POST("/api/v2/polls/query")
    suspend fun queryPolls(
        @Body queryPollsRequest: io.getstream.chat.android.network.models.QueryPollsRequest,
    ): io.getstream.chat.android.network.models.QueryPollsResponse

    /**
     * Delete poll
     * Deletes a poll
     */
    @DELETE("/api/v2/polls/{poll_id}")
    suspend fun deletePoll(
        @Path("poll_id") pollId: String,
    ): io.getstream.chat.android.network.models.Response

    /**
     * Get poll
     * Retrieves a poll
     */
    @GET("/api/v2/polls/{poll_id}")
    suspend fun getPoll(
        @Path("poll_id") pollId: String,
    ): io.getstream.chat.android.network.models.PollResponse

    /**
     * Partial update poll
     * Updates a poll partially
     */
    @PATCH("/api/v2/polls/{poll_id}")
    suspend fun updatePollPartial(
        @Path("poll_id") pollId: String,
        @Body updatePollPartialRequest: io.getstream.chat.android.network.models.UpdatePollPartialRequest,
    ): io.getstream.chat.android.network.models.PollResponse

    /**
     * Create poll option
     * Creates a poll option
     */
    @POST("/api/v2/polls/{poll_id}/options")
    suspend fun createPollOption(
        @Path("poll_id") pollId: String,
        @Body createPollOptionRequest: io.getstream.chat.android.network.models.CreatePollOptionRequest,
    ): io.getstream.chat.android.network.models.PollOptionResponse

    /**
     * Update poll option
     * Updates a poll option
     */
    @PUT("/api/v2/polls/{poll_id}/options")
    suspend fun updatePollOption(
        @Path("poll_id") pollId: String,
        @Body updatePollOptionRequest: io.getstream.chat.android.network.models.UpdatePollOptionRequest,
    ): io.getstream.chat.android.network.models.PollOptionResponse

    /**
     * Delete poll option
     * Deletes a poll option
     */
    @DELETE("/api/v2/polls/{poll_id}/options/{option_id}")
    suspend fun deletePollOption(
        @Path("poll_id") pollId: String,
        @Path("option_id") optionId: String,
    ): io.getstream.chat.android.network.models.Response

    /**
     * Query votes
     * Queries votes
     */
    @POST("/api/v2/polls/{poll_id}/votes")
    suspend fun queryPollVotes(
        @Path("poll_id") pollId: String,
        @Body queryPollVotesRequest: io.getstream.chat.android.network.models.QueryPollVotesRequest,
    ): io.getstream.chat.android.network.models.PollVotesResponse

    /**
     * Push notification preferences
     * Upserts the push preferences for a user and or channel member. Set to all, mentions or none
     */
    @POST("/api/v2/push_preferences")
    suspend fun updatePushNotificationPreferences(
        @Body upsertPushPreferencesRequest: io.getstream.chat.android.network.models.UpsertPushPreferencesRequest,
    ): io.getstream.chat.android.network.models.UpsertPushPreferencesResponse

    /**
     * Search roles
     * Searches mentionable roles (user-assignable + channel-assignable, built-in and custom) by name prefix for autocomplete
     */
    @GET("/api/v2/roles/search")
    suspend fun searchRoles(
        @Query("query") query: String,
        @Query("limit") limit: Int? = null,
        @Query("name_gt") nameGt: String? = null,
        @Query("role_type") roleType: String? = null,
        @Query("include_global_roles") includeGlobalRoles: Boolean? = null,
    ): io.getstream.chat.android.network.models.SearchRolesResponse

    /**
     * List user groups
     * Lists user groups with cursor-based pagination
     */
    @GET("/api/v2/usergroups")
    suspend fun listUserGroups(
        @Query("limit") limit: Int? = null,
        @Query("id_gt") idGt: String? = null,
        @Query("created_at_gt") createdAtGt: String? = null,
        @Query("team_id") teamId: String? = null,
    ): io.getstream.chat.android.network.models.ListUserGroupsResponse

    /**
     * Create user group
     * Creates a new user group, optionally with initial members
     */
    @POST("/api/v2/usergroups")
    suspend fun createUserGroup(
        @Body createUserGroupRequest: io.getstream.chat.android.network.models.CreateUserGroupRequest,
    ): io.getstream.chat.android.network.models.CreateUserGroupResponse

    /**
     * Search user groups
     * Searches user groups by name prefix for autocomplete
     */
    @GET("/api/v2/usergroups/search")
    suspend fun searchUserGroups(
        @Query("query") query: String,
        @Query("limit") limit: Int? = null,
        @Query("name_gt") nameGt: String? = null,
        @Query("id_gt") idGt: String? = null,
        @Query("team_id") teamId: String? = null,
    ): io.getstream.chat.android.network.models.SearchUserGroupsResponse

    /**
     * Delete user group
     * Deletes a user group and all its members
     */
    @DELETE("/api/v2/usergroups/{id}")
    suspend fun deleteUserGroup(
        @Path("id") id: String,
        @Query("team_id") teamId: String? = null,
    ): io.getstream.chat.android.network.models.Response

    /**
     * Get user group
     * Gets a user group by ID, including its members
     */
    @GET("/api/v2/usergroups/{id}")
    suspend fun getUserGroup(
        @Path("id") id: String,
        @Query("team_id") teamId: String? = null,
    ): io.getstream.chat.android.network.models.GetUserGroupResponse

    /**
     * Update user group
     * Updates a user group's name and/or description. team_id is immutable.
     */
    @PUT("/api/v2/usergroups/{id}")
    suspend fun updateUserGroup(
        @Path("id") id: String,
        @Body updateUserGroupRequest: io.getstream.chat.android.network.models.UpdateUserGroupRequest,
    ): io.getstream.chat.android.network.models.UpdateUserGroupResponse

    /**
     * Add user group members
     * Adds members to a user group. All user IDs must exist. The operation is all-or-nothing.
     */
    @POST("/api/v2/usergroups/{id}/members")
    suspend fun addUserGroupMembers(
        @Path("id") id: String,
        @Body addUserGroupMembersRequest: io.getstream.chat.android.network.models.AddUserGroupMembersRequest,
    ): io.getstream.chat.android.network.models.AddUserGroupMembersResponse

    /**
     * Remove user group members
     * Removes members from a user group. Users already not in the group are silently ignored.
     */
    @POST("/api/v2/usergroups/{id}/members/delete")
    suspend fun removeUserGroupMembers(
        @Path("id") id: String,
        @Body removeUserGroupMembersRequest: io.getstream.chat.android.network.models.RemoveUserGroupMembersRequest,
    ): io.getstream.chat.android.network.models.RemoveUserGroupMembersResponse
}
