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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import be.monmoisclair.app.R
import be.monmoisclair.app.ui.theme.A11yTokens

/**
 * Wordmark 7.12 : monogramme M mist/teal + nom serif (plus de lockup webp carnet).
 * Pas sur la liste du mois.
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
    val markSize = (width.value * 0.22f).dp.coerceIn(36.dp, 56.dp)
    val titleSp = (width.value * 0.105f).coerceIn(18f, 26f)
    Row(
        modifier = modifier
            .then(spoken)
            .widthIn(max = width),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_brand_mark),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier.size(markSize)
        )
        Text(
            text = "Mon Mois Clair",
            style = MaterialTheme.typography.displaySmall.copy(
                fontSize = titleSp.sp,
                lineHeight = (titleSp + 4f).sp,
                letterSpacing = (-0.01).sp
            ),
            color = A11yTokens.Ink,
            maxLines = 1
        )
    }
}

/** Monogramme seul, pour une bandeau ou une petite accroche. */
@Composable
fun BrandMark(
    modifier: Modifier = Modifier,
    height: Dp = 28.dp
) {
    Icon(
        painter = painterResource(R.drawable.ic_brand_mark),
        contentDescription = null,
        tint = Color.Unspecified,
        modifier = modifier.size(height)
    )
}
