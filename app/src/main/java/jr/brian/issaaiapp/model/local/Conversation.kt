package jr.brian.issaaiapp.model.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "conversations")
data class Conversation(
    @PrimaryKey val conversationName: String,
    val context: String = ""
)