package link.socket.kore.ui.conversation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import link.socket.kore.model.agent.KoreAgent
import link.socket.kore.model.agent.LLMAgent
import link.socket.kore.model.conversation.Conversation
import link.socket.kore.ui.conversation.chat.ChatHistory
import link.socket.kore.ui.conversation.selector.AgentSelectionState
import link.socket.kore.ui.conversation.selector.ConversationHeader
import link.socket.kore.ui.theme.themeColors
import link.socket.kore.ui.widget.SmallSnackbarHost

@Composable
fun ConversationScreen(
    modifier: Modifier = Modifier,
    existingConversation: Conversation?,
    isLoading: Boolean,
    agentList: List<KoreAgent>,
    onAgentSelected: (KoreAgent) -> Unit,
    onChatSent: () -> Unit,
    onBackClicked: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val scaffoldState = rememberScaffoldState()
    var textFieldValue by remember { mutableStateOf(TextFieldValue()) }

    var partiallySelectedAgent by remember { mutableStateOf<KoreAgent?>(null) }

    val selectionState = remember(existingConversation, partiallySelectedAgent) {
        derivedStateOf {
            when {
                partiallySelectedAgent != null ->
                    AgentSelectionState.PartiallySelected(
                        agent = partiallySelectedAgent!!,
                        neededInputs = partiallySelectedAgent!!.neededInputs,
                    )

                existingConversation != null ->
                    AgentSelectionState.Selected(existingConversation.agent)

                else ->
                    AgentSelectionState.Unselected(agentList)
            }
        }
    }

    val displaySnackbar: (String) -> Unit = { message ->
        scope.launch {
            scaffoldState.snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short,
            )
        }
    }

    val onHeaderAgentSelection: (KoreAgent) -> Unit = { agent ->
        if (agent.neededInputs.isNotEmpty()) {
            partiallySelectedAgent = agent
        } else {
            onAgentSelected(agent)
        }
    }

    val onHeaderAgentSubmission: (AgentSelectionState.PartiallySelected) -> Unit = { state ->
        onAgentSelected(state.agent)
        partiallySelectedAgent = null
    }

    Box(
        modifier = modifier
            .fillMaxSize(),
    ) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize(),
            scaffoldState = scaffoldState,
            topBar = {
                ConversationHeader(
                    selectionState = selectionState.value,
                    onAgentSelected = onHeaderAgentSelection,
                    onHeaderAgentSubmission = onHeaderAgentSubmission,
                    onBackClicked = onBackClicked,
                )
            },
            bottomBar = {
                if (selectionState.value is AgentSelectionState.Selected) {
                    (existingConversation?.agent as? KoreAgent.HumanAndLLMAssisted)?.let { assistedAgent ->
                        val onSendClicked: () -> Unit = {
                            assistedAgent.addUserChat(textFieldValue.text)
                            onChatSent()
                        }

                        ConversationTextEntry(
                            modifier = Modifier
                                .requiredHeight(72.dp)
                                .align(Alignment.BottomCenter),
                            textFieldValue = textFieldValue,
                            onSendClicked = onSendClicked,
                            onTextChanged = { textFieldValue = it },
                        )
                    }
                }
            },
            snackbarHost = { snackbarState ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth(),
                ) {
                    SmallSnackbarHost(
                        modifier = Modifier
                            .align(Alignment.BottomStart),
                        snackbarHostState = snackbarState,
                    )
                }
            }
        ) { contentPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(themeColors().background)
                    .padding(contentPadding),
            ) {
                if (selectionState.value is AgentSelectionState.Selected) {
                    val agent = existingConversation?.agent

                    ChatHistory(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 72.dp),
                        messages = (agent as? LLMAgent)?.getChatKoreMessages() ?: emptyList(),
                        isLoading = isLoading,
                        displaySnackbar = displaySnackbar,
                    )
                }
            }
        }
    }
}
