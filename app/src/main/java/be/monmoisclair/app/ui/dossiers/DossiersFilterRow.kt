package be.monmoisclair.app.ui.dossiers

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.unit.dp
import be.monmoisclair.app.domain.usecase.DossiersOpenFilter
import be.monmoisclair.app.domain.usecase.DossiersOpenFilterLogic
import be.monmoisclair.app.ui.theme.A11yTokens
import be.monmoisclair.app.ui.theme.StampShape

@Composable
fun DossiersFilterRow(
    selectedFilter: DossiersOpenFilter,
    onSelect: (DossiersOpenFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        DossiersOpenFilter.entries.forEach { filter ->
            val label = DossiersOpenFilterLogic.chipLabel(filter)
            val on = filter == selectedFilter
            val shape = StampShape
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                color = if (on) A11yTokens.Ink else A11yTokens.InkMuted,
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = A11yTokens.minTouchHeight)
                    .clip(shape)
                    .border(
                        width = if (on) 1.5.dp else 1.dp,
                        color = if (on) A11yTokens.Link else A11yTokens.PaperBorder,
                        shape = shape
                    )
                    .background(
                        if (on) A11yTokens.PaperCard else A11yTokens.PaperWarm
                    )
                    .clickable(role = Role.Button) { onSelect(filter) }
                    .padding(horizontal = 10.dp, vertical = 10.dp)
                    .semantics {
                        contentDescription = "Filtre $label"
                        selected = on
                    }
            )
        }
    }
}
