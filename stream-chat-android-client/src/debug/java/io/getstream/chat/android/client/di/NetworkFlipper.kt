package io.getstream.chat.android.client.di

import com.facebook.flipper.plugins.network.FlipperOkhttpInterceptor
import com.facebook.flipper.plugins.network.NetworkFlipperPlugin

public val networkFlipper: NetworkFlipperPlugin = NetworkFlipperPlugin()

public fun flipperInterceptor(): FlipperOkhttpInterceptor = FlipperOkhttpInterceptor(networkFlipper)
