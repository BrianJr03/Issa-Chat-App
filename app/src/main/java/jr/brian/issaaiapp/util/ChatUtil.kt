package jr.brian.issaaiapp.util

import android.content.Context
import android.content.Intent
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Toast
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import jr.brian.issaaiapp.model.local.Chat
import jr.brian.issaaiapp.model.local.ChatsDao
import java.time.format.DateTimeFormatter
import java.util.*

val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("h:mm a")
val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MM.dd.yy")

fun getConvoChats(
    dao: ChatsDao,
    chats: SnapshotStateList<Chat>,
    conversationText: MutableState<String>,
    conversationHeaderName: MutableState<String>,
    isConversationsDialogShowing: MutableState<Boolean>
) {
    val convoChats = dao.getChatsByConvo(conversationHeaderName.value)
    chats.clear()
    convoChats.forEach { chat ->
        chats.add(chat)
    }
    conversationText.value = ""
    isConversationsDialogShowing.value = false
}

fun senderAndTimeStyle(color: Color) = TextStyle(
    fontSize = 15.sp,
    fontWeight = FontWeight.Bold,
    color = color
)

fun getSpeechInputIntent(context: Context): Intent? {
    if (!SpeechRecognizer.isRecognitionAvailable(context)) {
        Toast.makeText(context, "Speech not available", Toast.LENGTH_SHORT).show()
    } else {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH
        )
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, ChatConfig.speakPrompts.random())
        return intent
    }
    return null
}

object SenderLabel {
    var HUMAN_SENDER_LABEL = ""
    const val DEFAULT_HUMAN_LABEL = "Me"
    const val CHATGPT_SENDER_LABEL = "ChatGPT"
}

@Suppress("unused")
object ChatConfig {
    const val GPT_3_5_TURBO = "gpt-3.5-turbo"
    const val GPT_4 = "gpt-4"

    const val SCROLL_ANIMATION_DELAY = 1500L

    const val DEFAULT_CONVO_CONTEXT = "You are my helpful assistant"

    private val aiAdjectives = listOf(
        "Sarcastic",
        "Helpful",
        "Unhelpful",
        "Optimistic",
        "Pessimistic",
        "Excited",
        "Joyful",
        "Charming",
        "Inspiring",
        "Nonchalant",
        "Relaxed",
        "Loud",
        "Annoyed"
    )

    private val randomChatGptAdjective = aiAdjectives.random()

    val conversationalContext = listOf(
        "Be as ${randomChatGptAdjective.lowercase()} as possible.",
        "You are my ${randomChatGptAdjective.lowercase()} assistant",
        "Play the role of the ${randomChatGptAdjective.lowercase()} bot",
        "Act as if you are extremely ${randomChatGptAdjective.lowercase()}",
        "Act as if you are the only ${randomChatGptAdjective.lowercase()} AI"
    )

    val speakPrompts = listOf(
        "What would you like to ask?",
        "Speak now... please",
        "LOL spit it out already...",
        "* ChatGPT is yawning... *",
        "Speaketh you may",
        "Listening for dat beautiful voice...",
        "Hello Human"
    )
}