package com.kisanalert.presentation.cropdoctor

import android.content.Context
import android.net.Uri
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import java.io.File
import java.util.concurrent.Executor

@Composable
fun CropDoctorCameraPreview(
    modifier: Modifier = Modifier,
    isActive: Boolean,
    onImageCaptureReady: (ImageCapture) -> Unit,
    onCameraError: (String) -> Unit
) {
    val context: Context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    var previewView by remember { mutableStateOf<PreviewView?>(null) }
    DisposableEffect(isActive, previewView) {
        if (!isActive || previewView == null) {
            onDispose { }
            return@DisposableEffect onDispose { }
        }
        val executor: Executor = ContextCompat.getMainExecutor(context)
        val listener = Runnable {
            bindCamera(
                context = context,
                lifecycleOwner = lifecycleOwner,
                previewView = previewView!!,
                cameraProviderFuture = cameraProviderFuture,
                onImageCaptureReady = onImageCaptureReady,
                onCameraError = onCameraError
            )
        }
        cameraProviderFuture.addListener(listener, executor)
        onDispose {
            runCatching {
                cameraProviderFuture.get().unbindAll()
            }
        }
    }
    AndroidView(
        modifier = modifier,
        factory = { viewContext ->
            PreviewView(viewContext).apply {
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                previewView = this
            }
        }
    )
}

private fun bindCamera(
    context: Context,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    previewView: PreviewView,
    cameraProviderFuture: com.google.common.util.concurrent.ListenableFuture<ProcessCameraProvider>,
    onImageCaptureReady: (ImageCapture) -> Unit,
    onCameraError: (String) -> Unit
) {
    try {
        val cameraProvider = cameraProviderFuture.get()
        val preview = Preview.Builder().build().also { previewUseCase ->
            previewUseCase.surfaceProvider = previewView.surfaceProvider
        }
        val imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()
        onImageCaptureReady(imageCapture)
        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(
            lifecycleOwner,
            CameraSelector.DEFAULT_BACK_CAMERA,
            preview,
            imageCapture
        )
    } catch (exception: Exception) {
        onCameraError(exception.localizedMessage ?: "Unable to start camera.")
    }
}

fun saveCropImageFromUri(
    context: Context,
    imageUri: Uri,
    onSaved: (String) -> Unit,
    onError: (String) -> Unit
) {
    try {
        val photoFile = File(context.cacheDir, "crop_scan_${System.currentTimeMillis()}.jpg")
        val inputStream = context.contentResolver.openInputStream(imageUri)
        if (inputStream == null) {
            onError("Unable to read the selected image.")
            return
        }
        inputStream.use { input ->
            photoFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        onSaved(photoFile.absolutePath)
    } catch (exception: Exception) {
        onError(exception.localizedMessage ?: "Failed to load the selected image.")
    }
}

fun captureCropImage(
    imageCapture: ImageCapture,
    context: Context,
    onCaptured: (String) -> Unit,
    onError: (String) -> Unit
) {
    val photoFile = File(context.cacheDir, "crop_scan_${System.currentTimeMillis()}.jpg")
    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
    imageCapture.takePicture(
        outputOptions,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                onCaptured(photoFile.absolutePath)
            }
            override fun onError(exception: ImageCaptureException) {
                onError(exception.localizedMessage ?: "Failed to capture image.")
            }
        }
    )
}
