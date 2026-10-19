package be.monmoisclair.app.ui.lock

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import be.monmoisclair.app.data.biometric.BiometricAuthGateway
import be.monmoisclair.app.data.biometric.BiometricAuthResult
import be.monmoisclair.app.ui.components.PrimaryA11yButton
import be.monmoisclair.app.ui.theme.A11yTokens

@Composable
fun BiometricLockScreen(
    gateway: BiometricAuthGateway,
    message: String?,
    onAuthenticated: () -> Unit,
    onAuthMessage: (String?) -> Unit,
    autoPrompt: Boolean = true,
    modifier: Modifier = Modifier
) {
    val activity = LocalContext.current as FragmentActivity

    fun prompt() {
        gateway.authenticate(
            activity = activity,
            title = "Mon Mois Clair",
            subtitle = "Déverrouillez avec votre empreinte ou le code du téléphone.",
            negativeButton = "Annuler",
            onResult = { result ->
                when (result) {
                    BiometricAuthResult.Success -> {
                        onAuthMessage(null)
                        onAuthenticated()
                    }
                    BiometricAuthResult.Cancelled ->
                        onAuthMessage("Verrouillage annulé. Touchez le bouton pour réessayer.")
                    is BiometricAuthResult.Failed ->
                        onAuthMessage(result.message)
                }
            }
        )
    }

    LaunchedEffect(Unit) {
        if (autoPrompt) prompt()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(A11yTokens.PaperCream)
            .padding(A11yTokens.screenPadding),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        Text(text = "Mon Mois Clair", style = MaterialTheme.typography.displayLarge)
        Text(
            text = "Application verrouillée",
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = "Posez votre doigt sur le lecteur d’empreinte, " +
                "ou utilisez le code / visage du téléphone.",
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = "Si rien ne s’ouvre : touchez le gros bouton vert ci-dessous.",
            style = MaterialTheme.typography.bodyMedium,
            color = A11yTokens.InkMuted
        )
        message?.let {
            Text(
                text = it,
                color = A11yTokens.AccentDanger,
                style = MaterialTheme.typography.bodyLarge
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        PrimaryA11yButton(text = "Déverrouiller maintenant", onClick = { prompt() })
    }
}
