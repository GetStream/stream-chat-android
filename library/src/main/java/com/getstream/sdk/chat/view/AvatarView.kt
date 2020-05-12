package com.getstream.sdk.chat.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.media.ThumbnailUtils
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import com.getstream.sdk.chat.ImageLoader
import io.getstream.chat.android.client.models.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class AvatarView @JvmOverloads constructor(
		context: Context,
		attrs: AttributeSet? = null,
		defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

	fun setUser(user: User, style: BaseStyle) {
		configUIs(style) { AvatarDrawable(listOfNotNull(user.createBitmap())) }
	}

	private fun configUIs(style: BaseStyle, generateAvatarDrawable: suspend () -> AvatarDrawable) {
		layoutParams = layoutParams.apply {
			width = style.getAvatarWidth()
			height = style.getAvatarHeight()
		}
		GlobalScope.launch(Dispatchers.Main) {
			setImageDrawable(generateAvatarDrawable())
		}
	}

	private suspend fun User.createBitmap(): Bitmap? =
		ImageLoader.getBitmap(context,
				getExtraValue("image", ""),
				ImageLoader.ImageTransformation.Circle)
}

private const val FACTOR = 1.7
private class AvatarDrawable(bitmaps: List<Bitmap>) : Drawable() {
	private val avatarBitmaps = bitmaps.take(3)
	private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
	private var avatarItems: List<AvatarItem> = listOf() // = ArrayList<AvatarItem>()

	private fun reconfigureItems() {
		avatarItems = when (avatarBitmaps.size) {
			0 -> listOf()
			1 -> configureSingleAvatar(avatarBitmaps[0])
			2 -> configureDoubleAvatar(avatarBitmaps[0], avatarBitmaps[1])
			else -> configureTripleAvatar(avatarBitmaps[0], avatarBitmaps[1], avatarBitmaps[2])
		}
	}

	private fun configureSingleAvatar(avatarBitmap: Bitmap) =
			listOf(AvatarItem(avatarBitmap.scaleCenterCrop(bounds),
					Rect(0, 0, bounds.width(), bounds.height())))

	private fun configureDoubleAvatar(topAvatarBitmap: Bitmap, bottomAvatarBitmap: Bitmap): List<AvatarItem> {
		val avatarBound = bounds.reduce()
		return listOf(AvatarItem(topAvatarBitmap.scaleCenterCrop(avatarBound), avatarBound),
				AvatarItem(bottomAvatarBitmap.scaleCenterCrop(avatarBound),
						Rect(bounds.right - avatarBound.right,
								bounds.bottom - avatarBound.bottom,
								bounds.right,
								bounds.bottom)
				)
		)
	}
	
	private fun configureTripleAvatar(topLeftAvatarBitmap: Bitmap,
	                                  topRightAvatarBitmap: Bitmap,
	                                  bottomAvatarBitmap: Bitmap): List<AvatarItem> {
		val avatarBound = bounds.reduce()
		return listOf(AvatarItem(topLeftAvatarBitmap.scaleCenterCrop(avatarBound), avatarBound),
				AvatarItem(topRightAvatarBitmap.scaleCenterCrop(avatarBound),
						Rect(bounds.right - avatarBound.right,
								0,
								bounds.right,
								avatarBound.bottom)),
				AvatarItem(bottomAvatarBitmap.scaleCenterCrop(avatarBound),
						Rect((bounds.right/2) - (avatarBound.right/2),
								bounds.bottom - avatarBound.bottom,
								(bounds.right/2) + (avatarBound.right/2),
								bounds.bottom)
				)
		)
	}

	override fun draw(canvas: Canvas) {
		avatarItems.forEach {
			canvas.drawBitmap(it.bitmap, bounds, it.position, paint)
		}
	}

	private data class AvatarItem(val bitmap: Bitmap, val position: Rect)


	override fun setAlpha(alpha: Int) {
		paint.alpha = alpha
	}

	override fun onBoundsChange(bounds: Rect) {
		super.onBoundsChange(bounds)
		reconfigureItems()
	}

	override fun getOpacity() = PixelFormat.TRANSLUCENT

	override fun setColorFilter(colorFilter: ColorFilter?) {
		paint.colorFilter = colorFilter
	}

	private fun Rect.reduce() = Rect(0, 0, width().div(FACTOR).toInt(), height().div(FACTOR).toInt())
	private fun Bitmap.scaleCenterCrop(size: Rect): Bitmap =
		ThumbnailUtils.extractThumbnail(this, size.width(), size.height())
}