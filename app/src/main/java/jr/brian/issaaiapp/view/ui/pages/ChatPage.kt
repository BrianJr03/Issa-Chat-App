package jr.brian.issaaiapp.view.ui.pages

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import jr.brian.issaaiapp.R
import jr.brian.issaaiapp.model.local.Chat
import jr.brian.issaaiapp.util.ChatSection
import jr.brian.issaaiapp.util.SenderLabel
import jr.brian.issaaiapp.util.EmptyTextFieldDialog
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatPage() {
    val scope = rememberCoroutineScope()
    val bringIntoViewRequester = BringIntoViewRequester()

    val formatter = DateTimeFormatter.ofPattern("h:mm a")
    val currentTime = LocalDateTime.now().format(formatter)

    var textFieldText by remember { mutableStateOf("") }

    val isDialogShowing = remember { mutableStateOf(false) }
    val chatListState = rememberLazyListState()

    val chats = remember {
        mutableStateListOf(
            Chat(
                "Hi, what can I help you with today?",
                SenderLabel.AI_SENDER_LABEL,
                currentTime
            ),
            Chat("Hi Bot", SenderLabel.HUMAN_SENDER_LABEL, currentTime),
            Chat("Hi Human", SenderLabel.AI_SENDER_LABEL, currentTime),
            Chat(
                "Can you generate some images of a cat?",
                SenderLabel.HUMAN_SENDER_LABEL,
                currentTime
            ),
            Chat("lol no.. goofy", SenderLabel.AI_SENDER_LABEL, currentTime),
        )
    }

    val sendOnClick = {
        if (textFieldText.isEmpty()) {
            isDialogShowing.value = true
        } else {
            chats.add(
                Chat(
                    text = textFieldText,
                    senderLabel = SenderLabel.HUMAN_SENDER_LABEL,
                    timeStamp = currentTime
                )
            )
            textFieldText = ""
            scope.launch { chatListState.animateScrollToItem(chats.size) }
        }
    }

    EmptyTextFieldDialog(title = "Please provide a prompt", isShowing = isDialogShowing)

    Column(
        modifier = Modifier.bringIntoViewRequester(bringIntoViewRequester),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(15.dp))

        ChatSection(
            modifier = Modifier.weight(.85f),
            chats = chats,
            listState = chatListState
        )

        Row( // TextField and Send Button Row
            modifier = Modifier
                .weight(.15f)
                .padding(start = 20.dp)
                .bringIntoViewRequester(bringIntoViewRequester),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                modifier = Modifier
                    .weight(.8f)
                    .onFocusEvent { event ->
                        if (event.isFocused) {
                            scope.launch {
                                bringIntoViewRequester.bringIntoView()
                            }
                        }
                    },
                value = textFieldText,
                onValueChange = { text ->
                    textFieldText = text
                },
                label = {
                    Text(
                        text = "Enter Prompt",
                        style = TextStyle(color = MaterialTheme.colors.primary, fontWeight = FontWeight.Bold)
                    )
                },
                colors = TextFieldDefaults.textFieldColors(
                    focusedIndicatorColor = MaterialTheme.colors.secondary,
                    unfocusedIndicatorColor = MaterialTheme.colors.primary
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    sendOnClick()
                })
            )

            Icon(
                painter = painterResource(id = R.drawable.send_icon),
                tint = MaterialTheme.colors.primary,
                contentDescription = "Send Message",
                modifier = Modifier
                    .weight(.2f)
                    .combinedClickable(
                        onClick = { sendOnClick() },
                        onLongClick = {
                            chats.add(
                                Chat(
                                    text = "Issa Easter Egg",
                                    senderLabel = SenderLabel.AI_SENDER_LABEL,
                                    timeStamp = currentTime
                                )
                            )
                            scope.launch { chatListState.animateScrollToItem(chats.size) }
                        },
                        onDoubleClick = {},
                    )
            )
        }

        Spacer(Modifier.height(15.dp))
    }
}