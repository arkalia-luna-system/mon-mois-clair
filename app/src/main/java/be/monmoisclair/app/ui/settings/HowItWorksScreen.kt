package be.monmoisclair.app.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
    val tipPad = if (fit.compactHomeChrome) 12.dp else 14.dp
    val sectionGap = if (fit.compactHomeChrome) 8.dp else 10.dp
    PinnedBackScaffold(onBack = onBack) {
        Spacer(modifier = Modifier.height(if (fit.compactHomeChrome) 2.dp else 4.dp))
        AppWordmark(markSize = if (fit.compactHomeChrome) 28.dp else 32.dp)
        Text(
            text = HowItWorksCopy.TITLE,
            style = MaterialTheme.typography.headlineMedium,
            color = A11yTokens.Ink
        )
        val introShape = MaterialTheme.shapes.medium
        Text(
            text = "Guide court pour utiliser Mon Mois Clair sans stress.",
            style = MaterialTheme.typography.bodyLarge,
            color = A11yTokens.InkMuted,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, A11yTokens.PaperBorder, introShape)
                .background(A11yTokens.PaperCard, introShape)
                .padding(tipPad)
        )
        Spacer(modifier = Modifier.height(if (fit.compactHomeChrome) 4.dp else 8.dp))
        HowItWorksCopy.sections.forEach { section ->
            val shape = MaterialTheme.shapes.medium
            Surface(
                color = A11yTokens.PaperCard,
                shape = shape,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, A11yTokens.PaperBorder, shape)
            ) {
                Column(
                    modifier = Modifier.padding(tipPad),
                    verticalArrangement = Arrangement.spacedBy(
                        if (fit.compactHomeChrome) 6.dp else 8.dp
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
            }
            Spacer(modifier = Modifier.height(sectionGap))
        }
    }
}
