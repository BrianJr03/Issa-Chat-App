package jr.brian.issaaiapp.view.ui.components

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import jr.brian.issaaiapp.R
import jr.brian.issaaiapp.model.local.ChatsDao
import jr.brian.issaaiapp.model.local.Conversation
import jr.brian.issaaiapp.util.ChatConfig
import jr.brian.issaaiapp.util.saveConversationToPDF
import jr.brian.issaaiapp.util.saveConversationToJson
import jr.brian.issaaiapp.view.ui.theme.*
import kotlinx.coroutines.launch

@Composable
private fun ShowDialog(
    title: String,
    modifier: Modifier = Modifier,
    titleColor: Color = TextWhite,
    backgroundColor: Color,
    content: @Composable (() -> Unit)?,
    confirmButton: @Composable () -> Unit,
    dismissButton: @Composable () -> Unit,
    isShowing: MutableState<Boolean>,
) {
    if (isShowing.value) {
        AlertDialog(
            title = { Text(title, fontSize = 22.sp, color = titleColor) },
            text = content,
            confirmButton = confirmButton,
            dismissButton = dismissButton,
            onDismissRequest = { isShowing.value = false },
            modifier = modifier,
            backgroundColor = backgroundColor
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ConversationsDialog(
    isShowing: MutableState<Boolean>,
    primaryColor: MutableState<Color>,
    secondaryColor: MutableState<Color>,
    isAmoledThemeToggled: MutableState<Boolean>,
    conversations: SnapshotStateList<Conversation>,
    conversationText: MutableState<String>,
    modifier: Modifier = Modifier,
    onSaveClick: () -> Unit,
    onSelectItem: (String) -> Unit,
    onDeleteItem: (String) -> Unit
) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val isDeleteIconShowing = remember { mutableStateOf(false) }

    if (!isShowing.value) {
        isDeleteIconShowing.value = false
    }

    ShowDialog(
        title = "",
        titleColor = TextWhite,
        backgroundColor = primaryColor.value,
        modifier = modifier,
        content = {
            Column {
                Text(
                    "Conversations",
                    color = TextWhite,
                    style = TextStyle(fontSize = 22.sp)
                )

                Box(modifier = Modifier.height(10.dp))

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = conversationText.value,
                        onValueChange = { text ->
                            conversationText.value = text
                        },
                        label = {
                            Text(
                                text = "New Conversation Name ",
                                style = TextStyle(
                                    color = TextWhite,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        },
                        colors = TextFieldDefaults.textFieldColors(
                            textColor = TextWhite,
                            focusedIndicatorColor = primaryColor.value,
                            unfocusedIndicatorColor = secondaryColor.value
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(5.dp))

                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            backgroundColor = secondaryColor.value
                        ),
                        onClick = {
                            onSaveClick()
                            scope.launch {
                                listState.animateScrollToItem(conversations.size)
                            }
                        }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_check_24),
                            tint = if (isAmoledThemeToggled.value) Color.Black else TextWhite,
                            contentDescription = "Save"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(state = listState) {
                    items(conversations.size) { index ->
                        val currentConversation = conversations.reversed()[index]
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .combinedClickable(
                                    onClick = {
                                        onSelectItem(currentConversation.conversationName)
                                    },
                                    onLongClick = {
                                        isDeleteIconShowing.value = !isDeleteIconShowing.value
                                    }),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                currentConversation.conversationName,
                                color = TextWhite,
                                style = TextStyle(fontSize = 16.sp),
                                modifier = Modifier.padding(top = 16.dp, bottom = 16.dp)
                            )
                            Spacer(modifier = Modifier.weight(1f))

                            if (isDeleteIconShowing.value) {
                                Icon(
                                    painter = painterResource(id = R.drawable.baseline_delete_24),
                                    "Delete Conversation",
                                    tint = TextWhite,
                                    modifier = Modifier.clickable {
                                        onDeleteItem(currentConversation.conversationName)
                                    }
                                )
                            }
                        }
                        if (index != conversations.size - 1) {
                            Divider(color = TextWhite)
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {},
        isShowing = isShowing
    )
}

@Composable
fun ExportDialog(
    isShowing: MutableState<Boolean>,
    primaryColor: MutableState<Color>,
    secondaryColor: MutableState<Color>,
    isAmoledThemeToggled: MutableState<Boolean>,
    dao: ChatsDao,
    conversations: SnapshotStateList<Conversation>
) {
    val context = LocalContext.current
    val selectedConversationName = remember { mutableStateOf("") }
    val isExportConfirmShowing = remember { mutableStateOf(false) }
    val isDownloaded = remember { mutableStateOf(false) }
    val btnTextColor = if (isAmoledThemeToggled.value) Color.Black else Color.White

    ShowDialog(
        title = "Download",
        backgroundColor = primaryColor.value,
        content = {
            Column {
                Text(
                    text = "Download a PDF and JSON of " +
                            "'${selectedConversationName.value}'",
                    fontSize = 16.sp,
                    color = TextWhite
                )
                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    colors = ButtonDefaults.outlinedButtonColors(
                        backgroundColor = secondaryColor.value
                    ),
                    onClick = {
                        val chats = dao.getChatsByConvo(selectedConversationName.value)
                        saveConversationToJson(
                            list = chats,
                            filename = "${selectedConversationName.value}.json"
                        )
                        saveConversationToPDF(
                            conversationName = selectedConversationName.value,
                            chats = chats
                        )
                        Toast.makeText(
                            context,
                            "Downloaded! Check your downloads folder.",
                            Toast.LENGTH_LONG
                        ).show()
                        isDownloaded.value = true
                        isExportConfirmShowing.value = false
                        isShowing.value = false
                    }, modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Download", color = btnTextColor)
                }
            }
        },
        confirmButton = {
            Button(
                colors = ButtonDefaults.outlinedButtonColors(
                    backgroundColor = primaryColor.value
                ),
                onClick = {
                    isExportConfirmShowing.value = false
                }) {
                Text(text = "Dismiss", color = Color.White)
            }
        },
        dismissButton = {

        },
        isShowing = isExportConfirmShowing
    )

    ShowDialog(
        title = "Select Conversation",
        backgroundColor = primaryColor.value,
        content = {
            if (conversations.isEmpty()) {
                Text(text = "No Conversations", color = Color.White)
            } else {
                LazyColumn {
                    items(conversations.size) { index ->
                        val currentConversation = conversations.reversed()[index]
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedConversationName.value =
                                        currentConversation.conversationName
                                    isExportConfirmShowing.value = true
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                currentConversation.conversationName,
                                color = TextWhite,
                                style = TextStyle(fontSize = 16.sp),
                                modifier = Modifier.padding(top = 16.dp, bottom = 16.dp)
                            )
                        }
                        if (index != conversations.size - 1) {
                            Divider(color = TextWhite)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                colors = ButtonDefaults.outlinedButtonColors(
                    backgroundColor = primaryColor.value
                ),
                onClick = {
                    isShowing.value = false
                }) {
                Text(text = "Dismiss", color = Color.White)
            }
        },
        dismissButton = {

        },
        isShowing = isShowing
    )
}

@Composable
fun ThemeDialog(
    isShowing: MutableState<Boolean>,
    primaryColor: MutableState<Color>,
    isThemeOneToggled: Boolean,
    isThemeTwoToggled: Boolean,
    isThemeThreeToggled: Boolean,
    isAmoledThemeToggled: Boolean,
    modifier: Modifier = Modifier,
    onThemeOneChange: ((Boolean) -> Unit)?,
    onThemeTwoChange: ((Boolean) -> Unit)?,
    onThemeThreeChange: ((Boolean) -> Unit)?,
    onAmoledThemeChange: ((Boolean) -> Unit)?,
) {
    ShowDialog(
        title = "Select App Theme",
        modifier = modifier,
        titleColor = Color.White,
        backgroundColor = Color.DarkGray,
        content = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                ThemeRow(
                    primaryColor = DefaultPrimaryColor,
                    secondaryColor = DefaultSecondaryColor,
                    isThemeToggled = isThemeOneToggled,
                    onThemeChange = onThemeOneChange
                )
                Spacer(modifier = Modifier.height(10.dp))
                ThemeRow(
                    primaryColor = ThemeTwoPrimary,
                    secondaryColor = ThemeTwoSecondary,
                    isThemeToggled = isThemeTwoToggled,
                    onThemeChange = onThemeTwoChange
                )
                Spacer(modifier = Modifier.height(10.dp))
                ThemeRow(
                    primaryColor = ThemeThreePrimary,
                    secondaryColor = ThemeThreeSecondary,
                    isThemeToggled = isThemeThreeToggled,
                    onThemeChange = onThemeThreeChange
                )
                Spacer(modifier = Modifier.height(10.dp))
                ThemeRow(
                    primaryColor = Color.Black,
                    secondaryColor = Color.White,
                    isThemeToggled = isAmoledThemeToggled,
                    onThemeChange = onAmoledThemeChange
                )
            }
        },
        confirmButton = {
            Button(
                colors = ButtonDefaults.outlinedButtonColors(
                    backgroundColor = primaryColor.value
                ),
                onClick = {
                    isShowing.value = false
                }) {
                Text(text = "Dismiss", color = Color.White)
            }
        },
        dismissButton = {},
        isShowing = isShowing
    )
}

@Composable
private fun ThemeRow(
    primaryColor: Color,
    secondaryColor: Color,
    isThemeToggled: Boolean,
    modifier: Modifier = Modifier,
    onThemeChange: ((Boolean) -> Unit)?,
) {

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = isThemeToggled,
            onCheckedChange = onThemeChange
        )
        Spacer(modifier = Modifier.width(25.dp))
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(primaryColor)
        )
        Spacer(modifier = Modifier.width(25.dp))
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(secondaryColor)
        )
    }
}

@Composable
fun DeleteDialog(
    title: String,
    isShowing: MutableState<Boolean>,
    primaryColor: MutableState<Color>,
    modifier: Modifier = Modifier,
    onDeleteClick: () -> Unit
) {
    ShowDialog(
        title = title,
        modifier = modifier,
        titleColor = TextWhite,
        backgroundColor = primaryColor.value,
        content = {
            Column {
                Text(
                    "This can't be undone.",
                    fontSize = 16.sp,
                    color = TextWhite
                )
            }
        },
        confirmButton = {
            Button(
                colors = ButtonDefaults.outlinedButtonColors(
                    backgroundColor = primaryColor.value
                ),
                onClick = {
                    onDeleteClick()
                    isShowing.value = false
                }) {
                Text(text = "Delete", color = Color.White)
            }
        },
        dismissButton = {
            Button(
                colors = ButtonDefaults.outlinedButtonColors(
                    backgroundColor = primaryColor.value
                ),
                onClick = {
                    isShowing.value = false
                }) {
                Text(text = "Cancel", color = Color.White)
            }
        },
        isShowing = isShowing
    )
}

@Composable
fun HowToUseDialog(
    isShowing: MutableState<Boolean>,
    primaryColor: MutableState<Color>,
    modifier: Modifier = Modifier,
) {
    ShowDialog(
        title = "How to use",
        modifier = modifier,
        titleColor = TextWhite,
        backgroundColor = primaryColor.value,
        content = {
            Column {
                Text(
                    "You must provide your own OpenAI API Key in Settings to use this app.",
                    fontSize = 17.sp,
                    color = TextWhite
                )
                Spacer(modifier = Modifier.height(10.dp))
                Divider(color = TextWhite)
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    "Single Tap to toggle a Chat's date and time",
                    fontSize = 16.sp,
                    color = TextWhite
                )
                Spacer(modifier = Modifier.height(10.dp))
                Divider(color = TextWhite)
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    "Double Tap to play the Chat's text as audio",
                    fontSize = 16.sp,
                    color = TextWhite
                )
                Spacer(modifier = Modifier.height(10.dp))
                Divider(color = TextWhite)
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    "Long Press to copy the Chat's text",
                    fontSize = 16.sp,
                    color = TextWhite
                )
                Spacer(modifier = Modifier.height(10.dp))
                Divider(color = TextWhite)
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    "Long Press to delete a Conversation",
                    fontSize = 16.sp,
                    color = TextWhite
                )
                Spacer(modifier = Modifier.height(10.dp))
                Divider(color = TextWhite)
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    "Conversational Context: Use this to have ChatGPT respond a certain way.",
                    fontSize = 16.sp,
                    color = TextWhite
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    "\"${ChatConfig.conversationalContext.random()}\"",
                    color = TextWhite,
                    fontSize = 16.sp,
                )
            }
        },
        confirmButton = {
            Button(
                colors = ButtonDefaults.outlinedButtonColors(
                    backgroundColor = primaryColor.value
                ),
                onClick = {
                    isShowing.value = false
                }) {
                Text(text = "Dismiss", color = Color.White)
            }
        },
        dismissButton = {},
        isShowing = isShowing
    )
}

@Composable
fun EmptyPromptDialog(
    isShowing: MutableState<Boolean>,
    primaryColor: MutableState<Color>,
    modifier: Modifier = Modifier
) {
    ShowDialog(
        title = "Please provide a prompt",
        modifier = modifier,
        titleColor = TextWhite,
        backgroundColor = primaryColor.value,
        content = {
            Column {
                Text(
                    "The text field can not be empty.",
                    fontSize = 16.sp,
                    color = TextWhite
                )
            }
        },
        confirmButton = {
            Button(
                colors = ButtonDefaults.outlinedButtonColors(
                    backgroundColor = primaryColor.value
                ),
                onClick = {
                    isShowing.value = false
                }) {
                Text(text = "Dismiss", color = Color.White)
            }
        },
        dismissButton = {},
        isShowing = isShowing
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SettingsDialog(
    primaryColor: MutableState<Color>,
    apiKey: String,
    apiKeyOnValueChange: (String) -> Unit,
    humanSenderLabel: String,
    senderLabelOnValueChange: (String) -> Unit,
    isShowing: MutableState<Boolean>,
    isAmoledThemeToggled: MutableState<Boolean>,
    modifier: Modifier = Modifier,
    showChatsDeletionWarning: () -> Unit,
    onClearApiKey: () -> Unit,
    showClearApiKeyWarning: () -> Unit,
    onResetAllChats: () -> Unit,
    isAutoSpeakToggled: Boolean,
    onAutoSpeakCheckedChange: ((Boolean) -> Unit)?,
) {
    val checkedColor = Color.Gray
    val focusedColor = Color.White
    val unfocusedColor = if (isAmoledThemeToggled.value) Color.White else Color.White

    ShowDialog(
        title = "Settings",
        modifier = modifier,
        titleColor = TextWhite,
        backgroundColor = primaryColor.value,
        content = {
            Spacer(modifier = Modifier.height(15.dp))
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {

                Text(
                    text = "Clear API Key",
                    color = TextWhite,
                    style = TextStyle(fontSize = 20.sp),
                    modifier = Modifier.combinedClickable(
                        onClick = {
                            showClearApiKeyWarning()
                        },
                        onLongClick = {
                            onClearApiKey()
                        }
                    ))

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Reset All Conversations",
                    color = TextWhite,
                    style = TextStyle(fontSize = 20.sp),
                    modifier = Modifier.combinedClickable(
                        onClick = {
                            showChatsDeletionWarning()
                        },
                        onLongClick = {
                            onResetAllChats()
                        }
                    ))

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isAutoSpeakToggled,
                        colors = CheckboxDefaults.colors(
                            checkedColor = checkedColor,
                            uncheckedColor = unfocusedColor
                        ),
                        onCheckedChange = onAutoSpeakCheckedChange
                    )
                    Text("Auto-play incoming Chat audio", color = TextWhite)
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = humanSenderLabel,
                    onValueChange = senderLabelOnValueChange,
                    label = {
                        Text(
                            text = "Custom Sender Label",
                            style = TextStyle(
                                color = TextWhite,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    },
                    colors = TextFieldDefaults.textFieldColors(
                        textColor = TextWhite,
                        focusedIndicatorColor = focusedColor,
                        unfocusedIndicatorColor = unfocusedColor
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        isShowing.value = false
                    })
                )

                Spacer(modifier = Modifier.height(25.dp))

                OutlinedTextField(
                    value = apiKey,
                    onValueChange = apiKeyOnValueChange,
                    label = {
                        Text(
                            text = "Your OpenAI API Key goes here",
                            style = TextStyle(
                                color = TextWhite,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    },
                    colors = TextFieldDefaults.textFieldColors(
                        textColor = TextWhite,
                        focusedIndicatorColor = focusedColor,
                        unfocusedIndicatorColor = unfocusedColor
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        isShowing.value = false
                    })
                )
            }
        },
        confirmButton = {
            Button(
                colors = ButtonDefaults.outlinedButtonColors(
                    backgroundColor = primaryColor.value
                ),
                onClick = {
                    isShowing.value = false
                }) {
                Text(text = "Dismiss", color = Color.White)
            }
        },
        dismissButton = {},
        isShowing = isShowing
    )
}