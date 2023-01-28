package jr.brian.issaaiapp.util.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import jr.brian.issaaiapp.model.local.Chat
import kotlinx.coroutines.launch

@Composable
fun ChatSection(modifier: Modifier, chats: MutableList<Chat>) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    LazyColumn(modifier = modifier, state = listState) {
        scope.launch { listState.animateScrollToItem(chats.size) }
        items(chats.size) {
            ChatBox(
                text = chats[it].text,
                senderLabel = chats[it].sender,
                timeStamp = chats[it].timeStamp,
                isFromAI = chats[it].sender == SenderLabel.AISendLabel
            )
            Spacer(Modifier.height(15.dp))
        }
    }
}

@Composable
fun ChatBox(text: String, senderLabel: String, timeStamp: String, isFromAI: Boolean) {
    val focusManager = LocalFocusManager.current
    if (isFromAI) {
        HumanChatBox(
            focusManager = focusManager,
            text = text,
            senderLabel = senderLabel,
            timeStamp = timeStamp
        )
    } else {
        AIChatBox(
            focusManager = focusManager,
            text = text,
            senderLabel = senderLabel,
            timeStamp = timeStamp
        )
    }
}

@Composable
fun AIChatBox(
    focusManager: FocusManager,
    text: String,
    senderLabel: String,
    timeStamp: String
) {
    val color = Color(0xFF7BAFB0)
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(10.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
                .background(color)
                .weight(.8f)
                .clickable { focusManager.clearFocus() }
        ) {

            Text(
                text,
                style = TextStyle(color = Color.White),
                modifier = Modifier.padding(15.dp),
            )
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(.2f)
        ) {
            Text(
                senderLabel,
                style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = color),
            )
            Text(
                timeStamp,
                style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = color),
            )
        }
    }
}

@Composable
fun HumanChatBox(
    focusManager: FocusManager,
    text: String,
    senderLabel: String,
    timeStamp: String
) {
    val color = Color(0xFFAF7C7B)
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(10.dp)) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(.2f)
        ) {
            Text(
                senderLabel,
                style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = color),
            )
            Text(
                timeStamp,
                style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = color),
            )
        }
        Box(
            modifier = Modifier
                .weight(.8f)
                .fillMaxWidth()
                .padding(10.dp)
                .background(color)
                .clickable { focusManager.clearFocus() }
        ) {
            Text(
                text,
                style = TextStyle(color = Color.White),
                modifier = Modifier.padding(15.dp)
            )
        }
    }
}

object SenderLabel {
    const val HumanSenderLabel = "Me"
    const val AISendLabel = "AI"
}