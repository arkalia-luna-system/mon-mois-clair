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
import be.monmoisclair.app.ui.components.PinnedBackScaffold
import be.monmoisclair.app.ui.theme.A11yTokens

@Composable
fun HowItWorksScreen(onBack: () -> Unit) {
    PinnedBackScaffold(onBack = onBack) {
        Spacer(modifier = Modifier.height(4.dp))
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
                .padding(14.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
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
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
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
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}
