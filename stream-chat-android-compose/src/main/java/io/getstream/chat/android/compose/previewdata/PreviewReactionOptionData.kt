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

package io.getstream.chat.android.compose.previewdata

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Face
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.ThumbUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.state.reactionoptions.ReactionOptionItemState
import io.getstream.chat.android.compose.ui.util.ReactionIcon

/**
 * Provides sample reaction option items that will be used to render component previews.
 */
internal object PreviewReactionOptionData {

    @Composable
    fun reactionOption1() = ReactionOptionItemState(
        painter = painterResource(R.drawable.stream_compose_ic_reaction_thumbs_up),
        type = "like",
        emojiCode = null,
    )

    @Composable
    fun reactionOption2() = ReactionOptionItemState(
        painter = painterResource(R.drawable.stream_compose_ic_reaction_love_selected),
        type = "love",
        emojiCode = null,
    )

    @Composable
    fun reactionOption3() = ReactionOptionItemState(
        painter = painterResource(R.drawable.stream_compose_ic_reaction_wut),
        type = "wow",
        emojiCode = null,
    )

    @Composable
    fun reactionOption4() = ReactionOptionItemState(
        painter = painterResource(R.drawable.stream_compose_ic_reaction_thumbs_down_selected),
        type = "sad",
        emojiCode = null,
    )

    @Composable
    fun oneReaction(): List<ReactionOptionItemState> = listOf(
        reactionOption1(),
    )

    @Composable
    fun manyReactions(): List<ReactionOptionItemState> = listOf(
        reactionOption1(),
        reactionOption2(),
        reactionOption3(),
        reactionOption4(),
    )

    @Suppress("MagicNumber")
    @Composable
    fun reactionPickerIcons(take: Int? = null): Map<String, ReactionIcon> {
        require(take == null || take > 0 || take < 15) { "take must be null or a positive number in [1,15]" }
        val icons = listOf(
            Icons.Rounded.ThumbUp,
            Icons.Rounded.Lock,
            Icons.Rounded.Favorite,
            Icons.Rounded.Add,
            Icons.Rounded.Check,
            Icons.Rounded.Build,
            Icons.Rounded.Clear,
            Icons.Rounded.Delete,
            Icons.Rounded.Edit,
            Icons.Rounded.Email,
            Icons.Rounded.Face,
            Icons.Rounded.LocationOn,
            Icons.Rounded.Star,
            Icons.Rounded.Home,
            Icons.Rounded.Info,
        )
        val generated = icons.mapIndexed { index, icon ->
            "reaction_$index" to ReactionIcon(
                painter = rememberTintedPainter(
                    imageVector = icon,
                    tintColor = colorResource(R.color.stream_compose_grey),
                ),
                selectedPainter = rememberTintedPainter(
                    imageVector = icon,
                    tintColor = colorResource(R.color.stream_compose_accent_blue),
                ),
            )
        }
        val counted = take?.let { generated.take(it) } ?: generated
        return counted.toMap()
    }

    @Composable
    private fun rememberTintedPainter(
        imageVector: ImageVector,
        tintColor: Color,
    ): Painter {
        // Create a base VectorPainter from the ImageVector
        val baseVectorPainter = rememberVectorPainter(image = imageVector)

        // Create and remember a custom Painter that applies a color filter
        return remember(baseVectorPainter, tintColor) {
            object : Painter() {
                override val intrinsicSize: Size
                    get() = baseVectorPainter.intrinsicSize

                override fun DrawScope.onDraw() {
                    with(baseVectorPainter) {
                        // Draw the vector using a tint color filter
                        draw(size = intrinsicSize, colorFilter = ColorFilter.tint(tintColor, BlendMode.SrcIn))
                    }
                }
            }
        }
    }
}
