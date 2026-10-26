package be.monmoisclair.app.ui.entry

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import be.monmoisclair.app.ui.components.QuietLinkA11y
import be.monmoisclair.app.ui.components.SecondaryA11yButton
import be.monmoisclair.app.ui.theme.A11yTokens
import be.monmoisclair.app.ui.theme.carnetOutlinedTextFieldColors

/** Section Paiement & preuve (BE QR / +++ / photo / PDF). */
@Composable
internal fun EntryFormPaymentSection(
    showPaymentAssist: Boolean,
    showAutoDebit: Boolean,
    isAutoDebit: Boolean,
    onAutoDebit: (Boolean) -> Unit,
    paymentNote: String,
    onPaymentNote: (String) -> Unit,
    noteBring: Modifier,
    isAgreement: Boolean,
    hasPendingPhoto: Boolean,
    hasPendingPdf: Boolean,
    hasReceiptPhoto: Boolean,
    receiptIsPdf: Boolean,
    receiptPreview: Bitmap?,
    extraReceiptPaths: List<String>,
    isPdfPath: (String) -> Boolean,
    applyFollowing: Boolean,
    isSavingsLine: Boolean,
    hasTemplate: Boolean,
    ocrBusy: Boolean,
    onScanEpc: (() -> Unit)?,
    onTakePhoto: (() -> Unit)?,
    onPickPdf: (() -> Unit)?,
    onClearPendingPdf: (() -> Unit)?,
    onClearReceipt: (() -> Unit)?,
    onViewReceipt: (() -> Unit)?,
    onViewExtraReceipt: ((String) -> Unit)?,
    onRemoveExtraReceipt: ((String) -> Unit)?,
    onCopyPaymentNote: (() -> Unit)?,
    onReadCommFromPhoto: (() -> Unit)?
) {
    // FORM01 : qui → combien → jour (ci-dessus) → Paiement & preuve → Accord.
    // Prélèvement vit dans Paiement (pas un tip cockpit entre grille et section).
    if (showPaymentAssist) {
        val hasPaymentContent =
            paymentNote.isNotBlank() ||
                hasReceiptPhoto ||
                hasPendingPhoto ||
                hasPendingPdf ||
                extraReceiptPaths.isNotEmpty() ||
                isAutoDebit
        // Fermé par défaut : grille jours reste au premier plan ; pièce en attente → ouvre.
        var paymentExpanded by remember {
            mutableStateOf(hasPendingPhoto || hasPendingPdf)
        }
        LaunchedEffect(hasPendingPhoto, hasPendingPdf) {
            if (hasPendingPhoto || hasPendingPdf) paymentExpanded = true
        }
        HorizontalDivider(color = A11yTokens.PaperBorder)
        FormSectionToggle(
            title = "Paiement & preuve",
            expanded = paymentExpanded,
            closedHint = when {
                hasPaymentContent -> "Noté — touchez pour voir"
                else -> "QR, +++ , photo, PDF"
            },
            onToggle = { paymentExpanded = !paymentExpanded }
        )
        if (paymentExpanded) {
            // Secondary = QR / photo / PDF seulement ;
            // QuietLink = Copier / Voir / Enlever / OCR (2ᵉ rang).
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (showAutoDebit) {
                    CheckRow(
                        checked = isAutoDebit,
                        label = "Prélèvement automatique",
                        onChecked = onAutoDebit
                    )
                }
                if (onScanEpc != null) {
                    SecondaryA11yButton(text = "Scanner une facture (QR)", onClick = onScanEpc)
                }
                OutlinedTextField(
                    value = paymentNote,
                    onValueChange = onPaymentNote,
                    label = { Text("Référence / communication") },
                    placeholder = { Text("+++090/9337/55493+++") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = A11yTokens.minTouchHeight)
                        .then(noteBring),
                    textStyle = MaterialTheme.typography.bodyLarge,
                    shape = MaterialTheme.shapes.medium,
                    colors = carnetOutlinedTextFieldColors(),
                )
                if (!isAgreement) {
                    Text(
                        text = "Eau, énergie, télécom : souvent une nouvelle +++ chaque mois.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = A11yTokens.InkMuted,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                if (onCopyPaymentNote != null && paymentNote.isNotBlank()) {
                    QuietLinkA11y(
                        text = "Copier la communication",
                        onClick = onCopyPaymentNote
                    )
                }
                if (onReadCommFromPhoto != null) {
                    QuietLinkA11y(
                        text = if (ocrBusy) {
                            "Lecture de la photo…"
                        } else {
                            "Lire la communication sur la photo"
                        },
                        onClick = onReadCommFromPhoto,
                        enabled = !ocrBusy
                    )
                }
                // Photo / PDF : CTA d’ajout seulement si absents ;
                // sinon via le bloc pièce (aperçu / reprendre / enlever).
                val noPhotoYet = !hasReceiptPhoto && !hasPendingPhoto
                val noPdfYet = !hasReceiptPhoto && !hasPendingPdf &&
                    extraReceiptPaths.none { isPdfPath(it) }
                val appendOnAgreement =
                    isAgreement && hasReceiptPhoto && !hasPendingPhoto && !hasPendingPdf
                if (onTakePhoto != null && (noPhotoYet || appendOnAgreement)) {
                    SecondaryA11yButton(
                        text = if (appendOnAgreement) {
                            "Ajouter un courrier (photo)"
                        } else {
                            "Photo de la facture papier"
                        },
                        onClick = onTakePhoto
                    )
                }
                if (onPickPdf != null && (noPdfYet || appendOnAgreement ||
                        (hasReceiptPhoto && !receiptIsPdf && !hasPendingPdf))
                ) {
                    // PDF absent, photo seule (joindre à côté), ou accord (ajouter).
                    SecondaryA11yButton(text = "Joindre un PDF", onClick = onPickPdf)
                }
                if (hasPendingPhoto) {
                    val photoOkShape = MaterialTheme.shapes.medium
                    val photoOkText = if (isAgreement && hasReceiptPhoto) {
                        "Photo prête — s’ajoute aux courriers à l’enregistrement."
                    } else {
                        "Photo prête — Enregistrer pour la garder."
                    }
                    Text(
                        text = photoOkText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = A11yTokens.AccentOkInk,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                1.dp,
                                A11yTokens.AccentOkInk.copy(alpha = 0.35f),
                                photoOkShape
                            )
                            .background(A11yTokens.AccentOkSoft, photoOkShape)
                            .padding(10.dp)
                            .semantics {
                                liveRegion = LiveRegionMode.Polite
                                contentDescription = photoOkText
                            }
                    )
                    if (onTakePhoto != null) {
                        QuietLinkA11y(
                            text = "Reprendre la photo",
                            onClick = onTakePhoto
                        )
                    }
                }
                if (hasPendingPdf) {
                    val pdfOkShape = MaterialTheme.shapes.medium
                    val pdfOkText = if (hasReceiptPhoto) {
                        "PDF prêt — s’ajoute à côté de la photo à l’enregistrement."
                    } else {
                        "PDF prêt — Enregistrer pour le garder."
                    }
                    Text(
                        text = pdfOkText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = A11yTokens.AccentOkInk,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                1.dp,
                                A11yTokens.AccentOkInk.copy(alpha = 0.35f),
                                pdfOkShape
                            )
                            .background(A11yTokens.AccentOkSoft, pdfOkShape)
                            .padding(10.dp)
                            .semantics {
                                liveRegion = LiveRegionMode.Polite
                                contentDescription = pdfOkText
                            }
                    )
                    if (onPickPdf != null) {
                        QuietLinkA11y(text = "Changer le PDF", onClick = onPickPdf)
                    }
                    if (onClearPendingPdf != null) {
                        QuietLinkA11y(
                            text = "Enlever ce courrier",
                            onClick = onClearPendingPdf
                        )
                    }
                }
                // Bloc pièce existante (aperçu / voir / reprendre / enlever).
                if (
                    hasReceiptPhoto &&
                    receiptPreview == null &&
                    onViewReceipt != null &&
                    !hasPendingPhoto &&
                    !hasPendingPdf
                ) {
                    QuietLinkA11y(
                        text = if (receiptIsPdf) {
                            "Voir le courrier (PDF)"
                        } else {
                            "Voir la photo de la facture"
                        },
                        onClick = onViewReceipt
                    )
                }
                receiptPreview?.let { bmp ->
                    // FORM06 : miniature seule — contentDescription / onClickLabel suffisent.
                    Image(
                        bitmap = bmp.asImageBitmap(),
                        contentDescription = "Photo de la facture — touchez pour agrandir",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clip(MaterialTheme.shapes.medium)
                            .background(A11yTokens.PaperWarm)
                            .then(
                                if (onViewReceipt != null) {
                                    Modifier.clickable(
                                        role = Role.Button,
                                        onClickLabel = "Agrandir la facture",
                                        onClick = onViewReceipt
                                    )
                                } else {
                                    Modifier
                                }
                            )
                    )
                }
                if (hasReceiptPhoto && !hasPendingPhoto) {
                    if (onTakePhoto != null && !receiptIsPdf) {
                        QuietLinkA11y(
                            text = "Reprendre la photo de la facture",
                            onClick = onTakePhoto
                        )
                    }
                    if (onPickPdf != null && receiptIsPdf) {
                        QuietLinkA11y(
                            text = "Remplacer le PDF",
                            onClick = onPickPdf
                        )
                    }
                    if (onClearReceipt != null) {
                        QuietLinkA11y(
                            text = if (
                                !applyFollowing &&
                                !isSavingsLine &&
                                (isAgreement || hasTemplate)
                            ) {
                                if (receiptIsPdf) {
                                    "Enlever le PDF de ce mois"
                                } else {
                                    "Enlever la photo de ce mois"
                                }
                            } else if (receiptIsPdf) {
                                "Enlever le PDF"
                            } else {
                                "Enlever la photo"
                            },
                            onClick = onClearReceipt
                        )
                    }
                }
                if (extraReceiptPaths.isNotEmpty()) {
                    Text(
                        text = if (hasReceiptPhoto) "Autres courriers" else "Courriers",
                        style = MaterialTheme.typography.bodyLarge,
                        color = A11yTokens.InkMuted
                    )
                    extraReceiptPaths.forEachIndexed { index, path ->
                        val n = if (hasReceiptPhoto) index + 2 else index + 1
                        val pdf = isPdfPath(path)
                        @OptIn(ExperimentalLayoutApi::class)
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (onViewExtraReceipt != null) {
                                QuietLinkA11y(
                                    text = if (pdf) {
                                        "Voir le courrier $n (PDF)"
                                    } else {
                                        "Voir le courrier $n"
                                    },
                                    onClick = { onViewExtraReceipt(path) },
                                    fillMaxWidth = false
                                )
                            }
                            if (onRemoveExtraReceipt != null) {
                                QuietLinkA11y(
                                    text = "Enlever le courrier $n",
                                    onClick = { onRemoveExtraReceipt(path) },
                                    fillMaxWidth = false
                                )
                            }
                        }
                    }
                }
            }
        }
    } else if (showAutoDebit) {
        CheckRow(
            checked = isAutoDebit,
            label = "Prélèvement automatique",
            onChecked = onAutoDebit
        )
    }
}
