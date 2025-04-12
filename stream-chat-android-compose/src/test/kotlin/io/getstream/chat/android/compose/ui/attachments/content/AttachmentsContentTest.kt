package io.getstream.chat.android.compose.ui.attachments.content

import androidx.compose.foundation.layout.Column
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import io.getstream.chat.android.compose.ui.SnapshotTest
import org.junit.Rule
import org.junit.Test

internal class AttachmentsContentTest : SnapshotTest {

    @get:Rule
    override val paparazzi = Paparazzi(deviceConfig = DeviceConfig.PIXEL_2)

    @Test
    fun `file attachment content`() {
        snapshotWithDarkMode { darkMode ->
            Column {
                FileAttachmentContent(
                    darkMode = darkMode,
                    isMine = true,
                )
                FileAttachmentContent(
                    darkMode = darkMode,
                    isMine = false,
                )
            }
        }
    }

    @Test
    fun `link attachment content`() {
        snapshotWithDarkMode { darkMode ->
            LinkAttachmentContent(
                darkMode = darkMode,
            )
        }
    }
}
