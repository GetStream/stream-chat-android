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

package io.getstream.chat.android.compose.util.extensions

import io.getstream.chat.android.compose.state.messages.MyOwn
import io.getstream.chat.android.compose.state.messages.NewMessageState
import io.getstream.chat.android.compose.state.messages.Other
import io.getstream.chat.android.common.state.messagelist.MyOwn as MyOwnCommon
import io.getstream.chat.android.common.state.messagelist.NewMessageState as NewMessageStateCommon
import io.getstream.chat.android.common.state.messagelist.Other as OtherCommon

/**
 * Converts [NewMessageStateCommon] to compose [NewMessageState].
 *
 * @return Composer [NewMessageState] derived from [NewMessageStateCommon].
 */
public fun NewMessageStateCommon.toComposeState(): NewMessageState {
    return when (this) {
        MyOwnCommon -> MyOwn
        OtherCommon -> Other
    }
}
