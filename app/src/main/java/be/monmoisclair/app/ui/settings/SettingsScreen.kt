package be.monmoisclair.app.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import be.monmoisclair.app.data.biometric.BiometricAuthResult
import be.monmoisclair.app.data.biometric.BiometricAvailability
import be.monmoisclair.app.ui.components.SecondaryA11yButton
import be.monmoisclair.app.ui.theme.A11yTokens
import kotlinx.coroutines.flow.collectLatest

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onOpenHealth: () -> Unit,
    onOpenSaf: () -> Unit,
    onOpenRestore: () -> Unit,
    onOpenHowItWorks: () -> Unit,
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
            } catch (e: Exception) {
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
                        "Ajoutez d’abord une empreinte ou un code d’écran dans les réglages Samsung."
                    )
                )
            }
            else -> {
                viewModel.onEnableAuthResult(
                    BiometricAuthResult.Failed("Biométrie indisponible sur cet appareil.")
                )
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(A11yTokens.PaperCream)
            .verticalScroll(rememberScrollState())
            .padding(A11yTokens.screenPadding),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = "Réglages", style = MaterialTheme.typography.headlineMedium)

        Text(text = "Aide", style = MaterialTheme.typography.titleLarge)
        Text(
            text = "Explications simples : reste, FAIT, rappels, sauvegarde.",
            style = MaterialTheme.typography.bodyMedium,
            color = A11yTokens.InkMuted
        )
        SecondaryA11yButton(
            text = "Comment ça marche",
            onClick = onOpenHowItWorks,
            enabled = !state.busy
        )

        HorizontalDivider(color = A11yTokens.PaperWarm)
        Text(text = "Confidentialité", style = MaterialTheme.typography.titleLarge)
        Text(
            text = "Par défaut l’app s’ouvre sans code.",
            style = MaterialTheme.typography.bodyLarge
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

        Text(
            text = when (state.availability) {
                BiometricAvailability.Ready ->
                    "Disponible sur ce téléphone (empreinte, visage ou code)."
                BiometricAvailability.NoneEnrolled ->
                    "Aucune empreinte / code d’écran enregistré. Configurez-les dans Samsung."
                BiometricAvailability.HardwareUnavailable ->
                    "Capteur indisponible pour le moment."
                BiometricAvailability.Unsupported ->
                    "Non supporté sur cet appareil."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = A11yTokens.InkMuted
        )

        state.message?.let { msg ->
            Text(
                text = msg,
                color = if (state.isError) A11yTokens.AccentDanger else A11yTokens.AccentOk,
                style = MaterialTheme.typography.bodyLarge
            )
        }

        HorizontalDivider(color = A11yTokens.PaperWarm)
        Text(text = "Rappels", style = MaterialTheme.typography.titleLarge)
        Text(
            text = "Pour vérifier que les alertes (notifications, batterie) sont bien autorisées.",
            style = MaterialTheme.typography.bodyMedium,
            color = A11yTokens.InkMuted
        )
        SecondaryA11yButton(
            text = "Santé des rappels",
            onClick = onOpenHealth,
            enabled = !state.busy
        )
        if (state.showDeviceCareSection) {
            Text(text = DeviceCareCopy.TITLE, style = MaterialTheme.typography.titleLarge)
            Text(text = DeviceCareCopy.BODY, style = MaterialTheme.typography.bodyLarge)
            SecondaryA11yButton(
                text = "Liste optimisation batterie",
                enabled = !state.busy,
                onClick = {
                    try {
                        context.startActivity(viewModel.intentBatteryList())
                    } catch (e: Exception) {
                        viewModel.reportExternalIntentFailed()
                    }
                }
            )
            SecondaryA11yButton(
                text = "Fiche de l’application",
                enabled = !state.busy,
                onClick = {
                    try {
                        context.startActivity(viewModel.intentAppDetails())
                    } catch (e: Exception) {
                        viewModel.reportExternalIntentFailed()
                    }
                }
            )
        }

        if (state.offMonthPeriodics.isNotEmpty()) {
            HorizontalDivider(color = A11yTokens.PaperWarm)
            Text(text = "Échéances hors ce mois", style = MaterialTheme.typography.titleLarge)
            Text(
                text = "Factures trimestrielles ou annuelles qui ne tombent pas ce mois-ci.",
                style = MaterialTheme.typography.bodyMedium,
                color = A11yTokens.InkMuted
            )
            state.offMonthPeriodics.forEach { item ->
                Text(
                    text = viewModel.formatPeriodicLine(item),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        HorizontalDivider(color = A11yTokens.PaperWarm)
        Text(text = "Sauvegarde", style = MaterialTheme.typography.titleLarge)
        Text(
            text = "À chaque modification, une copie JSON est écrite dans votre dossier " +
                "Documents (3 versions). En cas de réinstall : Restaurer une sauvegarde. " +
                "Pour un téléphone cassé ou volé : envoyez une copie (WhatsApp / Mail).",
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = BackupHealthCopy.statusLine(state.backupHealth),
            style = MaterialTheme.typography.bodyLarge,
            color = if (BackupHealthCopy.isOk(state.backupHealth)) {
                A11yTokens.AccentOk
            } else {
                A11yTokens.AccentDanger
            }
        )
        SecondaryA11yButton(
            text = "Dossier de sauvegarde",
            onClick = onOpenSaf,
            enabled = !state.busy
        )
        SecondaryA11yButton(
            text = "Envoyer une copie de sécurité (WhatsApp / Mail)",
            onClick = viewModel::shareSecurityCopy,
            enabled = !state.busy
        )
        SecondaryA11yButton(
            text = "Restaurer une sauvegarde",
            onClick = onOpenRestore,
            enabled = !state.busy
        )

        Spacer(modifier = Modifier.height(8.dp))
        SecondaryA11yButton(text = "Retour", onClick = onBack, enabled = !state.busy)
    }
}
