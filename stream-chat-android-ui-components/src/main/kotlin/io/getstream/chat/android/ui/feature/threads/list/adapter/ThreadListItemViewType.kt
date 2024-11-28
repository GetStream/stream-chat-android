/*
 * Copyright (c) 2014-2024 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.ui.feature.threads.list.adapter

/**
 * Defines the possible types of a Thread List item.
 */
public object ThreadListItemViewType {

    /**
     * Represents an item of type 'thread'.
     */
    public const val ITEM_THREAD: Int = 0

    /**
     * Represent a loading more indicator item.
     */
    public const val ITEM_LOADING_MORE: Int = 1
}
