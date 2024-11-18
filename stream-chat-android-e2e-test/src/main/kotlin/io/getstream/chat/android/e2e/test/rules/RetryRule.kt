/*
 * Copyright (c) 2014-2024 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.e2e.test.rules

import io.getstream.chat.android.e2e.test.helpers.DatabaseOperations
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

/**
 * Annotation to specify amount of retries of a specific test
 *
 * The value in this annotation takes precedence over Retry Rule defaultRetries value
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
public annotation class Retry(val times: Int = 0)

/**
 * Rule to retry failed tests
 *
 * If @Retry annotation is not specified in the test method, it defaults to DEFAULT_RETRIES
 *
 * @param defaultRetries amount of retries.
 */
public class RetryRule(
    private val defaultRetries: Int = DEFAULT_RETRIES,
) : TestRule {

    override fun apply(base: Statement, description: Description): Statement = statement(base, description)

    private fun statement(
        base: Statement,
        description: Description,
    ): Statement {
        return object : Statement() {
            @Throws(Throwable::class)
            override fun evaluate() {
                val retryAnnotation: Retry? = description.getAnnotation(Retry::class.java)
                val retryCount = retryAnnotation?.times ?: DEFAULT_RETRIES
                val databaseOperations = DatabaseOperations()

                System.err.println("retry_count: $retryCount")

                var caughtThrowable: Throwable? = null

                for (i in 0 until retryCount) {
                    System.err.println("${description.displayName}: run ${(i + 1)}")

                    try {
                        base.evaluate()
                        return
                    } catch (t: Throwable) {
                        caughtThrowable = t
                        System.err.println("${description.displayName}: run ${(i + 1)} failed.")
                        databaseOperations.clearDatabases()
                    }
                }
                System.err.println("${description.displayName}: Giving up after $retryCount  failures.")
                throw caughtThrowable ?: IllegalStateException()
            }
        }
    }

    public companion object {
        public const val DEFAULT_RETRIES: Int = 3
    }
}
