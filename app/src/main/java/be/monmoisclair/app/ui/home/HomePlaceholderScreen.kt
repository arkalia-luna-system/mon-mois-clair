package be.monmoisclair.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import be.monmoisclair.app.ui.components.PrimaryA11yButton
import be.monmoisclair.app.ui.components.SecondaryA11yButton
import be.monmoisclair.app.ui.theme.A11yTokens

/**
 * Placeholder jusqu’à l’écran liste (étape 3.x).
 * Permet de valider onboarding + Santé sur le S25 dès maintenant.
 */
@Composable
fun HomePlaceholderScreen(
    onOpenHealth: () -> Unit,
    onOpenSafAgain: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(A11yTokens.PaperCream)
            .padding(A11yTokens.screenPadding),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Text(text = "Mon Mois Clair", style = MaterialTheme.typography.displayLarge)
        Text(
            text = "Configuration terminée",
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = "La sauvegarde automatique et le moteur de rappels sont prêts. " +
                "La liste mensuelle arrive à la prochaine étape.",
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.weight(1f))
        PrimaryA11yButton(text = "Santé des rappels", onClick = onOpenHealth)
        SecondaryA11yButton(text = "Changer le dossier de sauvegarde", onClick = onOpenSafAgain)
    }
}
