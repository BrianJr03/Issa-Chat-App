package jr.brian.issaaiapp.model.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class MyDataStore @Inject constructor(private val context: Context) {
    companion object {
        private val Context.dataStore:
                DataStore<Preferences> by preferencesDataStore("api-key-data-store")
        val API_KEY = stringPreferencesKey("user_api_key")
        val HUMAN_SENDER_LABEL = stringPreferencesKey("human-sender-label")
        val USER_SET_CONVO_CONTEXT = stringPreferencesKey("convo-context")
        val AUTO_SPEAK = booleanPreferencesKey("auto-speak")
        val THEME_CHOICE = stringPreferencesKey("theme-choice")
        val CURRENT_CONVO_NAME = stringPreferencesKey("current-convo-name")
        val CURRENT_CONVO = stringSetPreferencesKey("current-convo")
    }

    val getApiKey: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[API_KEY]
    }

    suspend fun saveApiKey(apiKey: String) {
        context.dataStore.edit { preferences ->
            preferences[API_KEY] = apiKey
        }
    }

    // TODO --------------------

    val getCurrentConversation: Flow<Set<String>?> = context.dataStore.data.map { preferences ->
        preferences[CURRENT_CONVO]
    }

    suspend fun saveCurrentConversation(conversation: Conversation) {
        context.dataStore.edit { preferences ->
            preferences[CURRENT_CONVO] =
                setOf(conversation.conversationName, conversation.context)

        }
    }

    val getCurrentConversationName: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[CURRENT_CONVO_NAME]
    }

    suspend fun saveCurrentConversationName(currentConvo: String) {
        context.dataStore.edit { preferences ->
            preferences[CURRENT_CONVO_NAME] = currentConvo

        }
    }

    val getConvoContext: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_SET_CONVO_CONTEXT]
    }

    suspend fun saveConvoContext(convoContext: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_SET_CONVO_CONTEXT] = convoContext
        }
    }

    // TODO --------------------

    val getHumanSenderLabel: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[HUMAN_SENDER_LABEL]
    }

    suspend fun saveHumanSenderLabel(humanSenderLabel: String) {
        context.dataStore.edit { preferences ->
            preferences[HUMAN_SENDER_LABEL] = humanSenderLabel
        }
    }

    val getIsAutoSpeakToggled: Flow<Boolean?> = context.dataStore.data.map { preferences ->
        preferences[AUTO_SPEAK]
    }

    suspend fun saveIsAutoSpeakToggled(isToggled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[AUTO_SPEAK] = isToggled
        }
    }

    val getThemeChoice: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[THEME_CHOICE]
    }

    suspend fun saveThemeChoice(themeChoice: String) {
        context.dataStore.edit { preferences ->
            preferences[THEME_CHOICE] = themeChoice
        }
    }
}