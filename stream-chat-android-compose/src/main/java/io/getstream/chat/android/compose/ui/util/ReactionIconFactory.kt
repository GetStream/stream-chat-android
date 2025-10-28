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

package io.getstream.chat.android.compose.ui.util

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.ui.common.helper.ReactionDefaults

/**
 * An interface that allows the creation of reaction icons for reaction types.
 */
public interface ReactionIconFactory {

    /**
     * Checks if the factory is capable of creating an icon for the given reaction type.
     *
     * @return If the given reaction type is supported by this factory.
     */
    public fun isReactionSupported(type: String): Boolean

    /**
     * Creates an instance of [ReactionIcon] for the given reaction type.
     *
     * @param type The reaction type used to create an icon.
     * @return [ReactionIcon] that contains [Painter]s for normal and selected icon states.
     */
    @Composable
    public fun createReactionIcon(type: String): ReactionIcon

    /**
     * Creates [ReactionIcon]s for all the supported reaction types.
     *
     * @return A map with [ReactionIcon]s for all the supported reaction types.
     */
    @Composable
    public fun createReactionIcons(): Map<String, ReactionIcon>

    public companion object {
        /**
         * Builds the default reaction icon factory that creates reaction icons from
         * drawable resources.
         *
         * @return The default implementation of [ReactionIconFactory].
         */
        public fun defaultFactory(): ReactionIconFactory = DefaultReactionIconFactory()
    }
}

/**
 * The default implementation of [ReactionIconFactory] that uses drawable resources
 * to create reaction icons.
 *
 * @param supportedReactions The map of supported reaction types with corresponding drawable resources.
 */
private class DefaultReactionIconFactory(
    private val supportedReactions: Map<String, ReactionDrawable> = mapOf(
        ReactionDefaults.THUMBS_UP to ReactionDrawable(
            iconResId = R.drawable.stream_compose_ic_reaction_thumbs_up,
            selectedIconResId = R.drawable.stream_compose_ic_reaction_thumbs_up_selected,
        ),
        ReactionDefaults.LOVE to ReactionDrawable(
            iconResId = R.drawable.stream_compose_ic_reaction_love,
            selectedIconResId = R.drawable.stream_compose_ic_reaction_love_selected,
        ),
        ReactionDefaults.LOL to ReactionDrawable(
            iconResId = R.drawable.stream_compose_ic_reaction_lol,
            selectedIconResId = R.drawable.stream_compose_ic_reaction_lol_selected,
        ),
        ReactionDefaults.WUT to ReactionDrawable(
            iconResId = R.drawable.stream_compose_ic_reaction_wut,
            selectedIconResId = R.drawable.stream_compose_ic_reaction_wut_selected,
        ),
        ReactionDefaults.THUMBS_DOWN to ReactionDrawable(
            iconResId = R.drawable.stream_compose_ic_reaction_thumbs_down,
            selectedIconResId = R.drawable.stream_compose_ic_reaction_thumbs_down_selected,
        ),
    ),
) : ReactionIconFactory {

    /**
     * Checks if the factory is capable of creating an icon for the given reaction type.
     *
     * @return If the given reaction type is supported by this factory.
     */
    override fun isReactionSupported(type: String): Boolean = supportedReactions.containsKey(type)

    /**
     * Creates an instance of [ReactionIcon] for the given reaction type.
     *
     * @param type The reaction type used to create an icon.
     * @return [ReactionIcon] that contains [Painter]s for normal and selected icon states.
     */
    @Composable
    override fun createReactionIcon(type: String): ReactionIcon {
        val reactionDrawable = requireNotNull(supportedReactions[type])
        return ReactionIcon(
            painter = painterResource(reactionDrawable.iconResId),
            selectedPainter = painterResource(reactionDrawable.selectedIconResId),
        )
    }

    /**
     * Creates [ReactionIcon]s for all the supported reaction types.
     *
     * @return A map with [ReactionIcon]s for all the supported reaction types.
     */
    @Composable
    override fun createReactionIcons(): Map<String, ReactionIcon> = supportedReactions.mapValues {
        createReactionIcon(it.key)
    }
}

/**
 * Contains drawable resources for normal and selected reaction states.
 *
 * @param iconResId The drawable resource id for the normal icon.
 * @param selectedIconResId The drawable resource id for the selected state icon.
 */
public data class ReactionDrawable(
    @DrawableRes public val iconResId: Int,
    @DrawableRes public val selectedIconResId: Int,
)

/**
 * Contains [Painter]s for normal and selected states of the reaction icon.
 *
 * @param painter The [Painter] used to render the reaction in its normal state.
 * @param selectedPainter The [Painter] used to render the reaction in its selected state.
 */
public data class ReactionIcon(
    val painter: Painter,
    val selectedPainter: Painter,
) {
    /**
     * Returns either one of the [Painter]s depending on the reaction state.
     *
     * @param isSelected If the reaction is selected.
     * @return Either normal or selected [Painter] depending on the reaction state.
     */
    public fun getPainter(isSelected: Boolean): Painter = if (isSelected) selectedPainter else painter
}
