package be.monmoisclair.app.ui.settings

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
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
import be.monmoisclair.app.domain.model.ThemePreference
import be.monmoisclair.app.ui.components.CarnetBannerKind
import be.monmoisclair.app.ui.components.CarnetStateBanner
import be.monmoisclair.app.ui.components.DangerA11yButton
import be.monmoisclair.app.ui.components.PinnedBackScaffold
import be.monmoisclair.app.ui.components.QuietLinkA11y
import be.monmoisclair.app.ui.components.SecondaryA11yButton
import be.monmoisclair.app.ui.entry.DayOfMonthGrid
import be.monmoisclair.app.ui.theme.A11yTokens
import be.monmoisclair.app.ui.theme.carnetOutlinedTextFieldColors
import be.monmoisclair.app.ui.theme.imeOpen
import be.monmoisclair.app.ui.theme.rememberDisplayFit
import kotlinx.coroutines.flow.collectLatest

@Composable
internal fun SettingsShareEffect(viewModel: SettingsViewModel) {
    val context = LocalContext.current
    LaunchedEffect(viewModel) {
        viewModel.shareIntents.collectLatest { intent ->
            try {
                context.startActivity(intent)
            } catch (_: Exception) {
                viewModel.reportExternalIntentFailed()
            }
        }
    }
}

@Composable
internal fun SettingsResumeRefresh(viewModel: SettingsViewModel) {
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, viewModel) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.refresh()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
}

@Composable
fun SettingsAppearanceScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    SettingsResumeRefresh(viewModel)
    PinnedBackScaffold(onBack = onBack, backEnabled = !state.busy) {
        SettingsPageTitle(SettingsHubCopy.APPEARANCE)
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
}

@Composable
fun SettingsSavingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val fit = rememberDisplayFit()
    val tipPad = fit.tipPad
    val keyboardOpen = imeOpen()
    val pinSavingsAction = keyboardOpen && state.savingsAmountText.isNotBlank()
    var confirmRemoveSavings by remember { mutableStateOf(false) }

    SettingsResumeRefresh(viewModel)

    if (confirmRemoveSavings) {
        AlertDialog(
            onDismissRequest = { confirmRemoveSavings = false },
            shape = MaterialTheme.shapes.medium,
            containerColor = A11yTokens.PaperCream,
            title = { Text("Retirer « Mettre de côté » ?") },
            text = {
                Text("Retire la ligne du mois. Les FAIT déjà cochés restent dans l’historique.")
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
                    CarnetStateBanner(text = deficit, kind = CarnetBannerKind.Warn, pad = tipPad)
                }
                SecondaryA11yButton(
                    text = savingsActionLabel(state.savingsBusy, state.savingsActive),
                    onClick = viewModel::saveSavings,
                    enabled = !state.busy && !state.savingsBusy
                )
            }
        } else {
            null
        }
    ) {
        SettingsPageTitle(SettingsHubCopy.SAVINGS)
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
            colors = carnetOutlinedTextFieldColors(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Done
            ),
            enabled = !state.busy && !state.savingsBusy
        )
        DayOfMonthGrid(
            selectedDay = state.savingsDay,
            onSelect = { day ->
                if (!state.savingsBusy && !state.busy) viewModel.setSavingsDay(day)
            }
        )
        if (!pinSavingsAction) {
            state.savingsDeficit?.let { deficit ->
                CarnetStateBanner(text = deficit, kind = CarnetBannerKind.Warn, pad = tipPad)
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
            text = savingsActionLabel(state.savingsBusy, state.savingsActive),
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
            CarnetStateBanner(
                text = msg,
                kind = if (state.savingsMessageError) CarnetBannerKind.Error else CarnetBannerKind.Ok,
                pad = tipPad
            )
        }
    }
}

@Composable
fun SettingsPrivacyScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val activity = LocalContext.current as FragmentActivity
    val fit = rememberDisplayFit()
    val tipPad = fit.tipPad

    SettingsResumeRefresh(viewModel)

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

    PinnedBackScaffold(onBack = onBack, backEnabled = !state.busy) {
        SettingsPageTitle(SettingsHubCopy.PRIVACY)
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
                CarnetStateBanner(text = availText, kind = CarnetBannerKind.Warn, pad = tipPad)
            }
        }
        state.message?.let { msg ->
            CarnetStateBanner(
                text = msg,
                kind = if (state.isError) CarnetBannerKind.Error else CarnetBannerKind.Ok,
                pad = tipPad
            )
        }
    }
}

@Composable
fun SettingsDeadlinesScreen(
    onBack: () -> Unit,
    onEditTemplate: (String) -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val fit = rememberDisplayFit()
    val tipPadLg = fit.tipPadLg

    SettingsResumeRefresh(viewModel)

    PinnedBackScaffold(onBack = onBack, backEnabled = !state.busy) {
        SettingsPageTitle(SettingsHubCopy.DEADLINES)
        if (state.offMonthPeriodics.isEmpty()) {
            Text(
                text = "Aucune pour l’instant.",
                style = MaterialTheme.typography.bodyLarge,
                color = A11yTokens.InkMuted,
                modifier = Modifier.fillMaxWidth()
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
}

@Composable
fun SettingsSummaryScreen(
    onBack: () -> Unit,
    onOpenDossiers: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    SettingsResumeRefresh(viewModel)

    PinnedBackScaffold(onBack = onBack, backEnabled = !state.busy) {
        SettingsPageTitle(SettingsHubCopy.SUMMARY)
        Text(
            text = "Texte ou PDF pour un créancier ou un conseiller — dans Mes dossiers.",
            style = MaterialTheme.typography.bodyMedium,
            color = A11yTokens.InkMuted,
            modifier = Modifier.fillMaxWidth()
        )
        QuietLinkA11y(
            text = "Ouvrir Mes dossiers",
            onClick = onOpenDossiers
        )
    }
}

@Composable
fun SettingsBackupScreen(
    onBack: () -> Unit,
    onOpenSaf: () -> Unit,
    onOpenRestore: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val fit = rememberDisplayFit()
    val tipPad = fit.tipPad

    SettingsResumeRefresh(viewModel)
    SettingsShareEffect(viewModel)

    PinnedBackScaffold(onBack = onBack, backEnabled = !state.busy) {
        SettingsPageTitle(SettingsHubCopy.BACKUP)
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
        if (state.backupHealth != BackupHealth.NOT_CONFIGURED) {
            Text(
                text = "3 copies dans Documents. « Envoyer une copie » = WhatsApp / Mail.",
                style = MaterialTheme.typography.bodyMedium,
                color = A11yTokens.InkMuted,
                modifier = Modifier.fillMaxWidth()
            )
        }
        run {
            val healthLine = BackupHealthCopy.statusLine(state.backupHealth)
            val healthOk = BackupHealthCopy.isOk(state.backupHealth)
            CarnetStateBanner(
                text = healthLine,
                kind = if (healthOk) CarnetBannerKind.Ok else CarnetBannerKind.Error,
                pad = tipPad
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
        // SET04 : restaurer = Danger (destructif).
        DangerA11yButton(
            text = "Restaurer une sauvegarde",
            onClick = onOpenRestore,
            enabled = !state.busy
        )
        state.backupMessage?.let { msg ->
            CarnetStateBanner(
                text = msg,
                kind = if (state.backupMessageError) CarnetBannerKind.Error else CarnetBannerKind.Ok,
                pad = tipPad
            )
        }
        state.flashMessage?.let { msg ->
            CarnetStateBanner(
                text = msg,
                kind = if (state.flashMessageError) CarnetBannerKind.Error else CarnetBannerKind.Ok,
                pad = tipPad
            )
        }
    }
}

@Composable
fun SettingsHelpScreen(
    onBack: () -> Unit,
    onOpenHowItWorks: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val fit = rememberDisplayFit()
    val tipPad = fit.tipPad

    SettingsResumeRefresh(viewModel)
    SettingsShareEffect(viewModel)

    PinnedBackScaffold(onBack = onBack, backEnabled = !state.busy) {
        SettingsPageTitle(SettingsHubCopy.HELP)
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
        state.flashMessage?.let { msg ->
            CarnetStateBanner(
                text = msg,
                kind = if (state.flashMessageError) CarnetBannerKind.Error else CarnetBannerKind.Ok,
                pad = tipPad
            )
        }
    }
}

@Composable
internal fun SettingsPageTitle(title: String) {
    val fit = rememberDisplayFit()
    Spacer(modifier = Modifier.height(if (fit.compactHomeChrome) 2.dp else 4.dp))
    Text(
        text = title,
        style = MaterialTheme.typography.headlineMedium,
        color = A11yTokens.Ink
    )
}

private fun savingsActionLabel(busy: Boolean, active: Boolean): String = when {
    busy -> "Enregistrement…"
    active -> "Mettre à jour"
    else -> "Activer"
}

@Composable
internal fun ThemeOptionRow(
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
