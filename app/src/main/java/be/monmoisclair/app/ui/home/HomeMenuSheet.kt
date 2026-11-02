package be.monmoisclair.app.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import be.monmoisclair.app.R
import be.monmoisclair.app.ui.components.CarnetHubRow
import be.monmoisclair.app.ui.components.CarnetPageTitle
import be.monmoisclair.app.ui.components.CarnetSheetHandle
import be.monmoisclair.app.ui.components.QuietLinkA11y
import be.monmoisclair.app.ui.components.SecondaryA11yButton
import be.monmoisclair.app.ui.theme.A11yTokens
import be.monmoisclair.app.ui.theme.HomeTokens
import be.monmoisclair.app.ui.theme.rememberDisplayFit
import be.monmoisclair.app.ui.theme.rememberSheetBottomClearance

/**
 * Hub hors ledger — outils + lien calendrier (pas de mur de mois ici).
 * Aide / restore : dans Réglages. Contact = QuietLink bas (≠ Restore).
 * Titre « Menu » = PageTitle (InkMuted), distinct des [CarnetHubRow] cliquables.
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
    /** Ouverts (non soldés) — chiffre du Menu aligné sur « En cours ». */
    dossierCount: Int = 0,
    /** Accords soldés seuls : Menu reste visible sans faux « (N) ». */
    hasDossiers: Boolean = false,
    pendingLetterCount: Int = 0,
    onOpenDossiers: () -> Unit = {},
    onOpenUpcoming: () -> Unit = {},
    remindersBlocked: Boolean = false,
    onOpenHealth: () -> Unit = {},
    onOpenSupport: () -> Unit = {}
) {
    val showDossiers = hasDossiers || dossierCount > 0 || pendingLetterCount > 0
    // Libellé court « Dossiers » (= PageTitle / Aide « Menu → Dossiers »).
    val menuDossierLabel = when {
        dossierCount > 0 && pendingLetterCount > 0 ->
            "Dossiers ($dossierCount) · $pendingLetterCount courrier" +
                if (pendingLetterCount > 1) "s" else ""
        dossierCount > 0 -> "Dossiers ($dossierCount)"
        pendingLetterCount > 0 ->
            "Dossiers · $pendingLetterCount courrier" +
                if (pendingLetterCount > 1) "s" else ""
        else -> "Dossiers"
    }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val fit = rememberDisplayFit()
    val menuGap = if (fit.compactHomeChrome) 10.dp else 14.dp
    val sheetBottom = rememberSheetBottomClearance()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = MaterialTheme.shapes.extraLarge,
        dragHandle = { CarnetSheetHandle() },
        containerColor = HomeTokens.Canvas,
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
            CarnetPageTitle("Menu", withTopSpacer = false)
            Spacer(modifier = Modifier.height(menuGap))

            // 7.12 : leading icons + sections densifiées (top-app rows).

            if (remindersBlocked) {
                CarnetHubRow(
                    label = "Alertes",
                    onClick = {
                        onDismiss()
                        onOpenHealth()
                    },
                    labelColor = A11yTokens.AccentWarn,
                    showDivider = false,
                    leadingIconRes = R.drawable.ic_alerts
                )
            }

            CarnetHubRow(
                label = "Réglages",
                onClick = {
                    onDismiss()
                    onOpenSettings()
                },
                showDivider = !remindersBlocked,
                leadingIconRes = R.drawable.ic_settings
            )
            if (showDossiers) {
                CarnetHubRow(
                    label = menuDossierLabel,
                    onClick = {
                        onDismiss()
                        onOpenDossiers()
                    },
                    leadingIconRes = R.drawable.ic_folder
                )
            }
            CarnetHubRow(
                label = "À venir",
                onClick = {
                    onDismiss()
                    onOpenUpcoming()
                },
                leadingIconRes = R.drawable.ic_upcoming
            )
            CarnetHubRow(
                label = "Choisir un mois",
                onClick = {
                    onDismiss()
                    onOpenMonthPicker()
                },
                leadingIconRes = R.drawable.ic_calendar_month
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

            Spacer(modifier = Modifier.height(if (fit.compactHomeChrome) 14.dp else 18.dp))
            QuietLinkA11y(
                text = "Un souci ? Écrire à Arkalia Système",
                onClick = {
                    onDismiss()
                    onOpenSupport()
                },
                onClickLabel = "Écrire à Arkalia Système — bug, idée ou question"
            )
        }
    }
}
