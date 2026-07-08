package com.kisanalert.presentation.voice

import android.app.Activity
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.SmartToy
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kisanalert.R
import com.kisanalert.core.ui.components.KisanAppIcon
import com.kisanalert.core.ui.components.ServerErrorCard
import com.kisanalert.core.ui.localization.localizedLabel
import com.kisanalert.domain.model.PreferredLanguage
import com.kisanalert.domain.model.VoiceConversationTurn
import com.kisanalert.presentation.cropdoctor.cropDoctorErrorMessage
import com.kisanalert.presentation.cropdoctor.cropDoctorErrorTitle
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val ChatWallpaper: Color = Color(0xFFECE5DD)
private val ChatTopBarGreen: Color = Color(0xFF1B7A4B)
private val OutgoingBubble: Color = Color(0xFFDCF8C6)
private val IncomingBubble: Color = Color.White

private data class ChatMessage(
    val id: String,
    val text: String,
    val isFromUser: Boolean,
    val timestamp: Long,
    val turnId: String? = null
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun VoiceAssistantScreen(
    onNavigateBack: () -> Unit,
    viewModel: VoiceAssistantViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val activity = context as Activity
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.onEvent(VoiceAssistantEvent.MicPermissionResult(isGranted))
    }
    LaunchedEffect(Unit) {
        val isGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        if (isGranted) {
            viewModel.onEvent(VoiceAssistantEvent.MicPermissionResult(isGranted = true))
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }
    LaunchedEffect(uiState.shouldRecreateActivity) {
        if (uiState.shouldRecreateActivity) {
            viewModel.onEvent(VoiceAssistantEvent.RecreateHandled)
            activity.recreate()
        }
    }
    LaunchedEffect(uiState.errorMessage) {
        val errorMessage = uiState.errorMessage
        if (errorMessage != null) {
            snackbarHostState.showSnackbar(message = errorMessage)
            viewModel.onEvent(VoiceAssistantEvent.DismissError)
        }
    }
    DisposableEffect(Unit) {
        onDispose {
            viewModel.onEvent(VoiceAssistantEvent.ScreenLeft)
        }
    }
    Scaffold(
        topBar = {
            ChatTopBar(
                farmerName = uiState.farmerName,
                currentCrop = uiState.currentCrop,
                selectedLanguage = uiState.selectedLanguage,
                languageOptions = viewModel.languageOptions,
                isListening = uiState.isListening,
                isProcessing = uiState.isProcessing,
                isSpeaking = uiState.speakingTurnId != null,
                isPreparingSpeech = uiState.preparingTurnId != null,
                onNavigateBack = onNavigateBack,
                onLanguageSelected = { language ->
                    viewModel.onEvent(VoiceAssistantEvent.LanguageSelected(language))
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = ChatWallpaper,
        bottomBar = {
            if (!uiState.isLoading) {
                ChatBottomBar(
                    messageText = uiState.manualQuestionText,
                    isProcessing = uiState.isProcessing,
                    isListening = uiState.isListening,
                    hasMicPermission = uiState.hasMicPermission,
                    showStarterSuggestions = uiState.conversationHistory.isEmpty(),
                    starterSuggestions = uiState.starterSuggestions,
                    onMessageChanged = { text ->
                        viewModel.onEvent(VoiceAssistantEvent.ManualQuestionChanged(text))
                    },
                    onSend = { viewModel.onEvent(VoiceAssistantEvent.SubmitManualQuestion) },
                    onMicClick = {
                        if (uiState.isListening) {
                            viewModel.onEvent(VoiceAssistantEvent.StopListening)
                        } else if (uiState.hasMicPermission) {
                            viewModel.onEvent(VoiceAssistantEvent.StartListening)
                        } else {
                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    },
                    onSuggestionSelected = { suggestion ->
                        viewModel.onEvent(VoiceAssistantEvent.SuggestionSelected(suggestion))
                    }
                )
            }
        }
    ) { innerPadding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = ChatTopBarGreen)
                }
            }
            else -> {
                ChatMessageList(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    conversationHistory = uiState.conversationHistory,
                    isProcessing = uiState.isProcessing,
                    isFreshStart = uiState.conversationHistory.isEmpty(),
                    speakingTurnId = uiState.speakingTurnId,
                    preparingTurnId = uiState.preparingTurnId,
                    serverErrorCode = uiState.serverErrorCode,
                    isRetrying = uiState.isProcessing && uiState.retryableQuestion != null,
                    onPlayAnswer = { turnId, answerText ->
                        viewModel.onEvent(VoiceAssistantEvent.PlayAnswer(turnId, answerText))
                    },
                    onRetryServerRequest = {
                        viewModel.onEvent(VoiceAssistantEvent.RetryLastQuestion)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatTopBar(
    farmerName: String,
    currentCrop: String,
    selectedLanguage: PreferredLanguage,
    languageOptions: List<PreferredLanguage>,
    isListening: Boolean,
    isProcessing: Boolean,
    isSpeaking: Boolean,
    isPreparingSpeech: Boolean,
    onNavigateBack: () -> Unit,
    onLanguageSelected: (PreferredLanguage) -> Unit
) {
    val statusText = when {
        isListening -> stringResource(R.string.voice_assistant_listening)
        isPreparingSpeech -> stringResource(R.string.voice_assistant_preparing_speech)
        isSpeaking -> stringResource(R.string.voice_assistant_speaking)
        isProcessing -> stringResource(R.string.voice_chat_typing)
        else -> stringResource(R.string.voice_chat_online)
    }
    TopAppBar(
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = stringResource(R.string.auth_back),
                    tint = Color.White
                )
            }
        },
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.SmartToy,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Column {
                    Text(
                        text = stringResource(R.string.voice_assistant_header),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Text(
                        text = if (farmerName.isNotBlank() && currentCrop.isNotBlank()) {
                            "$statusText • $currentCrop"
                        } else {
                            statusText
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
            }
        },
        actions = {
            languageOptions.forEach { language ->
                FilterChip(
                    selected = selectedLanguage == language,
                    onClick = { onLanguageSelected(language) },
                    label = {
                        Text(
                            text = language.localizedLabel(),
                            fontSize = 11.sp
                        )
                    },
                    modifier = Modifier.padding(end = 4.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color.White.copy(alpha = 0.25f),
                        selectedLabelColor = Color.White,
                        containerColor = Color.Transparent,
                        labelColor = Color.White.copy(alpha = 0.7f)
                    ),
                    border = null
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = ChatTopBarGreen,
            scrolledContainerColor = ChatTopBarGreen
        )
    )
}

@Composable
private fun ChatMessageList(
    modifier: Modifier = Modifier,
    conversationHistory: List<VoiceConversationTurn>,
    isProcessing: Boolean,
    isFreshStart: Boolean,
    speakingTurnId: String?,
    preparingTurnId: String?,
    serverErrorCode: String?,
    isRetrying: Boolean,
    onPlayAnswer: (turnId: String, answerText: String) -> Unit,
    onRetryServerRequest: () -> Unit
) {
    val chatMessages = remember(conversationHistory) {
        buildChatMessages(conversationHistory)
    }
    val listState = rememberLazyListState()
    LaunchedEffect(chatMessages.size, isProcessing) {
        val targetIndex = chatMessages.lastIndex + if (isProcessing) 1 else 0
        if (targetIndex >= 0) {
            listState.animateScrollToItem(targetIndex)
        }
    }
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        state = listState,
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (isFreshStart && !isProcessing && serverErrorCode == null) {
            item(key = "welcome") {
                WelcomeBanner()
            }
        }
        serverErrorCode?.let { errorCode ->
            item(key = "server_error") {
                ServerErrorCard(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                    title = cropDoctorErrorTitle(errorCode = errorCode),
                    message = cropDoctorErrorMessage(errorCode = errorCode),
                    onRetry = onRetryServerRequest,
                    isRetrying = isRetrying
                )
            }
        }
        items(
            items = chatMessages,
            key = { message -> message.id }
        ) { message ->
            ChatBubble(
                text = message.text,
                isFromUser = message.isFromUser,
                timestamp = message.timestamp,
                turnId = message.turnId,
                isSpeaking = message.turnId != null && message.turnId == speakingTurnId,
                isPreparingSpeech = message.turnId != null && message.turnId == preparingTurnId,
                onPlayAnswer = onPlayAnswer
            )
        }
        if (isProcessing) {
            item(key = "typing") {
                TypingIndicatorBubble()
            }
        }
    }
}

@Composable
private fun WelcomeBanner() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFFD9FDD3).copy(alpha = 0.9f),
            shadowElevation = 0.dp
        ) {
            Text(
                text = stringResource(R.string.voice_chat_welcome),
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF54656F),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ChatBubble(
    text: String,
    isFromUser: Boolean,
    timestamp: Long,
    turnId: String? = null,
    isSpeaking: Boolean = false,
    isPreparingSpeech: Boolean = false,
    onPlayAnswer: (turnId: String, answerText: String) -> Unit = { _, _ -> }
) {
    val bubbleColor = if (isFromUser) OutgoingBubble else IncomingBubble
    val textColor = Color(0xFF111B21)
    val timeColor = Color(0xFF667781)
    val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
    val cornerShape = if (isFromUser) {
        RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp, bottomStart = 12.dp, bottomEnd = 2.dp)
    } else {
        RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp, bottomStart = 2.dp, bottomEnd = 12.dp)
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isFromUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isFromUser) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(ChatTopBarGreen.copy(alpha = 0.15f))
                    .align(Alignment.Bottom),
                contentAlignment = Alignment.Center
            ) {
                KisanAppIcon(
                    tint = ChatTopBarGreen,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
        }
        Column(
            modifier = Modifier.widthIn(max = 280.dp),
            horizontalAlignment = if (isFromUser) Alignment.End else Alignment.Start
        ) {
            Surface(
                shape = cornerShape,
                color = bubbleColor,
                shadowElevation = if (isFromUser) 0.dp else 1.dp
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodyMedium,
                        color = textColor,
                        lineHeight = 20.sp
                    )
                    Row(
                        modifier = Modifier.align(Alignment.End),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (!isFromUser && turnId != null) {
                            IconButton(
                                onClick = { onPlayAnswer(turnId, text) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                when {
                                    isPreparingSpeech -> {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(18.dp),
                                            strokeWidth = 2.dp,
                                            color = ChatTopBarGreen
                                        )
                                    }
                                    isSpeaking -> {
                                        Icon(
                                            imageVector = Icons.Rounded.Stop,
                                            contentDescription = stringResource(
                                                R.string.voice_assistant_stop_speaking
                                            ),
                                            tint = ChatTopBarGreen,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    else -> {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Rounded.VolumeUp,
                                            contentDescription = stringResource(
                                                R.string.voice_assistant_play_answer
                                            ),
                                            tint = ChatTopBarGreen,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                        Text(
                            text = timeFormat.format(Date(timestamp)),
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            color = timeColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TypingIndicatorBubble() {
    val infiniteTransition = rememberInfiniteTransition(label = "typing")
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(ChatTopBarGreen.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            KisanAppIcon(tint = ChatTopBarGreen, modifier = Modifier.size(16.dp))
        }
        Spacer(modifier = Modifier.width(4.dp))
        Surface(
            shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp, bottomStart = 2.dp, bottomEnd = 12.dp),
            color = IncomingBubble,
            shadowElevation = 1.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(3) { index ->
                    val alpha by infiniteTransition.animateFloat(
                        initialValue = 0.3f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(durationMillis = 600, delayMillis = index * 150, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "dot$index"
                    )
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF90A4AE).copy(alpha = alpha))
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChatBottomBar(
    messageText: String,
    isProcessing: Boolean,
    isListening: Boolean,
    hasMicPermission: Boolean,
    showStarterSuggestions: Boolean,
    starterSuggestions: List<String>,
    onMessageChanged: (String) -> Unit,
    onSend: () -> Unit,
    onMicClick: () -> Unit,
    onSuggestionSelected: (String) -> Unit
) {
    val canSend = messageText.isNotBlank() && !isProcessing
    Surface(
        color = Color(0xFFF0F2F5),
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
        ) {
            if (showStarterSuggestions && starterSuggestions.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    starterSuggestions.forEach { suggestion ->
                        Surface(
                            onClick = { onSuggestionSelected(suggestion) },
                            shape = RoundedCornerShape(18.dp),
                            color = Color.White,
                            shadowElevation = 1.dp,
                            enabled = !isProcessing
                        ) {
                            Text(
                                text = suggestion,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                style = MaterialTheme.typography.bodySmall,
                                color = ChatTopBarGreen,
                                maxLines = 2
                            )
                        }
                    }
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 6.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                TextField(
                    value = messageText,
                    onValueChange = onMessageChanged,
                    modifier = Modifier.weight(1f),
                    placeholder = {
                        Text(
                            text = if (isListening) {
                                stringResource(R.string.voice_assistant_listening)
                            } else {
                                stringResource(R.string.voice_chat_type_message)
                            },
                            color = Color(0xFF8696A0)
                        )
                    },
                    enabled = !isProcessing && !isListening,
                    maxLines = 4,
                    shape = RoundedCornerShape(24.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        disabledContainerColor = Color.White,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(
                        onSend = { if (canSend) onSend() }
                    )
                )
                IconButton(
                    onClick = {
                        if (canSend) {
                            onSend()
                        } else {
                            onMicClick()
                        }
                    },
                    enabled = !isProcessing,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            if (isListening) MaterialTheme.colorScheme.error else ChatTopBarGreen
                        )
                ) {
                    Icon(
                        imageVector = when {
                            canSend -> Icons.AutoMirrored.Rounded.Send
                            isListening -> Icons.Rounded.Stop
                            else -> Icons.Rounded.Mic
                        },
                        contentDescription = stringResource(R.string.voice_assistant_mic),
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

private fun buildChatMessages(conversationHistory: List<VoiceConversationTurn>): List<ChatMessage> {
    return conversationHistory
        .asReversed()
        .flatMap { turn ->
            listOf(
                ChatMessage(
                    id = "${turn.id}_q",
                    text = turn.question,
                    isFromUser = true,
                    timestamp = turn.createdAt
                ),
                ChatMessage(
                    id = "${turn.id}_a",
                    text = turn.answer,
                    isFromUser = false,
                    timestamp = turn.createdAt + 1L,
                    turnId = turn.id
                )
            )
        }
}
