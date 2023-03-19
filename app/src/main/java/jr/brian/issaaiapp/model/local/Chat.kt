package jr.brian.issaaiapp.model.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chats")
data class Chat(
    @PrimaryKey val fullTimestamp: String,
    val text: String,
    val senderLabel: String,
    val timeSent: String
)