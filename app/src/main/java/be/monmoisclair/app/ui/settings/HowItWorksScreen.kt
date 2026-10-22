package be.monmoisclair.app.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import be.monmoisclair.app.ui.components.AppWordmark
import be.monmoisclair.app.ui.components.PinnedBackScaffold
import be.monmoisclair.app.ui.components.QuietLinkA11y
import be.monmoisclair.app.ui.theme.A11yTokens
import be.monmoisclair.app.ui.theme.rememberDisplayFit

@Composable
fun HowItWorksScreen(
    onBack: () -> Unit,
    onOpenSupport: (() -> Unit)? = null
) {
    val fit = rememberDisplayFit()
    val sectionGap = if (fit.compactHomeChrome) 12.dp else 16.dp
    PinnedBackScaffold(onBack = onBack) {
        Spacer(modifier = Modifier.height(if (fit.compactHomeChrome) 2.dp else 4.dp))
        AppWordmark(width = if (fit.compactHomeChrome) 200.dp else 240.dp)
        Text(
            text = HowItWorksCopy.TITLE,
            style = MaterialTheme.typography.headlineMedium,
            color = A11yTokens.Ink
        )
        Spacer(modifier = Modifier.height(if (fit.compactHomeChrome) 4.dp else 8.dp))
        // AID01 : titres + corps InkMuted, filets papier (pas de cartes tip).
        HowItWorksCopy.sections.forEachIndexed { index, section ->
            if (index > 0) {
                HorizontalDivider(color = A11yTokens.PaperBorder)
                Spacer(modifier = Modifier.height(if (fit.compactHomeChrome) 8.dp else 10.dp))
            }
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(
                    if (fit.compactHomeChrome) 4.dp else 6.dp
                )
            ) {
                Text(
                    text = section.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = A11yTokens.Ink
                )
                Text(
                    text = section.body,
                    style = MaterialTheme.typography.bodyLarge,
                    color = A11yTokens.InkMuted
                )
            }
            Spacer(modifier = Modifier.height(sectionGap))
        }
        if (onOpenSupport != null) {
            HorizontalDivider(color = A11yTokens.PaperBorder)
            QuietLinkA11y(
                text = "Un souci avec l’app ?",
                onClick = onOpenSupport
            )
        }
    }
}
