package jr.brian.issaaiapp.util

import android.content.Context
import android.content.Intent
import android.os.Environment
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Toast
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.google.gson.Gson
import jr.brian.issaaiapp.model.local.Chat
import jr.brian.issaaiapp.model.local.ChatsDao
import java.io.File
import java.time.format.DateTimeFormatter
import java.util.*
import android.graphics.*
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Text
import com.itextpdf.layout.property.TextAlignment

val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("h:mm a")
val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MM.dd.yy")

fun saveConversationToPDF(conversationName: String, chats: List<Chat>) {
    val document = PdfDocument(
        PdfWriter(
            File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                "$conversationName.pdf"
            )
        )
    )

    val text = Text(conversationName).apply {
        setFontSize(20f)
        setBold()
    }

    val paragraph = Paragraph().apply {
        add(text)
        setTextAlignment(TextAlignment.CENTER)
        setMarginBottom(20f)
    }

    val doc = Document(document).apply {
        add(paragraph)
    }

    for (chat in chats) {
        val msgText =
            Text("${chat.senderLabel} - ${chat.dateSent}" +
                    " at ${chat.timeSent}\n\n--> ${chat.text}").apply {
                setFontSize(12f)
            }
        val msgPara = Paragraph().apply {
            add(msgText)
            setMarginTop(10f)
            setMarginBottom(10f)
            setMarginLeft(20f)
            setMarginRight(20f)
        }

        doc.add(msgPara)
    }

    doc.close()
}

fun saveConversationToJson(
    list: List<Chat>,
    filename: String
) {
    val gson = Gson()
    val json = gson.toJson(list)

    val downloadsDirectory =
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

    val file = File(downloadsDirectory, filename)
    file.writeText(json)
}

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

    val exampleConvoContext = "\"${conversationalContext.random()}\""

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