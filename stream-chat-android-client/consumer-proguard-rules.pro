## Stream Chat Android Client Proguard Rules

-keep class io.getstream.chat.android.client.api.* { *; }
-keep class io.getstream.chat.android.client.api.models.* { *; }
-keep class io.getstream.chat.android.client.api2.model.** { *; }
-keep class io.getstream.chat.android.client.errors.* { *; }
-keep class io.getstream.chat.android.client.events.* { *; }
-keep class io.getstream.chat.android.client.models.* { *; }
-keep class io.getstream.chat.android.client.parser.* { *; }
-keep class io.getstream.chat.android.client.socket.* { *; }
-keep class io.getstream.chat.android.client.socket.EventsParser.TypedEvent { *; }
-keep class io.getstream.chat.android.client.utils.FilterObject { *; }
-keep class io.getstream.chat.android.client.utils.Result { *; }
-keep class io.getstream.chat.android.client.utils.SyncStatus { *; }

-keepattributes Signature,*Annotation*

-keepattributes EnclosingMethod

-keep class kotlin.** { *; }
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**
-keepclassmembers class **$WhenMappings {
    <fields>;
}
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}
-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
    static void checkParameterIsNotNull(java.lang.Object, java.lang.String);
}