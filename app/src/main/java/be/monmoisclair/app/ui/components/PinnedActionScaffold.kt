package be.monmoisclair.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import be.monmoisclair.app.ui.theme.A11yTokens
import be.monmoisclair.app.ui.theme.rememberDisplayFit

/**
 * Onboarding / flux guidé : contenu scrollable + **une** action primaire fixe en bas
 * (évite que « Continuer » disparaisse sous le pli sur S26).
 */
@Composable
fun PinnedActionScaffold(
    primaryLabel: String,
    onPrimary: () -> Unit,
    primaryEnabled: Boolean = true,
    secondaryLabel: String? = null,
    onSecondary: (() -> Unit)? = null,
    secondaryEnabled: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    val fit = rememberDisplayFit()
    val contentGap = if (fit.compactHomeChrome) 8.dp else 12.dp
    val pinGap = if (fit.compactHomeChrome) 8.dp else 12.dp
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(A11yTokens.PaperCream)
            .imePadding()
            .padding(A11yTokens.screenPadding)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(contentGap),
            content = content
        )
        Spacer(modifier = Modifier.height(pinGap))
        if (secondaryLabel != null && onSecondary != null) {
            SecondaryA11yButton(
                text = secondaryLabel,
                onClick = onSecondary,
                enabled = secondaryEnabled
            )
            Spacer(modifier = Modifier.height(if (fit.compactHomeChrome) 8.dp else 10.dp))
        }
        PrimaryA11yButton(
            text = primaryLabel,
            onClick = onPrimary,
            enabled = primaryEnabled
        )
    }
}
