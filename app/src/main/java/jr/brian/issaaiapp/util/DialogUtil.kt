package jr.brian.issaaiapp.util

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
fun EmptyTextFieldDialog(title: String, isShowing: MutableState<Boolean>) {
    ShowDialog(
        title = title,
        titleColor = MaterialTheme.colors.primary,
        content = {
            Column {
                Text(
                    "The text field can not be empty.",
                    fontSize = 16.sp,
                    color = MaterialTheme.colors.primary
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
    textFieldOnValueChange: (String) -> Unit,
    isShowing: MutableState<Boolean>,
    showChatsDeletionWarning: () -> Unit,
    onClearApiKey: () -> Unit,
    showClearApiKeyWarning: () -> Unit,
    onDeleteAllChats: () -> Unit,
    isAutoConvoContextToggled: Boolean,
    isAutoGreetToggled: Boolean,
    onAutoConvoCheckedChange: ((Boolean) -> Unit)?,
    onAutoGreetCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier,
    textFieldModifier: Modifier
) {
    ShowDialog(
        title = "Settings",
        titleColor = MaterialTheme.colors.primary,
        content = {
            Spacer(modifier = Modifier.height(15.dp))
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = modifier
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
                            isShowing.value = false
                            onDeleteAllChats()
                        }
                    ))

                Spacer(modifier = Modifier.height(30.dp))

                Row(
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isAutoGreetToggled,
                        onCheckedChange = onAutoGreetCheckedChange
                    )
                    Text("Greet me on app start")
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = isAutoConvoContextToggled,
                        onCheckedChange = onAutoConvoCheckedChange
                    )
                    Text("Don't set Conversational Context")
                }

                Spacer(modifier = Modifier.height(30.dp))

                OutlinedTextField(
                    modifier = textFieldModifier,
                    value = apiKey,
                    onValueChange = textFieldOnValueChange,
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