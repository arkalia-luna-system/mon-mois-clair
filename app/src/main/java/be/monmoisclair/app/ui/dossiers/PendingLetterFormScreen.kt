package be.monmoisclair.app.ui.dossiers

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import be.monmoisclair.app.ui.components.PinnedBackScaffold
import be.monmoisclair.app.ui.components.PrimaryA11yButton
import be.monmoisclair.app.ui.components.SecondaryA11yButton
import be.monmoisclair.app.ui.theme.A11yTokens
import be.monmoisclair.app.ui.theme.rememberDisplayFit
import java.io.File

@Composable
fun PendingLetterFormScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: PendingLetterFormViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val fit = rememberDisplayFit()
    val tipPad = if (fit.compactHomeChrome) 12.dp else 14.dp
    var pendingPreview by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(state.saved) {
        if (state.saved) onSaved()
    }

    val cameraDir = remember {
        File(context.cacheDir, "camera").also { it.mkdirs() }
    }
    var cameraUri by remember { mutableStateOf<Uri?>(null) }
    val takePicture = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { ok ->
        if (!ok) return@rememberLauncherForActivityResult
        val uri = cameraUri ?: return@rememberLauncherForActivityResult
        val bmp = runCatching {
            if (Build.VERSION.SDK_INT >= 28) {
                ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))
            } else {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            }
        }.getOrNull()
        if (bmp == null) {
            viewModel.setPhotoError()
            return@rememberLauncherForActivityResult
        }
        pendingPreview = bmp
        viewModel.attachPreviewBitmap(bmp)
    }

    PinnedBackScaffold(onBack = onBack) {
        Text(
            text = "Courrier sans plan",
            style = MaterialTheme.typography.headlineMedium,
            color = A11yTokens.Ink
        )
        val tipShape = MaterialTheme.shapes.medium
        Text(
            text = "Qui, pour quoi, photo — pas de montant. Rien dans le Reste.",
            style = MaterialTheme.typography.bodyLarge,
            color = A11yTokens.InkMuted,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, A11yTokens.PaperBorder, tipShape)
                .background(A11yTokens.PaperCard, tipShape)
                .padding(tipPad)
        )
        Column(verticalArrangement = Arrangement.spacedBy(A11yTokens.cardGap)) {
            OutlinedTextField(
                value = state.who,
                onValueChange = viewModel::setWho,
                label = { Text("À qui") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = A11yTokens.minTouchHeight),
                textStyle = MaterialTheme.typography.bodyLarge,
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
                enabled = !state.saving
            )
            OutlinedTextField(
                value = state.what,
                onValueChange = viewModel::setWhat,
                label = { Text("Pour quoi (ex. Hôpital 2025)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = A11yTokens.minTouchHeight),
                textStyle = MaterialTheme.typography.bodyLarge,
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
                enabled = !state.saving
            )
            SecondaryA11yButton(
                text = if (pendingPreview != null) "Reprendre la photo" else "Photo du courrier",
                enabled = !state.saving,
                onClick = {
                    val file = File(cameraDir, "pending_${System.currentTimeMillis()}.jpg")
                    val uri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        file
                    )
                    cameraUri = uri
                    takePicture.launch(uri)
                }
            )
            pendingPreview?.let { bmp ->
                val okShape = MaterialTheme.shapes.medium
                Text(
                    text = "Photo prête. Touchez Ranger pour la garder.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = A11yTokens.AccentOk,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, A11yTokens.AccentOk.copy(alpha = 0.35f), okShape)
                        .background(A11yTokens.AccentOkSoft, okShape)
                        .padding(tipPad)
                        .semantics {
                            liveRegion = LiveRegionMode.Polite
                            contentDescription = "Photo prête. Touchez Ranger pour la garder."
                        }
                )
                Image(
                    bitmap = bmp.asImageBitmap(),
                    contentDescription = "Aperçu du courrier",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .background(A11yTokens.PaperWarm)
                        .border(1.dp, A11yTokens.PaperBorder, MaterialTheme.shapes.medium)
                )
            }
            state.error?.let { err ->
                val errShape = MaterialTheme.shapes.medium
                Text(
                    err,
                    style = MaterialTheme.typography.bodyLarge,
                    color = A11yTokens.AccentDanger,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, A11yTokens.AccentDanger.copy(alpha = 0.4f), errShape)
                        .background(A11yTokens.PaperWarm, errShape)
                        .padding(tipPad)
                        .semantics {
                            liveRegion = LiveRegionMode.Assertive
                            contentDescription = err
                        }
                )
            }
            if (state.saving) {
                val busyShape = MaterialTheme.shapes.medium
                Text(
                    text = "Enregistrement du courrier…",
                    style = MaterialTheme.typography.bodyMedium,
                    color = A11yTokens.InkMuted,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, A11yTokens.PaperBorder, busyShape)
                        .background(A11yTokens.PaperCard, busyShape)
                        .padding(tipPad)
                        .semantics {
                            liveRegion = LiveRegionMode.Polite
                            contentDescription = "Enregistrement du courrier"
                        }
                )
            }
            PrimaryA11yButton(
                text = if (state.saving) "Enregistrement…" else "Ranger",
                onClick = viewModel::save,
                enabled = !state.saving
            )
            SecondaryA11yButton(
                text = "Annuler",
                onClick = onBack,
                enabled = !state.saving
            )
        }
    }
}
