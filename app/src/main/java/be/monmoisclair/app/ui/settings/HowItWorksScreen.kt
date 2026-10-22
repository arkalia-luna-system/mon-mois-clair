package be.monmoisclair.app.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import be.monmoisclair.app.R
import be.monmoisclair.app.ui.components.AppWordmark
import be.monmoisclair.app.ui.components.PinnedBackScaffold
import be.monmoisclair.app.ui.components.QuietLinkA11y
import be.monmoisclair.app.ui.theme.A11yTokens
import be.monmoisclair.app.ui.theme.rememberDisplayFit

@Composable
fun HowItWorksScreen(
    onBack: () -> Unit,
    onOpenSupport: (() -> Unit)? = null
) {
    val fit = rememberDisplayFit()
    val sectionGap = if (fit.compactHomeChrome) 12.dp else 16.dp
    // Raccourci : une section ouverte à la fois (titres scannables, pas mur de texte).
    var openIndex by rememberSaveable { mutableStateOf(0) }
    PinnedBackScaffold(onBack = onBack) {
        Spacer(modifier = Modifier.height(if (fit.compactHomeChrome) 2.dp else 4.dp))
        AppWordmark(width = if (fit.compactHomeChrome) 200.dp else 240.dp)
        Text(
            text = HowItWorksCopy.TITLE,
            style = MaterialTheme.typography.headlineMedium,
            color = A11yTokens.Ink
        )
        Text(
            text = "Touchez un titre pour lire.",
            style = MaterialTheme.typography.bodyMedium,
            color = A11yTokens.InkMuted
        )
        Spacer(modifier = Modifier.height(if (fit.compactHomeChrome) 4.dp else 8.dp))
        // AID01 : titres + corps InkMuted, filets papier (pas de cartes tip).
        HowItWorksCopy.sections.forEachIndexed { index, section ->
            if (index > 0) {
                HorizontalDivider(color = A11yTokens.PaperBorder)
            }
            val expanded = openIndex == index
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(
                    if (fit.compactHomeChrome) 4.dp else 6.dp
                )
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
                        style = MaterialTheme.typography.titleLarge,
                        color = A11yTokens.Ink,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        painter = painterResource(
                            if (expanded) R.drawable.ic_expand_less
                            else R.drawable.ic_expand_more
                        ),
                        contentDescription = null,
                        tint = A11yTokens.InkMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }
                if (expanded) {
                    Text(
                        text = section.body,
                        style = MaterialTheme.typography.bodyLarge,
                        color = A11yTokens.InkMuted
                    )
                }
            }
            Spacer(modifier = Modifier.height(sectionGap))
        }
        if (onOpenSupport != null) {
            HorizontalDivider(color = A11yTokens.PaperBorder)
            QuietLinkA11y(
                text = "Un souci avec l’app ?",
                onClick = onOpenSupport
            )
        }
    }
}
