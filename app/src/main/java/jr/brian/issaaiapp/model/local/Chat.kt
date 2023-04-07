package jr.brian.issaaiapp.model.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chats")
data class Chat(
    @PrimaryKey val fullTimeStamp: String,
    val text: String,
    val senderLabel: String,
    val dateSent: String,
    val timeSent: String,
    val conversationName: String = "Conversation 1"
)