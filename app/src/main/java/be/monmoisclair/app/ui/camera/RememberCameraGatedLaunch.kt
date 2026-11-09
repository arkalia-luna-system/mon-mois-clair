package be.monmoisclair.app.ui.camera

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/**
 * Lance [onGranted] seulement si CAMERA est accordée.
 * Sinon : demande permission ; refus permanent → message + deep-link réglages optionnel.
 */
@Composable
fun rememberCameraGatedLaunch(
    onGranted: () -> Unit,
    onMessage: (String) -> Unit,
    onOpenSettings: (() -> Unit)? = null
): () -> Unit {
    val context = LocalContext.current
    var pendingAfterGrant by remember { mutableStateOf(false) }

    fun hasCamera(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            if (pendingAfterGrant) {
                pendingAfterGrant = false
                onGranted()
            }
            return@rememberLauncherForActivityResult
        }
        pendingAfterGrant = false
        val activity = context as? Activity
        val permanentlyDenied = activity != null &&
            !ActivityCompat.shouldShowRequestPermissionRationale(
                activity,
                Manifest.permission.CAMERA
            )
        if (permanentlyDenied) {
            onMessage(CameraCaptureMessages.PERMANENTLY_DENIED)
            onOpenSettings?.invoke()
        } else {
            onMessage(CameraCaptureMessages.NEED_PERMISSION)
        }
    }

    return {
        if (hasCamera()) {
            onGranted()
        } else {
            pendingAfterGrant = true
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }
}

fun openAppDetailsSettings(context: android.content.Context) {
    runCatching {
        context.startActivity(
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        )
    }
}
