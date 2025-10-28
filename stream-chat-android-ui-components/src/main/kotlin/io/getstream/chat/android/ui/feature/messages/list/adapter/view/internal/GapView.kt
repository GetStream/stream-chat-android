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

package io.getstream.chat.android.ui.feature.messages.list.adapter.view.internal

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Space
import androidx.core.view.isVisible
import io.getstream.chat.android.ui.utils.extensions.createStreamThemeWrapper
import io.getstream.chat.android.ui.utils.extensions.dpToPx

internal class GapView : LinearLayout {
    constructor(context: Context) : super(context.createStreamThemeWrapper())
    constructor(context: Context, attrs: AttributeSet?) : super(context.createStreamThemeWrapper(), attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context.createStreamThemeWrapper(),
        attrs,
        defStyleAttr,
    )

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(
        context.createStreamThemeWrapper(),
        attrs,
        defStyleAttr,
        defStyleRes,
    )

    private val smallGap: View
    private val bigGap: View

    init {
        smallGap = Space(context).apply {
            layoutParams = createLayoutParams(SMALL_GAP_HEIGHT_DP.dpToPx())
            isVisible = false
        }
        bigGap = Space(context).apply {
            layoutParams = createLayoutParams(BIG_GAP_HEIGHT_DP.dpToPx())
            isVisible = false
        }
        addView(smallGap)
        addView(bigGap)
    }

    fun showSmallGap() {
        smallGap.isVisible = true
        bigGap.isVisible = false
    }

    fun showBigGap() {
        smallGap.isVisible = false
        bigGap.isVisible = true
    }

    companion object {
        private const val SMALL_GAP_HEIGHT_DP = 2
        private const val BIG_GAP_HEIGHT_DP = 8
        private fun createLayoutParams(height: Int): ViewGroup.LayoutParams = LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height)
    }
}
