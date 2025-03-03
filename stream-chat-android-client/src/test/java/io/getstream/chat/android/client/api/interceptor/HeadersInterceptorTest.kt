package io.getstream.chat.android.client.api.interceptor

import io.getstream.chat.android.client.api.FakeChain
import io.getstream.chat.android.client.api.FakeResponse
import io.getstream.chat.android.client.utils.HeadersUtil
import org.amshove.kluent.`should be equal to`
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

internal class HeadersInterceptorTest {

	@Test
	fun testAnonymousUserHeaders() {
		// given
		val isAnonymous = { true }
		val headersUtil = mock<HeadersUtil>()
		whenever(headersUtil.buildSdkTrackingHeaders()).doReturn("sdkTrackingHeaders")
		whenever(headersUtil.buildUserAgent()).doReturn("userAgent")
		val interceptor = HeadersInterceptor(isAnonymous, headersUtil)
		// when
		val response = interceptor.intercept(FakeChain(FakeResponse(200)))
		//then
		response.request.header("User-Agent") `should be equal to` "userAgent"
		response.request.header("Content-Type") `should be equal to` "application/json"
		response.request.header("stream-auth-type") `should be equal to` "anonymous"
		response.request.header("X-Stream-Client") `should be equal to` "sdkTrackingHeaders"
		response.request.header("Cache-Control") `should be equal to` "no-cache"
	}

	@Test
	fun testAuthenticatedUserHeaders() {
		// given
		val isAnonymous = { false }
		val headersUtil = mock<HeadersUtil>()
		whenever(headersUtil.buildSdkTrackingHeaders()).doReturn("sdkTrackingHeaders")
		whenever(headersUtil.buildUserAgent()).doReturn("userAgent")
		val interceptor = HeadersInterceptor(isAnonymous, headersUtil)
		// when
		val response = interceptor.intercept(FakeChain(FakeResponse(200)))
		//then
		response.request.header("User-Agent") `should be equal to` "userAgent"
		response.request.header("Content-Type") `should be equal to` "application/json"
		response.request.header("stream-auth-type") `should be equal to` "jwt"
		response.request.header("X-Stream-Client") `should be equal to` "sdkTrackingHeaders"
		response.request.header("Cache-Control") `should be equal to` "no-cache"
	}
}
