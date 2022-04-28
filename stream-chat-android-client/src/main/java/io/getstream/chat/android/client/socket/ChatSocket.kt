/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
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

@file:Suppress("DEPRECATION_ERROR")

package io.getstream.chat.android.client.socket

import io.getstream.chat.android.client.models.User

/**
 * @startuml
 *
 * title Current WebSocket Layer
 * interface ChatSocket {
 *      ~ connect(user: User)
 *      ~ connectAnonymously()
 *      ~ addListener(listener: SocketListener)
 *      ~ removeListener(listener: SocketListener)
 *      ~ disconnect()
 *      ~ releaseConnection()
 * }
 *
 * interface ChatSocketService {
 *      ~ anonymousConnect(endpoint: String, apiKey: String)
 *      ~ userConnect(endpoint: String, apiKey: String, user: User)
 *      ~ disconnect()
 *      ~ releaseConnection()
 *      ~ addListener(listener: SocketListener)
 *      ~ removeListener(listener: SocketListener)
 *      ~ onSocketError(error: ChatError)
 *      ~ onConnectionResolved(event: ConnectedEvent)
 *      ~ onEvent(event: ChatEvent)
 * }
 *
 * class Socket {
 *      - {field} socket: WebSocket (OkHttp WebSocket instance)
 *      - parser: ChatParser
 *      ~ send(event: ChatEvent)
 *      ~ close(code: Int, reason: String)
 * }
 *
 * class EventsParser {
 *      .. constructor ..
 *      - parser: ChatParser
 *      - service: ChatSocketService
 *      ..
 *
 * }
 * class HealthMonitor
 * class SocketFactory {
 *      - parser: ChatParser
 *      - tokenManager: TokenManager
 *      ~ createAnonymousSocket(): Socket
 *      ~ createNormalSocket(): Socket
 * }
 *
 * interface SocketListener {
 *      + onConnecting()
 *      + onConnected(event: ConnectedEvent)
 *      + onDisconnected(cause: DisconnectCause)
 *      + onError(error: ChatError)
 *      + onEvent(event: ChatEvent)
 * }
 *
 * class ChatSocketImpl {
 *      .. constructor ..
 *      - apiKey: String
 *      - wssUrl: String
 *      - tokenManager: TokenManager
 *      - parser: ChatParser
 *      - networkStateProvider: NetworkStateProvider
 *      ..
 *      - service: ChatSocketService
 * }
 * class ChatSocketServiceImpl {
 *      .. constructor ..
 *      - tokenManager: TokenManager
 *      - socketFactory: SocketFactory,
 *      - networkStateProvider: NetworkStateProvider,
 *      - parser: ChatParser,
 *      ..
 *      - socket: Socket
 *      - eventsParser: EventParser
 *      - healthMonitor: HealthMonitor
 *      - networkStateListener: NetworkStateProvider.Listener
 *      - state: ChatSocketServiceImpl.State
 * }
 * class ChatClient {
 *      Most of the functionality of this class
 *      is omitted to keep simplicity in diagram.
 *      ==
 *      .. constructor ..
 *      - socket: ChatSocket
 *      - socketStateService: SocketStateService
 *      - userStateService: UserStateService
 *      ..
 *      - eventsObservable: ChatEventsObservable
 * }
 *
 * class ChatEventsObservable {
 *      .. constructor ..
 *      - socket: ChatSocket
 *      - client: ChatClient
 *      ..
 *      - eventsMapper: EventsMapper
 *      ..
 *      subscribe(filter: << lambda >>, listener: ChatEventListener<ChatEvent>): Disposable
 *      subscribeSingle(filter: << lambda >>, listener: ChatEventListener<ChatEvent>): Disposable
 * }
 *
 * class EventsMapper {
 *      .. constructor ..
 *      - observable: ChatEventsObservable
 * }
 * class State << (D,orchid) >>
 * class ConnectionConf << (D,orchid) >>
 * ChatSocketImpl ..|> ChatSocket
 * ChatSocketServiceImpl ..|> ChatSocketService
 * EventsParser --> ChatSocketService
 * ChatSocketServiceImpl --> EventsParser
 * ChatSocketImpl *--> ChatSocketService
 * ChatSocketImpl ..> SocketFactory
 * SocketFactory ..> Socket : produces
 * ChatSocketServiceImpl --> HealthMonitor
 * ChatSocketServiceImpl --> Socket
 * ChatSocketService ..> SocketListener
 * SocketFactory ..> EventsParser
 * ChatSocket ..> SocketListener
 * State --+ ChatSocketServiceImpl
 * ConnectionConf --+ ChatSocketServiceImpl
 * ChatEventsObservable --> ChatSocket
 * ChatEventsObservable <--* ChatClient
 * EventsMapper ..|> SocketListener
 * ChatEventsObservable <--> EventsMapper
 * ChatClient --> ChatSocket
 * ChatClient ..> SocketListener
 * @enduml
 *
 * @startuml
 * title Refactored WebSocket Layer
 *
 * interface ChatSocketService {
 *      ~ anonymousConnect(endpoint: String, apiKey: String)
 *      ~ userConnect(endpoint: String, apiKey: String, user: User)
 *      ~ disconnect()
 *      ~ releaseConnection()
 *      ~ addListener(listener: SocketListener)
 *      ~ removeListener(listener: SocketListener)
 *      ~ onSocketError(error: ChatError)
 *      ~ onConnectionResolved(event: ConnectedEvent)
 *      ~ onEvent(event: ChatEvent)
 * }
 *
 * class Socket {
 *      - {field} socket: WebSocket (OkHttp WebSocket instance)
 *      - parser: ChatParser
 *      ~ send(event: ChatEvent)
 *      ~ close(code: Int, reason: String)
 * }
 *
 * class EventsParser {
 *      .. constructor ..
 *      - parser: ChatParser
 *      - service: ChatSocketService
 *      ..
 *
 * }
 * class HealthMonitor
 * class SocketFactory {
 *      - parser: ChatParser
 *      - tokenManager: TokenManager
 *      ~ createAnonymousSocket(): Socket
 *      ~ createNormalSocket(): Socket
 * }
 *
 * interface SocketListener {
 *      + onConnecting()
 *      + onConnected(event: ConnectedEvent)
 *      + onDisconnected(cause: DisconnectCause)
 *      + onError(error: ChatError)
 *      + onEvent(event: ChatEvent)
 * }
 *
 * class ChatSocketServiceImpl {
 *      .. constructor ..
 *      - tokenManager: TokenManager
 *      - socketFactory: SocketFactory,
 *      - networkStateProvider: NetworkStateProvider,
 *      - parser: ChatParser,
 *      ..
 *      - socket: Socket
 *      - eventsParser: EventParser
 *      - healthMonitor: HealthMonitor
 *      - networkStateListener: NetworkStateProvider.Listener
 *      - state: ChatSocketServiceImpl.State
 * }
 * class ChatClient {
 *      Most of the functionality of this class
 *      is omitted to keep simplicity in diagram.
 *      ==
 *      .. constructor ..
 *      - socketService: ChatSocketService
 *      - socketStateService: SocketStateService
 *      - userStateService: UserStateService
 *      ..
 *      - eventsObservable: ChatEventsObservable
 * }
 *
 * class ChatEventsObservable {
 *      .. constructor ..
 *      - socketService: ChatSocketService
 *      ..
 *      subscribe(filter: << lambda >>, listener: ChatEventListener<ChatEvent>): Disposable
 *      subscribeSingle(filter: << lambda >>, listener: ChatEventListener<ChatEvent>): Disposable
 * }
 *
 * class State << (D,orchid) >>
 * class ConnectionConf << (D,orchid) >>
 * ChatSocketServiceImpl ..|> ChatSocketService
 * EventsParser --> ChatSocketService
 * ChatSocketServiceImpl --> EventsParser
 * ChatSocketServiceImpl ..> SocketFactory
 * SocketFactory ..> Socket : produces
 * ChatSocketServiceImpl --> HealthMonitor
 * ChatSocketServiceImpl --> Socket
 * ChatSocketService ..> SocketListener
 * SocketFactory ..> EventsParser
 * State --+ ChatSocketServiceImpl
 * ConnectionConf --+ ChatSocketServiceImpl
 * ChatEventsObservable --> ChatSocketService
 * ChatEventsObservable --* ChatClient
 * ChatEventsObservable ..|> SocketListener
 * ChatClient --> ChatSocketService
 * ChatClient ..> SocketListener
 * @enduml
 */
internal interface ChatSocket {
    fun connect(user: User)
    fun connectAnonymously()
    fun addListener(listener: SocketListener)
    fun removeListener(listener: SocketListener)
    fun disconnect()
    fun releaseConnection()
}
