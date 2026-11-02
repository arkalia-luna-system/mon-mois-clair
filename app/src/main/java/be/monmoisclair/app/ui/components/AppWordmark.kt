package be.monmoisclair.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import be.monmoisclair.app.R
import be.monmoisclair.app.ui.theme.A11yTokens

/**
 * Wordmark 7.15 : mark photoréal kit + « Mon Mois » encre + « Clair » or champagne.
 * Aligné kit brand (Downloads jf2e4w / wn6h37). Pas sur la liste du mois.
 */
@Composable
fun AppWordmark(
    modifier: Modifier = Modifier,
    width: Dp = 220.dp,
    announce: Boolean = true
) {
    val spoken = if (announce) {
        Modifier.semantics { contentDescription = "Mon Mois Clair" }
    } else {
        Modifier.clearAndSetSemantics {}
    }
    val markSize = (width.value * 0.18f).dp.coerceIn(32.dp, 52.dp)
    // Garde ≥ 18 sp a11y ; largeur min recommandée ~220 pour « Clair » visible.
    val titleSp = (width.value * 0.095f).coerceIn(18f, 26f)
    val wordmark = buildAnnotatedString {
        withStyle(SpanStyle(color = A11yTokens.Ink)) {
            append("Mon Mois ")
        }
        withStyle(SpanStyle(color = A11yTokens.AccentAuto)) {
            append("Clair")
        }
    }
    Row(
        modifier = modifier
            .then(spoken)
            .widthIn(max = width),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_brand_mark_kit),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier.size(markSize)
        )
        Text(
            text = wordmark,
            style = MaterialTheme.typography.displaySmall.copy(
                fontSize = titleSp.sp,
                lineHeight = (titleSp + 4f).sp,
                letterSpacing = (-0.01).sp
            ),
            maxLines = 1
        )
    }
}

/** Marque livre+coche seule, pour bandeau ou accroche. */
@Composable
fun BrandMark(
    modifier: Modifier = Modifier,
    height: Dp = 28.dp
) {
    Icon(
        painter = painterResource(R.drawable.ic_brand_mark_kit),
        contentDescription = null,
        tint = Color.Unspecified,
        modifier = modifier.size(height)
    )
}
