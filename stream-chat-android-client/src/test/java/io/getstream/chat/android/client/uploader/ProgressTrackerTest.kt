package io.getstream.chat.android.client.uploader

import io.getstream.chat.android.test.TestCoroutineRule
import io.getstream.chat.android.test.randomInt
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runBlockingTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Rule
import org.junit.jupiter.api.Test

public class ProgressTrackerTest {

    @get:Rule
    public val testCoroutines: TestCoroutineRule = TestCoroutineRule()

    @Test
    public fun `proves that progress is set`() {
        val tracker = ProgressTracker()

        val currentProgress = tracker.currentProgress()
        val progressValue = randomInt()

        tracker.setProgress(progressValue)

        currentProgress.value shouldBeEqualTo progressValue
    }

    @Test
    public fun `proves that progress gets communicated`(): Unit = testCoroutines.scope.runBlockingTest {
        val tracker = ProgressTracker()

        val currentProgress = tracker.currentProgress()
        val progressValueList = listOf(randomInt(), randomInt(), randomInt(), randomInt())

        progressValueList.forEach { value ->
            tracker.setProgress(value)

            currentProgress.first() shouldBeEqualTo value
        }
    }
}
