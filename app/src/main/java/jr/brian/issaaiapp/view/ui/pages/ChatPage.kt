package jr.brian.issaaiapp.view.ui.pages

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.*
import jr.brian.issaaiapp.model.local.Chat
import jr.brian.issaaiapp.util.*
import jr.brian.issaaiapp.viewmodel.MainViewModel
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatPage() {
    val viewModel = MainViewModel()
    val scope = rememberCoroutineScope()
    val bringIntoViewRequester = BringIntoViewRequester()
    val focusManager = LocalFocusManager.current

    var promptText by remember { mutableStateOf("") }
    val conversationalContextText = remember {
        mutableStateOf(ChatConfig.conversationalContext.random())
    }

    val isDialogShowing = remember { mutableStateOf(false) }
    val isChatGptTyping = remember { mutableStateOf(false) }
    val isConversationalContextShowing = remember { mutableStateOf(false) }

    val chatListState = rememberLazyListState()

    val dateTimeFormatter = DateTimeFormatter.ofPattern("h:mm a")
    val dateFormatter = DateTimeFormatter.ofPattern("MM.dd.yy")
    val timeSent: String = LocalDateTime.now().format(dateTimeFormatter)
    val dateSent: String = LocalDateTime.now().format(dateFormatter)

    val chats = remember {
        mutableStateListOf(
            Chat(
                fullTimeStamp = LocalDateTime.now().toString(),
                text = ChatConfig.greetings.random(),
                senderLabel = SenderLabel.GREETING_SENDER_LABEL,
                dateSent = dateSent,
                timeSent = timeSent
            )
        )
    }

    val sendOnClick = {
        focusManager.clearFocus()
        if (promptText.isEmpty()) {
            isDialogShowing.value = true
        } else {
            val prompt = promptText
            promptText = ""
            scope.launch {
                chats.add(
                    Chat(
                        fullTimeStamp = LocalDateTime.now().toString(),
                        text = prompt,
                        senderLabel = SenderLabel.HUMAN_SENDER_LABEL,
                        dateSent = dateSent,
                        timeSent = timeSent
                    )
                )
                chatListState.animateScrollToItem(chats.size)
                viewModel.getAIResponse(
                    userPrompt = prompt,
                    system = conversationalContextText,
                    isAITypingLabelShowing = isChatGptTyping
                )
                chats.add(
                    Chat(
                        fullTimeStamp = LocalDateTime.now().toString(),
                        text = viewModel.response.value ?: "No response. Please try again.",
                        senderLabel = SenderLabel.CHATGPT_SENDER_LABEL,
                        dateSent = dateSent,
                        timeSent = timeSent
                    )
                )
                chatListState.animateScrollToItem(chats.size)
            }
        }
    }

    EmptyTextFieldDialog(title = "Please provide a prompt", isShowing = isDialogShowing)

    Column(
        modifier = Modifier.bringIntoViewRequester(bringIntoViewRequester),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(15.dp))

        ChatHeader(
            modifier = Modifier,
            isChatGptTyping = isChatGptTyping
        )

        ChatSection(
            modifier = Modifier.weight(.85f),
            chats = chats,
            listState = chatListState
        )

        ChatTextFieldRows(
            promptText = promptText,
            convoContextText = conversationalContextText,
            sendOnClick = { sendOnClick() },
            textFieldOnValueChange = { text -> promptText = text },
            convoContextOnValueChange = { text -> conversationalContextText.value = text },
            isConvoContextFieldShowing = isConversationalContextShowing,
            modifier = Modifier
                .padding(start = 20.dp)
                .bringIntoViewRequester(bringIntoViewRequester),
            textFieldModifier = Modifier
                .weight(.7f)
                .onFocusEvent { event ->
                    if (event.isFocused) {
                        scope.launch {
                            bringIntoViewRequester.bringIntoView()
                        }
                    }
                },
            convoContextFieldModifier = Modifier
                .onFocusEvent { event ->
                    if (event.isFocused) {
                        scope.launch {
                            bringIntoViewRequester.bringIntoView()
                        }
                    }
                },
            iconRowModifier = Modifier
                .bringIntoViewRequester(bringIntoViewRequester)
                .weight(.3f),
            sendIconModifier = Modifier.clickable { sendOnClick() }
        )
    }
}