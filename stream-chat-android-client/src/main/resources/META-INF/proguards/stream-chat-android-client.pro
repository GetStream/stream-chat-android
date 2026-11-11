## Stream Chat Android Client Proguard Rules

# QuerySortByReflection reads model properties by name through kotlin-reflect, which needs each property's field and
# getter under its original name; without the getter it fails reading the private field. Model classes nothing uses
# can still be removed.
-keepnames class io.getstream.chat.android.models.**
-keepclassmembers class io.getstream.chat.android.models.** {
    <fields>;
    public *** get*();
    public *** is*();
}

# CustomObjectDtoAdapter tells custom data apart by matching the field names of the DTOs it parses against the JSON
# keys, so those names must survive minification. Unused DTOs and members can still be removed.
-keepnames class * extends io.getstream.chat.android.client.api2.model.dto.ExtraDataDto { *; }
-keepnames class io.getstream.chat.android.client.api2.model.dto.DownstreamModerationDetailsDto { *; }

# ChatModule reads these annotations off each Retrofit API to pick its HTTP client. Retrofit's own rules keep the
# endpoint methods; this only keeps the interfaces, and with them their annotations. No allowshrinking: Retrofit
# implements them at runtime, and R8 would otherwise assume nothing does and fail the cast to them.
-keep,allowobfuscation @io.getstream.chat.android.client.api.AuthenticatedApi interface *
-keep,allowobfuscation @io.getstream.chat.android.client.api.AnonymousApi interface *

# Rules necessary for R8 full mode
-keep class com.squareup.moshi.JsonReader
-keep class com.squareup.moshi.JsonAdapter

# Rules to improve the logs by keeping the names of the classes
-keep class * extends io.getstream.chat.android.client.clientstate.UserState

# The push library finds this class through manifest metadata and builds it with its no-argument constructor.
-keep class io.getstream.chat.android.client.notifications.ChatPushDelegate { <init>(); }
