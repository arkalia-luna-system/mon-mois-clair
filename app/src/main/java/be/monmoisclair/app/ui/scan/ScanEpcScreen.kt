package be.monmoisclair.app.ui.scan

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
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

@Composable
fun ScanEpcScreen(onResult: (String) -> Unit, onCancel: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var hasCamera by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    var status by remember { mutableStateOf("Pointez la caméra vers le QR Code de la facture.") }
    val handled = remember { AtomicBoolean(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCamera = granted
        if (!granted) status = "Autorisez la caméra pour scanner la facture."
    }
    DisposableEffect(Unit) {
        if (!hasCamera) permissionLauncher.launch(Manifest.permission.CAMERA)
        onDispose { }
    }
    Column(
        Modifier.fillMaxSize().background(A11yTokens.PaperCream).padding(A11yTokens.screenPadding)
    ) {
        Text("Scanner une facture", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))
        Text(status, style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(12.dp))
        if (hasCamera) {
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx)
                    ProcessCameraProvider.getInstance(ctx).addListener({
                        val cameraProvider = ProcessCameraProvider.getInstance(ctx).get()
                        val preview = Preview.Builder().build().also {
                            it.surfaceProvider = previewView.surfaceProvider
                        }
                        val analysis = ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()
                        val scanner = BarcodeScanning.getClient()
                        analysis.setAnalyzer(Executors.newSingleThreadExecutor()) { proxy ->
                            if (handled.get()) { proxy.close(); return@setAnalyzer }
                            val media = proxy.image ?: run { proxy.close(); return@setAnalyzer }
                            val image = InputImage.fromMediaImage(media, proxy.imageInfo.rotationDegrees)
                            scanner.process(image)
                                .addOnSuccessListener { barcodes ->
                                    val raw = barcodes.firstOrNull { it.format == Barcode.FORMAT_QR_CODE }?.rawValue
                                    if (raw != null && EpcQrParser.parse(raw) != null &&
                                        handled.compareAndSet(false, true)
                                    ) onResult(raw)
                                }
                                .addOnFailureListener { e -> Log.w("ScanEpc", "fail", e) }
                                .addOnCompleteListener { proxy.close() }
                        }
                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, analysis
                            )
                        } catch (e: Exception) {
                            Log.e("ScanEpc", "bind", e)
                            status = "Caméra indisponible."
                        }
                    }, ContextCompat.getMainExecutor(ctx))
                    previewView
                },
                modifier = Modifier.fillMaxWidth().weight(1f)
            )
        } else {
            Spacer(Modifier.weight(1f))
            PrimaryA11yButton(
                text = "Autoriser la caméra",
                onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                enabled = true
            )
        }
        Spacer(Modifier.height(12.dp))
        SecondaryA11yButton(
            text = "Annuler",
            onClick = onCancel,
            enabled = true
        )
    }
}
