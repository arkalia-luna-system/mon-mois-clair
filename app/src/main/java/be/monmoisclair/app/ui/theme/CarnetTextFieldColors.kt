package be.monmoisclair.app.ui.theme

import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable

/**
 * Champs carnet : fond [PaperCard], contour [FieldLine], focus teal ([Link]).
 * À passer à tout [androidx.compose.material3.OutlinedTextField] de l’app.
 */
@Composable
fun carnetOutlinedTextFieldColors(): TextFieldColors = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = A11yTokens.PaperCard,
    unfocusedContainerColor = A11yTokens.PaperCard,
    disabledContainerColor = A11yTokens.PaperWarm,
    errorContainerColor = A11yTokens.PaperCard,
    focusedBorderColor = A11yTokens.Link,
    unfocusedBorderColor = A11yTokens.FieldLine,
    disabledBorderColor = A11yTokens.PaperBorder,
    errorBorderColor = A11yTokens.DangerInk,
    cursorColor = A11yTokens.Link,
    errorCursorColor = A11yTokens.DangerInk,
    focusedLabelColor = A11yTokens.Link,
    unfocusedLabelColor = A11yTokens.InkMuted,
    disabledLabelColor = A11yTokens.InkMuted.copy(alpha = 0.55f),
    errorLabelColor = A11yTokens.DangerInk,
    focusedTextColor = A11yTokens.Ink,
    unfocusedTextColor = A11yTokens.Ink,
    disabledTextColor = A11yTokens.InkMuted,
    errorTextColor = A11yTokens.Ink,
    focusedPlaceholderColor = A11yTokens.InkMuted,
    unfocusedPlaceholderColor = A11yTokens.InkMuted,
    focusedSupportingTextColor = A11yTokens.InkMuted,
    unfocusedSupportingTextColor = A11yTokens.InkMuted,
    errorSupportingTextColor = A11yTokens.DangerInk
)
