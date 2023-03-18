package jr.brian.issaaiapp.view.ui.pages

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import jr.brian.issaaiapp.BuildConfig
import jr.brian.issaaiapp.R
import jr.brian.issaaiapp.model.local.Chat
import jr.brian.issaaiapp.model.remote.CachedChatBot
import jr.brian.issaaiapp.model.remote.ChatBot
import jr.brian.issaaiapp.util.ChatSection
import jr.brian.issaaiapp.util.SenderLabel
import jr.brian.issaaiapp.util.EmptyTextFieldDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatPage() {
    val scope = rememberCoroutineScope()
    val bringIntoViewRequester = BringIntoViewRequester()
    val focusManager = LocalFocusManager.current

    val formatter = DateTimeFormatter.ofPattern("h:mm a")
    val currentTime = LocalDateTime.now().format(formatter)

    var textFieldText by remember { mutableStateOf("") }
    var systemFieldText by remember { mutableStateOf(ChatBot.SARCASTIC_AI) }

    val isDialogShowing = remember { mutableStateOf(false) }
    val isAISystemFieldShowing = remember { mutableStateOf(false) }

    val chatListState = rememberLazyListState()

    val chats = remember {
        mutableStateListOf(
            Chat(
                text = ChatBot.GREETINGS.random(),
                senderLabel = SenderLabel.AI_SENDER_LABEL,
                timeStamp = currentTime
            )
        )
    }

    val sendOnClick = {
        focusManager.clearFocus()
        if (textFieldText.isEmpty()) {
            isDialogShowing.value = true
        } else {
            val prompt = textFieldText
            textFieldText = ""
            scope.launch {
                chats.add(
                    Chat(
                        text = prompt,
                        senderLabel = SenderLabel.HUMAN_SENDER_LABEL,
                        timeStamp = currentTime
                    )
                )
                chatListState.animateScrollToItem(chats.size)
                chats.add(
                    Chat(
                        text = getAIResponse(userPrompt = prompt, system = systemFieldText),
                        senderLabel = SenderLabel.AI_SENDER_LABEL,
                        timeStamp = currentTime
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
                    .weight(.7f)
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
                        style = TextStyle(
                            color = MaterialTheme.colors.primary,
                            fontWeight = FontWeight.Bold
                        )
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

            Row(
                modifier = Modifier
                    .bringIntoViewRequester(bringIntoViewRequester)
                    .weight(.3f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.send_icon),
                    tint = MaterialTheme.colors.primary,
                    contentDescription = "Send Message",
                    modifier = Modifier
                        .combinedClickable(
                            onClick = { sendOnClick() },
                            onDoubleClick = {},
                        )
                )
                Spacer(modifier = Modifier.width(10.dp))
                Icon(
                    painter = painterResource(
                        id = if (isAISystemFieldShowing.value)
                            R.drawable.baseline_keyboard_arrow_down_40
                        else R.drawable.baseline_keyboard_arrow_up_40
                    ),
                    tint = MaterialTheme.colors.primary,
                    contentDescription = "Send Message",
                    modifier = Modifier.clickable {
                        isAISystemFieldShowing.value = !isAISystemFieldShowing.value
                    }
                )
            }
        }

        if (isAISystemFieldShowing.value) {
            OutlinedTextField(
                modifier = Modifier
                    .onFocusEvent { event ->
                        if (event.isFocused) {
                            scope.launch {
                                bringIntoViewRequester.bringIntoView()
                            }
                        }
                    },
                value = systemFieldText,
                onValueChange = { text ->
                    systemFieldText = text
                },
                label = {
                    Text(
                        text = "Enter Conversational Instructions",
                        style = TextStyle(
                            color = MaterialTheme.colors.primary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                colors = TextFieldDefaults.textFieldColors(
                    focusedIndicatorColor = MaterialTheme.colors.secondary,
                    unfocusedIndicatorColor = MaterialTheme.colors.primary
                ),
            )
        }

        Spacer(Modifier.height(15.dp))
    }
}

suspend fun getAIResponse(
    userPrompt: String,
    system: String = ChatBot.SARCASTIC_AI
): String {
    val response: String
    var sys = system
    if (sys.isEmpty()) { sys = ChatBot.SARCASTIC_AI }
    withContext(Dispatchers.IO) {
        val key = BuildConfig.API_KEY
        val request = ChatBot.ChatCompletionRequest(ChatBot.MODEL, sys)
        val bot = CachedChatBot(key, request)
        response = bot.generateResponse(userPrompt)
    }
    return response
}