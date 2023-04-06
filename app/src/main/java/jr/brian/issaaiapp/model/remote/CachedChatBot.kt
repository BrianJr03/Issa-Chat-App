package jr.brian.issaaiapp.model.remote

import jr.brian.issaaiapp.model.local.Chat
import timber.log.Timber

/** WRITTEN BY: Collin Barber ~ https://github.com/CJCrafter **/

/**
 * Caches the initial request to make interactions easier (and String based).
 */
class CachedChatBot(
    apiKey: String,
    private val request: ChatCompletionRequest,
    prevChats: List<Chat>
) : ChatBot(apiKey) {
    private val prev = prevChats
    fun generateResponse(content: String, role: String = "user"): String {
        prev.forEach {
            request.messages.add(ChatMessage(role, it.text))
        }
        request.messages.add(ChatMessage(role, content))
        val response = super.generateResponse(request)
        val temp = response.choices[0].message
        request.messages.add(temp)
        Timber.tag("chats").i(request.messages.toString())
        return temp.content
    }
}