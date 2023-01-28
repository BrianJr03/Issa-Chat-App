package jr.brian.issaaiapp.view.ui.pages

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import jr.brian.issaaiapp.R
import jr.brian.issaaiapp.model.local.Chat
import jr.brian.issaaiapp.util.chat.ChatSection
import jr.brian.issaaiapp.util.chat.EmptyTextFieldDialog
import jr.brian.issaaiapp.util.chat.SenderLabel
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatPage() {
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()
    val bringIntoViewRequester = BringIntoViewRequester()

    val formatter = DateTimeFormatter.ofPattern("h:mm a")
    val currentTime = LocalDateTime.now().format(formatter)

    var textFieldText by remember { mutableStateOf("") }
    val isDialogShowing = remember { mutableStateOf(false) }

    val chats = remember {
        mutableStateListOf(
            Chat("Hi Bot", SenderLabel.HUMAN_SENDER_LABEL, currentTime),
            Chat("Hi Human", SenderLabel.AI_SENDER_LABEL, currentTime),
        )
    }

    val sendOnClick = {
        if (textFieldText.isEmpty()) {
            isDialogShowing.value = true
        } else {
            chats.add(
                Chat(
                    text = textFieldText,
                    sender = SenderLabel.HUMAN_SENDER_LABEL,
                    timeStamp = currentTime
                )
            )
            textFieldText = ""
            focusManager.clearFocus()
        }
    }
    
    EmptyTextFieldDialog(isShowing = isDialogShowing)

    Column(
        modifier = Modifier.bringIntoViewRequester(bringIntoViewRequester),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(Modifier.height(15.dp))

        ChatSection(
            modifier = Modifier.weight(.85f),
            chats = chats
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
                onValueChange = { textFieldText = it },
                label = {
                    Text("Enter Prompt")
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    sendOnClick()
                })
            )
            Icon(
                painter = painterResource(id = R.drawable.ic_baseline_send_24),
                contentDescription = "Send Message",
                modifier = Modifier
                    .weight(.2f)
                    .clickable { sendOnClick() }
            )
        }

        Spacer(Modifier.height(15.dp))
    }
}