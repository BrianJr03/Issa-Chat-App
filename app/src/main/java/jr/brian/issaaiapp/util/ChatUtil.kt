package jr.brian.issaaiapp.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

fun senderAndTimeStyle(color: Color) = TextStyle(
    fontSize = 15.sp,
    fontWeight = FontWeight.Bold,
    color = color
)

object SenderLabel {
    const val HUMAN_SENDER_LABEL = "Me"
    const val CHATGPT_SENDER_LABEL = "ChatGPT"
    const val GREETING_SENDER_LABEL = "Greetings"
}

object ChatConfig {
    const val GPT_3_5_TURBO = "gpt-3.5-turbo"

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

    val randomChatGptAdjective = aiAdjectives.random()
    var randomChatGptAdjectiveLabel = "( $randomChatGptAdjective )"

    val conversationalContext = listOf(
        "Be as ${randomChatGptAdjective.lowercase()} as possible.",
        "You are my ${randomChatGptAdjective.lowercase()} assistant",
        "Play the role of the ${randomChatGptAdjective.lowercase()} bot",
        "Act as if you are extremely ${randomChatGptAdjective.lowercase()}",
        "Act as if you are the only ${randomChatGptAdjective.lowercase()} AI"
    )

    val greetings = listOf(
        "What's good my human?",
        "You? Again? \uD83D\uDE43", // Upside down face emoji
        "How are you doing today?",
        "How may I help you today?",
        "Assuhh dude \uD83D\uDE0E", // Cool emoji; black shades
        "Hi Human."
    )
}