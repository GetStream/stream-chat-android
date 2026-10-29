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

package io.getstream.chat.android.network.infrastructure

import io.getstream.chat.android.client.api2.mapping.DomainMapping
import io.getstream.chat.android.models.NoOpChannelTransformer
import io.getstream.chat.android.models.NoOpMessageTransformer
import io.getstream.chat.android.models.NoOpUserTransformer
import io.getstream.chat.android.network.models.AddUserGroupMembersRequest
import io.getstream.chat.android.network.models.CastPollVoteRequest
import io.getstream.chat.android.network.models.CreatePollOptionRequest
import io.getstream.chat.android.network.models.CreatePollRequest
import io.getstream.chat.android.network.models.CreateUserGroupRequest
import io.getstream.chat.android.network.models.GetUserGroupResponse
import io.getstream.chat.android.network.models.PollOptionInput
import io.getstream.chat.android.network.models.PollOptionRequest
import io.getstream.chat.android.network.models.PollOptionResponse
import io.getstream.chat.android.network.models.PollResponse
import io.getstream.chat.android.network.models.PollVoteResponse
import io.getstream.chat.android.network.models.PollVotesResponse
import io.getstream.chat.android.network.models.QueryPollsRequest
import io.getstream.chat.android.network.models.UpdatePollOptionRequest
import io.getstream.chat.android.network.models.UpdatePollPartialRequest
import io.getstream.chat.android.network.models.UpdatePollRequest
import io.getstream.chat.android.network.models.VoteData
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.Date

/**
 * The endpoints of the third v2 batch (polls, poll votes, user groups) parse their recorded v2 responses through the
 * generated [Serializer] alone. Like users, v2 reads poll and option custom data only when it is nested under
 * `custom`, so those requests keep it nested; the other request bodies are the same as on v1.
 */
internal class V2LaneBatchThreeTest {

    private val mapping = DomainMapping(
        currentUserIdProvider = { "filip" },
        channelTransformer = NoOpChannelTransformer,
        messageTransformer = NoOpMessageTransformer,
        userTransformer = NoOpUserTransformer,
        hidesStoredDeclaredCustom = true,
    )

    private inline fun <reified T> parse(json: String): T = Serializer.moshi.adapter(T::class.java).fromJson(json)!!

    private inline fun <reified T> v2Body(request: T): String = Serializer.moshi.adapter(T::class.java).toJson(request)

    @Test
    fun `A poll parses its nested custom data and integer dates from v2`() {
        val poll = with(mapping) { parse<PollResponse>(POLL).poll.toDomain() }

        assertEquals(mapOf("poll_probe" to "nested"), poll.extraData)
        assertEquals(mapOf("opt_probe" to "a"), poll.options.first().extraData)
        assertEquals(Date(1791791868353), poll.createdAt)
        assertEquals(Date(1634853490403), poll.createdBy?.createdAt)
    }

    @Test
    fun `A poll option parses its nested custom data from v2`() {
        val option = with(mapping) { parse<PollOptionResponse>(OPTION).pollOption.toDomain() }

        assertEquals(mapOf("opt_probe" to "c"), option.extraData)
    }

    @Test
    fun `A vote and an answer parse from v2`() {
        val votes = parse<PollVotesResponse>(VOTES).votes

        val vote = with(mapping) { votes.first().toDomain() }
        assertEquals("cdcadf29-2246-448b-b351-f0b975b0a049", vote.id)
        assertEquals(Date(1791791887627), vote.createdAt)
        assertEquals("v2cap answer", votes.last().answerText)
    }

    @Test
    fun `A cast vote parses from v2 without its poll`() {
        val vote = with(mapping) { parse<PollVoteResponse>(CAST_VOTE).vote!!.toDomain() }

        assertEquals("6223cd5f-4f01-435e-ad7b-c758976e43c5", vote.optionId)
    }

    @Test
    fun `A user group parses its integer dates from v2`() {
        val group = with(mapping) { parse<GetUserGroupResponse>(USER_GROUP).userGroup!!.toDomain() }

        assertEquals("v2cap-group", group.id)
        assertEquals(Date(1791791900176), group.createdAt)
        assertEquals(listOf("filip"), group.members.map { it.userId })
    }

    @Test
    fun `A poll creation nests poll and option custom data on v2, where the v1 body would lose it`() {
        val request = CreatePollRequest(
            name = "Poll",
            options = listOf(PollOptionInput(text = "A", custom = mapOf("o" to "1"))),
            custom = mapOf("p" to "2"),
        )

        assertEquals(
            """{"name":"Poll","options":[{"text":"A","custom":{"o":"1"}}],"custom":{"p":"2"}}""",
            v2Body(request),
        )
    }

    @Test
    fun `A poll update nests poll and option custom data on v2`() {
        val request = UpdatePollRequest(
            id = "poll",
            name = "Poll",
            options = listOf(PollOptionRequest(id = "a", text = "A", custom = mapOf("o" to "1"))),
            custom = mapOf("p" to "2"),
        )

        assertEquals(
            """{"id":"poll","name":"Poll","options":[{"id":"a","text":"A","custom":{"o":"1"}}],"custom":{"p":"2"}}""",
            v2Body(request),
        )
    }

    @Test
    fun `An option creation and update nest custom data on v2`() {
        assertEquals(
            """{"text":"C","custom":{"o":"1"}}""",
            v2Body(CreatePollOptionRequest(text = "C", custom = mapOf("o" to "1"))),
        )
        assertEquals(
            """{"id":"c","text":"C","custom":{"o":"1"}}""",
            v2Body(UpdatePollOptionRequest(id = "c", text = "C", custom = mapOf("o" to "1"))),
        )
    }

    @Test
    fun `A partial poll update keeps its v1 body`() {
        // Root keys of `set` are custom data on both versions.
        assertBody(
            """{"unset":["q"],"set":{"name":"Poll","p":"2"}}""",
            UpdatePollPartialRequest(set = mapOf("name" to "Poll", "p" to "2"), unset = listOf("q")),
        )
    }

    @Test
    fun `A vote and a polls query keep their v1 bodies`() {
        assertBody("""{"vote":{"option_id":"a"}}""", CastPollVoteRequest(VoteData(optionId = "a")))
        assertBody("""{"vote":{"answer_text":"answer"}}""", CastPollVoteRequest(VoteData(answerText = "answer")))
        assertBody(
            """{"limit":1,"sort":[],"filter":{"id":"poll"}}""",
            QueryPollsRequest(filter = mapOf("id" to "poll"), limit = 1),
        )
    }

    @Test
    fun `User group requests keep their v1 bodies`() {
        assertBody(
            """{"name":"Group","id":"group","member_ids":["filip"]}""",
            CreateUserGroupRequest(name = "Group", id = "group", memberIds = listOf("filip")),
        )
        assertBody("""{"member_ids":["jaewoong"]}""", AddUserGroupMembersRequest(memberIds = listOf("jaewoong")))
    }

    private inline fun <reified T : Any> assertBody(expected: String, request: T) {
        assertEquals(expected, v2Body(request))
    }

    private companion object {
        private const val USER = """{"id":"filip","name":"Chewbacca","custom":{},"language":"","role":"user",
            "teams":[],"created_at":1634853490403855000,"updated_at":1791791284528654000,"banned":false,
            "online":true,"blocked_user_ids":[]}"""

        private const val POLL = """{"poll":{"id":"5f67f3d7-92dc-45d1-a307-e4a0b6a17f12","name":"v2cap poll",
            "description":"","voting_visibility":"public","enforce_unique_vote":false,"max_votes_allowed":null,
            "allow_user_suggested_options":false,"allow_answers":false,"vote_count":0,
            "options":[{"id":"26594437-ddba-4a32-bca1-6b4993f3a59b","text":"A","custom":{"opt_probe":"a"}},
            {"id":"32f1620a-2df8-4bbb-bf39-fddc83916fe8","text":"B","custom":{}}],"answers_count":0,
            "vote_counts_by_option":{},"latest_votes_by_option":{},"latest_answers":[],"own_votes":[],
            "created_by_id":"filip","created_by":$USER,"created_at":1791791868353689000,
            "updated_at":1791791868353689000,"custom":{"poll_probe":"nested"}},"duration":"5.80ms"}"""

        private const val OPTION = """{"poll_option":{"id":"0d804a6e-99a5-4ae3-aa9a-3ea63948da14","text":"C",
            "custom":{"opt_probe":"c"}},"duration":"13.63ms"}"""

        private const val VOTE = """{"poll_id":"43b51501-06ad-4edb-b14d-1892dca0e0e1",
            "id":"cdcadf29-2246-448b-b351-f0b975b0a049","option_id":"6223cd5f-4f01-435e-ad7b-c758976e43c5",
            "user_id":"filip","user":$USER,"created_at":1791791887627614000,"updated_at":1791791887627614000}"""

        private const val ANSWER = """{"poll_id":"43b51501-06ad-4edb-b14d-1892dca0e0e1",
            "id":"966e23c3-4b79-469c-922c-93335598e7bf","option_id":"","is_answer":true,
            "answer_text":"v2cap answer","user_id":"filip","user":$USER,"created_at":1791791887935330000,
            "updated_at":1791791887935330000}"""

        private const val VOTES = """{"votes":[$VOTE,$ANSWER],"duration":"16.21ms"}"""

        /** The recorded response also carries the whole poll, left out here. */
        private const val CAST_VOTE = """{"vote":$VOTE,"duration":"36.88ms"}"""

        private const val USER_GROUP = """{"user_group":{"id":"v2cap-group","name":"v2cap group",
            "description":"capture","created_at":1791791900176676000,"updated_at":1791791900182360000,
            "created_by":"filip","members":[{"app_pk":102398,"group_id":"v2cap-group","user_id":"filip",
            "is_admin":false,"created_at":1791791900182360000}]},"duration":"3.71ms"}"""
    }
}
