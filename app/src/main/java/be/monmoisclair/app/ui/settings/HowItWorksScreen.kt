package be.monmoisclair.app.ui.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import be.monmoisclair.app.R
import be.monmoisclair.app.ui.components.AppWordmark
import be.monmoisclair.app.ui.components.CarnetPageTitle
import be.monmoisclair.app.ui.components.PinnedBackScaffold
import be.monmoisclair.app.ui.theme.A11yTokens
import be.monmoisclair.app.ui.theme.HomeTokens
import be.monmoisclair.app.ui.theme.rememberDisplayFit

@Composable
fun HowItWorksScreen(
    onBack: () -> Unit
) {
    val fit = rememberDisplayFit()
    val sectionGap = if (fit.compactHomeChrome) 10.dp else 12.dp
    // Raccourci : une section ouverte à la fois (titres scannables, pas mur de texte).
    var openIndex by rememberSaveable { mutableStateOf(0) }
    PinnedBackScaffold(onBack = onBack) {
        Spacer(modifier = Modifier.height(if (fit.compactHomeChrome) 2.dp else 4.dp))
        AppWordmark(width = if (fit.compactHomeChrome) 200.dp else 240.dp)
        // PageTitle ≠ titres de section (titleMedium Ink, tappables).
        CarnetPageTitle(HowItWorksCopy.TITLE, withTopSpacer = false)
        Text(
            text = "Touchez un titre pour lire.",
            style = MaterialTheme.typography.bodyMedium,
            color = A11yTokens.InkMuted,
            modifier = Modifier
                .fillMaxWidth()
                .clip(HomeTokens.chipRadius)
                .background(A11yTokens.PaperWarm)
                .padding(horizontal = 14.dp, vertical = 10.dp)
        )
        Spacer(modifier = Modifier.height(if (fit.compactHomeChrome) 6.dp else 10.dp))
        // 7.23 AID : cartes Accueil (radius 20 + filet) + titreMedium.
        HowItWorksCopy.sections.forEachIndexed { index, section ->
            val expanded = openIndex == index
            val shape = HomeTokens.cardRadius
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(HomeTokens.cardElevation, shape, clip = false)
                    .clip(shape)
                    .background(A11yTokens.PaperCard)
                    .border(HomeTokens.hairline, A11yTokens.PaperBorder, shape)
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = A11yTokens.minTouchHeight)
                        .clickable(
                            role = Role.Button,
                            onClickLabel = if (expanded) {
                                "Replier ${section.title}"
                            } else {
                                "Lire ${section.title}"
                            }
                        ) {
                            openIndex = if (expanded) -1 else index
                        },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = section.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = A11yTokens.Ink,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        painter = painterResource(
                            if (expanded) R.drawable.ic_expand_less
                            else R.drawable.ic_expand_more
                        ),
                        contentDescription = null,
                        tint = if (expanded) A11yTokens.Link else A11yTokens.InkMuted,
                        modifier = Modifier.size(22.dp)
                    )
                }
                AnimatedVisibility(
                    visible = expanded,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Text(
                        text = section.body,
                        style = MaterialTheme.typography.bodyLarge,
                        color = A11yTokens.InkMuted,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(sectionGap))
        }
        // Contact = Menu Accueil (QuietLink) — pas de doublon ici.
    }
}
