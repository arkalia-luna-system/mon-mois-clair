package be.monmoisclair.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
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
    /** Teal par défaut. Forêt pour « FAIT ✓ » sur la ligne (pas un 2ᵉ bouton de barre). */
    containerColor: Color = A11yTokens.ButtonFill,
    contentColor: Color = A11yTokens.ButtonText
) {
    val haptics = LocalHapticFeedback.current
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
        shape = MaterialTheme.shapes.small,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = containerColor.copy(alpha = 0.45f),
            disabledContentColor = contentColor.copy(alpha = 0.85f)
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
    // Contour PaperBorder + poids Medium : plus discret que les titres de section.
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
            contentColor = A11yTokens.Ink,
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
    fillMaxWidth: Boolean = true
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
            .defaultMinSize(minHeight = A11yTokens.minTouchHeight),
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
