package io.getstream.chat.android.compose.ui.components.poll

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import io.getstream.chat.android.compose.ui.SnapshotTest
import org.junit.Rule
import org.junit.Test

internal class PollOptionInputTest : SnapshotTest {

    @get:Rule
    override val paparazzi: Paparazzi = Paparazzi(deviceConfig = DeviceConfig.PIXEL_2)

    @Test
    fun `empty input`() {
        snapshotWithDarkMode {
            PollOptionInput(
                value = "",
                onValueChange = {},
                description = "Description",
                decorationBox = { innerTextField -> innerTextField.invoke() },
            )
        }
    }

    @Test
    fun `with input`() {
        snapshotWithDarkMode {
            PollOptionInput(
                value = "Entered text",
                onValueChange = {},
                description = "Description",
                decorationBox = { innerTextField -> innerTextField.invoke() },
            )
        }
    }
}