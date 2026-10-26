package be.monmoisclair.app.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import be.monmoisclair.app.ui.theme.A11yTokens
import be.monmoisclair.app.ui.theme.CarnetPalette
import be.monmoisclair.app.ui.theme.rememberDisplayFit

/**
 * Rôles typo page / section — distincts des [CarnetHubRow] cliquables
 * (`titleLarge` + Ink + ›).
 *
 * PageTitle = modèle Menu : `titleLarge` + InkMuted.
 * SectionTitle = `titleMedium` + InkMuted (en-têtes de groupe non tappables).
 */
object CarnetTitleTokens {
    fun pageTitleStyle(ty: Typography): TextStyle = ty.titleLarge
    fun pageTitleColor(palette: CarnetPalette): Color = palette.inkMuted

    fun sectionTitleStyle(ty: Typography): TextStyle = ty.titleMedium
    fun sectionTitleColor(palette: CarnetPalette): Color = palette.inkMuted

    fun hubRowLabelStyle(ty: Typography): TextStyle = ty.titleLarge
    fun hubRowLabelColor(palette: CarnetPalette): Color = palette.ink
}

/** Titre de page non cliquable (Réglages, Menu, À venir, sheets…). */
@Composable
fun CarnetPageTitle(
    title: String,
    modifier: Modifier = Modifier,
    withTopSpacer: Boolean = true,
    /** Sheets courtes (1 champ) : titleMedium — moins de poids que titleLarge. */
    shortSheet: Boolean = false
) {
    val fit = rememberDisplayFit()
    if (withTopSpacer) {
        Spacer(modifier = Modifier.height(if (fit.compactHomeChrome) 2.dp else 4.dp))
    }
    Text(
        text = title,
        style = if (shortSheet) {
            MaterialTheme.typography.titleMedium
        } else {
            CarnetTitleTokens.pageTitleStyle(MaterialTheme.typography)
        },
        color = A11yTokens.InkMuted,
        modifier = modifier
    )
}

/** En-tête de section non disclosure (Dossiers, labels Fréquence…). */
@Composable
fun CarnetSectionTitle(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        style = CarnetTitleTokens.sectionTitleStyle(MaterialTheme.typography),
        color = A11yTokens.InkMuted,
        modifier = modifier.padding(top = 8.dp, bottom = 4.dp)
    )
}
