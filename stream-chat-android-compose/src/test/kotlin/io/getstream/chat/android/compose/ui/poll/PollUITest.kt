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

package io.getstream.chat.android.compose.ui.poll

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.BaseComposeTest
import io.getstream.chat.android.compose.ui.messages.attachments.factory.AttachmentsPickerPollTabFactory
import io.getstream.chat.android.compose.ui.messages.attachments.poll.PollCreationDiscardDialog
import io.getstream.chat.android.compose.ui.messages.attachments.poll.PollCreationHeader
import io.getstream.chat.android.compose.ui.messages.attachments.poll.PollOptionDuplicated
import io.getstream.chat.android.compose.ui.messages.attachments.poll.PollOptionItem
import io.getstream.chat.android.compose.ui.messages.attachments.poll.PollOptionList
import io.getstream.chat.android.compose.ui.messages.attachments.poll.PollOptionNumberExceed
import io.getstream.chat.android.compose.ui.messages.attachments.poll.PollSwitchInput
import io.getstream.chat.android.compose.ui.messages.attachments.poll.PollSwitchItem
import io.getstream.chat.android.compose.ui.messages.attachments.poll.PollSwitchList
import org.junit.Rule
import org.junit.Test

internal class PollUITest : BaseComposeTest() {

    @get:Rule
    val paparazzi = Paparazzi(deviceConfig = DeviceConfig.PIXEL_4A)

    override fun basePaparazzi(): Paparazzi = paparazzi

    @Test
    fun `snapshot PollCreationHeader composable`() {
        snapshotWithDarkMode {
            PollCreationHeader(
                modifier = Modifier.fillMaxWidth(),
                enabledCreation = true,
                onPollCreateClicked = { },
                onBackPressed = {},
            )
        }
    }

    @Test
    fun `snapshot PollOptionList composable`() {
        snapshotWithDarkMode {
            PollOptionList(
                lazyListState = rememberLazyListState(),
                optionItems = listOf(
                    PollOptionItem(title = "Option1"),
                    PollOptionItem(title = "Option2"),
                    PollOptionItem(title = "Option2", pollOptionError = PollOptionDuplicated("Duplicated optionø")),
                ),
                onQuestionsChanged = {},
            )
        }
    }

    @Test
    fun `snapshot Poll SwitchList composable`() {
        snapshotWithDarkMode {
            val switchItemList: MutableList<PollSwitchItem> = mutableListOf()

            switchItemList.addAll(
                listOf(
                    PollSwitchItem(
                        title = stringResource(id = R.string.stream_compose_poll_option_switch_multiple_answers),
                        pollSwitchInput = PollSwitchInput(
                            keyboardType = KeyboardType.Decimal,
                            maxValue = 10,
                            value = 11,
                        ),
                        pollOptionError = PollOptionNumberExceed("You can only put a number between 1~10"),
                        enabled = true,
                    ),
                    PollSwitchItem(
                        title = stringResource(id = R.string.stream_compose_poll_option_switch_multiple_answers),
                        pollSwitchInput = PollSwitchInput(
                            keyboardType = KeyboardType.Text,
                            value = "",
                            maxValue = "",
                        ),
                        enabled = true,
                    ),
                    PollSwitchItem(
                        title = stringResource(id = R.string.stream_compose_poll_option_switch_anonymous_poll),
                        enabled = false,
                    ),
                    PollSwitchItem(
                        title = stringResource(id = R.string.stream_compose_poll_option_switch_suggest_option),
                        enabled = false,
                    ),
                ),
            )

            PollSwitchList(
                pollSwitchItems = switchItemList,
                onSwitchesChanged = {},
            )
        }
    }

    @Test
    fun `snapshot PollCreationDiscardDialog composable`() {
        snapshotWithDarkMode {
            PollCreationDiscardDialog(
                usePlatformDefaultWidth = true,
                onCancelClicked = { },
                onDiscardClicked = {}
            )
        }
    }

    @Test
    fun `snapshot AttachmentsPickerPollTabFactory content light mode composable`() {
        snapshot {
            val pollCreationTabFactory = AttachmentsPickerPollTabFactory()
            pollCreationTabFactory.PickerTabContent(
                onBackPressed = { },
                attachments = listOf(),
                onAttachmentsChanged = {},
                onAttachmentItemSelected = {},
            ) {
            }
        }
    }

    @Test
    fun `snapshot AttachmentsPickerPollTabFactory content dark mode composable`() {
        snapshot(isInDarkMode = true) {
            val pollCreationTabFactory = AttachmentsPickerPollTabFactory()
            pollCreationTabFactory.PickerTabContent(
                onBackPressed = { },
                attachments = listOf(),
                onAttachmentsChanged = {},
                onAttachmentItemSelected = {},
            ) {
            }
        }
    }
}
