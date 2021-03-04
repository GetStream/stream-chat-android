package io.getstream.chat.android.livedata.mockwebserver

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth
import com.nhaarman.mockitokotlin2.mock
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.livedata.ChatDomain
import io.getstream.chat.android.livedata.ChatDomainImpl
import io.getstream.chat.android.livedata.repository.database.ChatDatabase
import io.getstream.chat.android.livedata.utils.EventObserver
import io.getstream.chat.android.livedata.utils.NoRetryPolicy
import io.getstream.chat.android.livedata.utils.TestLoggerHandler
import io.getstream.chat.android.livedata.utils.waitForSetUser
import io.getstream.chat.android.test.TestCoroutineRule
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.runBlocking
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import okhttp3.tls.HandshakeCertificates
import okhttp3.tls.HeldCertificate
import org.junit.After
import org.junit.Before
import org.junit.Rule
import java.net.InetAddress
import java.util.concurrent.Executors

/**
 * Base class for tests using mock web server.
 */
internal abstract class BaseMockWebServerIntegrationTest {

    /** single threaded arch components operations */
    @get:Rule
    val testCoroutines = TestCoroutineRule()

    /** single threaded coroutines via DispatcherProvider */
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    /** a realistic set of chat data, please only add to this, don't update */
    var data = MockWebServerTestDataHelper()

    /** chat client set up with mock web server url */
    lateinit var client: ChatClient

    /** the chat domain interface */
    lateinit var chatDomain: ChatDomain

    /** the chat domain implementation to access instance fields */
    lateinit var chatDomainImpl: ChatDomainImpl

    /** an in-memory chat database */
    private lateinit var chatDatabase: ChatDatabase

    /** mock web server which provides recorded responses */
    private lateinit var mockWebServer: MockWebServer

    /** spec of calls supported by mock web server */
    var serverCalls: List<MockWebServerCall> = emptyList()

    @Before
    fun setup() {
        mockWebServer = createMockWebServer()
        client = createClient()
        chatDatabase = createChatDatabase()
        createChatDomain(client, chatDatabase).apply {
            chatDomain = this
            chatDomainImpl = this
        }
        connectUser()
    }

    @After
    fun tearDown() {
        println("test cleanup")
        serverCalls = emptyList()
        mockWebServer.shutdown()
        client.disconnect()
        chatDatabase.close()
    }

    /**
     * Checks if a response is successful and raises a clear error message if it's not
     */
    fun assertSuccess(result: Result<*>) {
        if (result.isError) {
            Truth.assertWithMessage(result.error().toString()).that(result.isError).isFalse()
        }
    }

    /**
     * Checks if a response failed and raises a clear error message if it succeeded
     */
    fun assertFailure(result: Result<*>) {
        if (!result.isError) {
            Truth.assertWithMessage(result.data().toString()).that(result.isError).isTrue()
        }
    }

    /**
     * Handles requests to the mock web server, validates them, and provides prerecorded
     * mock responses to satisfy the request.
     */
    private fun createMockWebServer(): MockWebServer {
        return MockWebServer().apply {
            dispatcher = object : Dispatcher() {
                override fun dispatch(request: RecordedRequest): MockResponse {
                    if (isWebSocketUrl(request)) {
                        return MockResponse().withWebSocketUpgrade(object : WebSocketListener() {
                            override fun onOpen(webSocket: WebSocket, response: Response) {
                                super.onOpen(webSocket, response)
                                // very likely that this instance of `WebSocket`
                                // can be used to send further events
                                webSocket.send(createConnectedEventStringJson())
                            }
                        })
                    }
                    serverCalls.forEach {
                        if (it.isApplicable(request)) {
                            return it.executeCall()
                        }
                    }
                    throw IllegalStateException("Request not supported")
                }

                private fun isWebSocketUrl(request: RecordedRequest): Boolean {
                    return request.requestUrl.toString().contains("connect")
                }
            }
        }
    }

    /**
     * Creates an instance of [ChatClient] which communicates with mock web server.
     * Warm up call and TLS ciphering is disabled not to complicate things.
     */
    private fun createClient(): ChatClient {
        return ChatClient.Builder(data.apiKey, ApplicationProvider.getApplicationContext())
            .baseUrl(mockWebServer.url("/").toString())
            .logLevel(data.logLevel)
            .disableWarmUp()
            .disableTls()
            .loggerHandler(TestLoggerHandler()).build()
    }

    /**
     * Creates an in-memory database
     */
    private fun createChatDatabase(): ChatDatabase {
        return Room
            .inMemoryDatabaseBuilder(
                InstrumentationRegistry.getInstrumentation().targetContext,
                ChatDatabase::class.java
            )
            .allowMainThreadQueries()
            // Use a separate thread for Room transactions to avoid deadlocks
            // This means that tests that run Room transactions can't use testCoroutines.scope.runBlockingTest,
            // and have to simply use runBlocking instead
            .setTransactionExecutor(Executors.newSingleThreadExecutor())
            .setQueryExecutor(testCoroutines.dispatcher.asExecutor())
            .build()
    }

    /**
     * Creates an instance of [ChatDomain] with disabled recovery mode and no retries in case of
     * failed network requests.
     */
    private fun createChatDomain(client: ChatClient, chatDatabase: ChatDatabase): ChatDomainImpl {
        return runBlocking {
            ChatDomainImpl(
                client = client,
                userOverwrite = data.user,
                db = chatDatabase,
                mainHandler = mock(),
                offlineEnabled = true,
                userPresence = true,
                recoveryEnabled = false,
                backgroundSyncEnabled = false,
                appContext = ApplicationProvider.getApplicationContext()
            ).apply {
                retryPolicy = NoRetryPolicy()
                repos.insertUsers(listOf(data.user))
                errorEvents.observeForever(
                    EventObserver {
                        println("error event$it")
                    }
                )
            }
        }
    }

    /**
     * Sets the current user and awaits for network connection to be established.
     * TODO: check why this doesn't work without a delay in `waitForSetUser`
     */
    private fun connectUser() = runBlocking {
        waitForSetUser(client, data.user, data.token)
        Truth.assertThat(client.isSocketConnected()).isTrue()
    }

    /**
     * Enables TLS support for mock web server. Doesn't work unless client is set up
     * to trust self-signed certificates
     */
    private fun MockWebServer.useHttps() {
        val localhost: String = InetAddress.getByName("localhost").canonicalHostName
        val localhostCertificate: HeldCertificate = HeldCertificate.Builder()
            .addSubjectAlternativeName(localhost)
            .build()
        val serverCertificates: HandshakeCertificates = HandshakeCertificates.Builder()
            .heldCertificate(localhostCertificate)
            .build()
        useHttps(serverCertificates.sslSocketFactory(), false)
    }
}
