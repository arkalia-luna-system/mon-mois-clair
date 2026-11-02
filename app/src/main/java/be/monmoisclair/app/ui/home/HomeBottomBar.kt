package be.monmoisclair.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import be.monmoisclair.app.R
import be.monmoisclair.app.ui.theme.A11yTokens
import be.monmoisclair.app.ui.theme.HomeTokens
import be.monmoisclair.app.ui.theme.StampShape

/** Socle Menu / + Ajouter (7.10 — icônes filled + surface Accueil). */
@Composable
internal fun HomeBottomBar(
    isFuturePreview: Boolean,
    remindersBlocked: Boolean,
    onMenu: () -> Unit,
    onAdd: () -> Unit,
    onGoCurrentMonth: () -> Unit
) {
    HorizontalDivider(color = HomeTokens.Divider)
    Surface(
        color = HomeTokens.BarSurface,
        shadowElevation = HomeTokens.barElevation,
        tonalElevation = 0.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = HomeTokens.screenPad,
                    vertical = 10.dp
                ),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .semantics(mergeDescendants = true) {
                        contentDescription = if (remindersBlocked) {
                            "Menu. Alertes à régler"
                        } else {
                            "Menu"
                        }
                    }
            ) {
                BottomBarOutlined(
                    text = "Menu",
                    iconRes = R.drawable.ic_menu,
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
                            .background(HomeTokens.Warn, StampShape)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                            .semantics { contentDescription = "" }
                    )
                }
            }
            Box(modifier = Modifier.weight(1.2f)) {
                if (isFuturePreview) {
                    BottomBarOutlined(
                        text = "Mois en cours",
                        iconRes = R.drawable.ic_calendar_month,
                        onClick = onGoCurrentMonth,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    BottomBarFilled(
                        text = "Ajouter",
                        iconRes = R.drawable.ic_add,
                        onClick = onAdd,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun BottomBarFilled(
    text: String,
    iconRes: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptics = LocalHapticFeedback.current
    Button(
        onClick = {
            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onClick()
        },
        modifier = modifier
            .heightIn(min = HomeTokens.minTouch)
            .semantics { contentDescription = "+ $text" },
        shape = MaterialTheme.shapes.small,
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = A11yTokens.ButtonFill,
            contentColor = A11yTokens.ButtonText
        )
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = A11yTokens.ButtonText,
            modifier = Modifier
                .padding(end = 6.dp)
                .size(20.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            maxLines = 1
        )
    }
}

@Composable
private fun BottomBarOutlined(
    text: String,
    iconRes: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptics = LocalHapticFeedback.current
    OutlinedButton(
        onClick = {
            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onClick()
        },
        modifier = modifier.heightIn(min = HomeTokens.minTouch),
        shape = MaterialTheme.shapes.small,
        border = androidx.compose.foundation.BorderStroke(1.dp, HomeTokens.CardBorder),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = HomeTokens.Ink
        )
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = HomeTokens.Ink,
            modifier = Modifier
                .padding(end = 6.dp)
                .size(20.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
            maxLines = 1
        )
    }
}
