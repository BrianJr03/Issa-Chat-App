package jr.brian.issaaiapp.util

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import jr.brian.issaaiapp.model.local.Chat
import jr.brian.issaaiapp.view.ui.theme.AIChatBoxColor
import jr.brian.issaaiapp.view.ui.theme.HumanChatBoxColor
import jr.brian.issaaiapp.view.ui.theme.TextWhite

@Composable
fun ChatSection(modifier: Modifier, chats: MutableList<Chat>, listState: LazyListState) {
    val context = LocalContext.current
    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    LazyColumn(modifier = modifier, state = listState) {
        items(chats.size) { index ->
            ChatBox(
                text = chats[index].text,
                senderLabel = chats[index].senderLabel,
                timeStamp = chats[index].timeStamp,
                isHumanChatBox = chats[index].senderLabel == SenderLabel.HUMAN_SENDER_LABEL
            ) {
                clipboardManager.setText(AnnotatedString((chats[index].text)))
                Toast.makeText(
                    context,
                    "Your copy is ready for pasta!",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}

@Composable
private fun ChatBox(
    text: String,
    senderLabel: String,
    timeStamp: String,
    isHumanChatBox: Boolean,
    onLongCLick: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    if (isHumanChatBox) {
        HumanChatBox(
            focusManager = focusManager,
            text = text,
            senderLabel = senderLabel,
            timeStamp = timeStamp,
            onLongCLick = onLongCLick
        )
    } else {
        AIChatBox(
            focusManager = focusManager,
            text = text,
            senderLabel = senderLabel,
            timeStamp = timeStamp,
            onLongCLick = onLongCLick
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AIChatBox(
    focusManager: FocusManager,
    text: String,
    senderLabel: String,
    timeStamp: String,
    onLongCLick: () -> Unit
) {
    val color = AIChatBoxColor
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(10.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
                .background(color)
                .weight(.8f)
                .combinedClickable(
                    onClick = { focusManager.clearFocus() },
                    onLongClick = { onLongCLick() },
                    onDoubleClick = {},
                )
        ) {
            Text(
                text,
                style = TextStyle(color = TextWhite),
                modifier = Modifier.padding(15.dp),
            )
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(.2f)
        ) {
            Text(
                senderLabel,
                style = senderAndTimeStyle(color),
            )
            Text(
                timeStamp,
                style = senderAndTimeStyle(color),
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HumanChatBox(
    focusManager: FocusManager,
    text: String,
    senderLabel: String,
    timeStamp: String,
    onLongCLick: () -> Unit
) {
    val color = HumanChatBoxColor
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(10.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(.2f)
        ) {
            Text(
                senderLabel,
                style = senderAndTimeStyle(color),
            )
            Text(
                timeStamp,
                style = senderAndTimeStyle(color),
            )
        }
        Box(
            modifier = Modifier
                .weight(.8f)
                .fillMaxWidth()
                .padding(10.dp)
                .background(color)
                .combinedClickable(
                    onClick = { focusManager.clearFocus() },
                    onLongClick = { onLongCLick() },
                    onDoubleClick = {},
                )
        ) {
            Text(
                text,
                style = TextStyle(color = TextWhite),
                modifier = Modifier.padding(15.dp)
            )
        }
    }
}

private fun senderAndTimeStyle(color: Color) = TextStyle(
    fontSize = 15.sp,
    fontWeight = FontWeight.Bold,
    color = color
)

object SenderLabel {
    const val HUMAN_SENDER_LABEL = "Me"
    const val AI_SENDER_LABEL = "ChatGPT"
    const val GREETING_SENDER_LABEL = "Greetings"
}

object ChatConfig {
    const val GPT_3_5_turbo = "gpt-3.5-turbo"

    val AI_TYPES = listOf(
        "sarcastic",
        "helpful",
        "unhelpful",
        "optimistic",
        "pessimistic",
        "enthusiastic",
        "joyful",
        "relentless",
        "inspirational",
        "nonchalant",
        "normal",
        "relaxed",
        "loud"
    )

    val GREETINGS = listOf(
        "What's good my boy?",
        "You? Again? \uD83D\uDE43", // Upside down face emoji
        "How are you doing today?",
        "How may I help you today?",
        "Asssuhh dude \uD83D\uDE0E", // cool emoji; black shades
        "Hi Human."
    )
}