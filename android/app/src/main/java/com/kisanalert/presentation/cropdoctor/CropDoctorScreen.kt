package com.kisanalert.presentation.cropdoctor

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageCapture
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Biotech
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.MedicalServices
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import com.kisanalert.core.ui.KisanScaffoldDefaults
import com.kisanalert.core.ui.components.KisanTopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.SubcomposeAsyncImage
import com.kisanalert.R
import com.kisanalert.core.constants.CropDoctorConstants
import com.kisanalert.core.constants.CropDoctorErrors
import com.kisanalert.core.ui.components.KisanOutlinedButton
import com.kisanalert.core.ui.components.DataSourceBadge
import com.kisanalert.core.ui.components.KisanPrimaryButton
import com.kisanalert.core.ui.components.ServerErrorCard
import com.kisanalert.core.ui.theme.KisanColors
import com.kisanalert.domain.model.CropDoctorScan
import com.kisanalert.domain.model.DiseaseSeverity
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropDoctorScreen(
    onNavigateBack: () -> Unit,
    isTabRoot: Boolean = false,
    viewModel: CropDoctorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.onEvent(CropDoctorEvent.CameraPermissionResult(isGranted))
    }
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { imageUri ->
        if (imageUri == null) {
            return@rememberLauncherForActivityResult
        }
        saveCropImageFromUri(
            context = context,
            imageUri = imageUri,
            onSaved = { localPath ->
                viewModel.onEvent(CropDoctorEvent.ImageCaptured(localPath))
            },
            onError = { message ->
                viewModel.onEvent(
                    CropDoctorEvent.CaptureFailed(
                        message = context.getString(R.string.crop_doctor_upload_failed)
                    )
                )
            }
        )
    }
    val onUploadImage: () -> Unit = {
        galleryLauncher.launch("image/*")
    }
    LaunchedEffect(Unit) {
        val isGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        if (isGranted) {
            viewModel.onEvent(CropDoctorEvent.CameraPermissionResult(isGranted = true))
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }
    LaunchedEffect(uiState.errorMessage) {
        val errorMessage = uiState.errorMessage
        if (errorMessage != null) {
            val displayMessage = if (CropDoctorErrors.isKnownCode(errorMessage)) {
                context.getString(cropDoctorErrorStringRes(errorCode = errorMessage))
            } else {
                errorMessage
            }
            snackbarHostState.showSnackbar(message = displayMessage)
            viewModel.onEvent(CropDoctorEvent.DismissError)
        }
    }
    Scaffold(
        contentWindowInsets = KisanScaffoldDefaults.NestedTabContentInsets,
        topBar = {
            KisanTopAppBar(
                title = stringResource(R.string.crop_doctor_title),
                showBackButton = !isTabRoot,
                onNavigateBack = onNavigateBack
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        when {
            uiState.isLoading -> {
                LoadingContent(modifier = Modifier.padding(innerPadding))
            }
            else -> {
                CropDoctorContent(
                    modifier = Modifier.padding(innerPadding),
                    uiState = uiState,
                    onCapture = { imageCapture ->
                        captureCropImage(
                            imageCapture = imageCapture,
                            context = context,
                            onCaptured = { localPath ->
                                viewModel.onEvent(CropDoctorEvent.ImageCaptured(localPath))
                            },
                            onError = { message ->
                                viewModel.onEvent(CropDoctorEvent.CaptureFailed(message))
                            }
                        )
                    },
                    onCameraError = { message ->
                        viewModel.onEvent(CropDoctorEvent.CaptureFailed(message))
                    },
                    onAnalyze = { viewModel.onEvent(CropDoctorEvent.AnalyzeCapturedImage) },
                    onRetry = { viewModel.onEvent(CropDoctorEvent.RetryAnalysis) },
                    onRetake = { viewModel.onEvent(CropDoctorEvent.RetakePhoto) },
                    onSelectHistory = { scanId ->
                        viewModel.onEvent(CropDoctorEvent.SelectHistoryScan(scanId))
                    },
                    onRequestPermission = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                    onUploadImage = onUploadImage
                )
            }
        }
    }
}

@Composable
private fun LoadingContent(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun CropDoctorContent(
    modifier: Modifier = Modifier,
    uiState: CropDoctorUiState,
    onCapture: (ImageCapture) -> Unit,
    onCameraError: (String) -> Unit,
    onAnalyze: () -> Unit,
    onRetry: () -> Unit,
    onRetake: () -> Unit,
    onSelectHistory: (String) -> Unit,
    onRequestPermission: () -> Unit,
    onUploadImage: () -> Unit
) {
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    val context = LocalContext.current
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            HeaderSection(currentCrop = uiState.currentCrop)
        }
        item {
            when {
                uiState.showCamera && uiState.hasCameraPermission -> {
                    CameraPreviewSection(
                        imageCapture = imageCapture,
                        onImageCaptureReady = { capture -> imageCapture = capture },
                        onCameraError = onCameraError,
                        onCapture = {
                            val capture = imageCapture
                            if (capture != null) {
                                onCapture(capture)
                            }
                        }
                    )
                }
                uiState.showCamera && !uiState.hasCameraPermission -> {
                    PermissionCard(onRequestPermission = onRequestPermission)
                }
                uiState.capturedImagePath != null -> {
                    CapturedImageSection(imagePath = uiState.capturedImagePath)
                }
            }
        }
        if (uiState.showCamera) {
            item {
                UploadImageCard(onUploadImage = onUploadImage)
            }
        }
        if (!uiState.showCamera && uiState.latestScan == null && uiState.capturedImagePath != null) {
            uiState.analysisErrorCode?.let { errorCode ->
                item {
                    when (errorCode) {
                        CropDoctorErrors.INVALID_CROP_IMAGE -> {
                            InvalidImageCard(onRetake = onRetake)
                        }
                        else -> {
                            AnalysisServiceErrorCard(
                                errorCode = errorCode,
                                isRetrying = uiState.isAnalyzing,
                                onRetry = if (CropDoctorErrors.isRetryable(errorCode)) onRetry else null,
                                onRetake = onRetake,
                                onCallExpert = {
                                    val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                                        data = Uri.parse("tel:${CropDoctorConstants.EXPERT_CONTACT_PHONE}")
                                    }
                                    context.startActivity(dialIntent)
                                }
                            )
                        }
                    }
                }
            }
            item {
                if (uiState.analysisErrorCode == null) {
                    KisanPrimaryButton(
                        text = if (uiState.isAnalyzing) {
                            stringResource(R.string.crop_doctor_analyzing)
                        } else {
                            stringResource(R.string.crop_doctor_analyze)
                        },
                        onClick = onAnalyze,
                        isLoading = uiState.isAnalyzing,
                        enabled = !uiState.isAnalyzing
                    )
                }
            }
            item {
                if (uiState.analysisErrorCode == null) {
                    KisanOutlinedButton(
                        text = stringResource(R.string.crop_doctor_retake),
                        onClick = onRetake
                    )
                }
            }
        }
        uiState.latestScan?.let { scan ->
            item {
                DiagnosisCard(
                    scan = scan
                )
            }
            item {
                KisanOutlinedButton(
                    text = stringResource(R.string.crop_doctor_scan_again),
                    onClick = onRetake
                )
            }
        }
        if (uiState.scanHistory.isNotEmpty()) {
            item {
                HistorySection(
                    scans = uiState.scanHistory,
                    selectedScanId = uiState.latestScan?.id,
                    onSelectHistory = onSelectHistory
                )
            }
        }
        item { Spacer(modifier = Modifier.height(8.dp)) }
    }
}

@Composable
private fun HeaderSection(currentCrop: String) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Biotech,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = stringResource(R.string.crop_doctor_header),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
        Text(
            text = stringResource(R.string.crop_doctor_subtitle, currentCrop.ifBlank { "your crop" }),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PermissionCard(
    onRequestPermission: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Rounded.CameraAlt,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = stringResource(R.string.crop_doctor_permission_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = stringResource(R.string.crop_doctor_permission_message),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            KisanPrimaryButton(
                text = stringResource(R.string.crop_doctor_grant_permission),
                onClick = onRequestPermission
            )
        }
    }
}

@Composable
private fun CameraPreviewSection(
    imageCapture: ImageCapture?,
    onImageCaptureReady: (ImageCapture) -> Unit,
    onCameraError: (String) -> Unit,
    onCapture: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp)
                .clip(RoundedCornerShape(20.dp))
        ) {
            CropDoctorCameraPreview(
                modifier = Modifier.fillMaxSize(),
                isActive = true,
                onImageCaptureReady = onImageCaptureReady,
                onCameraError = onCameraError
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.55f))
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Text(
                    text = stringResource(R.string.crop_doctor_camera_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White
                )
            }
            FloatingActionButton(
                onClick = onCapture,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(bottom = 24.dp),
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Rounded.CameraAlt,
                    contentDescription = stringResource(R.string.crop_doctor_capture)
                )
            }
        }
    }
}

@Composable
private fun UploadImageCard(onUploadImage: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.PhotoLibrary,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = stringResource(R.string.crop_doctor_upload_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = stringResource(R.string.crop_doctor_upload_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            KisanOutlinedButton(
                text = stringResource(R.string.crop_doctor_upload),
                onClick = onUploadImage,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun CapturedImageSection(imagePath: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp)
    ) {
        ScanImagePreview(
            imageModel = File(imagePath),
            contentDescription = stringResource(R.string.crop_doctor_captured_image),
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
        )
        Text(
            text = stringResource(R.string.crop_doctor_preview_hint),
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun AnalysisServiceErrorCard(
    errorCode: String,
    isRetrying: Boolean,
    onRetry: (() -> Unit)?,
    onRetake: () -> Unit,
    onCallExpert: () -> Unit
) {
    val showExpertContact = CropDoctorErrors.shouldShowExpertContact(errorCode)
    val errorTitle = cropDoctorErrorTitle(errorCode = errorCode)
    val errorMessage = cropDoctorErrorMessage(errorCode = errorCode)
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ServerErrorCard(
            title = errorTitle,
            message = errorMessage,
            onRetry = onRetry ?: onRetake,
            isRetrying = isRetrying,
            retryLabel = if (onRetry != null) {
                stringResource(R.string.action_retry)
            } else {
                stringResource(R.string.crop_doctor_retake)
            },
            secondaryActionLabel = if (onRetry != null) {
                stringResource(R.string.crop_doctor_retake)
            } else {
                null
            },
            onSecondaryAction = if (onRetry != null) onRetake else null
        )
        if (showExpertContact) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
                border = BorderStroke(
                    width = 1.dp,
                    color = KisanColors.CropHealthWarning.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ExpertContactSection(onCallExpert = onCallExpert)
                }
            }
        }
    }
}

@Composable
private fun ExpertContactSection(onCallExpert: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = stringResource(R.string.crop_doctor_expert_contact_title),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = CropDoctorConstants.EXPERT_CONTACT_NAME,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = CropDoctorConstants.EXPERT_CONTACT_ROLE,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = stringResource(R.string.crop_doctor_expert_contact_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        KisanPrimaryButton(
            text = stringResource(
                R.string.crop_doctor_call_expert,
                formatExpertPhoneDisplay(CropDoctorConstants.EXPERT_CONTACT_PHONE)
            ),
            onClick = onCallExpert
        )
    }
}

@Composable
private fun InvalidImageCard(onRetake: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Warning,
                    contentDescription = null,
                    tint = KisanColors.CropHealthCritical
                )
                Text(
                    text = stringResource(R.string.crop_doctor_invalid_image_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = KisanColors.CropHealthCritical
                )
            }
            Text(
                text = stringResource(R.string.crop_doctor_invalid_image_message),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            KisanPrimaryButton(
                text = stringResource(R.string.crop_doctor_retake),
                onClick = onRetake
            )
        }
    }
}

@Composable
private fun DiagnosisCard(
    scan: CropDoctorScan
) {
    val diagnosis = scan.diagnosis
    val context = LocalContext.current
    val confidenceThreshold = CropDoctorConstants.LOW_CONFIDENCE_THRESHOLD_PERCENT
    val isLowConfidence = diagnosis.isLowConfidence(thresholdPercent = confidenceThreshold)
    val cardColor = when {
        diagnosis.isHealthy -> Color(0xFFE8F5E9)
        diagnosis.severity == DiseaseSeverity.HIGH -> Color(0xFFFFEBEE)
        diagnosis.severity == DiseaseSeverity.MEDIUM -> Color(0xFFFFF8E1)
        else -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (diagnosis.isHealthy) {
                            Icons.Rounded.CheckCircle
                        } else {
                            Icons.Rounded.Warning
                        },
                        contentDescription = null,
                        tint = if (diagnosis.isHealthy) {
                            KisanColors.CropHealthGood
                        } else {
                            MaterialTheme.colorScheme.error
                        }
                    )
                    Text(
                        text = stringResource(R.string.crop_doctor_diagnosis_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                DataSourceBadge(isFromCloud = true)
            }
            ScanImagePreview(
                imageModel = resolveScanImageModel(scan = scan),
                contentDescription = stringResource(R.string.crop_doctor_captured_image),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(14.dp))
            )
            Text(
                text = diagnosis.diseaseName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = stringResource(
                    R.string.crop_doctor_confidence,
                    diagnosis.confidencePercent
                ),
                style = MaterialTheme.typography.labelLarge,
                color = if (isLowConfidence) {
                    KisanColors.CropHealthWarning
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                fontWeight = if (isLowConfidence) FontWeight.SemiBold else FontWeight.Normal
            )
            if (isLowConfidence) {
                LowConfidenceExpertCard(
                    thresholdPercent = confidenceThreshold,
                    onCallExpert = {
                        val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                            data = Uri.parse("tel:${CropDoctorConstants.EXPERT_CONTACT_PHONE}")
                        }
                        context.startActivity(dialIntent)
                    }
                )
            }
            if (!diagnosis.isHealthy) {
                DiagnosisSection(
                    title = stringResource(R.string.crop_doctor_symptoms),
                    content = diagnosis.symptoms
                )
                DiagnosisSection(
                    title = stringResource(R.string.crop_doctor_treatment),
                    content = diagnosis.treatmentAdvice
                )
            }
            Text(
                text = stringResource(R.string.crop_doctor_prevention_title),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
            diagnosis.preventionTips.forEach { tip ->
                Text(
                    text = "• $tip",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun LowConfidenceExpertCard(
    thresholdPercent: Int,
    onCallExpert: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
        border = BorderStroke(
            width = 1.dp,
            color = KisanColors.CropHealthWarning.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Warning,
                    contentDescription = null,
                    tint = KisanColors.CropHealthWarning
                )
                Text(
                    text = stringResource(R.string.crop_doctor_low_confidence_title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = KisanColors.CropHealthWarning
                )
            }
            Text(
                text = stringResource(
                    R.string.crop_doctor_low_confidence_message,
                    thresholdPercent
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            ExpertContactSection(onCallExpert = onCallExpert)
        }
    }
}

private fun formatExpertPhoneDisplay(phoneNumber: String): String {
    val digitsOnly = phoneNumber.filter { character -> character.isDigit() }
    return when (digitsOnly.length) {
        10 -> "${digitsOnly.substring(0, 5)}-${digitsOnly.substring(5)}"
        11 -> "${digitsOnly.substring(0, 4)}-${digitsOnly.substring(4, 7)}-${digitsOnly.substring(7)}"
        else -> phoneNumber
    }
}

@Composable
private fun DiagnosisSection(
    title: String,
    content: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.MedicalServices,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.secondary
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
        Text(
            text = content,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun HistorySection(
    scans: List<CropDoctorScan>,
    selectedScanId: String?,
    onSelectHistory: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.History,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = stringResource(R.string.crop_doctor_history_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            scans.forEach { scan ->
                HistoryItemCard(
                    scan = scan,
                    isSelected = scan.id == selectedScanId,
                    onClick = { onSelectHistory(scan.id) }
                )
            }
        }
    }
}

@Composable
private fun HistoryItemCard(
    scan: CropDoctorScan,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM", Locale.getDefault()) }
    Card(
        modifier = Modifier
            .width(140.dp)
            .clickable(onClick = onClick)
            .then(
                if (isSelected) {
                    Modifier.border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(14.dp)
                    )
                } else {
                    Modifier
                }
            ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            ScanImagePreview(
                imageModel = resolveScanImageModel(scan = scan),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .clip(RoundedCornerShape(10.dp))
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = scan.diagnosis.diseaseName,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
            Text(
                text = dateFormat.format(Date(scan.scannedAt)),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ScanImagePreview(
    imageModel: Any?,
    contentDescription: String?,
    modifier: Modifier = Modifier
) {
    if (imageModel == null) {
        ImageUnavailablePlaceholder(modifier = modifier)
        return
    }
    SubcomposeAsyncImage(
        model = imageModel,
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = ContentScale.Crop,
        loading = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            }
        },
        error = {
            ImageUnavailablePlaceholder(modifier = modifier)
        }
    )
}

@Composable
private fun ImageUnavailablePlaceholder(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.crop_doctor_image_unavailable),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(8.dp)
        )
    }
}

private fun resolveScanImageModel(scan: CropDoctorScan): Any? {
    val localFile = File(scan.imageLocalPath)
    if (localFile.exists() && localFile.length() > 0L) {
        return localFile
    }
    return scan.imageStorageUrl?.trim()?.takeIf { url -> url.isNotEmpty() }
}
