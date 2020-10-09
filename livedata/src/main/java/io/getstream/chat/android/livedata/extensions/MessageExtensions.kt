import io.getstream.chat.android.client.models.Message
import java.util.Date

fun Message.wasCreatedAfterOrAt(date: Date?): Boolean = createdAt ?: createdLocallyAt ?: NEVER >= date
fun Message.wasCreatedAfter(date: Date?): Boolean = createdAt ?: createdLocallyAt ?: NEVER > date
fun Message.wasCreatedBefore(date: Date?): Boolean = createdAt ?: createdLocallyAt ?: NEVER < date
fun Message.wasCreatedBeforeOrAt(date: Date?): Boolean = createdAt ?: createdLocallyAt ?: NEVER <= date
val NEVER = Date(0)
