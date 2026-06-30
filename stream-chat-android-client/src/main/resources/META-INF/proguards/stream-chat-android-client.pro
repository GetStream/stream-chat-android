## Stream Chat Android Client Proguard Rules

# Classes that are using with QuerySort can't be minified, because QuerySort uses reflection. If the
# name of the fields of the classes being used by QuerySort, change, the sort won't work as expected.
-keep class io.getstream.chat.android.models.** { *; }
-keep class io.getstream.chat.android.client.api2.model.** { *; }

# Rules necessary for R8 full mode
-keep class io.getstream.chat.android.client.api2.endpoint.** { *; }
-keep class io.getstream.chat.android.client.call.RetrofitCall { *; }
-keep class com.squareup.moshi.JsonReader
-keep class com.squareup.moshi.JsonAdapter
-keep class kotlin.reflect.jvm.internal.* { *; }

# Rules to improve the logs by keeping the names of the classes
-keep class * extends io.getstream.chat.android.client.clientstate.UserState

# Classes that are used by reflection.
-keep class io.getstream.chat.android.client.notifications.ChatPushDelegate { *; }
