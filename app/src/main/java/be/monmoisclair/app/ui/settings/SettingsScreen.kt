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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import be.monmoisclair.app.domain.model.ThemePreference
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import be.monmoisclair.app.domain.model.BackupHealth
import be.monmoisclair.app.ui.components.AppWordmark
import be.monmoisclair.app.ui.components.DangerA11yButton
import be.monmoisclair.app.ui.components.PinnedBackScaffold
import be.monmoisclair.app.ui.components.SecondaryA11yButton
import be.monmoisclair.app.ui.entry.DayOfMonthGrid
import be.monmoisclair.app.ui.theme.A11yTokens
import be.monmoisclair.app.ui.theme.imeOpen
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
    val keyboardOpen = imeOpen()
    val pinSavingsAction = keyboardOpen && state.savingsAmountText.isNotBlank()
    var confirmRemoveSavings by remember { mutableStateOf(false) }
    if (confirmRemoveSavings) {
        AlertDialog(
            onDismissRequest = { confirmRemoveSavings = false },
            shape = MaterialTheme.shapes.medium,
            containerColor = A11yTokens.PaperCream,
            title = {
                Text("Retirer « Mettre de côté » ?")
            },
            text = {
                Text(
                    "Retire la ligne du mois. Les FAIT déjà cochés restent dans l’historique."
                )
            },
            confirmButton = {
                DangerA11yButton(
                    text = "Retirer",
                    onClick = {
                        confirmRemoveSavings = false
                        viewModel.removeSavings()
                    },
                    fillMaxWidth = false,
                    modifier = Modifier.padding(end = 8.dp, bottom = 8.dp)
                )
            },
            dismissButton = {
                SecondaryA11yButton(
                    text = "Annuler",
                    onClick = { confirmRemoveSavings = false },
                    fillMaxWidth = false,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
        )
    }
    PinnedBackScaffold(
        onBack = onBack,
        backEnabled = !state.busy,
        pinnedAboveBack = if (pinSavingsAction) {
            {
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
                            .padding(tipPad)
                            .semantics {
                                liveRegion = LiveRegionMode.Polite
                                contentDescription = deficit
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
            }
        } else {
            null
        }
    ) {
        Spacer(modifier = Modifier.height(if (fit.compactHomeChrome) 2.dp else 4.dp))
        Text(
            text = "Réglages",
            style = MaterialTheme.typography.headlineMedium,
            color = A11yTokens.Ink
        )
        if (state.busy) {
            Text(
                text = "Préparation de la copie…",
                style = MaterialTheme.typography.bodyMedium,
                color = A11yTokens.InkMuted,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics {
                        liveRegion = LiveRegionMode.Polite
                        contentDescription = "Préparation de la copie"
                    }
            )
        }
        state.flashMessage?.let { msg ->
            val flashShape = MaterialTheme.shapes.medium
            val err = state.flashMessageError
            Text(
                text = msg,
                color = if (err) A11yTokens.DangerInk else A11yTokens.AccentOkInk,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        if (err) A11yTokens.DangerInk.copy(alpha = 0.4f)
                        else A11yTokens.AccentOkInk.copy(alpha = 0.35f),
                        flashShape
                    )
                    .background(
                        if (err) A11yTokens.PaperWarm else A11yTokens.AccentOkSoft,
                        flashShape
                    )
                    .padding(tipPad)
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

        SettingsSection(title = "Aide") {
            SecondaryA11yButton(
                text = "Comment ça marche",
                onClick = onOpenHowItWorks,
                enabled = !state.busy
            )
            SecondaryA11yButton(
                text = "Un souci avec l’app ?",
                onClick = viewModel::writeSupportMail,
                enabled = !state.busy
            )
            Text(
                text = "Ouvre Mail / Gmail. Joignez une capture si besoin.",
                style = MaterialTheme.typography.bodyMedium,
                color = A11yTokens.InkMuted
            )
        }

        SettingsSection(title = "Apparence") {
            ThemeOptionRow(
                label = "Comme le téléphone",
                selected = state.themePreference == ThemePreference.System,
                enabled = !state.busy,
                onSelect = { viewModel.setThemePreference(ThemePreference.System) }
            )
            ThemeOptionRow(
                label = "Clair",
                selected = state.themePreference == ThemePreference.Light,
                enabled = !state.busy,
                onSelect = { viewModel.setThemePreference(ThemePreference.Light) }
            )
            ThemeOptionRow(
                label = "Sombre",
                selected = state.themePreference == ThemePreference.Dark,
                enabled = !state.busy,
                onSelect = { viewModel.setThemePreference(ThemePreference.Dark) }
            )
        }

        SettingsSection(title = "Mettre de côté") {
            // SET02 : tip 1 phrase.
            Text(
                text = "Chaque mois, ce montant compte dans le Reste (autre compte).",
                style = MaterialTheme.typography.bodyMedium,
                color = A11yTokens.InkMuted,
                modifier = Modifier.fillMaxWidth()
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
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Done
                ),
                enabled = !state.busy && !state.savingsBusy
            )
            // Jour puis Activer : la grille sous Activer disparaissait sous « Retour ».
            DayOfMonthGrid(
                selectedDay = state.savingsDay,
                onSelect = { day ->
                    if (!state.savingsBusy && !state.busy) viewModel.setSavingsDay(day)
                }
            )
            // Clavier ouvert : le déficit est déjà dans la barre épinglée — pas de doublon.
            if (!pinSavingsAction) {
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
                            .padding(tipPad)
                            .semantics {
                                liveRegion = LiveRegionMode.Polite
                                contentDescription = deficit
                            }
                    )
                }
            }
            state.savingsNextMonthNote?.let { note ->
                Text(
                    text = note,
                    style = MaterialTheme.typography.bodyMedium,
                    color = A11yTokens.InkMuted,
                    modifier = Modifier
                        .fillMaxWidth()
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
                    onClick = { confirmRemoveSavings = true },
                    enabled = !state.busy && !state.savingsBusy
                )
            }
            state.savingsMessage?.let { msg ->
                val msgShape = MaterialTheme.shapes.medium
                val err = state.savingsMessageError
                Text(
                    text = msg,
                    color = if (err) A11yTokens.DangerInk else A11yTokens.AccentOkInk,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            if (err) A11yTokens.DangerInk.copy(alpha = 0.4f)
                            else A11yTokens.AccentOkInk.copy(alpha = 0.35f),
                            msgShape
                        )
                        .background(
                            if (err) A11yTokens.PaperWarm else A11yTokens.AccentOkSoft,
                            msgShape
                        )
                        .padding(tipPad)
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

        SettingsSection(title = "Confidentialité") {
            Text(
                text = "Par défaut l’app s’ouvre sans code.",
                style = MaterialTheme.typography.bodyLarge,
                color = A11yTokens.InkMuted,
                modifier = Modifier.fillMaxWidth()
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
                    enabled = !state.busy && canToggle,
                    colors = CheckboxDefaults.colors(
                        checkedColor = A11yTokens.ButtonFill,
                        uncheckedColor = A11yTokens.FieldLine,
                        checkmarkColor = A11yTokens.ButtonText
                    )
                )
                Text(
                    text = "Verrouiller avec empreinte ou code",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            run {
                // Si Ready : la case suffit. Sinon : pourquoi ça ne marche pas.
                val availText = when (state.availability) {
                    BiometricAvailability.Ready -> null
                    BiometricAvailability.NoneEnrolled ->
                        "Ajoutez une empreinte ou un code d’écran dans les réglages du téléphone."
                    BiometricAvailability.HardwareUnavailable ->
                        "Empreinte ou code temporairement indisponible."
                    BiometricAvailability.Unsupported ->
                        "Pas d’empreinte ni de code sur ce téléphone."
                }
                if (availText != null) {
                    val availShape = MaterialTheme.shapes.medium
                    Text(
                        text = availText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = A11yTokens.AccentWarn,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                1.dp,
                                A11yTokens.AccentWarn.copy(alpha = 0.45f),
                                availShape
                            )
                            .background(A11yTokens.PaperWarm, availShape)
                            .padding(tipPad)
                            .semantics {
                                liveRegion = LiveRegionMode.Polite
                                contentDescription = availText
                            }
                    )
                }
            }
            state.message?.let { msg ->
                val msgShape = MaterialTheme.shapes.medium
                val err = state.isError
                Text(
                    text = msg,
                    color = if (err) A11yTokens.DangerInk else A11yTokens.AccentOkInk,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            if (err) A11yTokens.DangerInk.copy(alpha = 0.4f)
                            else A11yTokens.AccentOkInk.copy(alpha = 0.35f),
                            msgShape
                        )
                        .background(
                            if (err) A11yTokens.PaperWarm else A11yTokens.AccentOkSoft,
                            msgShape
                        )
                        .padding(tipPad)
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

        SettingsSection(title = "Alertes") {
            SecondaryA11yButton(
                text = "Vérifier les alertes",
                onClick = onOpenHealth,
                enabled = !state.busy
            )
        }

        SettingsSection(title = "Échéances hors ce mois") {
            if (state.offMonthPeriodics.isEmpty()) {
                Text(
                    text = "Aucune pour l’instant.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = A11yTokens.InkMuted,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Text(
                    text = "Factures 1×/an ou tous les 3 mois hors ce mois. Touchez pour modifier.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = A11yTokens.InkMuted,
                    modifier = Modifier.fillMaxWidth()
                )
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
            SettingsSection(title = "Résumé à montrer") {
                Text(
                    text = "Pour un créancier ou un conseiller. Texte ou PDF — même contenu.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = A11yTokens.InkMuted,
                    modifier = Modifier.fillMaxWidth()
                )
                // Un seul feedback busy : labels des boutons (pas de bandeau en plus).
                SecondaryA11yButton(
                    text = if (state.summaryBusy) "Préparation…" else "Préparer un résumé",
                    onClick = viewModel::shareMonthSummary,
                    enabled = !state.busy && !state.summaryBusy
                )
                SecondaryA11yButton(
                    text = if (state.summaryBusy) "Préparation…" else "PDF du résumé",
                    onClick = viewModel::shareMonthSummaryPdf,
                    enabled = !state.busy && !state.summaryBusy
                )
            }
        }

        SettingsSection(title = "Sauvegarde") {
            // Pas de tip « copie auto » si aucun dossier — le statut NOT_CONFIGURED suffit.
            if (state.backupHealth != BackupHealth.NOT_CONFIGURED) {
                Text(
                    text = "3 copies dans Documents (mois + photos). " +
                        "« Envoyer une copie » = archive WhatsApp / Mail.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = A11yTokens.InkMuted,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            run {
                val healthLine = BackupHealthCopy.statusLine(state.backupHealth)
                val healthOk = BackupHealthCopy.isOk(state.backupHealth)
                val healthShape = MaterialTheme.shapes.medium
                Text(
                    text = healthLine,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (healthOk) A11yTokens.AccentOkInk else A11yTokens.DangerInk,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            if (healthOk) A11yTokens.AccentOkInk.copy(alpha = 0.35f)
                            else A11yTokens.DangerInk.copy(alpha = 0.4f),
                            healthShape
                        )
                        .background(
                            if (healthOk) A11yTokens.AccentOkSoft else A11yTokens.PaperWarm,
                            healthShape
                        )
                        .padding(tipPad)
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
            // SET04 : restaurer = Danger (destructif), pas Secondary calme.
            DangerA11yButton(
                text = "Restaurer une sauvegarde",
                onClick = onOpenRestore,
                enabled = !state.busy
            )
            state.backupMessage?.let { msg ->
                val msgShape = MaterialTheme.shapes.medium
                val err = state.backupMessageError
                Text(
                    text = msg,
                    color = if (err) A11yTokens.DangerInk else A11yTokens.AccentOkInk,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            if (err) A11yTokens.DangerInk.copy(alpha = 0.4f)
                            else A11yTokens.AccentOkInk.copy(alpha = 0.35f),
                            msgShape
                        )
                        .background(
                            if (err) A11yTokens.PaperWarm else A11yTokens.AccentOkSoft,
                            msgShape
                        )
                        .padding(tipPad)
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

        val versionName = try {
            val p = context.packageManager.getPackageInfo(context.packageName, 0)
            p.versionName
        } catch (_: Exception) {
            null
        }
        val stepLabel = AppVersionDisplay.stepLabel(versionName)
        Column(
            modifier = Modifier
                .padding(top = 8.dp)
                .fillMaxWidth()
                .padding(vertical = tipPad)
                .semantics(mergeDescendants = true) {
                    contentDescription = buildString {
                        append("Mon Mois Clair")
                        if (stepLabel != null) append(", étape $stepLabel")
                        append(". Données sur ce téléphone uniquement.")
                    }
                },
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // SET05 : wordmark discret hors « carte marketing ».
            AppWordmark(
                width = if (fit.compactHomeChrome) 140.dp else 160.dp,
                announce = false
            )
            if (stepLabel != null) {
                Text(
                    text = stepLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = A11yTokens.InkMuted
                )
            }
            Text(
                text = "Données sur ce téléphone — pas de compte, pas de cloud.",
                style = MaterialTheme.typography.bodyMedium,
                color = A11yTokens.InkMuted
            )
        }
    }
}

@Composable
private fun ThemeOptionRow(
    label: String,
    selected: Boolean,
    enabled: Boolean,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = A11yTokens.minTouchHeight)
            .clickable(
                enabled = enabled,
                role = Role.RadioButton,
                onClick = onSelect
            )
            .semantics {
                contentDescription = if (selected) {
                    "$label, sélectionné"
                } else {
                    label
                }
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        // SET03 : radio teal carnet.
        RadioButton(
            selected = selected,
            onClick = onSelect,
            enabled = enabled,
            colors = RadioButtonDefaults.colors(
                selectedColor = A11yTokens.ButtonFill,
                unselectedColor = A11yTokens.FieldLine
            )
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = A11yTokens.Ink
        )
    }
}

/** SET01 : filets + une texture page (PaperCream), pas 8 cartes clones. */
@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    val fit = rememberDisplayFit()
    val innerGap = if (fit.compactHomeChrome) 8.dp else 10.dp
    val between = if (fit.compactHomeChrome) 14.dp else 18.dp
    HorizontalDivider(color = A11yTokens.PaperBorder)
    Spacer(modifier = Modifier.height(if (fit.compactHomeChrome) 10.dp else 12.dp))
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(innerGap)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = A11yTokens.Ink
        )
        content()
    }
    Spacer(modifier = Modifier.height(between))
}
