package jr.brian.issaaiapp.model.local

data class Chat(
    val fullTimeStamp: String,
    val text: String,
    val senderLabel: String,
    val dateSent: String,
    val timeSent: String
)