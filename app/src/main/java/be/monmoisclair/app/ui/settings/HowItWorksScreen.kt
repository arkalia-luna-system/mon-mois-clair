package be.monmoisclair.app.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import be.monmoisclair.app.ui.components.SecondaryA11yButton
import be.monmoisclair.app.ui.theme.A11yTokens

@Composable
fun HowItWorksScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(A11yTokens.PaperCream)
            .verticalScroll(rememberScrollState())
            .padding(A11yTokens.screenPadding),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = HowItWorksCopy.TITLE, style = MaterialTheme.typography.headlineMedium)
        Text(
            text = "Guide court pour utiliser Mon Mois Clair sans stress.",
            style = MaterialTheme.typography.bodyLarge
        )
        HowItWorksCopy.sections.forEach { section ->
            Text(text = section.title, style = MaterialTheme.typography.titleLarge)
            Text(text = section.body, style = MaterialTheme.typography.bodyLarge)
        }
        Spacer(modifier = Modifier.height(8.dp))
        SecondaryA11yButton(text = "Retour", onClick = onBack)
        Spacer(modifier = Modifier.height(A11yTokens.bottomSystemClearance))
    }
}
