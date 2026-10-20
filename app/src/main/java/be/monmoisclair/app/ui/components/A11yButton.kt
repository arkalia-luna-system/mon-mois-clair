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
    Button(
        onClick = onClick,
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
            contentColor = A11yTokens.ButtonText
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
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .then(if (fillMaxWidth) Modifier.fillMaxWidth() else Modifier)
            .heightIn(min = A11yTokens.minTouchHeight)
            .defaultMinSize(minHeight = A11yTokens.minTouchHeight),
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(1.dp, A11yTokens.InkMuted),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = A11yTokens.Ink),
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
    enabled: Boolean = true
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = A11yTokens.minTouchHeight),
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(1.dp, A11yTokens.AccentDanger),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = A11yTokens.AccentDanger
        )
    ) {
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
}
