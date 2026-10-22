package be.monmoisclair.app.data.summary

import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Même texte que [be.monmoisclair.app.domain.usecase.PrepareMonthSummaryUseCase],
 * en feuille A4 pour imprimer ou envoyer. Pas un formulaire CPAS.
 */
@Singleton
class MonthSummaryPdfWriter @Inject constructor() {

    fun write(text: String, outFile: File): File {
        outFile.parentFile?.mkdirs()
        if (outFile.exists()) outFile.delete()
        val doc = PdfDocument()
        try {
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFF1E1B18.toInt()
                textSize = BODY_SP
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            }
            val titlePaint = Paint(paint).apply {
                textSize = TITLE_SP
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            }
            var pageIndex = 1
            var page = doc.startPage(pageInfo(pageIndex))
            var canvas = page.canvas
            var y = MARGIN_TOP
            val lines = text.replace("\r\n", "\n").lines()
            for ((i, raw) in lines.withIndex()) {
                val linePaint = if (i == 0) titlePaint else paint
                val wrapped = wrapForWidth(
                    raw.ifEmpty { " " },
                    CONTENT_WIDTH
                ) { s -> linePaint.measureText(s) }
                for (chunk in wrapped) {
                    if (y + linePaint.textSize > PAGE_HEIGHT - MARGIN_BOTTOM) {
                        doc.finishPage(page)
                        pageIndex++
                        page = doc.startPage(pageInfo(pageIndex))
                        canvas = page.canvas
                        y = MARGIN_TOP
                    }
                    canvas.drawText(chunk, MARGIN_LEFT, y, linePaint)
                    y += linePaint.textSize + LINE_GAP
                }
            }
            doc.finishPage(page)
            FileOutputStream(outFile).use { doc.writeTo(it) }
        } finally {
            doc.close()
        }
        return outFile
    }

    private fun pageInfo(index: Int): PdfDocument.PageInfo =
        PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, index).create()

    companion object {
        /** A4 en points PDF (~72 dpi). */
        const val PAGE_WIDTH = 595
        const val PAGE_HEIGHT = 842
        private const val MARGIN_LEFT = 48f
        private const val MARGIN_TOP = 56f
        private const val MARGIN_BOTTOM = 48f
        private val CONTENT_WIDTH = PAGE_WIDTH - MARGIN_LEFT - 48f
        private const val BODY_SP = 11f
        private const val TITLE_SP = 14f
        private const val LINE_GAP = 4f

        const val FILE_NAME = "mon-mois-clair-resume.pdf"

        /**
         * Découpe une ligne pour une largeur max.
         * [measure] = largeur d’une chaîne (Paint.measureText en prod).
         */
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
