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
import androidx.compose.material3.Checkbox
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

    PinnedBackScaffold(onBack = onBack, backEnabled = !state.busy) {
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = "Réglages", style = MaterialTheme.typography.headlineMedium)
        if (state.busy) {
            val busyShape = MaterialTheme.shapes.medium
            Text(
                text = "Préparation de la copie…",
                style = MaterialTheme.typography.bodyMedium,
                color = A11yTokens.InkMuted,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, A11yTokens.PaperBorder, busyShape)
                    .background(A11yTokens.PaperWarm, busyShape)
                    .padding(12.dp)
                    .semantics {
                        liveRegion = LiveRegionMode.Polite
                        contentDescription = "Préparation de la copie"
                    }
            )
        }

        SettingsCard(title = "Aide") {
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
        }

        SettingsCard(title = "Confidentialité") {
            Text(
                text = "Par défaut l’app s’ouvre sans code.",
                style = MaterialTheme.typography.bodyLarge,
                color = A11yTokens.InkMuted
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
            Text(
                text = when (state.availability) {
                    BiometricAvailability.Ready ->
                        "Disponible sur ce téléphone (empreinte, visage ou code)."
                    BiometricAvailability.NoneEnrolled ->
                        "Aucune empreinte / code d’écran enregistré. Configurez-les dans les réglages du téléphone."
                    BiometricAvailability.HardwareUnavailable ->
                        "Empreinte ou code temporairement indisponible."
                    BiometricAvailability.Unsupported ->
                        "Pas d’empreinte ni de code sur ce téléphone."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = A11yTokens.InkMuted
            )
            state.message?.let { msg ->
                Text(
                    text = msg,
                    color = if (state.isError) A11yTokens.AccentDanger else A11yTokens.AccentOk,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.semantics {
                        liveRegion = if (state.isError) {
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
            Text(
                text = "Notifications, réveil à l’heure et batterie, au même endroit.",
                style = MaterialTheme.typography.bodyMedium,
                color = A11yTokens.InkMuted
            )
            SecondaryA11yButton(
                text = "Vérifier les alertes",
                onClick = onOpenHealth,
                enabled = !state.busy
            )
        }

        SettingsCard(title = "Échéances hors ce mois") {
            Text(
                text = "Assurances, taxes, factures 1×/an ou tous les 3 mois — " +
                    "visibles ici quand elles ne tombent pas ce mois-ci. " +
                    "Touchez une carte pour corriger le nom, le montant, le jour ou le mois.",
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
                Column(verticalArrangement = Arrangement.spacedBy(A11yTokens.cardGap)) {
                    state.offMonthPeriodics.forEach { item ->
                        Surface(
                            color = A11yTokens.PaperCream,
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
            }
        }

        SettingsCard(title = "Sauvegarde") {
            Text(
                text = "Copie automatique dans Documents (3 versions) : mois + photos. " +
                    "« Envoyer une copie » envoie une archive (mois et photos de factures) " +
                    "par WhatsApp ou Mail.",
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = BackupHealthCopy.statusLine(state.backupHealth),
                style = MaterialTheme.typography.bodyLarge,
                color = if (BackupHealthCopy.isOk(state.backupHealth)) {
                    A11yTokens.AccentOk
                } else {
                    A11yTokens.AccentDanger
                },
                modifier = Modifier.semantics {
                    liveRegion = LiveRegionMode.Polite
                    contentDescription = BackupHealthCopy.statusLine(state.backupHealth)
                }
            )
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
        Column(
            modifier = Modifier
                .padding(top = 8.dp)
                .semantics(mergeDescendants = true) {
                    contentDescription = buildString {
                        append("Mon Mois Clair")
                        if (stepLabel != null) append(", étape $stepLabel")
                        append(". Données sur cet appareil uniquement.")
                    }
                }
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
                    color = A11yTokens.InkMuted,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            } else {
                Spacer(modifier = Modifier.height(4.dp))
            }
            Text(
                text = "Données sur cet appareil uniquement. Pas de compte, pas de cloud.",
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
    Surface(
        color = A11yTokens.PaperCard,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, A11yTokens.PaperBorder, MaterialTheme.shapes.medium)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = A11yTokens.Ink
            )
            content()
        }
    }
    Spacer(modifier = Modifier.height(14.dp))
}
