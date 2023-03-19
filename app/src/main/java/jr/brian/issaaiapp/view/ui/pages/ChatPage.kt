package jr.brian.issaaiapp.view.ui.pages

import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.airbnb.lottie.compose.*
import jr.brian.issaaiapp.model.local.Chat
import jr.brian.issaaiapp.model.local.ChatsDao
import jr.brian.issaaiapp.util.*
import jr.brian.issaaiapp.viewmodel.MainViewModel
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatPage(dao: ChatsDao, viewModel: MainViewModel = hiltViewModel()) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val bringIntoViewRequester = BringIntoViewRequester()
    val focusManager = LocalFocusManager.current

    val formatter = DateTimeFormatter.ofPattern("h:mm a")
    val now = LocalDateTime.now()
    val currentTime = now.format(formatter)

    var promptText by remember { mutableStateOf("") }
    var apiKey by remember { mutableStateOf("") }
    val conversationalContextText = remember {
        mutableStateOf(ChatConfig.conversationalContext.random())
    }

    val isErrorDialogShowing = remember { mutableStateOf(false) }
    val isSettingsDialogShowing = remember { mutableStateOf(false) }
    val isChatGptTyping = remember { mutableStateOf(false) }
    val isConversationalContextShowing = remember { mutableStateOf(false) }

    val chatListState = rememberLazyListState()

    val chats = remember { dao.getChats().toMutableStateList() }

    if (chats.isEmpty()) {
        val initChat = Chat(
            fullTimestamp = now.toString(),
            text = ChatConfig.greetings.random(),
            senderLabel = SenderLabel.GREETING_SENDER_LABEL,
            timeSent = currentTime
        )
        chats.add(initChat)
        dao.insertChat(initChat)
    }

    LaunchedEffect(key1 = 1, block = {
        chatListState.animateScrollToItem(chats.size)
    })

    val sendOnClick = {
        focusManager.clearFocus()
        if (promptText.isEmpty()) {
            isErrorDialogShowing.value = true
        } else {
            val prompt = promptText
            promptText = ""
            scope.launch {
                val myChat = Chat(
                    fullTimestamp = LocalDateTime.now().toString(),
                    text = prompt,
                    senderLabel = SenderLabel.HUMAN_SENDER_LABEL,
                    timeSent = currentTime
                )
                chats.add(myChat)
                dao.insertChat(myChat)
                chatListState.animateScrollToItem(chats.size)
                viewModel.getChatGptResponse(
                    userPrompt = prompt,
                    system = conversationalContextText,
                    isAITypingLabelShowing = isChatGptTyping
                )
                val chatGptChat = Chat(
                    fullTimestamp = LocalDateTime.now().toString(),
                    text = viewModel.response.value ?: "No response. Please try again.",
                    senderLabel = SenderLabel.CHATGPT_SENDER_LABEL,
                    timeSent = currentTime
                )
                chats.add(chatGptChat)
                dao.insertChat(chatGptChat)
                chatListState.animateScrollToItem(chats.size)
            }
        }
    }

    EmptyTextFieldDialog(title = "Please provide a prompt", isShowing = isErrorDialogShowing)

    SettingsDialog(
        apiKey = apiKey,
        textFieldOnValueChange = { text -> apiKey = text },
        isShowing = isSettingsDialogShowing,
        onSaveApiKey = {
            /* TODO - SAVE API KEY IN DATASTORE */
            Toast.makeText(
                context,
                "Api-Key saved!",
                Toast.LENGTH_LONG
            ).show()
        },
        onDeleteAllChats = {
            chats.clear()
            dao.removeAllChats()
        },
        modifier = Modifier,
        textFieldModifier = Modifier
    )

    Column(
        modifier = Modifier.bringIntoViewRequester(bringIntoViewRequester),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(15.dp))

        ChatHeader(
            modifier = Modifier.weight(.04f),
            isChatGptTyping = isChatGptTyping
        )

        ChatSection(
            modifier = Modifier.weight(.90f),
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
                .weight(.20f)
                .padding(start = 5.dp)
                .bringIntoViewRequester(bringIntoViewRequester),
            textFieldModifier = Modifier
                .weight(.6f)
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
            sendIconModifier = Modifier.clickable { sendOnClick() },
            settingsIconModifier = Modifier
                .weight(.15f)
                .size(30.dp)
                .clickable {
                    isSettingsDialogShowing.value = !isSettingsDialogShowing.value
                }
        )
    }
}