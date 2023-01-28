package jr.brian.issaaiapp.util.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.LocalFocusManager
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
    LazyColumn(modifier = modifier, state = listState) {
        items(chats.size) {
            ChatBox(
                text = chats[it].text,
                senderLabel = chats[it].senderLabel,
                timeStamp = chats[it].timeStamp,
                isHumanChatBox = chats[it].senderLabel == SenderLabel.HUMAN_SENDER_LABEL
            )
            Spacer(Modifier.height(15.dp))
        }
    }
}

@Composable
fun ChatBox(
    text: String,
    senderLabel: String,
    timeStamp: String,
    isHumanChatBox: Boolean
) {
    val focusManager = LocalFocusManager.current
    if (isHumanChatBox) {
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
                .clickable { focusManager.clearFocus() }
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
    val color = HumanChatBoxColor
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
                style = TextStyle(color = TextWhite),
                modifier = Modifier.padding(15.dp)
            )
        }
    }
}

object SenderLabel {
    const val HUMAN_SENDER_LABEL = "Me"
    const val AI_SENDER_LABEL = "AI"
}