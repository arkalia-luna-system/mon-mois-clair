package be.monmoisclair.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import be.monmoisclair.app.ui.theme.A11yTokens

/** Bannières d’état 7.10 (ok / erreur / warn) — cartes soft, hors tip muted. */
enum class CarnetBannerKind { Ok, Error, Warn }

@Composable
fun CarnetStateBanner(
    text: String,
    kind: CarnetBannerKind,
    pad: Dp = 14.dp,
    modifier: Modifier = Modifier
) {
    val shape = MaterialTheme.shapes.medium
    val ink = when (kind) {
        CarnetBannerKind.Ok -> A11yTokens.AccentOkInk
        CarnetBannerKind.Error -> A11yTokens.DangerInk
        CarnetBannerKind.Warn -> A11yTokens.AccentWarn
    }
    val border = when (kind) {
        CarnetBannerKind.Ok -> A11yTokens.AccentOkInk.copy(alpha = 0.28f)
        CarnetBannerKind.Error -> A11yTokens.DangerInk.copy(alpha = 0.35f)
        CarnetBannerKind.Warn -> A11yTokens.AccentWarn.copy(alpha = 0.35f)
    }
    val bg = when (kind) {
        CarnetBannerKind.Ok -> A11yTokens.AccentOkSoft
        CarnetBannerKind.Error -> A11yTokens.PaperWarm
        CarnetBannerKind.Warn -> A11yTokens.PaperWarm
    }
    val assertive = kind == CarnetBannerKind.Error
    Text(
        text = text,
        color = ink,
        style = MaterialTheme.typography.bodyLarge,
        modifier = modifier
            .fillMaxWidth()
            .shadow(1.dp, shape, clip = false)
            .border(1.dp, border, shape)
            .background(bg, shape)
            .padding(pad)
            .semantics {
                liveRegion = if (assertive) LiveRegionMode.Assertive else LiveRegionMode.Polite
                contentDescription = text
            }
    )
}
