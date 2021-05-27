---
id: client-initializing-sdk
title: Initializing SDK
sidebar_position: 2
---

When integrating with Stream Cha, your first step is initializing the `ChatClient`. The `ChatClient` is the main entry point for all operations in the library. Create a single `ChatClient` and re-use it across your application.

A best practice is to initialize `ChatClient` in the `Application` class and create a static reference to the client:

 ```kotlin
 class App : Application() {
     override fun onCreate() {
         super.onCreate()
         val chatClient = ChatClient.Builder("apiKey", context).build()

         // Static reference to initialised client
         val staticClientRef = ChatClient.instance()
     }
 }
 ```
> You can access your apiKey in the [Dashboard](https://getstream.io/dashboard)

If you create the `ChatClient` instance following the pattern in the previous example, you will be able to access that instance from any part of your application. 

Access the `ChatClient` using the `instance()` method:

 ```kotlin
 class MainActivity: AppCompatActivity() {
     override fun onCreate(savedInstanceState: Bundle?) {
         super.onCreate(savedInstanceState)
         val chatClient = ChatClient.instance() // Returns the same value as `chatClient` in the previous example.
     }
 }
 ```
