package jr.brian.issaaiapp.util

import android.content.Context
import android.content.Intent
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Toast
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import java.util.*

fun senderAndTimeStyle(color: Color) = TextStyle(
    fontSize = 15.sp,
    fontWeight = FontWeight.Bold,
    color = color
)

fun getSpeechInputIntent(context: Context) : Intent? {
    if (!SpeechRecognizer.isRecognitionAvailable(context)) {
        Toast.makeText(context, "Speech not Available", Toast.LENGTH_SHORT).show()
    } else {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH
        )
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak Something")
        return intent
    }
    return null
}

object SenderLabel {
    const val HUMAN_SENDER_LABEL = "Me"
    const val CHATGPT_SENDER_LABEL = "ChatGPT"
}

@Suppress("unused")
object ChatConfig {
    const val GPT_3_5_TURBO = "gpt-3.5-turbo"
    const val GPT_4 = "gpt-4"

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
}