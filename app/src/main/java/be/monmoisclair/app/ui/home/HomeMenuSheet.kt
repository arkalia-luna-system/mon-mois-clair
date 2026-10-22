package be.monmoisclair.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import be.monmoisclair.app.domain.AppClock
import be.monmoisclair.app.ui.components.SecondaryA11yButton
import be.monmoisclair.app.ui.format.DayLabelFormat
import be.monmoisclair.app.ui.theme.A11yTokens
import be.monmoisclair.app.ui.theme.StampShape
import be.monmoisclair.app.ui.theme.rememberDisplayFit
import be.monmoisclair.app.ui.theme.rememberSheetBottomClearance
import java.time.YearMonth

/**
 * Hub hors ledger — un seul outil (Réglages), puis calendrier des mois.
 * Alertes / aide / restore : dans Réglages (pas de doublon).
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HomeMenuSheet(
    selectedYearMonth: YearMonth,
    isPastMonth: Boolean,
    isFuturePreview: Boolean = false,
    onDismiss: () -> Unit,
    onSelectMonth: (YearMonth) -> Unit,
    onGoToCurrentMonth: () -> Unit,
    onOpenSettings: () -> Unit,
    /** 0 = pas de bouton (zéro dossier = zéro pixel), sauf courriers sans plan. */
    dossierCount: Int = 0,
    pendingLetterCount: Int = 0,
    onOpenDossiers: () -> Unit = {},
    onOpenUpcoming: () -> Unit = {}
) {
    val showDossiers = dossierCount > 0 || pendingLetterCount > 0
    val menuDossierLabel = when {
        dossierCount > 0 && pendingLetterCount > 0 ->
            "Mes dossiers ($dossierCount) · $pendingLetterCount courrier" +
                if (pendingLetterCount > 1) "s" else ""
        dossierCount > 0 -> "Mes dossiers ($dossierCount)"
        else ->
            "Mes dossiers · $pendingLetterCount courrier" +
                if (pendingLetterCount > 1) "s" else ""
    }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val current = AppClock.currentYearMonth()
    val months = HomeMonthChoices.recentMonths(current)
    val fit = rememberDisplayFit()
    val menuGap = if (fit.compactHomeChrome) 12.dp else 16.dp
    val sheetBottom = rememberSheetBottomClearance()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        // MEN03 : coins feuille = 16 dp (famille cartes), pas 12.
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        containerColor = A11yTokens.PaperCream,
        // On gère la marge basse nous-mêmes (secours si insets dialogue = 0).
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                // Hors du défilement : la zone visible s’arrête au-dessus de la barre.
                .padding(bottom = sheetBottom)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = A11yTokens.screenPadding)
                .padding(bottom = 8.dp)
        ) {
            // MEN01 : titre « Menu » seul (pas wordmark + titre).
            Text(
                text = "Menu",
                style = MaterialTheme.typography.headlineMedium,
                color = A11yTokens.Ink
            )
            Text(
                text = if (showDossiers) {
                    "Réglages, dossiers, ou un autre mois."
                } else {
                    "Réglages, ou un autre mois."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = A11yTokens.InkMuted,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            )
            Spacer(modifier = Modifier.height(menuGap))

            SecondaryA11yButton(
                text = "Réglages",
                onClick = {
                    onDismiss()
                    onOpenSettings()
                },
                modifier = Modifier.fillMaxWidth()
            )
            if (showDossiers) {
                Spacer(modifier = Modifier.height(8.dp))
                SecondaryA11yButton(
                    text = menuDossierLabel,
                    onClick = {
                        onDismiss()
                        onOpenDossiers()
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            SecondaryA11yButton(
                text = "À venir",
                onClick = {
                    onDismiss()
                    onOpenUpcoming()
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(if (fit.compactHomeChrome) 14.dp else 20.dp))
            // MEN01 : section Mois = titre typo, pas carte-titre.
            Text(
                text = "Mois",
                style = MaterialTheme.typography.titleLarge,
                color = A11yTokens.Ink
            )
            Text(
                text = "Mois à venir en tête (aperçu) — sans FAIT ni ajout.",
                style = MaterialTheme.typography.bodyMedium,
                color = A11yTokens.InkMuted,
                modifier = Modifier.padding(top = 6.dp)
            )
            val byYear = months.groupBy { it.year }
            HomeMonthChoices.yearsInOrder(months).forEach { year ->
                val yearMonths = byYear[year] ?: return@forEach
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = year.toString(),
                    style = MaterialTheme.typography.titleLarge,
                    color = A11yTokens.Ink
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    yearMonths.forEach { ym ->
                        val selected = ym == selectedYearMonth
                        val chipLabel = DayLabelFormat.monthChipShort(ym)
                        // MEN02 : pastilles exclusives = capsule.
                        val chipShape = StampShape
                        Surface(
                            color = if (selected) A11yTokens.ButtonFill else A11yTokens.PaperWarm,
                            shape = chipShape,
                            modifier = Modifier
                                .heightIn(min = A11yTokens.minTouchHeight)
                                .border(
                                    1.dp,
                                    if (selected) A11yTokens.ButtonFill else A11yTokens.PaperBorder,
                                    chipShape
                                )
                                .semantics(mergeDescendants = true) {
                                    contentDescription = if (selected) {
                                        "Mois affiché : $chipLabel"
                                    } else {
                                        "Aller à $chipLabel"
                                    }
                                    this.selected = selected
                                }
                        ) {
                            Text(
                                text = chipLabel,
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (selected) A11yTokens.ButtonText else A11yTokens.Ink,
                                modifier = Modifier
                                    .clickable(
                                        role = Role.Button,
                                        onClickLabel = "Afficher $chipLabel"
                                    ) {
                                        onSelectMonth(ym)
                                        onDismiss()
                                    }
                                    .padding(horizontal = 14.dp, vertical = 12.dp)
                            )
                        }
                    }
                }
            }
            if (isPastMonth || isFuturePreview) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = if (isFuturePreview) {
                        "Aperçu d’un mois à venir. Rien n’est coché FAIT ici — le mois en cours reste sur l’accueil."
                    } else {
                        "Vous consultez un mois passé. Les totaux ne bougent plus."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = A11yTokens.InkMuted,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                SecondaryA11yButton(
                    text = "Revenir au mois en cours",
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
