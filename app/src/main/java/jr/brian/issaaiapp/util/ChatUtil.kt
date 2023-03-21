package jr.brian.issaaiapp.util

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.*
import jr.brian.issaaiapp.R
import jr.brian.issaaiapp.model.local.Chat
import jr.brian.issaaiapp.model.local.ChatsDao
import jr.brian.issaaiapp.view.ui.theme.AIChatBoxColor
import jr.brian.issaaiapp.view.ui.theme.HumanChatBoxColor
import jr.brian.issaaiapp.view.ui.theme.TextWhite
import java.time.LocalDateTime

@Composable
fun LottieLoading(isChatGptTyping: MutableState<Boolean>) {
    val isPlaying by remember { mutableStateOf(isChatGptTyping.value) }
    val speed by remember { mutableStateOf(1f) }
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.loading)
    )
    val progress by animateLottieCompositionAsState(
        composition,
        iterations = LottieConstants.IterateForever,
        isPlaying = isPlaying,
        speed = speed,
        restartOnPlay = false
    )
    LottieAnimation(
        composition,
        progress,
        modifier = Modifier.size(40.dp)
    )
}

@Composable
fun ChatHeader(modifier: Modifier, isChatGptTyping: MutableState<Boolean>) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
    ) {
        if (isChatGptTyping.value) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    "ChatGPT is typing",
                    color = MaterialTheme.colors.primary,
                    style = TextStyle(fontWeight = FontWeight.Bold)
                )
                LottieLoading(isChatGptTyping)
            }
        }
        if (isChatGptTyping.value.not()) {
            Text(
                "Issa AI App x ChatGPT",
                color = MaterialTheme.colors.primary,
                style = TextStyle(fontWeight = FontWeight.Bold)
            )
        }
    }
}

@Composable
fun ChatSection(modifier: Modifier, chats: MutableList<Chat>, listState: LazyListState) {
    val context = LocalContext.current
    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    val copyToastMsgs = listOf(
        "Your copy is ready for pasta!",
        "What are you waiting for? Paste!",
        "Your clipboard has been blessed.",
        "Chat copied!",
        "Copied, the chat has been."
    )
    LazyColumn(modifier = modifier, state = listState) {
        items(chats.size) { index ->
            ChatBox(
                text = chats[index].text,
                senderLabel = chats[index].senderLabel,
                dateSent = chats[index].dateSent,
                timeSent = chats[index].timeSent,
                isHumanChatBox = chats[index].senderLabel == SenderLabel.HUMAN_SENDER_LABEL
            ) {
                clipboardManager.setText(AnnotatedString((chats[index].text)))
                Toast.makeText(
                    context,
                    copyToastMsgs.random(),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}

@Composable
fun ChatTextFieldRows(
    promptText: String,
    convoContextText: MutableState<String>,
    sendOnClick: () -> Unit,
    textFieldOnValueChange: (String) -> Unit,
    convoContextOnValueChange: (String) -> Unit,
    isConvoContextFieldShowing: MutableState<Boolean>,
    modifier: Modifier,
    textFieldModifier: Modifier,
    convoContextFieldModifier: Modifier,
    iconRowModifier: Modifier,
    sendIconModifier: Modifier,
    settingsIconModifier: Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.baseline_settings_40),
            tint = MaterialTheme.colors.primary,
            contentDescription = "Toggle Conversational Context",
            modifier = settingsIconModifier
        )
        OutlinedTextField(
            modifier = textFieldModifier,
            value = promptText,
            onValueChange = textFieldOnValueChange,
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
            modifier = iconRowModifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.send_icon),
                tint = MaterialTheme.colors.primary,
                contentDescription = "Send Message",
                modifier = sendIconModifier
            )
            Spacer(modifier = Modifier.width(10.dp))
            Icon(
                painter = painterResource(
                    id = if (isConvoContextFieldShowing.value)
                        R.drawable.baseline_keyboard_arrow_down_40
                    else R.drawable.baseline_keyboard_arrow_up_40
                ),
                tint = MaterialTheme.colors.primary,
                contentDescription = "Toggle Conversational Context",
                modifier = Modifier.clickable {
                    isConvoContextFieldShowing.value = !isConvoContextFieldShowing.value
                }
            )
        }
    }

    if (isConvoContextFieldShowing.value) {
        OutlinedTextField(
            modifier = convoContextFieldModifier,
            value = convoContextText.value,
            onValueChange = convoContextOnValueChange,
            label = {
                Text(
                    text = "Enter Conversational Context",
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
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ChatBox(
    text: String,
    senderLabel: String,
    dateSent: String,
    timeSent: String,
    isHumanChatBox: Boolean,
    onLongCLick: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    val color = if (isHumanChatBox) HumanChatBoxColor else AIChatBoxColor
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(.8f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp)
                        .background(color)
                        .combinedClickable(
                            onClick = { focusManager.clearFocus() },
                            onLongClick = { onLongCLick() },
                            onDoubleClick = {},
                        )
                ) {
                    Text(
                        text,
                        style = TextStyle(color = TextWhite),
                        modifier = Modifier.padding(15.dp)
                    )
                }
                Row() {
                    Text(
                        senderLabel,
                        style = senderAndTimeStyle(color),
                    )
                    Spacer(Modifier.width(5.dp))
                    Text("•", style = senderAndTimeStyle(color))
                    Spacer(Modifier.width(5.dp))
                    Text(
                        dateSent,
                        style = senderAndTimeStyle(color),
                    )
                    Spacer(Modifier.width(5.dp))
                    Text("•", style = senderAndTimeStyle(color))
                    Spacer(Modifier.width(5.dp))
                    Text(
                        timeSent,
                        style = senderAndTimeStyle(color),
                    )
                }
            }
        }
    }
}

fun autoGreet(
    chats: SnapshotStateList<Chat>,
    hasBeenGreeted: MutableState<Boolean>,
    dateSent: String,
    timeSent: String,
    dao: ChatsDao
) {
    if (chats.isEmpty() || !hasBeenGreeted.value) {
        val chat = Chat(
            fullTimeStamp = LocalDateTime.now().toString(),
            text = ChatConfig.greetings.random(),
            senderLabel = SenderLabel.GREETING_SENDER_LABEL,
            dateSent = dateSent,
            timeSent = timeSent
        )
        chats.add(chat)
        dao.insertChat(chat)
        hasBeenGreeted.value = true
    }
}

private fun senderAndTimeStyle(color: Color) = TextStyle(
    fontSize = 15.sp,
    fontWeight = FontWeight.Bold,
    color = color
)

object SenderLabel {
    const val HUMAN_SENDER_LABEL = "Me"
    const val CHATGPT_SENDER_LABEL = "ChatGPT"
    const val GREETING_SENDER_LABEL = "Greetings"
}

object ChatConfig {
    const val GPT_3_5_TURBO = "gpt-3.5-turbo"

    private val aiAdjectives = listOf(
        "Sarcastic",
        "Helpful",
        "Unhelpful",
        "Optimistic",
        "Pessimistic",
        "Excited",
        "Joyful",
        "Charming",
        "Inspiring",
        "Nonchalant",
        "Relaxed",
        "Loud",
        "Annoyed"
    )

    private val randomChatGptAdjective = aiAdjectives.random()

    val conversationalContext = listOf(
        "Be as ${randomChatGptAdjective.lowercase()} as possible.",
        "You are my ${randomChatGptAdjective.lowercase()} assistant",
        "Play the role of the ${randomChatGptAdjective.lowercase()} bot",
        "Act as if you are extremely ${randomChatGptAdjective.lowercase()}",
        "Act as if you are the only ${randomChatGptAdjective.lowercase()} AI"
    )

    val greetings = listOf(
        "What's good my human friend?",
        "You? Again? \uD83D\uDE43", // Upside down face emoji
        "How are you doing today?",
        "How may I help you today?",
        "Assuhh dude \uD83D\uDE0E", // Cool emoji; black shades
        "Hi Human.",
        "Ah, here we go again \uD83E\uDD26" // Facepalm emoji
    )
}