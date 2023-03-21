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
import jr.brian.issaaiapp.model.local.MyDataStore
import jr.brian.issaaiapp.model.remote.ApiService
import jr.brian.issaaiapp.util.*
import jr.brian.issaaiapp.viewmodel.MainViewModel
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatPage(dao: ChatsDao, dataStore: MyDataStore, viewModel: MainViewModel = hiltViewModel()) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val bringIntoViewRequester = BringIntoViewRequester()
    val focusManager = LocalFocusManager.current

    val storedApiKey = dataStore.getApiKey.collectAsState(initial = "").value ?: ""
    val storedIsAutoConvoContextToggled =
        dataStore.getIsAutoConvoContextToggled.collectAsState(initial = false).value ?: false
    val storedIsAutoGreetToggled =
        dataStore.getIsAutoGreetToggled.collectAsState(initial = false).value ?: false
    var promptText by remember { mutableStateOf("") }
    var apiKeyText by remember { mutableStateOf("") }
    val conversationalContextText = remember {
        mutableStateOf(ChatConfig.conversationalContext.random())
    }

    val isErrorDialogShowing = remember { mutableStateOf(false) }
    val isSettingsDialogShowing = remember { mutableStateOf(false) }
    val isAutoConvoContextToggled = remember { mutableStateOf(storedIsAutoConvoContextToggled) }
    val isAutoGreetToggled = remember { mutableStateOf(storedIsAutoGreetToggled) }
    val isChatGptTyping = remember { mutableStateOf(false) }
    val isConversationalContextShowing = remember { mutableStateOf(false) }
    val hasBeenGreeted = remember { mutableStateOf(false) }

    val dateTimeFormatter = DateTimeFormatter.ofPattern("h:mm a")
    val dateFormatter = DateTimeFormatter.ofPattern("MM.dd.yy")
    val timeSent: String = LocalDateTime.now().format(dateTimeFormatter)
    val dateSent: String = LocalDateTime.now().format(dateFormatter)

    val chatListState = rememberLazyListState()
    val chats = remember { dao.getChats().toMutableStateList() }

    LaunchedEffect(key1 = 1, block = {
        chatListState.scrollToItem(chats.size)
    })

    if (storedIsAutoGreetToggled) {
        autoGreet(
            chats = chats,
            hasBeenGreeted = hasBeenGreeted,
            dateSent = dateSent,
            timeSent = timeSent,
            dao = dao
        )
    }

    if (storedIsAutoConvoContextToggled) {
        conversationalContextText.value = ""
    }

    val sendOnClick = {
        focusManager.clearFocus()
        if (storedApiKey.isEmpty()) {
            isSettingsDialogShowing.value = true
            Toast.makeText(
                context,
                "API Key is required",
                Toast.LENGTH_LONG
            ).show()
        } else if (promptText.isEmpty()) {
            isErrorDialogShowing.value = true
        } else {
            val prompt = promptText
            promptText = ""
            scope.launch {
                val myChat = Chat(
                    fullTimeStamp = LocalDateTime.now().toString(),
                    text = prompt,
                    senderLabel = SenderLabel.HUMAN_SENDER_LABEL,
                    dateSent = dateSent,
                    timeSent = timeSent
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
                    fullTimeStamp = LocalDateTime.now().toString(),
                    text = viewModel.response.value ?: "No response. Please try again.",
                    senderLabel = SenderLabel.CHATGPT_SENDER_LABEL,
                    dateSent = dateSent,
                    timeSent = timeSent
                )
                chats.add(chatGptChat)
                dao.insertChat(chatGptChat)
                chatListState.animateScrollToItem(chats.size)
            }
        }
    }

    EmptyTextFieldDialog(title = "Please provide a prompt", isShowing = isErrorDialogShowing)

    SettingsDialog(
        apiKey = apiKeyText.ifEmpty { storedApiKey },
        textFieldOnValueChange = { text ->
            apiKeyText = text
            scope.launch { dataStore.saveApiKey(apiKeyText) }
            ApiService.ApiKey.userApiKey = apiKeyText
        },
        isShowing = isSettingsDialogShowing,
        showChatsDeletionWarning = {
            Toast.makeText(
                context,
                "Long-press to delete all chats",
                Toast.LENGTH_LONG
            ).show()
        },
        onClearApiKey = {
            scope.launch {
                dataStore.saveApiKey("")
                apiKeyText = ""
                ApiService.ApiKey.userApiKey = ""
            }
        },
        showClearApiKeyWarning = {
            Toast.makeText(
                context,
                "Long-press to clear your API Key",
                Toast.LENGTH_LONG
            ).show()
        },
        onDeleteAllChats = {
            chats.clear()
            dao.removeAllChats()
            Toast.makeText(context, "Chats deleted!", Toast.LENGTH_LONG).show()
        },
        modifier = Modifier,
        textFieldModifier = Modifier,
        isAutoConvoContextToggled = storedIsAutoConvoContextToggled,
        isAutoGreetToggled = storedIsAutoGreetToggled,
        onAutoConvoCheckedChange = {
            isAutoConvoContextToggled.value = it
            scope.launch { dataStore.saveIsAutoConvoContextToggled(isAutoConvoContextToggled.value) }
        },
        onAutoGreetCheckedChange = {
            isAutoGreetToggled.value = it
            scope.launch { dataStore.saveIsAutoGreetToggles(isAutoGreetToggled.value) }
        }
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
            sendIconModifier = Modifier
                .size(30.dp)
                .clickable { sendOnClick() },
            settingsIconModifier = Modifier
                .weight(.15f)
                .size(30.dp)
                .clickable {
                    isSettingsDialogShowing.value = !isSettingsDialogShowing.value
                }
        )

        Spacer(modifier = Modifier.height(15.dp))
    }
}