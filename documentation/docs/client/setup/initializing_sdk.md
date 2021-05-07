---
id: clientInitializingSDK
title: Initializing SDK
sidebar_position: 2
---

 As a first step, you need to initialize `ChatClient`, which is the main entry point for all operations in the library. You should only create the client once and re-use it across your application. Typically `ChatClient` is initialized in `Application` class:

 ```kotlin
 class App : Application() {
     override fun onCreate() {
         super.onCreate()
         val client = ChatClient.Builder("apiKey", context).build()
         // Static reference to initialised client
         val staticClientRef = ChatClient.instance()
     }
 }
 ```

 With this, you will be able to retrieve instances of the different components from any part of your application using `instance()`. Here's an example:

 ```kotlin
 class MainActivity: AppCompatActivity() {
     override fun onCreate(savedInstanceState: Bundle?) {
         super.onCreate(savedInstanceState)
         val chatClient = ChatClient.instance()
     }
 }
 ```
