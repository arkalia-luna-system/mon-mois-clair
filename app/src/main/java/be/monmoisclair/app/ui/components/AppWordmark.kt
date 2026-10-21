package be.monmoisclair.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import be.monmoisclair.app.R

/** Carnet + nom, repris du logo (livre ouvert, filet or, coche). */
private const val LockupRatio = 960f / 715f

/** Livre seul, pour une bandeau ou une petite accroche. */
private const val MarkRatio = 256f / 210f

/**
 * Le logo complet. Pas sur la liste du mois.
 * [width] est la largeur affichée ; la hauteur suit l’image.
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
    Image(
        painter = painterResource(R.drawable.book_lockup),
        contentDescription = null,
        contentScale = ContentScale.Fit,
        modifier = modifier
            .then(spoken)
            .widthIn(max = width)
            .fillMaxWidth()
            .aspectRatio(LockupRatio)
    )
}

/** Le carnet seul, à poser au bord d’un bandeau. */
@Composable
fun BrandMark(
    modifier: Modifier = Modifier,
    height: Dp = 28.dp
) {
    Image(
        painter = painterResource(R.drawable.book_mark),
        contentDescription = null,
        contentScale = ContentScale.Fit,
        modifier = modifier
            .height(height)
            .aspectRatio(MarkRatio)
    )
}
