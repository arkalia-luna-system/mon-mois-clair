package be.monmoisclair.app.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import be.monmoisclair.app.ui.components.CarnetPageTitle
import be.monmoisclair.app.ui.theme.A11yTokens
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
internal fun SettingsPageTitle(title: String) {
    // PageTitle ≠ HubRow : titleLarge + InkMuted (modèle Menu).
    CarnetPageTitle(title)
}

internal fun savingsActionLabel(busy: Boolean, active: Boolean): String = when {
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
    // 7.11 Apparence : cartes soft sélectionnables (pas radio plat).
    val shape = MaterialTheme.shapes.medium
    val bg = if (selected) A11yTokens.PaperWarm else A11yTokens.PaperCard
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(if (selected) 0.dp else 2.dp, shape, clip = false)
            .clip(shape)
            .background(bg)
            .heightIn(min = A11yTokens.minTouchHeight)
            .clickable(
                enabled = enabled,
                role = Role.RadioButton,
                onClick = onSelect
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
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
            style = MaterialTheme.typography.titleLarge,
            color = A11yTokens.Ink
        )
    }
}
