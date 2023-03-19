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
import jr.brian.issaaiapp.R
import jr.brian.issaaiapp.model.local.Chat
import jr.brian.issaaiapp.util.ChatConfig
import jr.brian.issaaiapp.util.ChatSection
import jr.brian.issaaiapp.util.SenderLabel
import jr.brian.issaaiapp.util.EmptyTextFieldDialog
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

    val formatter = DateTimeFormatter.ofPattern("h:mm a")
    val currentTime = LocalDateTime.now().format(formatter)

    var promptText by remember { mutableStateOf("") }
    val conversationalInstructionsText = remember {
        mutableStateOf(ChatConfig.conversationalInstructions.random())
    }

    val isDialogShowing = remember { mutableStateOf(false) }
    val isChatGptTyping = remember { mutableStateOf(false) }
    val isConvoInstructionsShowing = remember { mutableStateOf(false) }

    val chatListState = rememberLazyListState()

    val chats = remember {
        mutableStateListOf(
            Chat(
                text = ChatConfig.greetings.random(),
                senderLabel = SenderLabel.GREETING_SENDER_LABEL,
                timeStamp = currentTime
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
                        text = prompt,
                        senderLabel = SenderLabel.HUMAN_SENDER_LABEL,
                        timeStamp = currentTime
                    )
                )
                chatListState.animateScrollToItem(chats.size)
                viewModel.getAIResponse(
                    userPrompt = prompt,
                    system = conversationalInstructionsText,
                    isAITypingLabelShowing = isChatGptTyping
                )
                chats.add(
                    Chat(
                        text = viewModel.response.value ?: "No response. Please try again.",
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

        if (isChatGptTyping.value) {
            Column {
                Spacer(Modifier.height(5.dp))
                Text(
                    "ChatGPT is typing...",
                    color = MaterialTheme.colors.primary,
                    style = TextStyle(fontWeight = FontWeight.Bold)
                )
            }
        }

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
                value = promptText,
                onValueChange = { text ->
                    promptText = text
                },
                label = {
                    Text(
                        text = "Enter a prompt for ChatGPT",
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
                keyboardActions = KeyboardActions(onDone = { sendOnClick() })
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
                    modifier = Modifier.combinedClickable(
                        onClick = { sendOnClick() },
                        onDoubleClick = {},
                    )
                )
                Spacer(modifier = Modifier.width(10.dp))
                Icon(
                    painter = painterResource(
                        id = if (isConvoInstructionsShowing.value)
                            R.drawable.baseline_keyboard_arrow_down_40
                        else R.drawable.baseline_keyboard_arrow_up_40
                    ),
                    tint = MaterialTheme.colors.primary,
                    contentDescription = "Toggle Conversational Instructions",
                    modifier = Modifier.clickable {
                        isConvoInstructionsShowing.value = !isConvoInstructionsShowing.value
                    }
                )
            }
        }

        if (isConvoInstructionsShowing.value) {
            OutlinedTextField(
                modifier = Modifier
                    .onFocusEvent { event ->
                        if (event.isFocused) {
                            scope.launch {
                                bringIntoViewRequester.bringIntoView()
                            }
                        }
                    },
                value = conversationalInstructionsText.value,
                onValueChange = { text ->
                    conversationalInstructionsText.value = text
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