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

package io.getstream.chat.android.state.plugin.state.internal

/**
 * Marker object that identifies an active channel watch. Held as a field inside
 * [WatchedChannelStateFlow] so it lives as long as the caller holds the flow.
 * [io.getstream.chat.android.state.plugin.state.StateRegistry] stores
 * [java.lang.ref.WeakReference]s to these — when the flow is GC'd, the tracker
 * is GC'd and the weak reference goes null.
 */
internal data class WatchedChannelRecord(val cid: String)
