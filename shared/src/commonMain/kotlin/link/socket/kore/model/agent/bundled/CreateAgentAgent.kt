package link.socket.kore.model.agent.bundled

import com.aallam.openai.client.OpenAI
import kotlinx.coroutines.CoroutineScope
import link.socket.kore.model.agent.KoreAgent
import link.socket.kore.ui.conversation.selector.AgentInput

data class CreateAgentAgent(
    override val openAI: OpenAI,
    override val scope: CoroutineScope,
) : KoreAgent.HumanAndLLMAssisted(scope) {

    private lateinit var description: String

    companion object {
        const val NAME = "Create Agent"

        private const val INSTRUCTIONS =
            "You are an Agent that is an expert in writing a LLM Agent descriptions, which includes both the " +
                    "system instructions and the initial Chat prompt for the Agent as described by the Developer. " +
                    "You should use your own system instructions and initial User Chat prompt as an example of what the " +
                    "Developer is looking for in your response."

        private fun initialPromptFrom(description: String): String =
                "You are tasked with defining an Agent that is follows this description:\n" +
                        "$description\n\n" +
                        "Your output should only be the Agent's system instructions and their initial User Chat prompt."

        private val descriptionArg = AgentInput.StringArg(
            key = "Agent Description",
            value = "",
        )
    }

    override val name: String = NAME
    override val instructions: String = "${super.instructions}\n\n" + INSTRUCTIONS
    override val initialPrompt: String by lazy { initialPromptFrom(description) }
    override val neededInputs: List<AgentInput> = listOf(descriptionArg)

    override fun parseNeededInputs(inputs: Map<String, AgentInput>) {
        description = inputs[descriptionArg.key]?.value ?: ""
    }

    override suspend fun executeHumanAssistance(): String {
        // TODO: Implement human verification
        return "Test"
    }
}