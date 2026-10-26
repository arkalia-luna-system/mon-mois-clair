package be.monmoisclair.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import be.monmoisclair.app.ui.components.PrimaryA11yButton
import be.monmoisclair.app.ui.components.SecondaryA11yButton
import be.monmoisclair.app.ui.theme.A11yTokens
import be.monmoisclair.app.ui.theme.StampShape

/** Socle Menu / + Ajouter (ou Mois en cours en aperçu futur). */
@Composable
internal fun HomeBottomBar(
    isFuturePreview: Boolean,
    remindersBlocked: Boolean,
    onMenu: () -> Unit,
    onAdd: () -> Unit,
    onGoCurrentMonth: () -> Unit
) {
    HorizontalDivider(color = A11yTokens.PaperBorder)
    // H11 / P0-D1 : socle ≥ 8 dp ; filet seul (pas shadowElevation).
    Surface(
        color = A11yTokens.PaperWarm,
        shadowElevation = 0.dp,
        tonalElevation = 0.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = A11yTokens.screenPadding,
                    vertical = 8.dp
                ),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.weight(1f)) {
                SecondaryA11yButton(
                    text = "Menu",
                    onClick = onMenu,
                    modifier = Modifier.fillMaxWidth()
                )
                if (remindersBlocked) {
                    Text(
                        text = "!",
                        style = MaterialTheme.typography.labelLarge,
                        color = A11yTokens.ButtonText,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 2.dp, end = 2.dp)
                            .background(A11yTokens.AccentWarn, StampShape)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                            .semantics {
                                contentDescription = "Alertes à régler — ouvrir le Menu"
                            }
                    )
                }
            }
            Box(modifier = Modifier.weight(1.2f)) {
                if (isFuturePreview) {
                    SecondaryA11yButton(
                        text = "Mois en cours",
                        onClick = onGoCurrentMonth,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    PrimaryA11yButton(
                        text = "+ Ajouter",
                        onClick = onAdd,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
