package be.monmoisclair.app.ui.scan

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import be.monmoisclair.app.domain.EpcQrParser
import be.monmoisclair.app.ui.components.PrimaryA11yButton
import be.monmoisclair.app.ui.components.SecondaryA11yButton
import be.monmoisclair.app.ui.theme.A11yTokens
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.delay

@Composable
fun ScanEpcScreen(onResult: (String) -> Unit, onCancel: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val haptics = LocalHapticFeedback.current
    var hasCamera by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    var status by remember { mutableStateOf("Placez le QR dans le cadre.") }
    var flash by remember { mutableStateOf(false) }
    val flashAlpha by animateFloatAsState(
        targetValue = if (flash) 0.55f else 0f,
        animationSpec = tween(180),
        label = "scanFlash"
    )
    val handled = remember { AtomicBoolean(false) }
    val nonEpcTold = remember { AtomicBoolean(false) }
    val previewView = remember { PreviewView(context) }
    var pendingRaw by remember { mutableStateOf<String?>(null) }
    var torchOn by remember { mutableStateOf(false) }
    var camera by remember { mutableStateOf<Camera?>(null) }
    var permanentlyDenied by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCamera = granted
        val activity = context as? android.app.Activity
        permanentlyDenied = !granted && activity != null &&
            !ActivityCompat.shouldShowRequestPermissionRationale(
                activity,
                Manifest.permission.CAMERA
            )
        if (!granted) status = "Autorisez la caméra pour scanner la facture."
    }

    LaunchedEffect(pendingRaw) {
        val raw = pendingRaw ?: return@LaunchedEffect
        flash = true
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        status = "Facture reconnue."
        delay(280)
        onResult(raw)
    }

    DisposableEffect(Unit) {
        if (!hasCamera) permissionLauncher.launch(Manifest.permission.CAMERA)
        onDispose { }
    }

    DisposableEffect(hasCamera, lifecycleOwner) {
        if (!hasCamera) {
            return@DisposableEffect onDispose { }
        }
        val mainExecutor = ContextCompat.getMainExecutor(context)
        val analysisExecutor = Executors.newSingleThreadExecutor()
        val scanner = BarcodeScanning.getClient()
        val providerHolder = arrayOfNulls<ProcessCameraProvider>(1)
        var disposed = false
        val future = ProcessCameraProvider.getInstance(context)
        future.addListener({
            if (disposed) return@addListener
            try {
                val provider = future.get()
                if (disposed) {
                    provider.unbindAll()
                    return@addListener
                }
                providerHolder[0] = provider
                val preview = Preview.Builder().build().also {
                    it.surfaceProvider = previewView.surfaceProvider
                }
                val analysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                analysis.setAnalyzer(analysisExecutor) { proxy ->
                    if (handled.get() || disposed) {
                        proxy.close()
                        return@setAnalyzer
                    }
                    val media = proxy.image ?: run {
                        proxy.close()
                        return@setAnalyzer
                    }
                    val image = InputImage.fromMediaImage(media, proxy.imageInfo.rotationDegrees)
                    scanner.process(image)
                        .addOnSuccessListener { barcodes ->
                            if (disposed) return@addOnSuccessListener
                            val raw = barcodes.firstOrNull {
                                it.format == Barcode.FORMAT_QR_CODE
                            }?.rawValue
                            if (raw == null) return@addOnSuccessListener
                            if (EpcQrParser.parse(raw) != null) {
                                if (handled.compareAndSet(false, true)) {
                                    pendingRaw = raw
                                }
                            } else if (nonEpcTold.compareAndSet(false, true)) {
                                mainExecutor.execute {
                                    status = "Ce QR n’est pas une facture belge."
                                }
                            }
                        }
                        .addOnFailureListener { e -> Log.w("ScanEpc", "fail", e) }
                        .addOnCompleteListener { proxy.close() }
                }
                provider.unbindAll()
                camera = provider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    analysis
                )
            } catch (e: Exception) {
                Log.e("ScanEpc", "bind", e)
                if (!disposed) status = "Caméra indisponible."
            }
        }, mainExecutor)

        onDispose {
            disposed = true
            torchOn = false
            camera = null
            runCatching { providerHolder[0]?.unbindAll() }
            runCatching {
                if (future.isDone) future.get().unbindAll()
            }
            runCatching { scanner.close() }
            analysisExecutor.shutdown()
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(A11yTokens.PaperCream)
            .imePadding()
            .padding(A11yTokens.screenPadding)
    ) {
        Text("Scanner une facture", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))
        Text(status, style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(12.dp))
        if (hasCamera) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(MaterialTheme.shapes.medium)
                    .border(1.dp, A11yTokens.PaperBorder, MaterialTheme.shapes.medium)
            ) {
                AndroidView(
                    factory = { previewView },
                    modifier = Modifier.fillMaxSize()
                )
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val frame = size.minDimension * 0.62f
                    val left = (size.width - frame) / 2f
                    val top = (size.height - frame) / 2f
                    drawRoundRect(
                        color = A11yTokens.PaperCard.copy(alpha = 0.92f),
                        topLeft = Offset(left, top),
                        size = Size(frame, frame),
                        cornerRadius = CornerRadius(24f, 24f),
                        style = Stroke(width = 4f)
                    )
                    val cx = size.width / 2f
                    val cy = size.height / 2f
                    drawLine(
                        color = A11yTokens.ButtonFill.copy(alpha = 0.9f),
                        start = Offset(cx - 28f, cy),
                        end = Offset(cx + 28f, cy),
                        strokeWidth = 3f
                    )
                    drawLine(
                        color = A11yTokens.ButtonFill.copy(alpha = 0.9f),
                        start = Offset(cx, cy - 28f),
                        end = Offset(cx, cy + 28f),
                        strokeWidth = 3f
                    )
                }
                if (flashAlpha > 0.01f) {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(A11yTokens.AccentOk.copy(alpha = flashAlpha))
                    )
                }
            }
        } else {
            Spacer(Modifier.weight(1f))
            if (permanentlyDenied) {
                SecondaryA11yButton(
                    text = "Ouvrir les réglages",
                    onClick = {
                        context.startActivity(
                            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                        )
                    }
                )
            } else {
                PrimaryA11yButton(
                    text = "Autoriser la caméra",
                    onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }
                )
            }
        }
        if (hasCamera) {
            Spacer(Modifier.height(8.dp))
            SecondaryA11yButton(
                text = if (torchOn) "Éteindre la lampe" else "Lampe",
                onClick = {
                    torchOn = !torchOn
                    camera?.cameraControl?.enableTorch(torchOn)
                }
            )
        }
        Spacer(Modifier.height(12.dp))
        SecondaryA11yButton(text = "Annuler", onClick = onCancel)
    }
}
