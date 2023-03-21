package jr.brian.issaaiapp.model.remote

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.annotations.SerializedName
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.lang.IllegalArgumentException
import java.util.concurrent.TimeUnit

/** WRITTEN BY: Collin Barber ~ https://github.com/CJCrafter **/

/**
 * The ChatBot class wraps the OpenAI API and lets you send messages and
 * receive responses. For more information on how this works, check out
 * the [OpenAI Documentation](https://platform.openai.com/docs/api-reference/completions).
 *
 * @param apiKey Your OpenAI API key that starts with "sk-".
 */
open class ChatBot(private val apiKey: String) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val mediaType: MediaType = "application/json; charset=utf-8".toMediaType()
    private val gson = GsonBuilder().create()

    /**
     * Blocks the current thread until OpenAI responds to the http request.
     * You can access the generated message in [ChatCompletionRequest.messages].
     *
     * @param request The data used to control the output.
     * @return The returned response.
     * @throws IllegalArgumentException If the server returns an error.
     */
    fun generateResponse(request: ChatCompletionRequest): ChatCompletionResponse {
        val json = gson.toJson(request)
        val body: RequestBody = json.toRequestBody(mediaType)
        val httpRequest: Request = Request.Builder()
            .url("https://api.openai.com/v1/chat/completions")
            .addHeader("Content-Type", "application/json")
            .addHeader("Authorization", "Bearer $apiKey")
            .post(body)
            .build()

        // Block the thread and wait for OpenAI's json response
        val response = client.newCall(httpRequest).execute()
        val jsonResponse = response.body!!.string()
        val rootObject = JsonParser.parseString(jsonResponse).asJsonObject

        // Usually happens if you give improper arguments (either an unrecognized argument or bad argument value)
        if (rootObject.has("error"))
            throw IllegalArgumentException(rootObject["error"].asJsonObject["message"].asString)

        return ChatCompletionResponse(rootObject)
    }

    /**
     * The ChatGPT API takes a list of 'roles' and 'content'. The role is
     * one of 3 options: system, assistant, and user. 'System' is used to
     * prompt ChatGPT before the user gives input. 'Assistant' is a message
     * from ChatGPT. 'User' is a message from the human.
     *
     * @param role Who sent the message.
     * @param content The raw content of the message.
     */
    data class ChatMessage(val role: String, val content: String) {
        constructor(json: JsonObject) : this(
            json["role"].asString,
            json["content"].asString
        )
    }

    /**
     * These are the arguments that control the result of the output. For more
     * information, refer to the [OpenAI Docs](https://platform.openai.com/docs/api-reference/completions/create).
     *
     * @param model The model used to generate the text. Recommended: "gpt-3.5-turbo."
     * @param messages All previous messages from the conversation.
     * @param temperature How "creative" the results are. [[0.0, 2.0]].
     * @param topP Controls how "on topic" the tokens are.
     * @param n Controls how many responses to generate. Numbers >1 will chew through your tokens.
     * @param stream **UNTESTED** recommend keeping this false.
     * @param stop The sequence used to stop generating tokens.
     * @param maxTokens The maximum number of tokens to use.
     * @param presencePenalty Prevent talking about duplicate topics.
     * @param frequencyPenalty Prevent repeating the same text.
     * @param logitBias Control specific tokens from being used.
     * @param user Who send this request (for moderation).
     */
    data class ChatCompletionRequest(
        val model: String, // recommend: "gpt-3.5-turbo"
        val messages: MutableList<ChatMessage>,
        val temperature: Float = 1.0f,
        @SerializedName("top_p") val topP: Float = 1.0f,
        val n: Int = 1,
        val stream: Boolean = false,
        val stop: String? = null,
        @SerializedName("max_tokens") val maxTokens: Int? = null, // default is 4096
        @SerializedName("presence_penalty") val presencePenalty: Float = 0.0f,
        @SerializedName("frequency_penalty") val frequencyPenalty: Float = 0.0f,
        @SerializedName("logit_bias") val logitBias: JsonObject? = null,
        val user: String? = null
    ) {
        constructor(model: String, systemContent: String) : this(
            model,
            arrayListOf(ChatMessage("system", systemContent))
        )
    }

    /**
     * This is the object returned from the API. You want to access choices[0]
     * to get your response.
     */
    data class ChatCompletionResponse(
        val id: String,
        val `object`: String,
        val created: Long,
        val choices: List<ChatCompletionChoice>,
        val usage: ChatCompletionUsage,
    ) {
        constructor(json: JsonObject) : this(
            json["id"].asString,
            json["object"].asString,
            json["created"].asLong,
            json["choices"].asJsonArray.map { ChatCompletionChoice(it.asJsonObject) },
            ChatCompletionUsage(json["usage"].asJsonObject)
        )
    }

    /**
     * Holds the data for 1 generated text completion.
     *
     * @param index The index in the array... 0 if n=1.
     * @param message The generated text.
     * @param finishReason Why did the bot stop generating tokens?
     */
    data class ChatCompletionChoice(
        val index: Int,
        val message: ChatMessage,
        val finishReason: String
    ) {
        constructor(json: JsonObject) : this(
            json["index"].asInt,
            ChatMessage(json["message"].asJsonObject),
            json["finish_reason"].asString
        )
    }

    /**
     * Holds how many tokens that were used by your API request. Use these
     * tokens to calculate how much money you have spent on each request.
     *
     * @param promptTokens How many tokens the input used.
     * @param completionTokens How many tokens the output used.
     * @param totalTokens How many tokens in total.
     */
    data class ChatCompletionUsage(
        val promptTokens: Int,
        val completionTokens: Int,
        val totalTokens: Int
    ) {
        constructor(json: JsonObject) : this(
            json["prompt_tokens"].asInt,
            json["completion_tokens"].asInt,
            json["total_tokens"].asInt
        )
    }
}