package jr.brian.issaaiapp.view.ui.pages

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
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
import jr.brian.issaaiapp.view.ui.theme.TextWhite
import jr.brian.issaaiapp.view.ui.util.copyToastMsgs
import kotlinx.coroutines.launch

@Composable
fun ConvoContextPage(
    primaryColor: MutableState<Color>,
    secondaryColor: MutableState<Color>,
    storedConvoContext: String,
    dataStore: MyDataStore
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    val conversationalContextText = remember { mutableStateOf("") }
    conversationalContextText.value = storedConvoContext

    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    val example = exampleConvoContext

    Scaffold {
        Spacer(Modifier.height(5.dp))
        Column(
            modifier = Modifier
                .scrollable(scrollState, orientation = Orientation.Vertical)
                .padding(it),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.padding(start = 30.dp, top = 20.dp, end = 30.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = example,
                    color = primaryColor.value,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Icon(
                    painter = painterResource(id = R.drawable.baseline_content_copy_24),
                    tint = primaryColor.value,
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
                modifier = Modifier.padding(20.dp).background(primaryColor.value)
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
                                    color = TextWhite, fontWeight = FontWeight.Bold
                                )
                            )
                        },
                        colors = TextFieldDefaults.textFieldColors(
                            textColor = TextWhite,
                            focusedIndicatorColor = secondaryColor.value,
                            unfocusedIndicatorColor = primaryColor.value
                        ),
                        modifier = Modifier
                            .padding(start = 16.dp, bottom = 16.dp, end = 16.dp)
                            .onFocusEvent { event ->
                                if (event.isFocused) {
                                    scope.launch {
                                        scrollState.animateScrollTo(scrollState.maxValue)
                                    }
                                }
                            }
                    )
                }
            }
        }
    }
}