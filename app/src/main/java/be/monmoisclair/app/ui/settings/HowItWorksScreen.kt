package be.monmoisclair.app.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import be.monmoisclair.app.ui.components.AppWordmark
import be.monmoisclair.app.ui.components.PinnedBackScaffold
import be.monmoisclair.app.ui.theme.A11yTokens
import be.monmoisclair.app.ui.theme.rememberDisplayFit

@Composable
fun HowItWorksScreen(onBack: () -> Unit) {
    val fit = rememberDisplayFit()
    val sectionGap = if (fit.compactHomeChrome) 14.dp else 18.dp
    PinnedBackScaffold(onBack = onBack) {
        Spacer(modifier = Modifier.height(if (fit.compactHomeChrome) 2.dp else 4.dp))
        AppWordmark(width = if (fit.compactHomeChrome) 200.dp else 240.dp)
        Text(
            text = HowItWorksCopy.TITLE,
            style = MaterialTheme.typography.headlineMedium,
            color = A11yTokens.Ink
        )
        Spacer(modifier = Modifier.height(if (fit.compactHomeChrome) 4.dp else 8.dp))
        // AID01 : titres + corps InkMuted, sans cartes tip empilées.
        HowItWorksCopy.sections.forEach { section ->
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
    }
}
