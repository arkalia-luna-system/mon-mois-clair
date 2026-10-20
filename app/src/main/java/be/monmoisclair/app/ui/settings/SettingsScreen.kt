package be.monmoisclair.app.ui.settings

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import be.monmoisclair.app.ui.components.PinnedBackScaffold
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

    PinnedBackScaffold(onBack = onBack, backEnabled = !state.busy) {
        Spacer(modifier = Modifier.height(4.dp))
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

        HorizontalDivider(color = A11yTokens.InkMuted.copy(alpha = 0.25f))
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

        HorizontalDivider(color = A11yTokens.InkMuted.copy(alpha = 0.25f))
        Text(text = "Rappels", style = MaterialTheme.typography.titleLarge)
        Text(
            text = "Notifications, batterie et Device Care, au même endroit.",
            style = MaterialTheme.typography.bodyMedium,
            color = A11yTokens.InkMuted
        )
        SecondaryA11yButton(
            text = "Santé des rappels",
            onClick = onOpenHealth,
            enabled = !state.busy
        )

        HorizontalDivider(color = A11yTokens.InkMuted.copy(alpha = 0.25f))
        Text(text = "Échéances hors ce mois", style = MaterialTheme.typography.titleLarge)
        Text(
            text = "Assurances, taxes, factures 1×/an ou tous les 3 mois — " +
                "visibles ici quand elles ne tombent pas ce mois-ci.",
            style = MaterialTheme.typography.bodyMedium,
            color = A11yTokens.InkMuted
        )
        if (state.offMonthPeriodics.isEmpty()) {
            Text(
                text = "Aucune pour l’instant. Quand vous créerez une facture « 1× par an » " +
                    "ou « tous les 3 mois », elle apparaîtra ici les autres mois.",
                style = MaterialTheme.typography.bodyLarge
            )
        } else {
            state.offMonthPeriodics.forEach { item ->
                Surface(
                    color = A11yTokens.PaperCard,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, A11yTokens.PaperBorder, MaterialTheme.shapes.medium)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(text = item.label, style = MaterialTheme.typography.titleLarge)
                        Text(
                            text = viewModel.formatPeriodicAmountLine(item),
                            style = MaterialTheme.typography.bodyLarge
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

        HorizontalDivider(color = A11yTokens.InkMuted.copy(alpha = 0.25f))
        Text(text = "Sauvegarde", style = MaterialTheme.typography.titleLarge)
        Text(
            text = "Copie automatique dans Documents (3 versions). " +
                "Envoyez une copie si le téléphone casse ; restaurez après réinstall.",
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
            text = "Envoyer une copie de sécurité",
            onClick = viewModel::shareSecurityCopy,
            enabled = !state.busy
        )
        SecondaryA11yButton(
            text = "Restaurer une sauvegarde",
            onClick = onOpenRestore,
            enabled = !state.busy
        )

        HorizontalDivider(color = A11yTokens.InkMuted.copy(alpha = 0.25f))
        Text(text = "À propos", style = MaterialTheme.typography.titleLarge)
        Text(
            text = "Mon Mois Clair — suivi local des paiements du mois.",
            style = MaterialTheme.typography.bodyLarge
        )
        val versionName = try {
            val p = context.packageManager.getPackageInfo(context.packageName, 0)
            p.versionName ?: "—"
        } catch (_: Exception) {
            "—"
        }
        Text(
            text = "Version $versionName",
            style = MaterialTheme.typography.bodyMedium,
            color = A11yTokens.InkMuted
        )
        Text(
            text = "Données sur cet appareil uniquement. Pas de compte, pas de cloud.",
            style = MaterialTheme.typography.bodyMedium,
            color = A11yTokens.InkMuted
        )
    }
}
