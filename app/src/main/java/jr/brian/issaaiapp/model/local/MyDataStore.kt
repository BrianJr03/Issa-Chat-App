package jr.brian.issaaiapp.model.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class MyDataStore @Inject constructor(private val context: Context) {
    companion object {
        private val Context.dataStore:
                DataStore<Preferences> by preferencesDataStore("api-key-data-store")
        val API_KEY = stringPreferencesKey("user_api_key")
        val AUTO_CONVO_CONTEXT_TOGGLE = booleanPreferencesKey("auto-toggle")
        val AUTO_SPEAK = booleanPreferencesKey("auto-speak")
    }

    val getApiKey: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[API_KEY]
    }

    suspend fun saveApiKey(apiKey: String) {
        context.dataStore.edit { preferences ->
            preferences[API_KEY] = apiKey
        }
    }

    val getIsAutoConvoContextToggled: Flow<Boolean?> = context.dataStore.data.map { preferences ->
        preferences[AUTO_CONVO_CONTEXT_TOGGLE]
    }

    suspend fun saveIsAutoConvoContextToggled(isToggled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[AUTO_CONVO_CONTEXT_TOGGLE] = isToggled
        }
    }

    val getIsAutoSpeakToggled: Flow<Boolean?> = context.dataStore.data.map { preferences ->
        preferences[AUTO_SPEAK]
    }

    suspend fun saveIsAutoSpeakToggles(isToggled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[AUTO_SPEAK] = isToggled
        }
    }
}