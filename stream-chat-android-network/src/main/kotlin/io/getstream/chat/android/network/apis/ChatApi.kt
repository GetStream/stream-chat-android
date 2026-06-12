
/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-video-android/blob/main/LICENSE
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
    "UnusedImport"
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

interface ChatApi {

    /**
     * Get App Settings
     * This Method returns the application settings
     */
    @GET("/api/v2/app")
    suspend fun getApp(
    ): io.getstream.chat.android.network.models.GetApplicationResponse

    /**
     * List block lists
     * Returns all available block lists
     */
    @GET("/api/v2/blocklists")
    suspend fun listBlockLists(
        @Query("team") team: kotlin.String? = null
    ): io.getstream.chat.android.network.models.ListBlockListResponse

    /**
     * Create block list
     * Creates a new application blocklist, once created the blocklist can be used by any channel type
     */
    @POST("/api/v2/blocklists")
    suspend fun createBlockList(
        @Body createBlockListRequest: io.getstream.chat.android.network.models.CreateBlockListRequest
    ): io.getstream.chat.android.network.models.CreateBlockListResponse

    /**
     * Delete block list
     * Deletes previously created application blocklist
     */
    @DELETE("/api/v2/blocklists/{name}")
    suspend fun deleteBlockList(
        @Path("name") name: kotlin.String,
        @Query("team") team: kotlin.String? = null
    ): io.getstream.chat.android.network.models.Response

    /**
     * Update block list
     * Updates contents of the block list
     */
    @PUT("/api/v2/blocklists/{name}")
    suspend fun updateBlockList(
        @Path("name") name: kotlin.String ,
        @Body updateBlockListRequest: io.getstream.chat.android.network.models.UpdateBlockListRequest
    ): io.getstream.chat.android.network.models.UpdateBlockListResponse

    /**
     * Update block list
     * Updates contents of the block list
     */
    @PUT("/api/v2/blocklists/{name}")
    suspend fun updateBlockList(
        @Path("name") name: kotlin.String
    ): io.getstream.chat.android.network.models.UpdateBlockListResponse

    /**
     * Query channels
     * Query channels with filter query
     */
    @POST("/api/v2/chat/channels")
    suspend fun queryChannels(
        @Query("connection_id") connectionId: kotlin.String? = null ,
        @Body queryChannelsRequest: io.getstream.chat.android.network.models.QueryChannelsRequest
    ): io.getstream.chat.android.network.models.QueryChannelsResponse

    /**
     * Query channels
     * Query channels with filter query
     */
    @POST("/api/v2/chat/channels")
    suspend fun queryChannels(
        @Query("connection_id") connectionId: kotlin.String? = null
    ): io.getstream.chat.android.network.models.QueryChannelsResponse

    /**
     * Deletes channels asynchronously
     * Allows to delete several channels at once asynchronously
     */
    @POST("/api/v2/chat/channels/delete")
    suspend fun deleteChannels(
        @Body deleteChannelsRequest: io.getstream.chat.android.network.models.DeleteChannelsRequest
    ): io.getstream.chat.android.network.models.DeleteChannelsResponse

    /**
     * Mark channel message delivery status
     * Mark the status of a channel message delivered.
     */
    @POST("/api/v2/chat/channels/delivered")
    suspend fun markDelivered(
        @Body markDeliveredRequest: io.getstream.chat.android.network.models.MarkDeliveredRequest
    ): io.getstream.chat.android.network.models.MarkDeliveredResponse

    /**
     * Mark channel message delivery status
     * Mark the status of a channel message delivered.
     */
    @POST("/api/v2/chat/channels/delivered")
    suspend fun markDelivered(
    ): io.getstream.chat.android.network.models.MarkDeliveredResponse

    /**
     * Grouped query channels
     * Query channels grouped into predefined buckets. Only available for enterprise apps.
     */
    @POST("/api/v2/chat/channels/grouped")
    suspend fun groupedQueryChannels(
        @Query("connection_id") connectionId: kotlin.String? = null ,
        @Body groupedQueryChannelsRequest: io.getstream.chat.android.network.models.GroupedQueryChannelsRequest
    ): io.getstream.chat.android.network.models.GroupedQueryChannelsResponse

    /**
     * Grouped query channels
     * Query channels grouped into predefined buckets. Only available for enterprise apps.
     */
    @POST("/api/v2/chat/channels/grouped")
    suspend fun groupedQueryChannels(
        @Query("connection_id") connectionId: kotlin.String? = null
    ): io.getstream.chat.android.network.models.GroupedQueryChannelsResponse

    /**
     * Mark channels as read
     * Marks channels as read up to the specific message. If no channels is given, mark all channel as read
     */
    @POST("/api/v2/chat/channels/read")
    suspend fun markChannelsRead(
        @Body markChannelsReadRequest: io.getstream.chat.android.network.models.MarkChannelsReadRequest
    ): io.getstream.chat.android.network.models.MarkReadResponse

    /**
     * Mark channels as read
     * Marks channels as read up to the specific message. If no channels is given, mark all channel as read
     */
    @POST("/api/v2/chat/channels/read")
    suspend fun markChannelsRead(
    ): io.getstream.chat.android.network.models.MarkReadResponse

    /**
     * Get or create channel
     * This Method creates a channel or returns an existing one with matching attributes
     */
    @POST("/api/v2/chat/channels/{type}/query")
    suspend fun getOrCreateDistinctChannel(
        @Path("type") type: kotlin.String,
        @Query("connection_id") connectionId: kotlin.String? = null ,
        @Body channelGetOrCreateRequest: io.getstream.chat.android.network.models.ChannelGetOrCreateRequest
    ): io.getstream.chat.android.network.models.ChannelStateResponse

    /**
     * Get or create channel
     * This Method creates a channel or returns an existing one with matching attributes
     */
    @POST("/api/v2/chat/channels/{type}/query")
    suspend fun getOrCreateDistinctChannel(
        @Path("type") type: kotlin.String,
        @Query("connection_id") connectionId: kotlin.String? = null
    ): io.getstream.chat.android.network.models.ChannelStateResponse

    /**
     * Delete channel
     * Deletes channel
     */
    @DELETE("/api/v2/chat/channels/{type}/{id}")
    suspend fun deleteChannel(
        @Path("type") type: kotlin.String,
        @Path("id") id: kotlin.String,
        @Query("hard_delete") hardDelete: kotlin.Boolean? = null
    ): io.getstream.chat.android.network.models.DeleteChannelResponse

    /**
     * Partially update channel
     * Updates certain fields of the channel
     */
    @PATCH("/api/v2/chat/channels/{type}/{id}")
    suspend fun updateChannelPartial(
        @Path("type") type: kotlin.String,
        @Path("id") id: kotlin.String ,
        @Body updateChannelPartialRequest: io.getstream.chat.android.network.models.UpdateChannelPartialRequest
    ): io.getstream.chat.android.network.models.UpdateChannelPartialResponse

    /**
     * Partially update channel
     * Updates certain fields of the channel
     */
    @PATCH("/api/v2/chat/channels/{type}/{id}")
    suspend fun updateChannelPartial(
        @Path("type") type: kotlin.String,
        @Path("id") id: kotlin.String
    ): io.getstream.chat.android.network.models.UpdateChannelPartialResponse

    /**
     * Update channel
     * Change channel data
     */
    @POST("/api/v2/chat/channels/{type}/{id}")
    suspend fun updateChannel(
        @Path("type") type: kotlin.String,
        @Path("id") id: kotlin.String ,
        @Body updateChannelRequest: io.getstream.chat.android.network.models.UpdateChannelRequest
    ): io.getstream.chat.android.network.models.UpdateChannelResponse

    /**
     * Update channel
     * Change channel data
     */
    @POST("/api/v2/chat/channels/{type}/{id}")
    suspend fun updateChannel(
        @Path("type") type: kotlin.String,
        @Path("id") id: kotlin.String
    ): io.getstream.chat.android.network.models.UpdateChannelResponse

    /**
     * Delete draft
     * Deletes a draft
     */
    @DELETE("/api/v2/chat/channels/{type}/{id}/draft")
    suspend fun deleteDraft(
        @Path("type") type: kotlin.String,
        @Path("id") id: kotlin.String,
        @Query("parent_id") parentId: kotlin.String? = null
    ): io.getstream.chat.android.network.models.Response

    /**
     * Get draft
     * Get a draft
     */
    @GET("/api/v2/chat/channels/{type}/{id}/draft")
    suspend fun getDraft(
        @Path("type") type: kotlin.String,
        @Path("id") id: kotlin.String,
        @Query("parent_id") parentId: kotlin.String? = null
    ): io.getstream.chat.android.network.models.GetDraftResponse

    /**
     * Create a draft
     * Creates a draft
     */
    @POST("/api/v2/chat/channels/{type}/{id}/draft")
    suspend fun createDraft(
        @Path("type") type: kotlin.String,
        @Path("id") id: kotlin.String ,
        @Body createDraftRequest: io.getstream.chat.android.network.models.CreateDraftRequest
    ): io.getstream.chat.android.network.models.CreateDraftResponse

    /**
     * Send event
     * Sends event to the channel
     */
    @POST("/api/v2/chat/channels/{type}/{id}/event")
    suspend fun sendEvent(
        @Path("type") type: kotlin.String,
        @Path("id") id: kotlin.String ,
        @Body sendEventRequest: io.getstream.chat.android.network.models.SendEventRequest
    ): io.getstream.chat.android.network.models.EventResponse

    /**
     * Delete file
     * Deletes previously uploaded file
     */
    @DELETE("/api/v2/chat/channels/{type}/{id}/file")
    suspend fun deleteChannelFile(
        @Path("type") type: kotlin.String,
        @Path("id") id: kotlin.String,
        @Query("url") url: kotlin.String? = null
    ): io.getstream.chat.android.network.models.Response

    /**
     * Upload file
     * Uploads file
     */
    @POST("/api/v2/chat/channels/{type}/{id}/file")
    suspend fun uploadChannelFile(
        @Path("type") type: kotlin.String,
        @Path("id") id: kotlin.String ,
        @Body uploadChannelFileRequest: io.getstream.chat.android.network.models.UploadChannelFileRequest
    ): io.getstream.chat.android.network.models.UploadChannelFileResponse

    /**
     * Upload file
     * Uploads file
     */
    @POST("/api/v2/chat/channels/{type}/{id}/file")
    suspend fun uploadChannelFile(
        @Path("type") type: kotlin.String,
        @Path("id") id: kotlin.String
    ): io.getstream.chat.android.network.models.UploadChannelFileResponse

    /**
     * Hide channel
     * Marks channel as hidden for current user
     */
    @POST("/api/v2/chat/channels/{type}/{id}/hide")
    suspend fun hideChannel(
        @Path("type") type: kotlin.String,
        @Path("id") id: kotlin.String ,
        @Body hideChannelRequest: io.getstream.chat.android.network.models.HideChannelRequest
    ): io.getstream.chat.android.network.models.HideChannelResponse

    /**
     * Hide channel
     * Marks channel as hidden for current user
     */
    @POST("/api/v2/chat/channels/{type}/{id}/hide")
    suspend fun hideChannel(
        @Path("type") type: kotlin.String,
        @Path("id") id: kotlin.String
    ): io.getstream.chat.android.network.models.HideChannelResponse

    /**
     * Delete image
     * Deletes previously uploaded image
     */
    @DELETE("/api/v2/chat/channels/{type}/{id}/image")
    suspend fun deleteChannelImage(
        @Path("type") type: kotlin.String,
        @Path("id") id: kotlin.String,
        @Query("url") url: kotlin.String? = null
    ): io.getstream.chat.android.network.models.Response

    /**
     * Upload image
     * Uploads image
     */
    @POST("/api/v2/chat/channels/{type}/{id}/image")
    suspend fun uploadChannelImage(
        @Path("type") type: kotlin.String,
        @Path("id") id: kotlin.String ,
        @Body uploadChannelRequest: io.getstream.chat.android.network.models.UploadChannelRequest
    ): io.getstream.chat.android.network.models.UploadChannelResponse

    /**
     * Upload image
     * Uploads image
     */
    @POST("/api/v2/chat/channels/{type}/{id}/image")
    suspend fun uploadChannelImage(
        @Path("type") type: kotlin.String,
        @Path("id") id: kotlin.String
    ): io.getstream.chat.android.network.models.UploadChannelResponse

    /**
     * Partially channel member update
     *
     */
    @PATCH("/api/v2/chat/channels/{type}/{id}/member")
    suspend fun updateMemberPartial(
        @Path("type") type: kotlin.String,
        @Path("id") id: kotlin.String ,
        @Body updateMemberPartialRequest: io.getstream.chat.android.network.models.UpdateMemberPartialRequest
    ): io.getstream.chat.android.network.models.UpdateMemberPartialResponse

    /**
     * Partially channel member update
     *
     */
    @PATCH("/api/v2/chat/channels/{type}/{id}/member")
    suspend fun updateMemberPartial(
        @Path("type") type: kotlin.String,
        @Path("id") id: kotlin.String
    ): io.getstream.chat.android.network.models.UpdateMemberPartialResponse

    /**
     * Send new message
     * Sends new message to the specified channel
     */
    @POST("/api/v2/chat/channels/{type}/{id}/message")
    suspend fun sendMessage(
        @Path("type") type: kotlin.String,
        @Path("id") id: kotlin.String ,
        @Body sendMessageRequest: io.getstream.chat.android.network.models.SendMessageRequest
    ): io.getstream.chat.android.network.models.SendMessageResponse

    /**
     * Get many messages
     * Returns list messages found by IDs
     */
    @GET("/api/v2/chat/channels/{type}/{id}/messages")
    suspend fun getManyMessages(
        @Path("type") type: kotlin.String,
        @Path("id") id: kotlin.String,
        @Query("ids") ids: kotlin.collections.List<kotlin.String>
    ): io.getstream.chat.android.network.models.GetManyMessagesResponse

    /**
     * Get or create channel
     * This Method creates a channel or returns an existing one with matching attributes
     */
    @POST("/api/v2/chat/channels/{type}/{id}/query")
    suspend fun getOrCreateChannel(
        @Path("type") type: kotlin.String,
        @Path("id") id: kotlin.String,
        @Query("connection_id") connectionId: kotlin.String? = null ,
        @Body channelGetOrCreateRequest: io.getstream.chat.android.network.models.ChannelGetOrCreateRequest
    ): io.getstream.chat.android.network.models.ChannelStateResponse

    /**
     * Get or create channel
     * This Method creates a channel or returns an existing one with matching attributes
     */
    @POST("/api/v2/chat/channels/{type}/{id}/query")
    suspend fun getOrCreateChannel(
        @Path("type") type: kotlin.String,
        @Path("id") id: kotlin.String,
        @Query("connection_id") connectionId: kotlin.String? = null
    ): io.getstream.chat.android.network.models.ChannelStateResponse

    /**
     * Mark read
     * Marks channel as read up to the specific message
     */
    @POST("/api/v2/chat/channels/{type}/{id}/read")
    suspend fun markRead(
        @Path("type") type: kotlin.String,
        @Path("id") id: kotlin.String ,
        @Body markReadRequest: io.getstream.chat.android.network.models.MarkReadRequest
    ): io.getstream.chat.android.network.models.MarkReadResponse

    /**
     * Mark read
     * Marks channel as read up to the specific message
     */
    @POST("/api/v2/chat/channels/{type}/{id}/read")
    suspend fun markRead(
        @Path("type") type: kotlin.String,
        @Path("id") id: kotlin.String
    ): io.getstream.chat.android.network.models.MarkReadResponse

    /**
     * Show channel
     * Shows previously hidden channel
     */
    @POST("/api/v2/chat/channels/{type}/{id}/show")
    suspend fun showChannel(
        @Path("type") type: kotlin.String,
        @Path("id") id: kotlin.String
    ): io.getstream.chat.android.network.models.ShowChannelResponse

    /**
     * Stop watching channel
     * Call this Method to stop receiving channel events
     */
    @POST("/api/v2/chat/channels/{type}/{id}/stop-watching")
    suspend fun stopWatchingChannel(
        @Path("type") type: kotlin.String,
        @Path("id") id: kotlin.String,
        @Query("connection_id") connectionId: kotlin.String? = null
    ): io.getstream.chat.android.network.models.Response

    /**
     * Truncate channel
     * Truncates messages from a channel. Can be applied to the entire channel or scoped to specific members.
     */
    @POST("/api/v2/chat/channels/{type}/{id}/truncate")
    suspend fun truncateChannel(
        @Path("type") type: kotlin.String,
        @Path("id") id: kotlin.String ,
        @Body truncateChannelRequest: io.getstream.chat.android.network.models.TruncateChannelRequest
    ): io.getstream.chat.android.network.models.TruncateChannelResponse

    /**
     * Truncate channel
     * Truncates messages from a channel. Can be applied to the entire channel or scoped to specific members.
     */
    @POST("/api/v2/chat/channels/{type}/{id}/truncate")
    suspend fun truncateChannel(
        @Path("type") type: kotlin.String,
        @Path("id") id: kotlin.String
    ): io.getstream.chat.android.network.models.TruncateChannelResponse

    /**
     * Mark unread
     * Marks channel as unread from a specific message
     */
    @POST("/api/v2/chat/channels/{type}/{id}/unread")
    suspend fun markUnread(
        @Path("type") type: kotlin.String,
        @Path("id") id: kotlin.String ,
        @Body markUnreadRequest: io.getstream.chat.android.network.models.MarkUnreadRequest
    ): io.getstream.chat.android.network.models.Response

    /**
     * Mark unread
     * Marks channel as unread from a specific message
     */
    @POST("/api/v2/chat/channels/{type}/{id}/unread")
    suspend fun markUnread(
        @Path("type") type: kotlin.String,
        @Path("id") id: kotlin.String
    ): io.getstream.chat.android.network.models.Response

    /**
     * Query draft messages
     * Queries draft messages for a user
     */
    @POST("/api/v2/chat/drafts/query")
    suspend fun queryDrafts(
        @Body queryDraftsRequest: io.getstream.chat.android.network.models.QueryDraftsRequest
    ): io.getstream.chat.android.network.models.QueryDraftsResponse

    /**
     * Query draft messages
     * Queries draft messages for a user
     */
    @POST("/api/v2/chat/drafts/query")
    suspend fun queryDrafts(
    ): io.getstream.chat.android.network.models.QueryDraftsResponse

    /**
     * Query members
     * Find and filter channel members
     */
    @GET("/api/v2/chat/members")
    suspend fun queryMembers(
        @Query("payload") payload: io.getstream.chat.android.network.models.QueryMembersPayload? = null
    ): io.getstream.chat.android.network.models.MembersResponse

    /**
     * Delete message
     * Deletes message
     */
    @DELETE("/api/v2/chat/messages/{id}")
    suspend fun deleteMessage(
        @Path("id") id: kotlin.String,
        @Query("hard") hard: kotlin.Boolean? = null,
        @Query("deleted_by") deletedBy: kotlin.String? = null,
        @Query("delete_for_me") deleteForMe: kotlin.Boolean? = null
    ): io.getstream.chat.android.network.models.DeleteMessageResponse

    /**
     * Get message
     * Returns message by ID
     */
    @GET("/api/v2/chat/messages/{id}")
    suspend fun getMessage(
        @Path("id") id: kotlin.String
    ): io.getstream.chat.android.network.models.GetMessageResponse

    /**
     * Update message
     * Updates message with new data
     */
    @POST("/api/v2/chat/messages/{id}")
    suspend fun updateMessage(
        @Path("id") id: kotlin.String ,
        @Body updateMessageRequest: io.getstream.chat.android.network.models.UpdateMessageRequest
    ): io.getstream.chat.android.network.models.UpdateMessageResponse

    /**
     * Partially message update
     * Updates certain fields of the message
     */
    @PUT("/api/v2/chat/messages/{id}")
    suspend fun updateMessagePartial(
        @Path("id") id: kotlin.String ,
        @Body updateMessagePartialRequest: io.getstream.chat.android.network.models.UpdateMessagePartialRequest
    ): io.getstream.chat.android.network.models.UpdateMessagePartialResponse

    /**
     * Partially message update
     * Updates certain fields of the message
     */
    @PUT("/api/v2/chat/messages/{id}")
    suspend fun updateMessagePartial(
        @Path("id") id: kotlin.String
    ): io.getstream.chat.android.network.models.UpdateMessagePartialResponse

    /**
     * Run message command action
     * Executes message command action with given parameters
     */
    @POST("/api/v2/chat/messages/{id}/action")
    suspend fun runMessageAction(
        @Path("id") id: kotlin.String ,
        @Body messageActionRequest: io.getstream.chat.android.network.models.MessageActionRequest
    ): io.getstream.chat.android.network.models.MessageActionResponse

    /**
     * Send reaction
     * Sends reaction to specified message
     */
    @POST("/api/v2/chat/messages/{id}/reaction")
    suspend fun sendReaction(
        @Path("id") id: kotlin.String ,
        @Body sendReactionRequest: io.getstream.chat.android.network.models.SendReactionRequest
    ): io.getstream.chat.android.network.models.SendReactionResponse

    /**
     * Delete reaction
     * Removes user reaction from the message
     */
    @DELETE("/api/v2/chat/messages/{id}/reaction/{type}")
    suspend fun deleteReaction(
        @Path("id") id: kotlin.String,
        @Path("type") type: kotlin.String,
        @Query("user_id") userId: kotlin.String? = null
    ): io.getstream.chat.android.network.models.DeleteReactionResponse

    /**
     * Get reactions
     * Returns list of reactions of specific message
     */
    @GET("/api/v2/chat/messages/{id}/reactions")
    suspend fun getReactions(
        @Path("id") id: kotlin.String,
        @Query("limit") limit: kotlin.Int? = null,
        @Query("offset") offset: kotlin.Int? = null
    ): io.getstream.chat.android.network.models.GetReactionsResponse

    /**
     *
     * Get reactions on a message
     */
    @POST("/api/v2/chat/messages/{id}/reactions")
    suspend fun queryReactions(
        @Path("id") id: kotlin.String ,
        @Body queryReactionsRequest: io.getstream.chat.android.network.models.QueryReactionsRequest
    ): io.getstream.chat.android.network.models.QueryReactionsResponse

    /**
     *
     * Get reactions on a message
     */
    @POST("/api/v2/chat/messages/{id}/reactions")
    suspend fun queryReactions(
        @Path("id") id: kotlin.String
    ): io.getstream.chat.android.network.models.QueryReactionsResponse

    /**
     * Translate message
     * Translates message to a given language using automated translation software
     */
    @POST("/api/v2/chat/messages/{id}/translate")
    suspend fun translateMessage(
        @Path("id") id: kotlin.String ,
        @Body translateMessageRequest: io.getstream.chat.android.network.models.TranslateMessageRequest
    ): io.getstream.chat.android.network.models.MessageActionResponse

    /**
     * Cast vote
     * Cast a vote on a poll
     */
    @POST("/api/v2/chat/messages/{message_id}/polls/{poll_id}/vote")
    suspend fun castPollVote(
        @Path("message_id") messageId: kotlin.String,
        @Path("poll_id") pollId: kotlin.String ,
        @Body castPollVoteRequest: io.getstream.chat.android.network.models.CastPollVoteRequest
    ): io.getstream.chat.android.network.models.PollVoteResponse

    /**
     * Cast vote
     * Cast a vote on a poll
     */
    @POST("/api/v2/chat/messages/{message_id}/polls/{poll_id}/vote")
    suspend fun castPollVote(
        @Path("message_id") messageId: kotlin.String,
        @Path("poll_id") pollId: kotlin.String
    ): io.getstream.chat.android.network.models.PollVoteResponse

    /**
     * Delete vote
     * Delete a vote from a poll
     */
    @DELETE("/api/v2/chat/messages/{message_id}/polls/{poll_id}/vote/{vote_id}")
    suspend fun deletePollVote(
        @Path("message_id") messageId: kotlin.String,
        @Path("poll_id") pollId: kotlin.String,
        @Path("vote_id") voteId: kotlin.String,
        @Query("user_id") userId: kotlin.String? = null
    ): io.getstream.chat.android.network.models.PollVoteResponse

    /**
     * Delete reminder
     * Deletes a user's created reminder
     */
    @DELETE("/api/v2/chat/messages/{message_id}/reminders")
    suspend fun deleteReminder(
        @Path("message_id") messageId: kotlin.String
    ): io.getstream.chat.android.network.models.DeleteReminderResponse

    /**
     * Updates Reminder
     * Updates an existing reminder
     */
    @PATCH("/api/v2/chat/messages/{message_id}/reminders")
    suspend fun updateReminder(
        @Path("message_id") messageId: kotlin.String ,
        @Body updateReminderRequest: io.getstream.chat.android.network.models.UpdateReminderRequest
    ): io.getstream.chat.android.network.models.UpdateReminderResponse

    /**
     * Updates Reminder
     * Updates an existing reminder
     */
    @PATCH("/api/v2/chat/messages/{message_id}/reminders")
    suspend fun updateReminder(
        @Path("message_id") messageId: kotlin.String
    ): io.getstream.chat.android.network.models.UpdateReminderResponse

    /**
     * Create reminder
     * Creates a new reminder
     */
    @POST("/api/v2/chat/messages/{message_id}/reminders")
    suspend fun createReminder(
        @Path("message_id") messageId: kotlin.String ,
        @Body createReminderRequest: io.getstream.chat.android.network.models.CreateReminderRequest
    ): io.getstream.chat.android.network.models.ReminderResponseData

    /**
     * Create reminder
     * Creates a new reminder
     */
    @POST("/api/v2/chat/messages/{message_id}/reminders")
    suspend fun createReminder(
        @Path("message_id") messageId: kotlin.String
    ): io.getstream.chat.android.network.models.ReminderResponseData

    /**
     * Get replies
     * Returns replies (thread) of the message
     */
    @GET("/api/v2/chat/messages/{parent_id}/replies")
    suspend fun getReplies(
        @Path("parent_id") parentId: kotlin.String,
        @Query("limit") limit: kotlin.Int? = null,
        @Query("id_gte") idGte: kotlin.String? = null,
        @Query("id_gt") idGt: kotlin.String? = null,
        @Query("id_lte") idLte: kotlin.String? = null,
        @Query("id_lt") idLt: kotlin.String? = null,
        @Query("id_around") idAround: kotlin.String? = null,
        @Query("sort") sort: kotlin.collections.List<io.getstream.chat.android.network.models.SortParamRequest>? = null
    ): io.getstream.chat.android.network.models.GetRepliesResponse

    /**
     * Query Message Flags
     * Find and filter message flags
     */
    @GET("/api/v2/chat/moderation/flags/message")
    suspend fun queryMessageFlags(
        @Query("payload") payload: io.getstream.chat.android.network.models.QueryMessageFlagsPayload? = null
    ): io.getstream.chat.android.network.models.QueryMessageFlagsResponse

    /**
     * Mute channel
     * Mutes channel for user
     */
    @POST("/api/v2/chat/moderation/mute/channel")
    suspend fun muteChannel(
        @Body muteChannelRequest: io.getstream.chat.android.network.models.MuteChannelRequest
    ): io.getstream.chat.android.network.models.MuteChannelResponse

    /**
     * Mute channel
     * Mutes channel for user
     */
    @POST("/api/v2/chat/moderation/mute/channel")
    suspend fun muteChannel(
    ): io.getstream.chat.android.network.models.MuteChannelResponse

    /**
     * Unmute channel
     * Unmutes channel for user
     */
    @POST("/api/v2/chat/moderation/unmute/channel")
    suspend fun unmuteChannel(
        @Body unmuteChannelRequest: io.getstream.chat.android.network.models.UnmuteChannelRequest
    ): io.getstream.chat.android.network.models.UnmuteResponse

    /**
     * Unmute channel
     * Unmutes channel for user
     */
    @POST("/api/v2/chat/moderation/unmute/channel")
    suspend fun unmuteChannel(
    ): io.getstream.chat.android.network.models.UnmuteResponse

    /**
     * Query Banned Users
     * Find and filter channel scoped or global user bans
     */
    @GET("/api/v2/chat/query_banned_users")
    suspend fun queryBannedUsers(
        @Query("payload") payload: io.getstream.chat.android.network.models.QueryBannedUsersPayload? = null
    ): io.getstream.chat.android.network.models.QueryBannedUsersResponse

    /**
     * Query Future Channel Bans
     * Find and filter future channel bans created by the authenticated user
     */
    @GET("/api/v2/chat/query_future_channel_bans")
    suspend fun queryFutureChannelBans(
        @Query("payload") payload: io.getstream.chat.android.network.models.QueryFutureChannelBansPayload? = null
    ): io.getstream.chat.android.network.models.QueryFutureChannelBansResponse

    /**
     * Query reminders
     * Queries reminders
     */
    @POST("/api/v2/chat/reminders/query")
    suspend fun queryReminders(
        @Body queryRemindersRequest: io.getstream.chat.android.network.models.QueryRemindersRequest
    ): io.getstream.chat.android.network.models.QueryRemindersResponse

    /**
     * Query reminders
     * Queries reminders
     */
    @POST("/api/v2/chat/reminders/query")
    suspend fun queryReminders(
    ): io.getstream.chat.android.network.models.QueryRemindersResponse

    /**
     * Search messages
     * Search messages across channels
     */
    @GET("/api/v2/chat/search")
    suspend fun search(
        @Query("payload") payload: io.getstream.chat.android.network.models.SearchPayload? = null
    ): io.getstream.chat.android.network.models.SearchResponse

    /**
     * Sync
     * Returns all events happened since client disconnect in specified channels
     */
    @POST("/api/v2/chat/sync")
    suspend fun sync(
        @Query("with_inaccessible_cids") withInaccessibleCids: kotlin.Boolean? = null,
        @Query("watch") watch: kotlin.Boolean? = null,
        @Query("connection_id") connectionId: kotlin.String? = null ,
        @Body syncRequest: io.getstream.chat.android.network.models.SyncRequest
    ): io.getstream.chat.android.network.models.SyncResponse

    /**
     * Query Threads
     * Returns the list of threads for specific user
     */
    @POST("/api/v2/chat/threads")
    suspend fun queryThreads(
        @Query("connection_id") connectionId: kotlin.String? = null ,
        @Body queryThreadsRequest: io.getstream.chat.android.network.models.QueryThreadsRequest
    ): io.getstream.chat.android.network.models.QueryThreadsResponse

    /**
     * Query Threads
     * Returns the list of threads for specific user
     */
    @POST("/api/v2/chat/threads")
    suspend fun queryThreads(
        @Query("connection_id") connectionId: kotlin.String? = null
    ): io.getstream.chat.android.network.models.QueryThreadsResponse

    /**
     * Get Thread
     * Return a specific thread
     */
    @GET("/api/v2/chat/threads/{message_id}")
    suspend fun getThread(
        @Path("message_id") messageId: kotlin.String,
        @Query("watch") watch: kotlin.Boolean? = null,
        @Query("connection_id") connectionId: kotlin.String? = null,
        @Query("reply_limit") replyLimit: kotlin.Int? = null,
        @Query("participant_limit") participantLimit: kotlin.Int? = null,
        @Query("member_limit") memberLimit: kotlin.Int? = null
    ): io.getstream.chat.android.network.models.GetThreadResponse

    /**
     * Partially update thread
     * Updates certain fields of the thread
     */
    @PATCH("/api/v2/chat/threads/{message_id}")
    suspend fun updateThreadPartial(
        @Path("message_id") messageId: kotlin.String ,
        @Body updateThreadPartialRequest: io.getstream.chat.android.network.models.UpdateThreadPartialRequest
    ): io.getstream.chat.android.network.models.UpdateThreadPartialResponse

    /**
     * Partially update thread
     * Updates certain fields of the thread
     */
    @PATCH("/api/v2/chat/threads/{message_id}")
    suspend fun updateThreadPartial(
        @Path("message_id") messageId: kotlin.String
    ): io.getstream.chat.android.network.models.UpdateThreadPartialResponse

    /**
     * Unread counts
     * Fetch unread counts for a single user
     */
    @GET("/api/v2/chat/unread")
    suspend fun unreadCounts(
    ): io.getstream.chat.android.network.models.WrappedUnreadCountsResponse

    /**
     * Delete device
     * Deletes one device
     */
    @DELETE("/api/v2/devices")
    suspend fun deleteDevice(
        @Query("id") id: kotlin.String
    ): io.getstream.chat.android.network.models.Response

    /**
     * List devices
     * Returns all available devices
     */
    @GET("/api/v2/devices")
    suspend fun listDevices(
    ): io.getstream.chat.android.network.models.ListDevicesResponse

    /**
     * Create device
     * Adds a new device to a user, if the same device already exists the call will have no effect
     */
    @POST("/api/v2/devices")
    suspend fun createDevice(
        @Body createDeviceRequest: io.getstream.chat.android.network.models.CreateDeviceRequest
    ): io.getstream.chat.android.network.models.Response

    /**
     * Create Guest
     *
     */
    @POST("/api/v2/guest")
    suspend fun createGuest(
        @Body createGuestRequest: io.getstream.chat.android.network.models.CreateGuestRequest
    ): io.getstream.chat.android.network.models.CreateGuestResponse

    /**
     * Long Poll (Transport)
     * WebSocket fallback transport endpoint
     */
    @GET("/api/v2/longpoll")
    suspend fun longPoll(
        @Query("connection_id") connectionId: kotlin.String? = null,
        @Query("json") json: io.getstream.chat.android.network.models.WSAuthMessage? = null
    )

    /**
     * Get moderation action configs
     * Returns moderation action configs grouped by entity type, sorted by order ascending. Supports fetching DB-configured actions, hardcoded defaults, or both.
     */
    @GET("/api/v2/moderation/action_config")
    suspend fun getActionConfig(
        @Query("queue_type") queueType: kotlin.String? = null,
        @Query("entity_type") entityType: kotlin.String? = null,
        @Query("exclude_defaults") excludeDefaults: kotlin.Boolean? = null,
        @Query("only_defaults") onlyDefaults: kotlin.Boolean? = null
    ): io.getstream.chat.android.network.models.GetActionConfigResponse

    /**
     * Create or update a moderation action config
     * Create a new moderation action config entry or update an existing one. Action configs control the action buttons displayed in the moderation dashboard for each entity type.
     */
    @POST("/api/v2/moderation/action_config")
    suspend fun upsertActionConfig(
        @Body upsertActionConfigRequest: io.getstream.chat.android.network.models.UpsertActionConfigRequest
    ): io.getstream.chat.android.network.models.UpsertActionConfigResponse

    /**
     * Bulk create or update moderation action configs
     * Create or update multiple moderation action config entries in a single request. Omit the ID field to create; provide an ID to update.
     */
    @POST("/api/v2/moderation/action_config/bulk")
    suspend fun bulkUpsertActionConfig(
        @Body bulkUpsertActionConfigRequest: io.getstream.chat.android.network.models.BulkUpsertActionConfigRequest
    ): io.getstream.chat.android.network.models.BulkUpsertActionConfigResponse

    /**
     * Bulk delete moderation action configs
     * Delete multiple moderation action config entries by UUID in a single request.
     */
    @POST("/api/v2/moderation/action_config/bulk_delete")
    suspend fun bulkDeleteActionConfig(
        @Body bulkDeleteActionConfigRequest: io.getstream.chat.android.network.models.BulkDeleteActionConfigRequest
    ): io.getstream.chat.android.network.models.BulkDeleteActionConfigResponse

    /**
     * Delete a moderation action config
     * Delete a specific moderation action config entry by its UUID.
     */
    @DELETE("/api/v2/moderation/action_config/{id}")
    suspend fun deleteActionConfig(
        @Path("id") id: kotlin.String
    ): io.getstream.chat.android.network.models.DeleteActionConfigResponse

    /**
     * Appeal against the moderation decision
     * Appeal against the moderation decision
     */
    @POST("/api/v2/moderation/appeal")
    suspend fun appeal(
        @Body appealRequest: io.getstream.chat.android.network.models.AppealRequest
    ): io.getstream.chat.android.network.models.AppealResponse

    /**
     * Get appeal item
     * Retrieve a specific appeal item by its ID
     */
    @GET("/api/v2/moderation/appeal/{id}")
    suspend fun getAppeal(
        @Path("id") id: kotlin.String
    ): io.getstream.chat.android.network.models.GetAppealResponse

    /**
     * Query Appeals
     * Query Appeals
     */
    @POST("/api/v2/moderation/appeals")
    suspend fun queryAppeals(
        @Body queryAppealsRequest: io.getstream.chat.android.network.models.QueryAppealsRequest
    ): io.getstream.chat.android.network.models.QueryAppealsResponse

    /**
     * Query Appeals
     * Query Appeals
     */
    @POST("/api/v2/moderation/appeals")
    suspend fun queryAppeals(
    ): io.getstream.chat.android.network.models.QueryAppealsResponse

    /**
     * Bulk action appeals
     * Process multiple appeals in a single request by applying the specified action to each. Supported actions: unban, restore, unblock, mark_reviewed, reject_appeal. Each appeal goes through the same path as a single submit_action call.
     */
    @POST("/api/v2/moderation/appeals/bulk_action")
    suspend fun bulkActionAppeals(
        @Body bulkActionAppealsRequest: io.getstream.chat.android.network.models.BulkActionAppealsRequest
    ): io.getstream.chat.android.network.models.BulkActionAppealsResponse

    /**
     * Ban
     * Ban a user from a channel or the entire app
     */
    @POST("/api/v2/moderation/ban")
    suspend fun ban(
        @Body banRequest: io.getstream.chat.android.network.models.BanRequest
    ): io.getstream.chat.android.network.models.BanResponse

    /**
     * Create or update moderation configuration
     * Create a new moderation configuration or update an existing one. Configure settings for content filtering, AI analysis, toxicity detection, and other moderation features.
     */
    @POST("/api/v2/moderation/config")
    suspend fun upsertConfig(
        @Body upsertConfigRequest: io.getstream.chat.android.network.models.UpsertConfigRequest
    ): io.getstream.chat.android.network.models.UpsertConfigResponse

    /**
     * Delete a moderation policy
     * Delete a specific moderation policy by its name
     */
    @DELETE("/api/v2/moderation/config/{key}")
    suspend fun deleteConfig(
        @Path("key") key: kotlin.String,
        @Query("team") team: kotlin.String? = null
    ): io.getstream.chat.android.network.models.DeleteModerationConfigResponse

    /**
     * Get moderation configuration
     * Retrieve a specific moderation configuration by its key and team. This configuration contains settings for various moderation features like toxicity detection, AI analysis, and filtering rules.
     */
    @GET("/api/v2/moderation/config/{key}")
    suspend fun getConfig(
        @Path("key") key: kotlin.String,
        @Query("team") team: kotlin.String? = null
    ): io.getstream.chat.android.network.models.GetConfigResponse

    /**
     * Query moderation configurations
     * Search and filter moderation configurations across your application. This endpoint is designed for building moderation dashboards and managing multiple configuration sets.
     */
    @POST("/api/v2/moderation/configs")
    suspend fun queryModerationConfigs(
        @Body queryModerationConfigsRequest: io.getstream.chat.android.network.models.QueryModerationConfigsRequest
    ): io.getstream.chat.android.network.models.QueryModerationConfigsResponse

    /**
     * Query moderation configurations
     * Search and filter moderation configurations across your application. This endpoint is designed for building moderation dashboards and managing multiple configuration sets.
     */
    @POST("/api/v2/moderation/configs")
    suspend fun queryModerationConfigs(
    ): io.getstream.chat.android.network.models.QueryModerationConfigsResponse

    /**
     * Flag content for moderation
     * Flag any type of content (messages, users, channels, activities) for moderation review. Supports custom content types and additional metadata for flagged content.
     */
    @POST("/api/v2/moderation/flag")
    suspend fun flag(
        @Body flagRequest: io.getstream.chat.android.network.models.FlagRequest
    ): io.getstream.chat.android.network.models.FlagResponse

    /**
     * Mute
     * Mute a user. Mutes are generally not visible to the user you mute, while block is something you notice.
     */
    @POST("/api/v2/moderation/mute")
    suspend fun mute(
        @Body muteRequest: io.getstream.chat.android.network.models.MuteRequest
    ): io.getstream.chat.android.network.models.MuteResponse

    /**
     * Query review queue items
     * Query review queue items allows you to filter the review queue items. This is used for building a moderation dashboard.
     */
    @POST("/api/v2/moderation/review_queue")
    suspend fun queryReviewQueue(
        @Body queryReviewQueueRequest: io.getstream.chat.android.network.models.QueryReviewQueueRequest
    ): io.getstream.chat.android.network.models.QueryReviewQueueResponse

    /**
     * Query review queue items
     * Query review queue items allows you to filter the review queue items. This is used for building a moderation dashboard.
     */
    @POST("/api/v2/moderation/review_queue")
    suspend fun queryReviewQueue(
    ): io.getstream.chat.android.network.models.QueryReviewQueueResponse

    /**
     * Submit moderation action
     * Take action on flagged content, such as marking content as safe, deleting content, banning users, or executing custom moderation actions. Supports various action types with configurable parameters.
     */
    @POST("/api/v2/moderation/submit_action")
    suspend fun submitAction(
        @Body submitActionRequest: io.getstream.chat.android.network.models.SubmitActionRequest
    ): io.getstream.chat.android.network.models.SubmitActionResponse

    /**
     * Get OG
     * Get an OpenGraph attachment for a link
     */
    @GET("/api/v2/og")
    suspend fun getOG(
        @Query("url") url: kotlin.String
    ): io.getstream.chat.android.network.models.GetOGResponse

    /**
     * Create poll
     * Creates a new poll
     */
    @POST("/api/v2/polls")
    suspend fun createPoll(
        @Body createPollRequest: io.getstream.chat.android.network.models.CreatePollRequest
    ): io.getstream.chat.android.network.models.PollResponse

    /**
     * Update poll
     * Updates a poll
     */
    @PUT("/api/v2/polls")
    suspend fun updatePoll(
        @Body updatePollRequest: io.getstream.chat.android.network.models.UpdatePollRequest
    ): io.getstream.chat.android.network.models.PollResponse

    /**
     * Query polls
     * Queries polls
     */
    @POST("/api/v2/polls/query")
    suspend fun queryPolls(
        @Query("user_id") userId: kotlin.String? = null ,
        @Body queryPollsRequest: io.getstream.chat.android.network.models.QueryPollsRequest
    ): io.getstream.chat.android.network.models.QueryPollsResponse

    /**
     * Query polls
     * Queries polls
     */
    @POST("/api/v2/polls/query")
    suspend fun queryPolls(
        @Query("user_id") userId: kotlin.String? = null
    ): io.getstream.chat.android.network.models.QueryPollsResponse

    /**
     * Delete poll
     * Deletes a poll
     */
    @DELETE("/api/v2/polls/{poll_id}")
    suspend fun deletePoll(
        @Path("poll_id") pollId: kotlin.String,
        @Query("user_id") userId: kotlin.String? = null
    ): io.getstream.chat.android.network.models.Response

    /**
     * Get poll
     * Retrieves a poll
     */
    @GET("/api/v2/polls/{poll_id}")
    suspend fun getPoll(
        @Path("poll_id") pollId: kotlin.String,
        @Query("user_id") userId: kotlin.String? = null
    ): io.getstream.chat.android.network.models.PollResponse

    /**
     * Partial update poll
     * Updates a poll partially
     */
    @PATCH("/api/v2/polls/{poll_id}")
    suspend fun updatePollPartial(
        @Path("poll_id") pollId: kotlin.String ,
        @Body updatePollPartialRequest: io.getstream.chat.android.network.models.UpdatePollPartialRequest
    ): io.getstream.chat.android.network.models.PollResponse

    /**
     * Partial update poll
     * Updates a poll partially
     */
    @PATCH("/api/v2/polls/{poll_id}")
    suspend fun updatePollPartial(
        @Path("poll_id") pollId: kotlin.String
    ): io.getstream.chat.android.network.models.PollResponse

    /**
     * Create poll option
     * Creates a poll option
     */
    @POST("/api/v2/polls/{poll_id}/options")
    suspend fun createPollOption(
        @Path("poll_id") pollId: kotlin.String ,
        @Body createPollOptionRequest: io.getstream.chat.android.network.models.CreatePollOptionRequest
    ): io.getstream.chat.android.network.models.PollOptionResponse

    /**
     * Update poll option
     * Updates a poll option
     */
    @PUT("/api/v2/polls/{poll_id}/options")
    suspend fun updatePollOption(
        @Path("poll_id") pollId: kotlin.String ,
        @Body updatePollOptionRequest: io.getstream.chat.android.network.models.UpdatePollOptionRequest
    ): io.getstream.chat.android.network.models.PollOptionResponse

    /**
     * Delete poll option
     * Deletes a poll option
     */
    @DELETE("/api/v2/polls/{poll_id}/options/{option_id}")
    suspend fun deletePollOption(
        @Path("poll_id") pollId: kotlin.String,
        @Path("option_id") optionId: kotlin.String,
        @Query("user_id") userId: kotlin.String? = null
    ): io.getstream.chat.android.network.models.Response

    /**
     * Get poll option
     * Retrieves a poll option
     */
    @GET("/api/v2/polls/{poll_id}/options/{option_id}")
    suspend fun getPollOption(
        @Path("poll_id") pollId: kotlin.String,
        @Path("option_id") optionId: kotlin.String,
        @Query("user_id") userId: kotlin.String? = null
    ): io.getstream.chat.android.network.models.PollOptionResponse

    /**
     * Query votes
     * Queries votes
     */
    @POST("/api/v2/polls/{poll_id}/votes")
    suspend fun queryPollVotes(
        @Path("poll_id") pollId: kotlin.String,
        @Query("user_id") userId: kotlin.String? = null ,
        @Body queryPollVotesRequest: io.getstream.chat.android.network.models.QueryPollVotesRequest
    ): io.getstream.chat.android.network.models.PollVotesResponse

    /**
     * Query votes
     * Queries votes
     */
    @POST("/api/v2/polls/{poll_id}/votes")
    suspend fun queryPollVotes(
        @Path("poll_id") pollId: kotlin.String,
        @Query("user_id") userId: kotlin.String? = null
    ): io.getstream.chat.android.network.models.PollVotesResponse

    /**
     * Push notification preferences
     * Upserts the push preferences for a user and or channel member. Set to all, mentions or none
     */
    @POST("/api/v2/push_preferences")
    suspend fun updatePushNotificationPreferences(
        @Body upsertPushPreferencesRequest: io.getstream.chat.android.network.models.UpsertPushPreferencesRequest
    ): io.getstream.chat.android.network.models.UpsertPushPreferencesResponse

    /**
     * Search roles
     * Searches mentionable roles (user-assignable + channel-assignable, built-in and custom) by name prefix for autocomplete
     */
    @GET("/api/v2/roles/search")
    suspend fun searchRoles(
        @Query("query") query: kotlin.String,
        @Query("limit") limit: kotlin.Int? = null,
        @Query("name_gt") nameGt: kotlin.String? = null,
        @Query("role_type") roleType: kotlin.String? = null,
        @Query("include_global_roles") includeGlobalRoles: kotlin.Boolean? = null
    ): io.getstream.chat.android.network.models.SearchRolesResponse

    /**
     * Delete file
     * Deletes previously uploaded file
     */
    @DELETE("/api/v2/uploads/file")
    suspend fun deleteFile(
        @Query("url") url: kotlin.String? = null
    ): io.getstream.chat.android.network.models.Response

    /**
     * Upload file
     * Uploads file
     */
    @POST("/api/v2/uploads/file")
    suspend fun uploadFile(
        @Body fileUploadRequest: io.getstream.chat.android.network.models.FileUploadRequest
    ): io.getstream.chat.android.network.models.FileUploadResponse

    /**
     * Upload file
     * Uploads file
     */
    @POST("/api/v2/uploads/file")
    suspend fun uploadFile(
    ): io.getstream.chat.android.network.models.FileUploadResponse

    /**
     * Delete image
     * Deletes previously uploaded image
     */
    @DELETE("/api/v2/uploads/image")
    suspend fun deleteImage(
        @Query("url") url: kotlin.String? = null
    ): io.getstream.chat.android.network.models.Response

    /**
     * Upload image
     * Uploads image
     */
    @POST("/api/v2/uploads/image")
    suspend fun uploadImage(
        @Body imageUploadRequest: io.getstream.chat.android.network.models.ImageUploadRequest
    ): io.getstream.chat.android.network.models.ImageUploadResponse

    /**
     * Upload image
     * Uploads image
     */
    @POST("/api/v2/uploads/image")
    suspend fun uploadImage(
    ): io.getstream.chat.android.network.models.ImageUploadResponse

    /**
     * List user groups
     * Lists user groups with cursor-based pagination
     */
    @GET("/api/v2/usergroups")
    suspend fun listUserGroups(
        @Query("limit") limit: kotlin.Int? = null,
        @Query("id_gt") idGt: kotlin.String? = null,
        @Query("created_at_gt") createdAtGt: kotlin.String? = null,
        @Query("team_id") teamId: kotlin.String? = null
    ): io.getstream.chat.android.network.models.ListUserGroupsResponse

    /**
     * Create user group
     * Creates a new user group, optionally with initial members
     */
    @POST("/api/v2/usergroups")
    suspend fun createUserGroup(
        @Body createUserGroupRequest: io.getstream.chat.android.network.models.CreateUserGroupRequest
    ): io.getstream.chat.android.network.models.CreateUserGroupResponse

    /**
     * Search user groups
     * Searches user groups by name prefix for autocomplete
     */
    @GET("/api/v2/usergroups/search")
    suspend fun searchUserGroups(
        @Query("query") query: kotlin.String,
        @Query("limit") limit: kotlin.Int? = null,
        @Query("name_gt") nameGt: kotlin.String? = null,
        @Query("id_gt") idGt: kotlin.String? = null,
        @Query("team_id") teamId: kotlin.String? = null
    ): io.getstream.chat.android.network.models.SearchUserGroupsResponse

    /**
     * Delete user group
     * Deletes a user group and all its members
     */
    @DELETE("/api/v2/usergroups/{id}")
    suspend fun deleteUserGroup(
        @Path("id") id: kotlin.String,
        @Query("team_id") teamId: kotlin.String? = null
    ): io.getstream.chat.android.network.models.Response

    /**
     * Get user group
     * Gets a user group by ID, including its members
     */
    @GET("/api/v2/usergroups/{id}")
    suspend fun getUserGroup(
        @Path("id") id: kotlin.String,
        @Query("team_id") teamId: kotlin.String? = null
    ): io.getstream.chat.android.network.models.GetUserGroupResponse

    /**
     * Update user group
     * Updates a user group's name and/or description. team_id is immutable.
     */
    @PUT("/api/v2/usergroups/{id}")
    suspend fun updateUserGroup(
        @Path("id") id: kotlin.String ,
        @Body updateUserGroupRequest: io.getstream.chat.android.network.models.UpdateUserGroupRequest
    ): io.getstream.chat.android.network.models.UpdateUserGroupResponse

    /**
     * Update user group
     * Updates a user group's name and/or description. team_id is immutable.
     */
    @PUT("/api/v2/usergroups/{id}")
    suspend fun updateUserGroup(
        @Path("id") id: kotlin.String
    ): io.getstream.chat.android.network.models.UpdateUserGroupResponse

    /**
     * Add user group members
     * Adds members to a user group. All user IDs must exist. The operation is all-or-nothing.
     */
    @POST("/api/v2/usergroups/{id}/members")
    suspend fun addUserGroupMembers(
        @Path("id") id: kotlin.String ,
        @Body addUserGroupMembersRequest: io.getstream.chat.android.network.models.AddUserGroupMembersRequest
    ): io.getstream.chat.android.network.models.AddUserGroupMembersResponse

    /**
     * Remove user group members
     * Removes members from a user group. Users already not in the group are silently ignored.
     */
    @POST("/api/v2/usergroups/{id}/members/delete")
    suspend fun removeUserGroupMembers(
        @Path("id") id: kotlin.String ,
        @Body removeUserGroupMembersRequest: io.getstream.chat.android.network.models.RemoveUserGroupMembersRequest
    ): io.getstream.chat.android.network.models.RemoveUserGroupMembersResponse

    /**
     * Query users
     * Find and filter users
     */
    @GET("/api/v2/users")
    suspend fun queryUsers(
        @Query("payload") payload: io.getstream.chat.android.network.models.QueryUsersPayload? = null
    ): io.getstream.chat.android.network.models.QueryUsersResponse

    /**
     * Partially update user
     * Updates certain fields of the user
     */
    @PATCH("/api/v2/users")
    suspend fun updateUsersPartial(
        @Body updateUsersPartialRequest: io.getstream.chat.android.network.models.UpdateUsersPartialRequest
    ): io.getstream.chat.android.network.models.UpdateUsersResponse

    /**
     * Upsert users
     * Update or create users in bulk
     */
    @POST("/api/v2/users")
    suspend fun updateUsers(
        @Body updateUsersRequest: io.getstream.chat.android.network.models.UpdateUsersRequest
    ): io.getstream.chat.android.network.models.UpdateUsersResponse

    /**
     * Get list of blocked Users
     * Get list of blocked Users
     */
    @GET("/api/v2/users/block")
    suspend fun getBlockedUsers(
    ): io.getstream.chat.android.network.models.GetBlockedUsersResponse

    /**
     * Block user
     * Block users
     */
    @POST("/api/v2/users/block")
    suspend fun blockUsers(
        @Body blockUsersRequest: io.getstream.chat.android.network.models.BlockUsersRequest
    ): io.getstream.chat.android.network.models.BlockUsersResponse

    /**
     * Get user live locations
     * Retrieves all active live locations for a user
     */
    @GET("/api/v2/users/live_locations")
    suspend fun getUserLiveLocations(
    ): io.getstream.chat.android.network.models.SharedLocationsResponse

    /**
     * Update live location
     * Updates an existing live location with new coordinates or expiration time
     */
    @PUT("/api/v2/users/live_locations")
    suspend fun updateLiveLocation(
        @Body updateLiveLocationRequest: io.getstream.chat.android.network.models.UpdateLiveLocationRequest
    ): io.getstream.chat.android.network.models.SharedLocationResponse

    /**
     * Unblock user
     * Unblock users
     */
    @POST("/api/v2/users/unblock")
    suspend fun unblockUsers(
        @Body unblockUsersRequest: io.getstream.chat.android.network.models.UnblockUsersRequest
    ): io.getstream.chat.android.network.models.UnblockUsersResponse

}
