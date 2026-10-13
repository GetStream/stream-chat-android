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

package io.getstream.chat.android.ai.compose.ui.component.internal

import android.content.Context
import android.content.Intent
import android.content.pm.ResolveInfo
import android.content.pm.ServiceInfo
import android.os.Bundle
import android.os.Looper
import android.speech.RecognitionService
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import org.robolectric.shadow.api.Shadow
import org.robolectric.shadows.ShadowSpeechRecognizer

@RunWith(AndroidJUnit4::class)
internal class SpeechRecognizerHelperTest {

    @get:Rule
    val rule = createComposeRule()

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val partials = mutableListOf<String>()
    private val finals = mutableListOf<String>()
    private var shown by mutableStateOf(true)
    private lateinit var helper: SpeechRecognizerHelper

    @After
    fun tearDown() {
        ShadowSpeechRecognizer.reset()
    }

    private fun setUp(recognitionAvailable: Boolean = true) {
        if (recognitionAvailable) registerRecognitionService()
        rule.setContent {
            if (shown) {
                helper = rememberSpeechRecognizerHelper(
                    onPartialResult = { partials += it },
                    onFinalResult = { finals += it },
                )
            }
        }
        rule.waitForIdle()
    }

    // The recognizer hands its listener over in a message on the main looper.
    private fun start(): Boolean = rule.runOnIdle { helper.startListening() }.also {
        shadowOf(Looper.getMainLooper()).idle()
    }

    private val recognizer: ShadowSpeechRecognizer
        get() = Shadow.extract(ShadowSpeechRecognizer.getLatestSpeechRecognizer())

    @Test
    fun `listening fails when the device has no speech recognition`() {
        setUp(recognitionAvailable = false)

        val started = start()

        assertFalse(started)
        assertFalse(helper.isListening)
    }

    @Test
    fun `listening asks for partial results`() {
        setUp()

        val started = start()

        assertTrue(started)
        assertTrue(helper.isListening)
        val intent = recognizer.lastRecognizerIntent
        assertEquals(RecognizerIntent.ACTION_RECOGNIZE_SPEECH, intent.action)
        assertTrue(intent.getBooleanExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false))
    }

    @Test
    fun `listening again while listening does nothing`() {
        setUp()
        start()

        val startedAgain = start()

        assertFalse(startedAgain)
        assertTrue(helper.isListening)
    }

    @Test
    fun `the audio level follows the recognizer`() {
        setUp()
        start()

        recognizer.triggerOnRmsChanged(LoudRms)

        assertEquals(LoudRms, helper.rmsdB, 0f)
    }

    @Test
    fun `a partial result is reported as it is heard`() {
        setUp()
        start()

        recognizer.triggerOnPartialResults(results("hello"))

        assertEquals(listOf("hello"), partials)
    }

    @Test
    fun `a blank or missing partial result is ignored`() {
        setUp()
        start()

        recognizer.triggerOnPartialResults(results(" "))
        recognizer.triggerOnPartialResults(Bundle())

        assertEquals(emptyList<String>(), partials)
    }

    @Test
    fun `the final result is the segment the recognizer settled on`() {
        setUp()
        start()
        recognizer.triggerOnPartialResults(results("hello", final = true))

        recognizer.triggerOnResults(Bundle())

        assertEquals(listOf("hello"), finals)
        assertFalse(helper.isListening)
    }

    @Test
    fun `segments are joined with spaces as they are heard and at the end`() {
        setUp()
        start()

        recognizer.triggerOnPartialResults(results("hello", final = true))
        recognizer.triggerOnPartialResults(results("world"))
        recognizer.triggerOnPartialResults(results("world", final = true))
        recognizer.triggerOnResults(Bundle())

        assertEquals(listOf("hello", "hello world", "hello world"), partials)
        assertEquals(listOf("hello world"), finals)
    }

    @Test
    fun `segments that start with a space are joined with a single space`() {
        setUp()
        start()

        recognizer.triggerOnPartialResults(results("hello world", final = true))
        recognizer.triggerOnPartialResults(results(" how are you", final = true))
        recognizer.triggerOnResults(Bundle())

        assertEquals(listOf("hello world", "hello world how are you"), partials)
        assertEquals(listOf("hello world how are you"), finals)
    }

    @Test
    fun `speech that only arrives with the final result is kept`() {
        setUp()
        start()

        recognizer.triggerOnResults(results("hello"))

        assertEquals(listOf("hello"), finals)
    }

    @Test
    fun `a last segment that only arrives with the final result is added once`() {
        setUp()
        start()
        recognizer.triggerOnPartialResults(results("hello", final = true))

        recognizer.triggerOnResults(results("world"))

        assertEquals(listOf("hello world"), finals)
    }

    @Test
    fun `a final result repeating the last segment is not added twice`() {
        setUp()
        start()
        recognizer.triggerOnPartialResults(results("hello", final = true))

        recognizer.triggerOnResults(results("hello"))

        assertEquals(listOf("hello"), finals)
    }

    @Test
    fun `listening after an error starts from nothing`() {
        setUp()
        start()
        recognizer.triggerOnPartialResults(results("hello", final = true))
        recognizer.triggerOnError(SpeechRecognizer.ERROR_NETWORK)

        start()
        recognizer.triggerOnPartialResults(results("world", final = true))
        recognizer.triggerOnResults(Bundle())

        assertEquals(listOf("world"), finals)
    }

    @Test
    fun `any recognizer error stops listening`() {
        setUp()
        RecognizerErrors.forEach { error ->
            start()
            recognizer.triggerOnRmsChanged(LoudRms)

            recognizer.triggerOnError(error)

            assertFalse("error $error", helper.isListening)
            assertEquals("error $error", 0f, helper.rmsdB, 0f)
        }
    }

    @Test
    fun `stopping ends listening and waits for the final result`() {
        setUp()
        start()
        recognizer.triggerOnPartialResults(results("hello", final = true))

        rule.runOnIdle { helper.stopListening() }
        recognizer.triggerOnResults(Bundle())

        assertFalse(helper.isListening)
        assertEquals(listOf("hello"), finals)
    }

    @Test
    fun `stopping when not listening does nothing`() {
        setUp()

        rule.runOnIdle { helper.stopListening() }

        assertFalse(helper.isListening)
    }

    @Test
    fun `cancelling drops what was heard`() {
        setUp()
        start()
        recognizer.triggerOnPartialResults(results("hello", final = true))

        rule.runOnIdle { helper.cancel() }
        assertFalse(helper.isListening)

        start()
        recognizer.triggerOnPartialResults(results("world", final = true))
        recognizer.triggerOnResults(Bundle())

        assertEquals(listOf("world"), finals)
    }

    @Test
    fun `releasing destroys the recognizer and a new one is made for the next listen`() {
        setUp()
        start()
        val first = ShadowSpeechRecognizer.getLatestSpeechRecognizer()

        rule.runOnIdle { helper.release() }
        assertTrue(Shadow.extract<ShadowSpeechRecognizer>(first).isDestroyed)

        start()
        assertTrue(first !== ShadowSpeechRecognizer.getLatestSpeechRecognizer())
    }

    @Test
    fun `leaving the screen releases the recognizer`() {
        setUp()
        start()
        val recognizer = ShadowSpeechRecognizer.getLatestSpeechRecognizer()

        rule.runOnIdle { shown = false }
        rule.waitForIdle()

        assertTrue(Shadow.extract<ShadowSpeechRecognizer>(recognizer).isDestroyed)
    }

    private fun registerRecognitionService() {
        val service = ResolveInfo().apply {
            serviceInfo = ServiceInfo().apply {
                packageName = "com.example.speech"
                name = "com.example.speech.RecognitionService"
            }
        }
        shadowOf(context.packageManager).addResolveInfoForIntent(Intent(RecognitionService.SERVICE_INTERFACE), service)
    }

    private fun results(text: String, final: Boolean = false) = Bundle().apply {
        putStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION, arrayListOf(text))
        putBoolean("final_result", final)
    }

    private companion object {
        const val LoudRms = 7f

        val RecognizerErrors = listOf(
            SpeechRecognizer.ERROR_AUDIO,
            SpeechRecognizer.ERROR_CANNOT_CHECK_SUPPORT,
            SpeechRecognizer.ERROR_CANNOT_LISTEN_TO_DOWNLOAD_EVENTS,
            SpeechRecognizer.ERROR_CLIENT,
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS,
            SpeechRecognizer.ERROR_LANGUAGE_NOT_SUPPORTED,
            SpeechRecognizer.ERROR_LANGUAGE_UNAVAILABLE,
            SpeechRecognizer.ERROR_NETWORK,
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT,
            SpeechRecognizer.ERROR_NO_MATCH,
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY,
            SpeechRecognizer.ERROR_SERVER,
            SpeechRecognizer.ERROR_SERVER_DISCONNECTED,
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT,
            SpeechRecognizer.ERROR_TOO_MANY_REQUESTS,
            UnknownError,
        )

        const val UnknownError = 999
    }
}
