package be.monmoisclair.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
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
    fillMaxWidth: Boolean = true
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
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(
            containerColor = A11yTokens.ButtonFill,
            contentColor = A11yTokens.ButtonText,
            disabledContainerColor = A11yTokens.ButtonFill.copy(alpha = 0.45f),
            disabledContentColor = A11yTokens.ButtonText.copy(alpha = 0.85f)
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
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(
            1.dp,
            if (enabled) A11yTokens.FieldLine else A11yTokens.FieldLine.copy(alpha = 0.45f)
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
            style = MaterialTheme.typography.labelLarge,
            textAlign = TextAlign.Center,
            maxLines = 2
        )
    }
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
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(
            1.dp,
            if (enabled) A11yTokens.AccentDanger else A11yTokens.AccentDanger.copy(alpha = 0.4f)
        ),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = A11yTokens.AccentDanger,
            disabledContentColor = A11yTokens.AccentDanger.copy(alpha = 0.45f)
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
