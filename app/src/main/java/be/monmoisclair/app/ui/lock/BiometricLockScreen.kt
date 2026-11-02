package be.monmoisclair.app.ui.lock

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import be.monmoisclair.app.data.biometric.BiometricAuthGateway
import be.monmoisclair.app.data.biometric.BiometricAuthResult
import be.monmoisclair.app.ui.components.AppWordmark
import be.monmoisclair.app.ui.components.CarnetPageTitle
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
            subtitle = "Empreinte ou code du téléphone.",
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

    val lockGap = if (fit.compactHomeChrome) 12.dp else 16.dp
    // Taps + scrolls : ne pas atteindre le NavHost dessous (TalkBack = clearAndSetSemantics côté Activity).
    val eatClicks = remember { MutableInteractionSource() }
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(A11yTokens.PaperCream)
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    while (true) {
                        val event = awaitPointerEvent()
                        event.changes.forEach { it.consume() }
                        if (event.changes.none { it.pressed }) break
                    }
                }
            }
            .clickable(
                interactionSource = eatClicks,
                indication = null,
                onClick = { }
            )
            .semantics {
                contentDescription = "Application verrouillée"
            }
            .padding(A11yTokens.screenPadding),
        verticalArrangement = Arrangement.spacedBy(lockGap)
    ) {
        Spacer(modifier = Modifier.height(if (fit.compactHomeChrome) 12.dp else 32.dp))
        // LOCK01 : une composition, pas deux cartes empilées.
        AppWordmark(width = if (fit.compactHomeChrome) 200.dp else 260.dp)
        CarnetPageTitle("Application verrouillée", withTopSpacer = false)
        Text(
            text = "Empreinte, code ou visage. Si rien ne s’ouvre : le bouton ci-dessous.",
            style = MaterialTheme.typography.bodyLarge,
            color = A11yTokens.InkMuted
        )
        message?.let {
            val errShape = MaterialTheme.shapes.medium
            Text(
                text = it,
                color = A11yTokens.DangerInk,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, A11yTokens.DangerInk.copy(alpha = 0.4f), errShape)
                    .background(A11yTokens.PaperCard, errShape)
                    .padding(if (fit.compactHomeChrome) 10.dp else 12.dp)
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
