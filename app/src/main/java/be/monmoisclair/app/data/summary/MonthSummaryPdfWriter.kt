package be.monmoisclair.app.data.summary

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.res.ResourcesCompat
import be.monmoisclair.app.R
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Même texte que [be.monmoisclair.app.domain.usecase.PrepareMonthSummaryUseCase],
 * en feuille A4 à gros caractères (charte 18 / 22 pt). Pas un formulaire CPAS.
 *
 * Les titres de section (Revenus, mois, Déficit…) sont en semibold ;
 * le corps n’est pas entièrement à 18 pt (pied 14 pt, titre doc 22 pt).
 *
 * **Honnêteté sécu :** PDF **clair** (MMC1/MRF1 le rendrait illisible hors app) —
 * écrit uniquement sous `cache/share/` via [MonthSummaryPdfShare] (éphémère,
 * wipe au verrou + TTL) ; watermark [SENSITIVE_MARK] ; pas une copie `.mmc`.
 */
@Singleton
class MonthSummaryPdfWriter @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun write(text: String, outFile: File): File {
        outFile.parentFile?.mkdirs()
        if (outFile.exists()) outFile.delete()

        val bodyFace = fontOr(R.font.source_sans3_regular, Typeface.SANS_SERIF)
        val sectionFace = fontOr(R.font.source_sans3_semibold, Typeface.DEFAULT_BOLD)
        val titleFace = fontOr(R.font.source_serif4_semibold, Typeface.SERIF)

        val paperPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = PAPER
        }
        val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = INK
            textSize = BODY_PT
            typeface = bodyFace
        }
        val sectionPaint = Paint(bodyPaint).apply {
            textSize = BODY_PT
            typeface = sectionFace
        }
        val titlePaint = Paint(bodyPaint).apply {
            textSize = TITLE_PT
            typeface = titleFace
        }
        val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = MUTED
            textSize = FOOTER_PT
            typeface = bodyFace
        }

        val doc = PdfDocument()
        try {
            var pageIndex = 1
            var page = doc.startPage(pageInfo(pageIndex))
            var canvas = page.canvas
            drawPaper(canvas, paperPaint)
            var y = MARGIN_TOP

            fun newPage() {
                drawFooter(canvas, footerPaint, pageIndex)
                doc.finishPage(page)
                pageIndex++
                page = doc.startPage(pageInfo(pageIndex))
                canvas = page.canvas
                drawPaper(canvas, paperPaint)
                y = MARGIN_TOP
            }

            fun ensureRoom(needed: Float) {
                if (y + needed > PAGE_HEIGHT - MARGIN_BOTTOM) {
                    newPage()
                }
            }

            val lines = text.replace("\r\n", "\n").lines()
            for ((i, raw) in lines.withIndex()) {
                if (raw.isEmpty()) {
                    ensureRoom(BODY_PT * EMPTY_GAP_FACTOR)
                    y += BODY_PT * EMPTY_GAP_FACTOR
                    continue
                }
                val linePaint = when {
                    i == 0 -> titlePaint
                    isSectionHeading(raw) -> sectionPaint
                    else -> bodyPaint
                }
                val wrapped = wrapForWidth(raw, CONTENT_WIDTH) { s -> linePaint.measureText(s) }
                for (chunk in wrapped) {
                    ensureRoom(linePaint.textSize)
                    canvas.drawText(chunk, MARGIN_LEFT, y + linePaint.textSize, linePaint)
                    y += linePaint.textSize + LINE_GAP
                }
            }
            drawFooter(canvas, footerPaint, pageIndex)
            doc.finishPage(page)
            FileOutputStream(outFile).use { doc.writeTo(it) }
        } finally {
            doc.close()
        }
        return outFile
    }

    private fun fontOr(resId: Int, fallback: Typeface): Typeface =
        ResourcesCompat.getFont(context, resId) ?: fallback

    private fun pageInfo(index: Int): PdfDocument.PageInfo =
        PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, index).create()

    private fun drawPaper(canvas: Canvas, paint: Paint) {
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), PAGE_HEIGHT.toFloat(), paint)
    }

    private fun drawFooter(canvas: Canvas, paint: Paint, pageIndex: Int) {
        val baseline = PAGE_HEIGHT - FOOTER_FROM_BOTTOM
        canvas.drawText("Mon Mois Clair — $SENSITIVE_MARK", MARGIN_LEFT, baseline, paint)
        val pageLabel = pageIndex.toString()
        val x = PAGE_WIDTH - MARGIN_RIGHT - paint.measureText(pageLabel)
        canvas.drawText(pageLabel, x, baseline, paint)
    }

    companion object {
        /** A4 en points PDF (~72 dpi). */
        const val PAGE_WIDTH = 595
        const val PAGE_HEIGHT = 842

        /** Corps = charte a11y [be.monmoisclair.app.ui.theme.A11yTokens.minBodySp]. */
        const val BODY_PT = 18f

        /** Titre document = charte [be.monmoisclair.app.ui.theme.A11yTokens.minTitleSp]. */
        const val TITLE_PT = 22f

        const val FOOTER_PT = 14f
        const val LINE_GAP = 6f
        const val EMPTY_GAP_FACTOR = 0.6f

        private const val MARGIN_LEFT = 56f
        private const val MARGIN_RIGHT = 56f
        private const val MARGIN_TOP = 56f
        /** Contenu au-dessus du pied (pied à 36 pt du bas). */
        private const val MARGIN_BOTTOM = 64f
        private const val FOOTER_FROM_BOTTOM = 36f

        private val CONTENT_WIDTH = PAGE_WIDTH - MARGIN_LEFT - MARGIN_RIGHT

        private const val PAPER = 0xFFF6F3E8.toInt()
        private const val INK = 0xFF1E1B18.toInt()
        private const val MUTED = 0xFF4A453C.toInt()

        const val FILE_NAME = "mon-mois-clair-resume.pdf"

        /** Filigrane pied de page — PDF résumé = clair éphémère, pas une copie .mmc. */
        const val SENSITIVE_MARK = "données sensibles"

        private val MONTH_TITLE =
            Regex("""^[A-Za-zÀ-ÿ][A-Za-zÀ-ÿ'’\- ]+ \d{4}$""")

        /**
         * Rubriques et titre de mois en semibold.
         * Ignore lignes vides, puces et suites indentées (versements).
         */
        fun isSectionHeading(line: String): Boolean {
            if (line.isEmpty()) return false
            if (line.startsWith(" ") || line.startsWith("\t")) return false
            val t = line.trim()
            if (t.isEmpty()) return false
            if (t.startsWith("•") || t.startsWith("—") || t.startsWith("(")) return false
            return when {
                t == "Revenus" -> true
                t == "Factures du mois" -> true
                t == "Dossiers (accords)" -> true
                t == "Mis de côté" -> true
                t.startsWith("Déficit ce mois") -> true
                MONTH_TITLE.matches(t) -> true
                else -> false
            }
        }

        fun wrapForWidth(
            line: String,
            maxWidth: Float,
            measure: (String) -> Float
        ): List<String> {
            if (measure(line) <= maxWidth) return listOf(line)
            val words = line.split(' ')
            if (words.size == 1) return hardWrap(line, maxWidth, measure)
            val out = mutableListOf<String>()
            var current = StringBuilder()
            for (word in words) {
                val candidate = if (current.isEmpty()) word else "$current $word"
                if (measure(candidate) <= maxWidth) {
                    current = StringBuilder(candidate)
                } else {
                    if (current.isNotEmpty()) out.add(current.toString())
                    if (measure(word) <= maxWidth) {
                        current = StringBuilder(word)
                    } else {
                        out.addAll(hardWrap(word, maxWidth, measure))
                        current = StringBuilder()
                    }
                }
            }
            if (current.isNotEmpty()) out.add(current.toString())
            return out.ifEmpty { listOf(" ") }
        }

        private fun hardWrap(
            s: String,
            maxWidth: Float,
            measure: (String) -> Float
        ): List<String> {
            val out = mutableListOf<String>()
            var start = 0
            while (start < s.length) {
                var end = start + 1
                while (end <= s.length && measure(s.substring(start, end)) <= maxWidth) {
                    end++
                }
                end = (end - 1).coerceAtLeast(start + 1)
                out.add(s.substring(start, end.coerceAtMost(s.length)))
                start = end
            }
            return out
        }
    }
}
