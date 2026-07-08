package com.kisanalert.presentation.auth

import android.app.Activity
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import com.kisanalert.core.ui.components.KisanAppIcon
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kisanalert.R
import com.kisanalert.core.ui.components.KisanOutlinedButton
import com.kisanalert.core.ui.components.KisanPrimaryButton
import com.kisanalert.core.ui.components.KisanTextField
import com.kisanalert.core.ui.theme.KrishakSevaTheme
import com.kisanalert.core.ui.theme.KisanColors
import com.kisanalert.core.ui.localization.localizedLabel
import com.kisanalert.domain.model.PreferredLanguage

@Composable
fun AuthScreen(
    onNavigateToFarmerRegistration: () -> Unit,
    onNavigateToDashboard: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity = context as Activity
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.destination) {
        when (uiState.destination) {
            AuthDestination.FarmerRegistration -> onNavigateToFarmerRegistration()
            AuthDestination.Dashboard -> onNavigateToDashboard()
            null -> Unit
        }
    }
    LaunchedEffect(uiState.shouldRecreateActivity) {
        if (uiState.shouldRecreateActivity) {
            viewModel.onRecreateHandled()
            activity.recreate()
        }
    }
    LaunchedEffect(uiState.errorMessage) {
        val errorMessage = uiState.errorMessage
        if (errorMessage != null) {
            snackbarHostState.showSnackbar(message = errorMessage)
            viewModel.onDismissError()
        }
    }
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        AuthContent(
            uiState = uiState,
            languageOptions = viewModel.languageOptions,
            modifier = Modifier.padding(innerPadding),
            onLanguageSelected = viewModel::onLanguageSelected,
            onSignInAnonymously = viewModel::onSignInAnonymously,
            onPhoneNumberChanged = viewModel::onPhoneNumberChanged,
            onSendOtp = { viewModel.onSendOtp(activity) },
            onOtpCodeChanged = viewModel::onOtpCodeChanged,
            onVerifyOtp = viewModel::onVerifyOtp,
            onBackToPhoneEntry = viewModel::onBackToPhoneEntry
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AuthContent(
    uiState: AuthUiState,
    languageOptions: List<PreferredLanguage>,
    modifier: Modifier = Modifier,
    onLanguageSelected: (PreferredLanguage) -> Unit,
    onSignInAnonymously: () -> Unit,
    onPhoneNumberChanged: (String) -> Unit,
    onSendOtp: () -> Unit,
    onOtpCodeChanged: (String) -> Unit,
    onVerifyOtp: () -> Unit,
    onBackToPhoneEntry: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding()
    ) {
        AuthHeader()
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            AnimatedContent(
                targetState = uiState.authStep,
                transitionSpec = {
                    if (targetState == AuthStep.PhoneOtp) {
                        slideInHorizontally { width -> width } + fadeIn() togetherWith
                            slideOutHorizontally { width -> -width } + fadeOut()
                    } else {
                        slideInHorizontally { width -> -width } + fadeIn() togetherWith
                            slideOutHorizontally { width -> width } + fadeOut()
                    }
                },
                label = "auth_step"
            ) { authStep ->
                when (authStep) {
                    AuthStep.Welcome -> WelcomeAuthStep(
                        uiState = uiState,
                        languageOptions = languageOptions,
                        onLanguageSelected = onLanguageSelected,
                        onSignInAnonymously = onSignInAnonymously,
                        onPhoneNumberChanged = onPhoneNumberChanged,
                        onSendOtp = onSendOtp
                    )
                    AuthStep.PhoneOtp -> PhoneOtpAuthStep(
                        uiState = uiState,
                        onOtpCodeChanged = onOtpCodeChanged,
                        onVerifyOtp = onVerifyOtp,
                        onBackToPhoneEntry = onBackToPhoneEntry
                    )
                }
            }
        }
    }
}

@Composable
private fun AuthHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        KisanColors.CardGradientStart,
                        KisanColors.CardGradientEnd
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                KisanAppIcon(
                    modifier = Modifier.size(52.dp),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.auth_welcome_title),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.auth_welcome_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.auth_welcome_subtitle_telugu),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WelcomeAuthStep(
    uiState: AuthUiState,
    languageOptions: List<PreferredLanguage>,
    onLanguageSelected: (PreferredLanguage) -> Unit,
    onSignInAnonymously: () -> Unit,
    onPhoneNumberChanged: (String) -> Unit,
    onSendOtp: () -> Unit
) {
    Column(
        modifier = Modifier.padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = stringResource(R.string.auth_language_label),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = stringResource(R.string.auth_language_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                languageOptions.forEach { language ->
                    FilterChip(
                        selected = uiState.selectedLanguage == language,
                        onClick = { onLanguageSelected(language) },
                        label = { Text(text = language.localizedLabel()) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }
        }
        Text(
            text = stringResource(R.string.auth_get_started),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = stringResource(R.string.auth_get_started_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        KisanPrimaryButton(
            text = stringResource(R.string.auth_continue_guest),
            onClick = onSignInAnonymously,
            isLoading = uiState.isLoading && uiState.authStep == AuthStep.Welcome && uiState.phoneNumber.isEmpty()
        )
        AuthDivider()
        Text(
            text = stringResource(R.string.auth_phone_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )
        KisanTextField(
            value = uiState.phoneNumber,
            onValueChange = onPhoneNumberChanged,
            label = stringResource(R.string.auth_phone_label),
            placeholder = stringResource(R.string.auth_phone_placeholder),
            keyboardType = KeyboardType.Phone,
            imeAction = ImeAction.Done,
            onImeAction = onSendOtp,
            leadingContent = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PhoneAndroid,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "+91",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
        )
        KisanOutlinedButton(
            text = stringResource(R.string.auth_send_otp),
            onClick = onSendOtp,
            isLoading = uiState.isLoading && uiState.phoneNumber.isNotEmpty()
        )
    }
}

@Composable
private fun PhoneOtpAuthStep(
    uiState: AuthUiState,
    onOtpCodeChanged: (String) -> Unit,
    onVerifyOtp: () -> Unit,
    onBackToPhoneEntry: () -> Unit
) {
    Column(
        modifier = Modifier.padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackToPhoneEntry) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = stringResource(R.string.auth_back)
                )
            }
            Text(
                text = stringResource(R.string.auth_verify_otp_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
        val infoMessage = uiState.infoMessage
        if (infoMessage != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.VerifiedUser,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = infoMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        KisanTextField(
            value = uiState.otpCode,
            onValueChange = onOtpCodeChanged,
            label = stringResource(R.string.auth_otp_label),
            placeholder = stringResource(R.string.auth_otp_placeholder),
            keyboardType = KeyboardType.NumberPassword,
            imeAction = ImeAction.Done,
            onImeAction = onVerifyOtp
        )
        KisanPrimaryButton(
            text = stringResource(R.string.auth_verify_otp),
            onClick = onVerifyOtp,
            isLoading = uiState.isLoading
        )
    }
}

@Composable
private fun AuthDivider() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f))
        Text(
            text = stringResource(R.string.auth_or),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        HorizontalDivider(modifier = Modifier.weight(1f))
    }
}

@Preview(showBackground = true)
@Composable
private fun WelcomeAuthStepPreview() {
    KrishakSevaTheme {
        AuthContent(
            uiState = AuthUiState(),
            languageOptions = PreferredLanguage.entries,
            onLanguageSelected = {},
            onSignInAnonymously = {},
            onPhoneNumberChanged = {},
            onSendOtp = {},
            onOtpCodeChanged = {},
            onVerifyOtp = {},
            onBackToPhoneEntry = {}
        )
    }
}
