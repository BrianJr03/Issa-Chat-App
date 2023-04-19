package jr.brian.issaaiapp.view.ui.pages

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import jr.brian.issaaiapp.R
import jr.brian.issaaiapp.model.local.MyDataStore
import jr.brian.issaaiapp.util.ChatConfig.exampleConvoContext
import jr.brian.issaaiapp.view.ui.util.copyToastMsgs
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ConvoContextPage(
    primaryColor: MutableState<Color>,
    secondaryColor: MutableState<Color>,
    isAmoledThemeToggled: MutableState<Boolean>,
    storedConvoContext: String,
    dataStore: MyDataStore
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val bringIntoViewRequester = BringIntoViewRequester()

    val conversationalContextText = remember { mutableStateOf("") }
    conversationalContextText.value = storedConvoContext

    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    val example = exampleConvoContext

    val scaffoldBgColor = if (isAmoledThemeToggled.value) Color.Black
    else MaterialTheme.colors.background

    val primary = if (isAmoledThemeToggled.value) Color.White else primaryColor.value
    val textColor = if (isAmoledThemeToggled.value) Color.Black else primaryColor.value

    Scaffold(backgroundColor = scaffoldBgColor) {
        Spacer(Modifier.height(5.dp))
        Column(
            modifier = Modifier
                .bringIntoViewRequester(bringIntoViewRequester)
                .padding(it),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.padding(start = 30.dp, top = 20.dp, end = 30.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = example,
                    color = primary,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Icon(
                    painter = painterResource(id = R.drawable.baseline_content_copy_24),
                    tint = primary,
                    contentDescription = "Random Conversational Context",
                    modifier = Modifier.clickable {
                        clipboardManager.setText(AnnotatedString((example)))
                        Toast.makeText(
                            context,
                            copyToastMsgs.random(),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                )
            }
            LazyColumn(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(20.dp)
                    .background(primary)
            ) {
                items(1) {
                    OutlinedTextField(
                        value = conversationalContextText.value,
                        onValueChange = { text ->
                            conversationalContextText.value = text
                            scope.launch {
                                dataStore.saveConvoContext(text)
                            }
                        },
                        label = {
                            Text(
                                text = "Enter Conversational Context", style = TextStyle(
                                    color = if (isAmoledThemeToggled.value) Color.Black
                                    else Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        },
                        colors = TextFieldDefaults.textFieldColors(
                            textColor = textColor,
                            focusedIndicatorColor = secondaryColor.value,
                            unfocusedIndicatorColor = primary
                        ),
                        modifier = Modifier
                            .padding(start = 16.dp, bottom = 16.dp, end = 16.dp)
                            .onFocusEvent { event ->
                                if (event.isFocused) {
                                    scope.launch {
                                        bringIntoViewRequester.bringIntoView()
                                    }
                                }
                            }
                    )
                }
            }
        }
    }
}