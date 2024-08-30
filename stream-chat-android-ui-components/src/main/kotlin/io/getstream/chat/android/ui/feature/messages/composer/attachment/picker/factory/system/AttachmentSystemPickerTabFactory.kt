package io.getstream.chat.android.ui.feature.messages.composer.attachment.picker.factory.system

import android.graphics.drawable.Drawable
import androidx.fragment.app.Fragment
import io.getstream.chat.android.ui.feature.messages.composer.attachment.picker.AttachmentsPickerDialogStyle
import io.getstream.chat.android.ui.feature.messages.composer.attachment.picker.factory.AttachmentsPickerTabFactory
import io.getstream.chat.android.ui.feature.messages.composer.attachment.picker.factory.AttachmentsPickerTabListener
import io.getstream.chat.android.ui.feature.messages.composer.attachment.picker.factory.system.internal.AttachmentSystemPickerFragment

/**
 * An attachment factory that creates a tab with the few icons and uses system pickers instead.
 */
public class AttachmentSystemPickerTabFactory(
    private val mediaAttachmentsTabEnabled: Boolean,
    private val fileAttachmentsTabEnabled: Boolean,
    private val cameraAttachmentsTabEnabled: Boolean,
    private val pollAttachmentsTabEnabled: Boolean,
) : AttachmentsPickerTabFactory {

    /**
     * Create the tab icon.
     * @param style The style of the dialog.
     */
    override fun createTabIcon(style: AttachmentsPickerDialogStyle): Drawable {
        return style.submitAttachmentsButtonIconDrawable
    }

    /**
     * Create the tab fragment.
     * @param style The style of the dialog.
     * @param attachmentsPickerTabListener The listener for the tab.
     */
    override fun createTabFragment(
        style: AttachmentsPickerDialogStyle,
        attachmentsPickerTabListener: AttachmentsPickerTabListener,
    ): Fragment {
        return AttachmentSystemPickerFragment.newInstance(
            style,
            attachmentsPickerTabListener,
            mediaAttachmentsTabEnabled,
            fileAttachmentsTabEnabled,
            cameraAttachmentsTabEnabled,
            pollAttachmentsTabEnabled
        )
    }
}