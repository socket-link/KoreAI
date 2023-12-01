package link.socket.kore.model.agent

import com.aallam.openai.api.chat.ChatCompletionRequest
import link.socket.kore.model.conversation.ChatHistory

sealed interface KoreAgent {

    val name: String

    interface Unassisted : KoreAgent
    interface HumanAssisted : KoreAgent

    abstract class HumanAndLLMAssisted : LLMAssisted(), HumanAssisted

    abstract class LLMAssisted : KoreAgent, LLMAgent {

        override var chatHistory: ChatHistory = ChatHistory.Threaded.Uninitialized
            set(value) {
                field = value
                updateCompletionRequest()
            }

        override var completionRequest: ChatCompletionRequest? = null

        override fun addUserChat(input: String) {
            error("Cannot use this function, please extend the HumanAndLLMAssisted class instead of LLMAssisted")
        }
    }
}
