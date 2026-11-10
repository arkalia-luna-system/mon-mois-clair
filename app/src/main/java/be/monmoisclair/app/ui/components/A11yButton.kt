package be.monmoisclair.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import be.monmoisclair.app.ui.theme.A11yTokens

@Composable
fun PrimaryA11yButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    /** false = bouton compact (ex. FAIT à droite d’une carte). */
    fillMaxWidth: Boolean = true,
    /** Teal par défaut. Forêt pour « FAIT » sur la ligne (pas un 2ᵉ bouton de barre). */
    containerColor: Color = A11yTokens.ButtonFill,
    contentColor: Color = A11yTokens.ButtonText,
    /** 7.13 / 7.15 : check FAIT — kit or/teal si [leadingColoredIcon]. */
    leadingIconRes: Int? = null,
    leadingColoredIcon: Boolean = false
) {
    val haptics = LocalHapticFeedback.current
    // 7.11 : FAIT compact = large (24) premium ; barre = small (16).
    val shape = if (fillMaxWidth) MaterialTheme.shapes.small else MaterialTheme.shapes.large
    Button(
        onClick = {
            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onClick()
        },
        enabled = enabled,
        modifier = modifier
            .then(if (fillMaxWidth) Modifier.fillMaxWidth() else Modifier)
            .heightIn(min = A11yTokens.minTouchHeight)
            .defaultMinSize(minHeight = A11yTokens.minTouchHeight),
        contentPadding = if (fillMaxWidth) {
            PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        } else {
            PaddingValues(horizontal = 12.dp, vertical = 8.dp)
        },
        shape = shape,
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = if (fillMaxWidth) 0.dp else 2.dp,
            pressedElevation = 0.dp,
            disabledElevation = 0.dp
        ),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = containerColor.copy(alpha = 0.45f),
            disabledContentColor = contentColor.copy(alpha = 0.85f)
        )
    ) {
        if (leadingIconRes != null) {
            Icon(
                painter = painterResource(leadingIconRes),
                contentDescription = null,
                tint = if (leadingColoredIcon) Color.Unspecified else contentColor,
                modifier = Modifier.size(if (leadingColoredIcon) 20.dp else 18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            textAlign = TextAlign.Center,
            maxLines = 2
        )
    }
}

@Composable
fun SecondaryA11yButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    /** false = compact (ex. FAIT à droite d’une carte — charte un seul primary). */
    fillMaxWidth: Boolean = true
) {
    val haptics = LocalHapticFeedback.current
    // 7.11 : Secondary = carte soft (pas trait plat carnet).
    OutlinedButton(
        onClick = {
            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onClick()
        },
        enabled = enabled,
        modifier = modifier
            .then(if (fillMaxWidth) Modifier.fillMaxWidth() else Modifier)
            .heightIn(min = A11yTokens.minTouchHeight)
            .defaultMinSize(minHeight = A11yTokens.minTouchHeight),
        shape = MaterialTheme.shapes.small,
        border = BorderStroke(
            1.dp,
            if (enabled) A11yTokens.PaperBorder else A11yTokens.PaperBorder.copy(alpha = 0.45f)
        ),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = A11yTokens.PaperCard,
            contentColor = A11yTokens.Ink,
            disabledContainerColor = A11yTokens.PaperCard.copy(alpha = 0.55f),
            disabledContentColor = A11yTokens.InkMuted.copy(alpha = 0.55f)
        ),
        contentPadding = if (fillMaxWidth) {
            PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        } else {
            PaddingValues(horizontal = 12.dp, vertical = 8.dp)
        }
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
            textAlign = TextAlign.Center,
            maxLines = 2
        )
    }
}

/**
 * Action tertiaire (Copier, Voir, Enlever…) — lien teal, pas un 2ᵉ rangée de boutons.
 * Cible ≥ 48 dp (pouce One UI).
 */
@Composable
fun QuietLinkA11y(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    fillMaxWidth: Boolean = true,
    onClickLabel: String? = null
) {
    val haptics = LocalHapticFeedback.current
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        color = if (enabled) A11yTokens.Link else A11yTokens.InkMuted.copy(alpha = 0.55f),
        textAlign = if (fillMaxWidth) TextAlign.Start else TextAlign.Center,
        maxLines = 2,
        modifier = modifier
            .then(if (fillMaxWidth) Modifier.fillMaxWidth() else Modifier)
            .heightIn(min = A11yTokens.minTouchHeight)
            // P1-5 : largeur min ≥ FAIT (96) si lien court — évite faux clics vs Photo.
            .then(
                if (!fillMaxWidth) {
                    Modifier.widthIn(min = 96.dp)
                } else {
                    Modifier
                }
            )
            .clickable(
                enabled = enabled,
                role = Role.Button,
                onClickLabel = onClickLabel ?: text
            ) {
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            }
            .padding(horizontal = 2.dp, vertical = 12.dp)
            .semantics { contentDescription = text }
    )
}

@Composable
fun DangerA11yButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    fillMaxWidth: Boolean = true,
    onClickLabel: String? = null
) {
    val haptics = LocalHapticFeedback.current
    OutlinedButton(
        onClick = {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        },
        enabled = enabled,
        modifier = modifier
            .then(if (fillMaxWidth) Modifier.fillMaxWidth() else Modifier)
            .heightIn(min = A11yTokens.minTouchHeight)
            .defaultMinSize(minHeight = A11yTokens.minTouchHeight)
            .then(
                if (onClickLabel != null) {
                    Modifier.semantics { onClick(label = onClickLabel, action = null) }
                } else {
                    Modifier
                }
            ),
        shape = MaterialTheme.shapes.small,
        border = BorderStroke(
            1.dp,
            if (enabled) A11yTokens.DangerInk else A11yTokens.DangerInk.copy(alpha = 0.4f)
        ),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = A11yTokens.DangerInk,
            disabledContentColor = A11yTokens.DangerInk.copy(alpha = 0.45f)
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            textAlign = TextAlign.Center,
            maxLines = 2
        )
    }
}
