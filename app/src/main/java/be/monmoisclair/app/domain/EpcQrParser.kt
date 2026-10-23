package be.monmoisclair.app.domain

data class EpcPayment(
    val beneficiaryName: String,
    val iban: String,
    val amountCents: Long?,
    val remittance: String
)

/**
 * QR EPC / Girocode (Belgique) — texte brut non chiffré.
 *
 * Après IBAN + montant, Purpose (4 lettres ISO) est **optionnel** : beaucoup de QR
 * belges l’omettent. On cherche d’abord une communication structurée dans le reste
 * du payload, puis on retombe sur la ligne remittance (en sautant un Purpose vide / 4 lettres).
 */
object EpcQrParser {
    fun parse(raw: String): EpcPayment? {
        val lines = raw.replace("\r\n", "\n").replace('\r', '\n').lines().map { it.trimEnd() }
        if (lines.isEmpty() || !lines[0].equals("BCD", ignoreCase = true) || lines.size < 7) return null
        if (lines.getOrNull(3)?.uppercase() != "SCT") return null
        val name = lines.getOrNull(5)?.trim().orEmpty()
        val iban = lines.getOrNull(6)?.trim().orEmpty().replace(" ", "")
        if (name.isEmpty() || iban.isEmpty()) return null
        val amountCents = parseAmountCents(lines.getOrNull(7).orEmpty())
        val afterAmount = lines.drop(8).map { it.trim() }
        val remittance = resolveRemittance(afterAmount)
        return EpcPayment(name, iban, amountCents, remittance)
    }

    /**
     * 1) Ligne `+++…+++` / `***…***` / 12 chiffres mod 97 dans le payload.
     * 2) Sinon : sauter Purpose vide ou 4 lettres, prendre la 1ʳᵉ ligne non vide.
     */
    internal fun resolveRemittance(afterAmount: List<String>): String {
        for (line in afterAmount) {
            findStructuredRemittance(line)?.let { return it }
        }
        val withoutPurpose = afterAmount.dropWhile { isPurposeOrEmpty(it) }
        val free = withoutPurpose.firstOrNull { it.isNotEmpty() }.orEmpty()
        if (free.isNotEmpty()) return free
        // Compat fixtures historiques : index 9 puis 10 du payload complet.
        return afterAmount.getOrNull(1).orEmpty().ifEmpty {
            afterAmount.getOrNull(2).orEmpty()
        }
    }

    private fun isPurposeOrEmpty(line: String): Boolean {
        if (line.isEmpty()) return true
        // Purpose EPC = 4 lettres A–Z (ex. CHAR, GDDS) — pas une communication.
        return line.length in 1..4 && line.all { it.isLetter() }
    }

    private fun findStructuredRemittance(line: String): String? {
        if (line.isEmpty()) return null
        BelgianStructuredCommunication.findInOcrText(line)?.let { return it }
        val digits = line.filter { it.isDigit() }
        if (digits.length == 12 && BelgianStructuredCommunication.isValidMod97(digits)) {
            return BelgianStructuredCommunication.formatDisplay(digits) ?: line.trim()
        }
        // Forme déjà compacte +++…+++ sans passer par OCR context.
        if ((line.contains("+++") || line.contains("***")) && digits.length == 12 &&
            BelgianStructuredCommunication.isValidMod97(digits)
        ) {
            return BelgianStructuredCommunication.formatDisplay(digits) ?: line.trim()
        }
        return null
    }

    fun parseAmountCents(amountLine: String): Long? {
        val t = amountLine.trim()
        if (t.isEmpty() || !t.uppercase().startsWith("EUR")) return null
        var num = t.substring(3).trim().replace(" ", "")
        if (num.isEmpty()) return null
        if (num.contains(',') && !num.contains('.')) num = num.replace(',', '.')
        else if (num.contains(',') && num.contains('.')) num = num.replace(",", "")
        val value = num.toBigDecimalOrNull() ?: return null
        return value.movePointRight(2).toLong()
    }
}
