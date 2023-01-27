package jr.brian.issaaiapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import jr.brian.issaaiapp.ui.theme.IssaAIAppTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content))
        { view, insets ->
            val bottom = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
            view.updatePadding(bottom = bottom)
            insets
        }

        setContent {
            IssaAIAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    val focusManager = LocalFocusManager.current
                    val scope = rememberCoroutineScope()
                    val bringIntoViewRequester = BringIntoViewRequester()

                    Column(
                        modifier = Modifier.bringIntoViewRequester(bringIntoViewRequester),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val chatBubbleCount = remember { mutableStateOf(0) }

                        Spacer(Modifier.height(15.dp))

                        ChatSection(
                            modifier = Modifier.weight(.85f),
                            chatBubbleCount = chatBubbleCount
                        )

                        TextFieldSendButtonRow(modifier = Modifier
                            .weight(.15f)
                            .padding(start = 20.dp)
                            .bringIntoViewRequester(bringIntoViewRequester),
                            textFieldModifier = Modifier
                                .weight(.8f)
                                .height(300.dp)
                                .onFocusEvent { event ->
                                    if (event.isFocused) {
                                        scope.launch {
                                            bringIntoViewRequester.bringIntoView()
                                        }
                                    }
                                }, sendOnClick = {
                                chatBubbleCount.value++
                                focusManager.clearFocus()
                            }, focusManager = focusManager
                        )

                    }
                }
            }
        }
    }
}

@Composable
fun ChatSection(modifier: Modifier, chatBubbleCount: MutableState<Int>) {
    LazyColumn(modifier = modifier) {
        items(chatBubbleCount.value) {
            ChatBox(
                color = if (it % 2 == 0) Color(0xFF7BAFB0)
                else Color(0xFFAF7C7B)
            )
            Spacer(Modifier.height(15.dp))
        }
    }
}

@Composable
fun TextFieldSendButtonRow(
    modifier: Modifier,
    textFieldModifier: Modifier,
    focusManager: FocusManager,
    sendOnClick: () -> Unit
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(
            modifier = textFieldModifier, focusManager = focusManager
        )
        Icon(
            painter = painterResource(id = R.drawable.ic_baseline_send_24),
            contentDescription = "Send Message",
            modifier = Modifier
                .weight(.2f)
                .clickable {
                    sendOnClick()
                }
        )
    }
}

@Composable
fun TextField(modifier: Modifier, focusManager: FocusManager) {
    var text by remember { mutableStateOf("") }
    OutlinedTextField(
        modifier = modifier,
        value = text,
        onValueChange = { text = it },
        label = {
            Text("Enter Prompt")
        },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
    )
}

@Composable
fun ChatBox(color: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
            .background(color)
    ) {
        Text(
            "ffffcvbghreiugherughioudfhgiudfhgiudfhgiudfhgiudfhgi" +
                    "oudfhgioudfhgioudfhigoufhdiugdfgufhjhgj" +
                    "ghjghjghjghjghjghjghjgh" +
                    "jghjhjgjghjghjghjghjghjghjghjghjghj",
            modifier = Modifier.padding(15.dp)
        )
    }
}