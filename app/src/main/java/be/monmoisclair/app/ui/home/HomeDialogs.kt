package be.monmoisclair.app.ui.home

import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import be.monmoisclair.app.ui.components.CarnetBannerKind
import be.monmoisclair.app.ui.components.CarnetStateBanner
import be.monmoisclair.app.ui.components.PrimaryA11yButton
import be.monmoisclair.app.ui.components.SecondaryA11yButton
import be.monmoisclair.app.ui.theme.A11yTokens
import be.monmoisclair.app.ui.theme.DisplayFit
import be.monmoisclair.app.ui.theme.HomeTokens
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/** Nouveautés après mise à jour (une fois par versionCode). */
@Composable
internal fun HomeWhatsNewDialog(
    bullets: List<String>,
    versionLabel: String?,
    fit: DisplayFit,
    onDismiss: () -> Unit
) {
    if (bullets.isEmpty()) return
    Dialog(
        onDismissRequest = { /* fermeture = bouton « J’ai compris » seulement */ },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
        )
    ) {
        Surface(
            color = HomeTokens.Card,
            shape = MaterialTheme.shapes.extraLarge,
            modifier = Modifier
                .fillMaxWidth()
                .padding(A11yTokens.screenPadding)
        ) {
            Column(
                modifier = Modifier
                    .padding(if (fit.compactHomeChrome) 12.dp else 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(
                    if (fit.compactHomeChrome) 8.dp else 12.dp
                )
            ) {
                Text(
                    text = WhatsNewCopy.TITLE,
                    style = MaterialTheme.typography.titleLarge,
                    color = A11yTokens.Ink
                )
                if (!versionLabel.isNullOrBlank()) {
                    Text(
                        text = versionLabel,
                        style = MaterialTheme.typography.titleMedium,
                        color = HomeTokens.Link
                    )
                }
                bullets.forEach { line ->
                    Text(
                        text = "• $line",
                        style = MaterialTheme.typography.bodyLarge,
                        color = A11yTokens.Ink
                    )
                }
                PrimaryA11yButton(
                    text = WhatsNewCopy.DISMISS,
                    onClick = onDismiss
                )
            }
        }
    }
}

/** Rappel sauvegarde — une fois si aucune copie n’a encore été faite. */
@Composable
internal fun HomeBackupNudgeDialog(
    onLater: () -> Unit,
    onOpenBackup: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onLater,
        shape = MaterialTheme.shapes.extraLarge,
        containerColor = A11yTokens.PaperCard,
        title = {
            Text(
                text = WhatsNewCopy.BACKUP_NUDGE_TITLE,
                style = MaterialTheme.typography.titleLarge,
                color = A11yTokens.Ink
            )
        },
        text = {
            Text(
                text = WhatsNewCopy.BACKUP_NUDGE_BODY,
                style = MaterialTheme.typography.bodyLarge,
                color = A11yTokens.Ink
            )
        },
        confirmButton = {
            PrimaryA11yButton(
                text = WhatsNewCopy.BACKUP_NUDGE_OPEN,
                onClick = onOpenBackup,
                fillMaxWidth = false,
                modifier = Modifier.padding(end = 8.dp, bottom = 8.dp)
            )
        },
        dismissButton = {
            SecondaryA11yButton(
                text = WhatsNewCopy.BACKUP_NUDGE_LATER,
                onClick = onLater,
                fillMaxWidth = false,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
    )
}

/** Dialogue photo de facture (Home). */
@Composable
internal fun HomeReceiptDialog(
    title: String,
    path: String?,
    bitmap: Bitmap?,
    loading: Boolean,
    fit: DisplayFit,
    snackbarHostState: SnackbarHostState,
    scope: CoroutineScope,
    viewModel: HomeViewModel,
    onDismiss: () -> Unit
) {
    if (path == null) return
    val context = LocalContext.current
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            color = HomeTokens.Card,
            shape = MaterialTheme.shapes.extraLarge,
            modifier = Modifier
                .fillMaxWidth()
                .padding(A11yTokens.screenPadding)
        ) {
            Column(
                modifier = Modifier.padding(if (fit.compactHomeChrome) 12.dp else 16.dp),
                verticalArrangement = Arrangement.spacedBy(
                    if (fit.compactHomeChrome) 8.dp else 12.dp
                )
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = A11yTokens.Ink,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                when {
                    loading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .background(HomeTokens.HeroSurface, MaterialTheme.shapes.medium)
                                .semantics {
                                    contentDescription = "Chargement de la photo"
                                    liveRegion = LiveRegionMode.Polite
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = A11yTokens.Link)
                        }
                    }
                    bitmap != null -> {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Photo de la facture",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(
                                    min = if (fit.compactHomeChrome) 220.dp else 280.dp,
                                    max = if (fit.compactHomeChrome) 420.dp else 520.dp
                                )
                                .background(HomeTokens.HeroSurface, MaterialTheme.shapes.medium)
                        )
                    }
                    else -> {
                        CarnetStateBanner(
                            text = "Impossible d’afficher cette photo ici. " +
                                "Essayez « Ouvrir à part », ou rouvrez la ligne pour en reprendre une.",
                            kind = CarnetBannerKind.Error,
                            pad = 12.dp
                        )
                        SecondaryA11yButton(
                            text = "Ouvrir à part",
                            onClick = {
                                if (path.isBlank()) return@SecondaryA11yButton
                                scope.launch {
                                    val intent = viewModel.viewReceiptIntent(path)
                                    if (intent == null) {
                                        snackbarHostState.showSnackbar(
                                            "Fichier introuvable — reprenez une photo."
                                        )
                                    } else {
                                        val ok = runCatching {
                                            context.startActivity(
                                                Intent.createChooser(intent, "Voir la photo")
                                            )
                                        }.isSuccess
                                        if (!ok) {
                                            snackbarHostState.showSnackbar(
                                                "Aucune app pour ouvrir cette photo."
                                            )
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
                SecondaryA11yButton(
                    text = "Fermer",
                    onClick = onDismiss
                )
            }
        }
    }
}

/** Dialogue FAIT sur mois passé. */
@Composable
internal fun HomePastToggleDialog(
    monthLabel: String,
    pastMarkingDone: Boolean,
    pendingId: String?,
    timeline: List<TimelineItem>,
    haptics: HapticFeedback,
    snackbarHostState: SnackbarHostState,
    scope: CoroutineScope,
    viewModel: HomeViewModel,
    onFaitUndoActive: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    if (pendingId == null) return
    // H14 7.11 : dialog carte soft (PaperCard + XL).
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.extraLarge,
        containerColor = A11yTokens.PaperCard,
        title = {
            Text(
                text = "Mois passé",
                style = MaterialTheme.typography.titleLarge,
                color = A11yTokens.Ink
            )
        },
        text = {
            Text(
                text = if (pastMarkingDone) {
                    "Vous êtes sur un mois passé ($monthLabel). " +
                        "Marquer cette ligne FAIT ?"
                } else {
                    "Vous êtes sur un mois passé ($monthLabel). " +
                        "Remettre cette ligne à payer ?"
                },
                style = MaterialTheme.typography.bodyLarge,
                color = A11yTokens.Ink
            )
        },
        confirmButton = {
            PrimaryA11yButton(
                text = if (pastMarkingDone) "Marquer FAIT" else "Remettre à payer",
                onClick = {
                    val id = pendingId
                    if (!viewModel.confirmPastToggle()) return@PrimaryA11yButton
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    scope.launch {
                        onFaitUndoActive(true)
                        try {
                            val agreementRemettre = !pastMarkingDone &&
                                timeline
                                    .filterIsInstance<TimelineItem.Expense>()
                                    .any { it.id == id && it.isAgreement }
                            val result = snackbarHostState.showSnackbar(
                                message = when {
                                    pastMarkingDone -> "Noté FAIT — touchez Annuler si erreur"
                                    agreementRemettre ->
                                        "Dossier rouvert — les mois suivants peuvent revenir"
                                    else -> "Remis à payer"
                                },
                                actionLabel = "Annuler",
                                withDismissAction = true,
                                duration = SnackbarDuration.Long
                            )
                            if (result == SnackbarResult.ActionPerformed) {
                                viewModel.undoToggleDone(id)
                            }
                        } finally {
                            onFaitUndoActive(false)
                        }
                    }
                },
                fillMaxWidth = false,
                modifier = Modifier.padding(end = 8.dp, bottom = 8.dp)
            )
        },
        dismissButton = {
            SecondaryA11yButton(
                text = "Annuler",
                onClick = onDismiss,
                fillMaxWidth = false,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
    )
}
