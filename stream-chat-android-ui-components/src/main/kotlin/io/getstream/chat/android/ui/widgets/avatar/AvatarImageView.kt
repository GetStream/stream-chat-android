package io.getstream.chat.android.ui.widgets.avatar

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.AttributeSet
import androidx.annotation.DrawableRes
import com.google.android.material.imageview.ShapeableImageView
import io.getstream.chat.android.ui.utils.load
import okhttp3.HttpUrl
import java.io.File
import java.nio.ByteBuffer

/**
 * Represents the base avatar image view, which is capable of displaying an avatar of any type.
 */
public class AvatarImageView : ShapeableImageView {

    public constructor(context: Context?) : super(context)
    public constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    public constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    /**
     * Sets an [avatar] to display in this image view.
     *
     * The default supported [avatar] types are:
     * - [String] (mapped to a [Uri])
     * - [Uri] ("android.resource", "content", "file", "http", and "https" schemes only)
     * - [HttpUrl]
     * - [File]
     * - [DrawableRes]
     * - [Drawable]
     * - [Bitmap]
     * - [ByteArray]
     * - [ByteBuffer]
     *
     * The default supported [placeholder] types are:
     * - [DrawableRes]
     * - [Drawable]
     */
    public fun setAvatar(avatar: Any?, placeholder: Any? = null) {
        when (placeholder) {
            is Drawable -> load(
                data = avatar,
                placeholderDrawable = placeholder,
            )
            else -> load(
                data = avatar,
                placeholderResId = placeholder as Int?,
            )
        }

    }
}