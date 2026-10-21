package be.monmoisclair.app.ui.lock

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import be.monmoisclair.app.R
import be.monmoisclair.app.data.biometric.BiometricAuthGateway
import be.monmoisclair.app.data.biometric.BiometricAuthResult
import be.monmoisclair.app.ui.components.PrimaryA11yButton
import be.monmoisclair.app.ui.theme.A11yTokens
import be.monmoisclair.app.ui.theme.rememberDisplayFit

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
    val fit = rememberDisplayFit()

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
        Spacer(modifier = Modifier.height(if (fit.compactHomeChrome) 16.dp else 32.dp))
        val brandShape = MaterialTheme.shapes.medium
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, A11yTokens.PaperBorder, brandShape)
                .background(A11yTokens.PaperCard, brandShape)
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.ic_brand_m),
                contentDescription = "Mon Mois Clair",
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = "Mon Mois Clair",
                style = MaterialTheme.typography.labelLarge,
                color = A11yTokens.InkMuted
            )
            Text(
                text = "Application verrouillée",
                style = MaterialTheme.typography.headlineMedium,
                color = A11yTokens.Ink
            )
        }
        val tipShape = MaterialTheme.shapes.medium
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, A11yTokens.PaperBorder, tipShape)
                .background(A11yTokens.PaperCard, tipShape)
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Posez votre doigt sur le lecteur d’empreinte, " +
                    "ou utilisez le code / visage du téléphone.",
                style = MaterialTheme.typography.bodyLarge,
                color = A11yTokens.InkMuted
            )
            Text(
                text = "Si rien ne s’ouvre : touchez « Déverrouiller maintenant ».",
                style = MaterialTheme.typography.bodyMedium,
                color = A11yTokens.InkMuted
            )
        }
        message?.let {
            val errShape = MaterialTheme.shapes.medium
            Text(
                text = it,
                color = A11yTokens.AccentDanger,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, A11yTokens.AccentDanger.copy(alpha = 0.4f), errShape)
                    .background(A11yTokens.PaperWarm, errShape)
                    .padding(12.dp)
                    .semantics {
                        liveRegion = LiveRegionMode.Assertive
                        contentDescription = it
                    }
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        PrimaryA11yButton(text = "Déverrouiller maintenant", onClick = { prompt() })
    }
}
