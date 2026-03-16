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

package io.getstream.chat.android.compose.sample.data

import io.getstream.chat.android.models.User

/**
 * Contains hardcoded [UserCredentials] for the demo environment.
 */
object PredefinedUserCredentials {

    const val API_KEY: String = "njbrpptcusun"

    val availableUsers: List<UserCredentials> = listOf(
        UserCredentials(
            apiKey = API_KEY,
            user = User(
                id = "alex",
                name = "Alex Morgan",
                image = "https://images.unsplash.com/photo-1560250097-0b93528c311a?w=300&h=300&fit=crop&crop=top",
            ),
            token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoiYWxleCJ9.qmIxsWcttjF-_FBLi6D884VsWMGreUPFk0KikbaD5Fc",
        ),
        UserCredentials(
            apiKey = API_KEY,
            user = User(
                id = "sarah",
                name = "Sarah Chen",
                image = "https://images.unsplash.com/photo-1548142813-c348350df52b?w=200&h=200&fit=crop&crop=face",
            ),
            token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoic2FyYWgifQ.s5kypZxZi0s72y_byhoFaMLsX0s8ZAi2MWwSgd70Eng",
        ),
        UserCredentials(
            apiKey = API_KEY,
            user = User(
                id = "marco",
                name = "Marco Rivera",
                image = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=300&h=300&fit=crop&crop=top",
            ),
            token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoibWFyY28ifQ.prFn4OzHtQ4W9bafWT5Z5VzeTS5FXoM_bJD3Uqo7smA",
        ),
        UserCredentials(
            apiKey = API_KEY,
            user = User(
                id = "priya",
                name = "Priya Sharma",
                image = "https://images.unsplash.com/photo-1573497019940-1c28c88b4f3e?w=200&h=200&fit=crop&crop=face",
            ),
            token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoicHJpeWEifQ.V_VqW1Lp4tYW2H55KXJADwNP2R4vrVIfhJuhdU_guiA",
        ),
        UserCredentials(
            apiKey = API_KEY,
            user = User(
                id = "james",
                name = "James Wilson",
                image = "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=200&h=200&fit=crop&crop=face",
            ),
            token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoiamFtZXMifQ.mw9E9mmaaVYASNeqKIOmg_Xm7pEJ0rMoW-zHERnEfgA",
        ),
        UserCredentials(
            apiKey = API_KEY,
            user = User(
                id = "aisha",
                name = "Aisha Patel",
                image = "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=300&h=300&fit=crop&crop=top",
            ),
            token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoiYWlzaGEifQ.50KdH3YYzigYIUU6Wf9xPCHsKr1dqFbExUNUH5WDs54",
        ),
        UserCredentials(
            apiKey = API_KEY,
            user = User(
                id = "lucas",
                name = "Lucas Berg",
                image = "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=300&h=300&fit=crop&crop=top",
            ),
            token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoibHVjYXMifQ.yvjBALk0QLHMDqAjIPoagATGvK5Hdj67H0rtJejmPYc",
        ),
        UserCredentials(
            apiKey = API_KEY,
            user = User(
                id = "maya",
                name = "Maya Johnson",
                image = "https://images.unsplash.com/photo-1531123897727-8f129e1688ce?w=300&h=300&fit=crop&crop=top",
            ),
            token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoibWF5YSJ9.nYt1j6WhTMFPjsrHwkdoM6Of5d_8RL5qUQVkQohRDGY",
        ),
        UserCredentials(
            apiKey = API_KEY,
            user = User(
                id = "omar",
                name = "Omar Hassan",
                image = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=300&h=300&fit=crop&crop=top",
            ),
            token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoib21hciJ9.Ny_CNHtB9AEtBgoUlgBPqO6AB4qBjFHhg1Cde65iwNU",
        ),
        UserCredentials(
            apiKey = API_KEY,
            user = User(
                id = "elena",
                name = "Elena Petrova",
                image = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=300&h=300&fit=crop&crop=top",
            ),
            token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoiZWxlbmEifQ.WKnfdr9sEHJ4teqg86LxaFD7ANFovWPKs0soLWf-5Is",
        ),
        UserCredentials(
            apiKey = API_KEY,
            user = User(
                id = "david",
                name = "David Kim",
                image = "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?w=300&h=300&fit=crop&crop=top",
            ),
            token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoiZGF2aWQifQ.A0JYTSnWCyvvcej61R8yevz1h1Vzhd4qYGbM2Dhqx3w",
        ),
        UserCredentials(
            apiKey = API_KEY,
            user = User(
                id = "nina",
                name = "Nina Tanaka",
                image = "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=200&h=200&fit=crop&crop=face",
            ),
            token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoibmluYSJ9.jPROdU2QCj6tD817jNHAbLbyiWh_x35yGbExLakp1qQ",
        ),
    )
}
