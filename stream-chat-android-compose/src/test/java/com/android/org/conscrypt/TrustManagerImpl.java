/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
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
package com.android.org.conscrypt;

/**
 * Test-only stub for the Android framework class {@code com.android.org.conscrypt.TrustManagerImpl}.
 *
 * <p>Paparazzi runs unit tests under layoutlib, whose framework jar does not include conscrypt's
 * {@code TrustManagerImpl}. When a rendered Compose preview builds a Coil image loader backed by
 * OkHttp ({@code coil-network-okhttp}), OkHttp selects {@code Android10Platform} and constructs
 * {@code android.net.http.X509TrustManagerExtensions}, which references this class — throwing
 * {@code NoClassDefFoundError} that OkHttp does not catch.
 *
 * <p>Merely having this class on the test classpath lets {@code X509TrustManagerExtensions} load.
 * Because the real trust manager is not an instance of it, that constructor throws
 * {@code IllegalArgumentException}, which OkHttp's {@code AndroidCertificateChainCleaner.buildIfSupported}
 * catches, falling back gracefully and building the client without a certificate chain cleaner.
 * Screenshot tests never make network calls, so this is harmless.
 */
public class TrustManagerImpl {
}
