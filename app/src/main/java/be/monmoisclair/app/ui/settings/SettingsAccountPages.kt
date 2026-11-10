package be.monmoisclair.app.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import be.monmoisclair.app.data.biometric.BiometricAuthResult
import be.monmoisclair.app.data.biometric.BiometricAvailability
import be.monmoisclair.app.data.biometric.BiometricMessages
import be.monmoisclair.app.ui.components.CarnetBannerKind
import be.monmoisclair.app.ui.components.CarnetStateBanner
import be.monmoisclair.app.ui.components.CarnetTipCard
import be.monmoisclair.app.ui.components.DangerA11yButton
import be.monmoisclair.app.ui.components.PinnedBackScaffold
import be.monmoisclair.app.ui.components.PrimaryA11yButton
import be.monmoisclair.app.ui.components.SecondaryA11yButton
import be.monmoisclair.app.ui.entry.DayOfMonthGrid
import be.monmoisclair.app.ui.theme.A11yTokens
import be.monmoisclair.app.ui.theme.carnetOutlinedTextFieldColors
import be.monmoisclair.app.ui.theme.imeOpen
import be.monmoisclair.app.ui.theme.rememberDisplayFit

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
            shape = MaterialTheme.shapes.extraLarge,
            containerColor = A11yTokens.PaperCard,
            title = {
                Text(
                    text = "Retirer « Mettre de côté » ?",
                    color = A11yTokens.Ink
                )
            },
            text = {
                Text(
                    text = "Retire la ligne du mois. Les FAIT déjà cochés restent dans l’historique.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = A11yTokens.Ink
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
                    CarnetStateBanner(text = deficit, kind = CarnetBannerKind.Warn, pad = tipPad)
                }
                PrimaryA11yButton(
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
        CarnetTipCard(
            text = "Montant : depuis Accueil (sous Courses). Ici : jour du mois et retrait. " +
                "Chaque mois, ce montant sort du Reste (vers un autre compte)."
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
        PrimaryA11yButton(
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
                        BiometricMessages.UNSUPPORTED
                    )
                )
            }
        }
    }

    PinnedBackScaffold(onBack = onBack, backEnabled = !state.busy) {
        SettingsPageTitle(SettingsHubCopy.PRIVACY)
        val lockShape = MaterialTheme.shapes.large
        // 7.13 : intro + toggle en cartes soft (plus texte plat).
        CarnetTipCard(
            text = "Le carnet est protégé sur le téléphone. " +
                "Le verrou ci-dessous cache aussi l’écran, le widget et les montants des alertes."
        )
        Spacer(modifier = Modifier.height(if (fit.compactHomeChrome) 10.dp else 14.dp))
        val canToggle = state.availability == BiometricAvailability.Ready || state.biometricEnabled
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(2.dp, lockShape, clip = false)
                .clip(lockShape)
                .background(A11yTokens.PaperCard)
                .heightIn(min = A11yTokens.minTouchHeight)
                .clickable(
                    enabled = !state.busy && canToggle,
                    role = Role.Checkbox
                ) {
                    if (state.biometricEnabled) viewModel.disableBiometric()
                    else requestEnable()
                }
                .padding(horizontal = 12.dp, vertical = 10.dp)
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
                    BiometricMessages.NONE_ENROLLED
                BiometricAvailability.HardwareUnavailable ->
                    "Empreinte ou code temporairement indisponible."
                BiometricAvailability.Unsupported ->
                    BiometricMessages.UNSUPPORTED
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
