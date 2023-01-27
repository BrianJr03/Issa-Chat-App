package jr.brian.issaaiapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import jr.brian.issaaiapp.ui.theme.IssaAIAppTheme
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

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

                        Spacer(Modifier.height(15.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ChatSection(modifier: Modifier, chatBubbleCount: MutableState<Int>) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    LazyColumn(modifier = modifier, state = listState) {
        scope.launch { listState.animateScrollToItem(chatBubbleCount.value) }
        items(chatBubbleCount.value) {
            ChatBox(isFromAI = it % 2 == 0)
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
        var text by remember { mutableStateOf("") }
        OutlinedTextField(
            modifier = textFieldModifier,
            value = text,
            onValueChange = { text = it },
            label = {
                Text("Enter Prompt")
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
        )
        Icon(
            painter = painterResource(id = R.drawable.ic_baseline_send_24),
            contentDescription = "Send Message",
            modifier = Modifier
                .weight(.2f)
                .clickable { sendOnClick() }
        )
    }
}

@Composable
fun ChatBox(isFromAI: Boolean) {
    val focusManager = LocalFocusManager.current
    val formatter = DateTimeFormatter.ofPattern("h:mm a")
    val currentTime = LocalDateTime.now().format(formatter)
    if (isFromAI) {
        HumanChatBox(focusManager = focusManager, timeStamp = currentTime)
    } else {
        AIChatBox(focusManager = focusManager, timeStamp = currentTime)
    }
}

@Composable
fun AIChatBox(focusManager: FocusManager, timeStamp: String) {
    val color = Color(0xFF7BAFB0)
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(10.dp)) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(.2f)
        ) {
            Text(
                "AI",
                style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = color),
            )
            Text(
                timeStamp,
                style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = color),
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
                .background(color)
                .weight(.8f)
                .clickable { focusManager.clearFocus() }
        ) {
            Text(
                "Hi, I am AI. Hi, I am AI. Hi, I am AI. Hi, I am AI. Hi, I am AI." +
                        "Hi, I am AI. Hi, I am AI. Hi, I am AI. Hi, I am AI. Hi, I am AI." +
                        "Hi, I am AI. Hi, I am AI. Hi, I am AI. Hi, I am AI. Hi, I am AI.",
                modifier = Modifier.padding(15.dp)
            )
        }
    }
}

@Composable
fun HumanChatBox(focusManager: FocusManager, timeStamp: String) {
    val color = Color(0xFFAF7C7B)
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(10.dp)) {
        Box(
            modifier = Modifier
                .weight(.8f)
                .fillMaxWidth()
                .padding(10.dp)
                .background(color)
                .clickable { focusManager.clearFocus() }
        ) {
            Text(
                "Hi, I am ME. Hi, I am ME. Hi, I am ME. Hi, I am ME. Hi, I am ME." +
                        "Hi, I am ME. Hi, I am ME. Hi, I am ME. Hi, I am ME. Hi, I am ME." +
                        "Hi, I am ME. Hi, I am ME. Hi, I am ME. Hi, I am ME. Hi, I am ME.",
                modifier = Modifier.padding(15.dp)
            )
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(.2f)
        ) {
            Text(
                "Me",
                style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = color),
            )
            Text(
                timeStamp,
                style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = color),
            )
        }
    }
}