package be.monmoisclair.app.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import be.monmoisclair.app.ui.components.PinnedBackScaffold
import be.monmoisclair.app.ui.theme.A11yTokens

@Composable
fun HowItWorksScreen(onBack: () -> Unit) {
    PinnedBackScaffold(onBack = onBack) {
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = HowItWorksCopy.TITLE, style = MaterialTheme.typography.headlineMedium)
        Text(
            text = "Guide court pour utiliser Mon Mois Clair sans stress.",
            style = MaterialTheme.typography.bodyLarge,
            color = A11yTokens.InkMuted
        )
        Spacer(modifier = Modifier.height(8.dp))
        HowItWorksCopy.sections.forEach { section ->
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(text = section.title, style = MaterialTheme.typography.titleLarge)
                Text(text = section.body, style = MaterialTheme.typography.bodyLarge)
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}
