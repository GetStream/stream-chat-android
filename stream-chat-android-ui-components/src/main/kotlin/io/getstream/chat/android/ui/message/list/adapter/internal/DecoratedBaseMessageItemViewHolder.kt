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

package io.getstream.chat.android.ui.message.list.adapter.internal

import android.view.View
import androidx.annotation.CallSuper
import com.getstream.sdk.chat.adapter.MessageListItem
import io.getstream.chat.android.ui.message.list.adapter.BaseMessageItemViewHolder
import io.getstream.chat.android.ui.message.list.adapter.MessageListItemPayloadDiff
import io.getstream.chat.android.ui.message.list.adapter.viewholder.decorator.internal.Decorator

internal abstract class DecoratedBaseMessageItemViewHolder<T : MessageListItem>(
    itemView: View,
    private val decorators: List<Decorator>,
) : BaseMessageItemViewHolder<T>(itemView) {
    @CallSuper
    override fun bindData(data: T, diff: MessageListItemPayloadDiff?) {
        decorators.forEach { it.decorate(this, data) }
    }
}
