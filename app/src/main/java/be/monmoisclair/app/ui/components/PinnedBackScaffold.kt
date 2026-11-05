package be.monmoisclair.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import be.monmoisclair.app.ui.theme.A11yTokens
import be.monmoisclair.app.ui.theme.rememberDisplayFit

/**
 * Écran secondaire : contenu scrollable + **Retour toujours visible** en bas
 * (évite de chercher le bouton sous le pli sur S26).
 */
@Composable
fun PinnedBackScaffold(
    onBack: () -> Unit,
    backEnabled: Boolean = true,
    backLabel: String = "Retour",
    /** Actions collées au-dessus de Retour (ex. Activer sous le clavier). */
    pinnedAboveBack: (@Composable ColumnScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val fit = rememberDisplayFit()
    val contentGap = if (fit.compactHomeChrome) 8.dp else 12.dp
    val pinGap = if (fit.compactHomeChrome) 8.dp else 12.dp
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(A11yTokens.PaperCream)
            .imePadding()
            .padding(A11yTokens.screenPadding)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(contentGap),
            content = content
        )
        HorizontalDivider(
            color = A11yTokens.PaperBorder.copy(alpha = 0.65f),
            modifier = Modifier.padding(top = pinGap)
        )
        if (pinnedAboveBack != null) {
            Spacer(modifier = Modifier.height(pinGap))
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(if (fit.compactHomeChrome) 8.dp else 10.dp),
                content = pinnedAboveBack
            )
        }
        Spacer(modifier = Modifier.height(pinGap))
        SecondaryA11yButton(
            text = backLabel,
            onClick = onBack,
            enabled = backEnabled
        )
    }
}
