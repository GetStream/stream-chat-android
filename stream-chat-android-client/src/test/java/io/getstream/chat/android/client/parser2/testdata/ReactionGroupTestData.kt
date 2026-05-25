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

package io.getstream.chat.android.client.parser2.testdata

import io.getstream.chat.android.models.ReactionGroup
import org.intellij.lang.annotations.Language
import java.util.Date

internal object ReactionGroupTestData {

    @Language("JSON")
    val jsonAllFields =
        """{"count":5,"sum_scores":10,"first_reaction_at":"2020-01-01T00:00:00.000Z","last_reaction_at":"2020-01-02T00:00:00.000Z"}"""

    @Language("JSON")
    val jsonMissingCount =
        """{"sum_scores":10,"first_reaction_at":"2020-01-01T00:00:00.000Z","last_reaction_at":"2020-01-02T00:00:00.000Z"}"""

    @Language("JSON")
    val jsonMissingSumScores =
        """{"count":5,"first_reaction_at":"2020-01-01T00:00:00.000Z","last_reaction_at":"2020-01-02T00:00:00.000Z"}"""

    @Language("JSON")
    val jsonMissingFirstReactionAt =
        """{"count":5,"sum_scores":10,"last_reaction_at":"2020-01-02T00:00:00.000Z"}"""

    @Language("JSON")
    val jsonMissingLastReactionAt =
        """{"count":5,"sum_scores":10,"first_reaction_at":"2020-01-01T00:00:00.000Z"}"""

    val expectedReactionGroupAllFields = ReactionGroup(
        type = "like",
        count = 5,
        sumScore = 10,
        firstReactionAt = Date(1577836800000L), // 2020-01-01T00:00:00.000Z
        lastReactionAt = Date(1577923200000L), // 2020-01-02T00:00:00.000Z
    )
}
