package be.monmoisclair.app.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import be.monmoisclair.app.data.biometric.BiometricAuthResult
import be.monmoisclair.app.data.biometric.BiometricAvailability
import be.monmoisclair.app.ui.components.DangerA11yButton
import be.monmoisclair.app.ui.components.PinnedBackScaffold
import be.monmoisclair.app.ui.components.SecondaryA11yButton
import be.monmoisclair.app.ui.entry.DayOfMonthGrid
import be.monmoisclair.app.ui.theme.A11yTokens
import be.monmoisclair.app.ui.theme.rememberDisplayFit
import kotlinx.coroutines.flow.collectLatest

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onOpenHealth: () -> Unit,
    onOpenSaf: () -> Unit,
    onOpenRestore: () -> Unit,
    onOpenHowItWorks: () -> Unit,
    onEditTemplate: (String) -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val activity = LocalContext.current as FragmentActivity
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.refresh()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(Unit) {
        viewModel.shareIntents.collectLatest { intent ->
            try {
                context.startActivity(intent)
            } catch (_: Exception) {
                viewModel.reportExternalIntentFailed()
            }
        }
    }

    fun requestEnable() {
        when (state.availability) {
            BiometricAvailability.Ready -> {
                viewModel.markBusyForPrompt()
                viewModel.biometricAuth.authenticate(
                    activity = activity,
                    title = "Activer le verrouillage",
                    subtitle = "Confirmez avec votre empreinte ou le code du téléphone.",
                    negativeButton = "Annuler",
                    onResult = viewModel::onEnableAuthResult
                )
            }
            BiometricAvailability.NoneEnrolled -> {
                viewModel.onEnableAuthResult(
                    BiometricAuthResult.Failed(
                        "Ajoutez d’abord une empreinte ou un code d’écran dans les réglages du téléphone."
                    )
                )
            }
            else -> {
                viewModel.onEnableAuthResult(
                    BiometricAuthResult.Failed(
                        "Pas d’empreinte ni de code sur ce téléphone."
                    )
                )
            }
        }
    }

    val fit = rememberDisplayFit()
    val tipPad = if (fit.compactHomeChrome) 10.dp else 12.dp
    val tipPadLg = if (fit.compactHomeChrome) 12.dp else 14.dp
    PinnedBackScaffold(onBack = onBack, backEnabled = !state.busy) {
        Spacer(modifier = Modifier.height(if (fit.compactHomeChrome) 2.dp else 4.dp))
        Text(
            text = "Réglages",
            style = MaterialTheme.typography.headlineMedium,
            color = A11yTokens.Ink
        )
        if (state.busy) {
            val busyShape = MaterialTheme.shapes.medium
            Text(
                text = "Préparation de la copie…",
                style = MaterialTheme.typography.bodyMedium,
                color = A11yTokens.InkMuted,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, A11yTokens.PaperBorder, busyShape)
                    .background(A11yTokens.PaperCard, busyShape)
                    .padding(12.dp)
                    .semantics {
                        liveRegion = LiveRegionMode.Polite
                        contentDescription = "Préparation de la copie"
                    }
            )
        }

        SettingsCard(title = "Aide") {
            val aideTipShape = MaterialTheme.shapes.medium
            Text(
                text = "Explications simples : reste, FAIT, rappels, sauvegarde.",
                style = MaterialTheme.typography.bodyMedium,
                color = A11yTokens.InkMuted,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, A11yTokens.PaperBorder, aideTipShape)
                    .background(A11yTokens.PaperWarm, aideTipShape)
                    .padding(12.dp)
            )
            SecondaryA11yButton(
                text = "Comment ça marche",
                onClick = onOpenHowItWorks,
                enabled = !state.busy
            )
        }

        SettingsCard(title = "Mettre de côté") {
            val savingsTipShape = MaterialTheme.shapes.medium
            Text(
                text = "Une facture mensuelle, virée vers un autre compte. " +
                    "Elle entre dans le Reste dès le 1er. " +
                    "Ce n’est pas de l’argent gardé dans l’application.",
                style = MaterialTheme.typography.bodyMedium,
                color = A11yTokens.InkMuted,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, A11yTokens.PaperBorder, savingsTipShape)
                    .background(A11yTokens.PaperWarm, savingsTipShape)
                    .padding(12.dp)
            )
            OutlinedTextField(
                value = state.savingsAmountText,
                onValueChange = viewModel::setSavingsAmount,
                label = { Text("Montant (€)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = A11yTokens.minTouchHeight),
                textStyle = MaterialTheme.typography.bodyLarge,
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                enabled = !state.busy && !state.savingsBusy
            )
            DayOfMonthGrid(
                selectedDay = state.savingsDay,
                onSelect = { day ->
                    if (!state.savingsBusy && !state.busy) viewModel.setSavingsDay(day)
                }
            )
            state.savingsDeficit?.let { deficit ->
                val deficitShape = MaterialTheme.shapes.medium
                Text(
                    text = deficit,
                    style = MaterialTheme.typography.bodyMedium,
                    color = A11yTokens.AccentWarn,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, A11yTokens.AccentWarn.copy(alpha = 0.45f), deficitShape)
                        .background(A11yTokens.PaperWarm, deficitShape)
                        .padding(12.dp)
                        .semantics {
                            liveRegion = LiveRegionMode.Polite
                            contentDescription = deficit
                        }
                )
            }
            state.savingsNextMonthNote?.let { note ->
                val nextShape = MaterialTheme.shapes.medium
                Text(
                    text = note,
                    style = MaterialTheme.typography.bodyMedium,
                    color = A11yTokens.InkMuted,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, A11yTokens.PaperBorder, nextShape)
                        .background(A11yTokens.PaperWarm, nextShape)
                        .padding(12.dp)
                        .semantics {
                            liveRegion = LiveRegionMode.Polite
                            contentDescription = note
                        }
                )
            }
            SecondaryA11yButton(
                text = if (state.savingsBusy) {
                    "Enregistrement…"
                } else if (state.savingsActive) {
                    "Mettre à jour"
                } else {
                    "Activer"
                },
                onClick = viewModel::saveSavings,
                enabled = !state.busy && !state.savingsBusy
            )
            if (state.savingsActive) {
                DangerA11yButton(
                    text = "Retirer la ligne",
                    onClick = viewModel::removeSavings,
                    enabled = !state.busy && !state.savingsBusy
                )
            }
            state.savingsMessage?.let { msg ->
                val msgShape = MaterialTheme.shapes.medium
                val err = state.savingsMessageError
                Text(
                    text = msg,
                    color = if (err) A11yTokens.AccentDanger else A11yTokens.AccentOk,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            if (err) A11yTokens.AccentDanger.copy(alpha = 0.4f)
                            else A11yTokens.AccentOk.copy(alpha = 0.35f),
                            msgShape
                        )
                        .background(
                            if (err) A11yTokens.PaperWarm else A11yTokens.AccentOkSoft,
                            msgShape
                        )
                        .padding(12.dp)
                        .semantics {
                            liveRegion = if (err) {
                                LiveRegionMode.Assertive
                            } else {
                                LiveRegionMode.Polite
                            }
                            contentDescription = msg
                        }
                )
            }
        }

        SettingsCard(title = "Confidentialité") {
            val privacyTipShape = MaterialTheme.shapes.medium
            Text(
                text = "Par défaut l’app s’ouvre sans code.",
                style = MaterialTheme.typography.bodyLarge,
                color = A11yTokens.InkMuted,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, A11yTokens.PaperBorder, privacyTipShape)
                    .background(A11yTokens.PaperWarm, privacyTipShape)
                    .padding(12.dp)
            )
            val canToggle = state.availability == BiometricAvailability.Ready || state.biometricEnabled
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = A11yTokens.minTouchHeight)
                    .clickable(
                        enabled = !state.busy && canToggle,
                        role = Role.Checkbox
                    ) {
                        if (state.biometricEnabled) viewModel.disableBiometric()
                        else requestEnable()
                    }
                    .semantics {
                        contentDescription = if (state.biometricEnabled) {
                            "Verrouiller avec empreinte ou code, activé"
                        } else {
                            "Verrouiller avec empreinte ou code, désactivé"
                        }
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = state.biometricEnabled,
                    onCheckedChange = { checked ->
                        if (state.busy) return@Checkbox
                        if (checked) requestEnable() else viewModel.disableBiometric()
                    },
                    enabled = !state.busy && canToggle
                )
                Text(
                    text = "Verrouiller avec empreinte ou code",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            run {
                val availText = when (state.availability) {
                    BiometricAvailability.Ready ->
                        "Disponible sur ce téléphone (empreinte, visage ou code)."
                    BiometricAvailability.NoneEnrolled ->
                        "Aucune empreinte / code d’écran enregistré. Configurez-les dans les réglages du téléphone."
                    BiometricAvailability.HardwareUnavailable ->
                        "Empreinte ou code temporairement indisponible."
                    BiometricAvailability.Unsupported ->
                        "Pas d’empreinte ni de code sur ce téléphone."
                }
                val needsAttention = state.availability != BiometricAvailability.Ready
                val availShape = MaterialTheme.shapes.medium
                Text(
                    text = availText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (needsAttention) A11yTokens.AccentWarn else A11yTokens.InkMuted,
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if (needsAttention) {
                                Modifier
                                    .border(
                                        1.dp,
                                        A11yTokens.AccentWarn.copy(alpha = 0.45f),
                                        availShape
                                    )
                                    .background(A11yTokens.PaperWarm, availShape)
                                    .padding(12.dp)
                                    .semantics {
                                        liveRegion = LiveRegionMode.Polite
                                        contentDescription = availText
                                    }
                            } else {
                                Modifier
                            }
                        )
                )
            }
            state.message?.let { msg ->
                val msgShape = MaterialTheme.shapes.medium
                val err = state.isError
                Text(
                    text = msg,
                    color = if (err) A11yTokens.AccentDanger else A11yTokens.AccentOk,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            if (err) A11yTokens.AccentDanger.copy(alpha = 0.4f)
                            else A11yTokens.AccentOk.copy(alpha = 0.35f),
                            msgShape
                        )
                        .background(
                            if (err) A11yTokens.PaperWarm else A11yTokens.AccentOkSoft,
                            msgShape
                        )
                        .padding(12.dp)
                        .semantics {
                            liveRegion = if (err) {
                                LiveRegionMode.Assertive
                            } else {
                                LiveRegionMode.Polite
                            }
                            contentDescription = msg
                        }
                )
            }
        }

        SettingsCard(title = "Alertes") {
            val alertesTipShape = MaterialTheme.shapes.medium
            Text(
                text = "Notifications, réveil à l’heure et batterie, au même endroit.",
                style = MaterialTheme.typography.bodyMedium,
                color = A11yTokens.InkMuted,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, A11yTokens.PaperBorder, alertesTipShape)
                    .background(A11yTokens.PaperWarm, alertesTipShape)
                    .padding(12.dp)
            )
            SecondaryA11yButton(
                text = "Vérifier les alertes",
                onClick = onOpenHealth,
                enabled = !state.busy
            )
        }

        SettingsCard(title = "Échéances hors ce mois") {
            val echeancesTipShape = MaterialTheme.shapes.medium
            Text(
                text = "Assurances, taxes, factures 1×/an ou tous les 3 mois — " +
                    "visibles ici quand elles ne tombent pas ce mois-ci. " +
                    "Touchez une carte pour corriger le nom, le montant, le jour ou le mois.",
                style = MaterialTheme.typography.bodyMedium,
                color = A11yTokens.InkMuted,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, A11yTokens.PaperBorder, echeancesTipShape)
                    .background(A11yTokens.PaperWarm, echeancesTipShape)
                    .padding(12.dp)
            )
            if (state.offMonthPeriodics.isEmpty()) {
                val emptyShape = MaterialTheme.shapes.medium
                Text(
                    text = "Aucune pour l’instant. Quand vous créerez une facture « 1× par an » " +
                        "ou « tous les 3 mois », elle apparaîtra ici les autres mois.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = A11yTokens.InkMuted,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, A11yTokens.PaperBorder, emptyShape)
                        .background(A11yTokens.PaperWarm, emptyShape)
                        .padding(12.dp)
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(A11yTokens.cardGap)) {
                    state.offMonthPeriodics.forEach { item ->
                        Surface(
                            color = A11yTokens.PaperWarm,
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = A11yTokens.minTouchHeight)
                                .border(1.dp, A11yTokens.PaperBorder, MaterialTheme.shapes.medium)
                                .clickable(
                                    role = Role.Button,
                                    onClickLabel = "Modifier ${item.label}"
                                ) { onEditTemplate(item.templateId) }
                        ) {
                            Column(
                                modifier = Modifier.padding(tipPadLg),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = item.label,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = A11yTokens.Ink
                                )
                                Text(
                                    text = viewModel.formatPeriodicAmountLine(item),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = A11yTokens.Ink
                                )
                                Text(
                                    text = "Prochaine : ${item.nextDueLabel}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = A11yTokens.AccentWarn
                                )
                            }
                        }
                    }
                }
            }
        }

        if (state.showSummaryButton) {
            SettingsCard(title = "Résumé à montrer") {
                val tipShape = MaterialTheme.shapes.medium
                Text(
                    text = "À envoyer à un créancier ou à un conseiller — pas à tout le monde. " +
                        "Utile si le mois est en déficit, ou s’il y a un dossier.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = A11yTokens.InkMuted,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, A11yTokens.PaperBorder, tipShape)
                        .background(A11yTokens.PaperCard, tipShape)
                        .padding(12.dp)
                )
                if (state.summaryBusy) {
                    Text(
                        text = "Préparation du résumé…",
                        style = MaterialTheme.typography.bodyMedium,
                        color = A11yTokens.InkMuted,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, A11yTokens.PaperBorder, tipShape)
                            .background(A11yTokens.PaperCard, tipShape)
                            .padding(12.dp)
                            .semantics {
                                liveRegion = LiveRegionMode.Polite
                                contentDescription = "Préparation du résumé"
                            }
                    )
                }
                SecondaryA11yButton(
                    text = if (state.summaryBusy) "Préparation…" else "Préparer un résumé",
                    onClick = viewModel::shareMonthSummary,
                    enabled = !state.busy && !state.summaryBusy
                )
            }
        }

        SettingsCard(title = "Sauvegarde") {
            val backupTipShape = MaterialTheme.shapes.medium
            Text(
                text = "Copie automatique dans Documents (3 versions) : mois + photos. " +
                    "« Envoyer une copie » envoie une archive (mois et photos de factures) " +
                    "par WhatsApp ou Mail.",
                style = MaterialTheme.typography.bodyLarge,
                color = A11yTokens.InkMuted,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, A11yTokens.PaperBorder, backupTipShape)
                    .background(A11yTokens.PaperWarm, backupTipShape)
                    .padding(12.dp)
            )
            run {
                val healthLine = BackupHealthCopy.statusLine(state.backupHealth)
                val healthOk = BackupHealthCopy.isOk(state.backupHealth)
                val healthShape = MaterialTheme.shapes.medium
                Text(
                    text = healthLine,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (healthOk) A11yTokens.AccentOk else A11yTokens.AccentDanger,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            if (healthOk) A11yTokens.AccentOk.copy(alpha = 0.35f)
                            else A11yTokens.AccentDanger.copy(alpha = 0.4f),
                            healthShape
                        )
                        .background(
                            if (healthOk) A11yTokens.AccentOkSoft else A11yTokens.PaperWarm,
                            healthShape
                        )
                        .padding(12.dp)
                        .semantics {
                            liveRegion = LiveRegionMode.Polite
                            contentDescription = healthLine
                        }
                )
            }
            SecondaryA11yButton(
                text = "Dossier de sauvegarde",
                onClick = onOpenSaf,
                enabled = !state.busy
            )
            SecondaryA11yButton(
                text = "Envoyer une copie",
                onClick = viewModel::shareSecurityCopy,
                enabled = !state.busy
            )
            SecondaryA11yButton(
                text = "Restaurer une sauvegarde",
                onClick = onOpenRestore,
                enabled = !state.busy
            )
        }

        val versionName = try {
            val p = context.packageManager.getPackageInfo(context.packageName, 0)
            p.versionName
        } catch (_: Exception) {
            null
        }
        val stepLabel = AppVersionDisplay.stepLabel(versionName)
        val footShape = MaterialTheme.shapes.medium
        Column(
            modifier = Modifier
                .padding(top = 8.dp)
                .fillMaxWidth()
                .border(1.dp, A11yTokens.PaperBorder, footShape)
                .background(A11yTokens.PaperCard, footShape)
                .padding(tipPadLg)
                .semantics(mergeDescendants = true) {
                    contentDescription = buildString {
                        append("Mon Mois Clair")
                        if (stepLabel != null) append(", étape $stepLabel")
                        append(". Données sur ce téléphone uniquement.")
                    }
                },
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Mon Mois Clair",
                style = MaterialTheme.typography.bodyMedium,
                color = A11yTokens.InkMuted
            )
            if (stepLabel != null) {
                Text(
                    text = stepLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = A11yTokens.InkMuted
                )
            }
            Text(
                text = "Données sur ce téléphone uniquement. Pas de compte, pas de cloud.",
                style = MaterialTheme.typography.bodyMedium,
                color = A11yTokens.InkMuted
            )
        }
    }
}

@Composable
private fun SettingsCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    val fit = rememberDisplayFit()
    val cardPad = if (fit.compactHomeChrome) 12.dp else 16.dp
    val innerGap = if (fit.compactHomeChrome) 8.dp else 10.dp
    val betweenCards = if (fit.compactHomeChrome) 10.dp else 14.dp
    Surface(
        color = A11yTokens.PaperCard,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, A11yTokens.PaperBorder, MaterialTheme.shapes.medium)
    ) {
        Column(
            modifier = Modifier.padding(cardPad),
            verticalArrangement = Arrangement.spacedBy(innerGap)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = A11yTokens.Ink
            )
            content()
        }
    }
    Spacer(modifier = Modifier.height(betweenCards))
}
