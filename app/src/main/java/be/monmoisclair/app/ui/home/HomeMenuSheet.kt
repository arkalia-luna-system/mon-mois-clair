package be.monmoisclair.app.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import be.monmoisclair.app.ui.components.CarnetHubRow
import be.monmoisclair.app.ui.components.SecondaryA11yButton
import be.monmoisclair.app.ui.theme.A11yTokens
import be.monmoisclair.app.ui.theme.rememberDisplayFit
import be.monmoisclair.app.ui.theme.rememberSheetBottomClearance

/**
 * Hub hors ledger — outils + lien calendrier (pas de mur de mois ici).
 * Aide / restore : dans Réglages. Alertes = signal en tête si bloquées.
 * Titre « Menu » = InkMuted, distinct des [CarnetHubRow] cliquables.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeMenuSheet(
    isPastMonth: Boolean,
    isFuturePreview: Boolean = false,
    onDismiss: () -> Unit,
    onOpenMonthPicker: () -> Unit,
    onGoToCurrentMonth: () -> Unit,
    onOpenSettings: () -> Unit,
    /** 0 = pas de bouton (zéro dossier = zéro pixel), sauf courriers sans plan. */
    dossierCount: Int = 0,
    pendingLetterCount: Int = 0,
    onOpenDossiers: () -> Unit = {},
    onOpenUpcoming: () -> Unit = {},
    remindersBlocked: Boolean = false,
    onOpenHealth: () -> Unit = {}
) {
    val showDossiers = dossierCount > 0 || pendingLetterCount > 0
    val menuDossierLabel = when {
        dossierCount > 0 && pendingLetterCount > 0 ->
            "Dossiers ($dossierCount) · $pendingLetterCount courrier" +
                if (pendingLetterCount > 1) "s" else ""
        dossierCount > 0 -> "Dossiers ($dossierCount)"
        else ->
            "Dossiers · $pendingLetterCount courrier" +
                if (pendingLetterCount > 1) "s" else ""
    }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val fit = rememberDisplayFit()
    val menuGap = if (fit.compactHomeChrome) 10.dp else 14.dp
    val sheetBottom = rememberSheetBottomClearance()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        containerColor = A11yTokens.PaperCream,
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = sheetBottom)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = A11yTokens.screenPadding)
                .padding(bottom = 8.dp)
        ) {
            Text(
                text = "Menu",
                style = MaterialTheme.typography.titleLarge,
                color = A11yTokens.InkMuted
            )
            Spacer(modifier = Modifier.height(menuGap))

            if (remindersBlocked) {
                CarnetHubRow(
                    label = "Alertes",
                    onClick = {
                        onDismiss()
                        onOpenHealth()
                    },
                    labelColor = A11yTokens.AccentWarn,
                    showDivider = false
                )
            }

            CarnetHubRow(
                label = "Réglages",
                onClick = {
                    onDismiss()
                    onOpenSettings()
                },
                showDivider = !remindersBlocked
            )
            if (showDossiers) {
                CarnetHubRow(
                    label = menuDossierLabel,
                    onClick = {
                        onDismiss()
                        onOpenDossiers()
                    }
                )
            }
            CarnetHubRow(
                label = "À venir",
                onClick = {
                    onDismiss()
                    onOpenUpcoming()
                }
            )
            CarnetHubRow(
                label = "Choisir un mois",
                onClick = {
                    onDismiss()
                    onOpenMonthPicker()
                }
            )

            if (isPastMonth || isFuturePreview) {
                Spacer(modifier = Modifier.height(12.dp))
                SecondaryA11yButton(
                    text = "Mois en cours",
                    onClick = {
                        onGoToCurrentMonth()
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
