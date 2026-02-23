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

@file:Suppress("MatchingDeclarationName")

package io.getstream.chat.android.compose.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Use [StreamDesign.Colors] instead.
 */
@Deprecated("Use StreamDesign.Colors", ReplaceWith("StreamDesign.Colors"))
public typealias StreamColors = StreamDesign.Colors

@Suppress("MagicNumber")
internal object StreamPrimitiveColors {
    val baseBlack = Color(0xFF000000)
    val baseTransparent = Color(0x00000000)
    val baseWhite = Color(0xFFFFFFFF)
    val blue50 = Color(0xFFF3F7FF)
    val blue100 = Color(0xFFD2E3FF)
    val blue150 = Color(0xFFC3D9FF)
    val blue200 = Color(0xFFA6C4FF)
    val blue300 = Color(0xFF7AA7FF)
    val blue400 = Color(0xFF4E8BFF)
    val blue500 = Color(0xFF005FFF)
    val blue600 = Color(0xFF0052CE)
    val blue700 = Color(0xFF0042A3)
    val blue800 = Color(0xFF003179)
    val blue900 = Color(0xFF091A3B)
    val cyan100 = Color(0xFFD7F7FB)
    val cyan800 = Color(0xFF1C8791)
    val green100 = Color(0xFFC9FCE7)
    val green400 = Color(0xFF59E9B5)
    val green500 = Color(0xFF00E2A1)
    val green800 = Color(0xFF006548)
    val neutral50 = Color(0xFFF7F7F7)
    val neutral100 = Color(0xFFEFEFEF)
    val neutral300 = Color(0xFFABABAB)
    val neutral400 = Color(0xFF8F8F8F)
    val neutral500 = Color(0xFF7F7F7F)
    val neutral600 = Color(0xFF565656)
    val neutral700 = Color(0xFF4A4A4A)
    val neutral800 = Color(0xFF323232)
    val neutral900 = Color(0xFF1C1C1C)
    val purple100 = Color(0xFFEBDEFD)
    val purple200 = Color(0xFFD8BFFC)
    val purple800 = Color(0xFF6640AB)
    val red400 = Color(0xFFE6756C)
    val red500 = Color(0xFFD92F26)
    val slate50 = Color(0xFFF6F8FA)
    val slate100 = Color(0xFFF2F4F6)
    val slate150 = Color(0xFFD5DBE1)
    val slate200 = Color(0xFFE2E6EA)
    val slate300 = Color(0xFFA3ACBA)
    val slate400 = Color(0xFFB8BEC4)
    val slate500 = Color(0xFF687385)
    val slate600 = Color(0xFF838990)
    val slate700 = Color(0xFF4A4A4A)
    val slate800 = Color(0xFF50565D)
    val slate900 = Color(0xFF1E252B)
    val yellow100 = Color(0xFFFFF1C2)
    val yellow200 = Color(0xFFFFE8A0)
    val yellow800 = Color(0xFF9F7700)
    val highlightLight = Color(0xFFFBF4DD)
    val highlightDark = Color(0xFF302D22)
}
