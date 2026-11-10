package be.monmoisclair.app.ui.lock

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import be.monmoisclair.app.R
import be.monmoisclair.app.ui.components.AppWordmark
import be.monmoisclair.app.ui.theme.A11yTokens

/**
 * Affiché si l’app est ouverte avant le premier déverrouillage post-boot (stockage CE fermé).
 */
@Composable
fun UserLockedScreen(modifier: Modifier = Modifier) {
    val title = stringResource(R.string.direct_boot_locked_title)
    val body = stringResource(R.string.direct_boot_locked_body)
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 32.dp)
            .semantics {
                contentDescription = "$title $body"
                liveRegion = LiveRegionMode.Polite
            },
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AppWordmark(width = 200.dp, announce = false)
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = A11yTokens.Ink,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = body,
            style = MaterialTheme.typography.bodyLarge,
            color = A11yTokens.InkMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
