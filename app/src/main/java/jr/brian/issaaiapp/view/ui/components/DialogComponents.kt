package jr.brian.issaaiapp.view.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import jr.brian.issaaiapp.util.ChatConfig
import jr.brian.issaaiapp.view.ui.theme.CardinalRed
import jr.brian.issaaiapp.view.ui.theme.TextWhite

@Composable
private fun ShowDialog(
    title: String,
    titleColor: Color = TextWhite,
    content: @Composable (() -> Unit)?,
    confirmButton: @Composable () -> Unit,
    dismissButton: @Composable () -> Unit,
    isShowing: MutableState<Boolean>
) {
    if (isShowing.value) {
        AlertDialog(
            title = { Text(title, fontSize = 22.sp, color = titleColor) },
            text = content,
            confirmButton = confirmButton,
            dismissButton = dismissButton,
            onDismissRequest = { isShowing.value = false },
        )
    }
}

@Composable
fun HowToUseDialog(isShowing: MutableState<Boolean>) {
    ShowDialog(
        title = "How to use",
        titleColor = MaterialTheme.colors.primary,
        content = {
            Column {
                Text(
                    "You must provide your own OpenAI API Key in Settings to use this app.",
                    fontSize = 17.sp,
                )

                Spacer(modifier = Modifier.height(10.dp))

                Divider()

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    "Single Tap to toggle a Chat's date and time",
                    fontSize = 16.sp,
                )

                Spacer(modifier = Modifier.height(10.dp))

                Divider()

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    "Double Tap to play the Chat's text as audio",
                    fontSize = 16.sp,
                )

                Spacer(modifier = Modifier.height(10.dp))

                Divider()

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    "Long Tap to copy the Chat's text",
                    fontSize = 16.sp,
                )

                Spacer(modifier = Modifier.height(10.dp))

                Divider()

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    "Conversational Context: Use this to have ChatGPT respond a certain way.",
                    fontSize = 16.sp,
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    "\"${ChatConfig.conversationalContext.random()}\"",
                    color = MaterialTheme.colors.primary,
                    fontSize = 16.sp,
                )
            }
        },
        confirmButton = {
            Button(
                colors = ButtonDefaults.outlinedButtonColors(
                    backgroundColor = MaterialTheme.colors.primary
                ),
                onClick = {
                    isShowing.value = false
                }) {
                Text(text = "OK", color = Color.White)
            }
        },
        dismissButton = {},
        isShowing = isShowing
    )
}

@Composable
fun EmptyPromptDialog(isShowing: MutableState<Boolean>) {
    ShowDialog(
        title = "Please provide a prompt",
        titleColor = MaterialTheme.colors.primary,
        content = {
            Column {
                Text(
                    "The text field can not be empty.",
                    fontSize = 16.sp,
                )
            }
        },
        confirmButton = {
            Button(
                colors = ButtonDefaults.outlinedButtonColors(
                    backgroundColor = MaterialTheme.colors.primary
                ),
                onClick = {
                    isShowing.value = false
                }) {
                Text(text = "OK", color = Color.White)
            }
        },
        dismissButton = {},
        isShowing = isShowing
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SettingsDialog(
    apiKey: String,
    apiKeyOnValueChange: (String) -> Unit,
    humanSenderLabel: String,
    senderLabelOnValueChange: (String) -> Unit,
    isShowing: MutableState<Boolean>,
    showChatsDeletionWarning: () -> Unit,
    onClearApiKey: () -> Unit,
    showClearApiKeyWarning: () -> Unit,
    onDeleteAllChats: () -> Unit,
    isAutoSpeakToggled: Boolean,
    onAutoSpeakCheckedChange: ((Boolean) -> Unit)?,
) {
    ShowDialog(
        title = "Settings",
        titleColor = MaterialTheme.colors.primary,
        content = {
            Spacer(modifier = Modifier.height(15.dp))
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {

                Text(
                    text = "Clear API Key",
                    color = CardinalRed,
                    style = TextStyle(fontSize = 20.sp),
                    modifier = Modifier.combinedClickable(
                        onClick = {
                            showClearApiKeyWarning()
                        },
                        onLongClick = {
                            onClearApiKey()
                        }
                    ))

                Spacer(modifier = Modifier.height(30.dp))

                Text(
                    text = "Delete All Chats",
                    color = CardinalRed,
                    style = TextStyle(fontSize = 20.sp),
                    modifier = Modifier.combinedClickable(
                        onClick = {
                            showChatsDeletionWarning()
                        },
                        onLongClick = {
                            onDeleteAllChats()
                        }
                    ))

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isAutoSpeakToggled,
                        onCheckedChange = onAutoSpeakCheckedChange
                    )
                    Text("Auto-play incoming Chat audio")
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = humanSenderLabel,
                    onValueChange = senderLabelOnValueChange,
                    label = {
                        Text(
                            text = "Custom Sender Label",
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
                        isShowing.value = false
                    })
                )
            }
        },
        confirmButton = {
            Button(
                colors = ButtonDefaults.outlinedButtonColors(
                    backgroundColor = MaterialTheme.colors.primary
                ),
                onClick = {
                    isShowing.value = false
                }) {
                Text(text = "Close", color = Color.White)
            }
        },
        dismissButton = {},
        isShowing = isShowing
    )
}