package be.monmoisclair.app.domain

data class EpcPayment(
    val beneficiaryName: String,
    val iban: String,
    val amountCents: Long?,
    val remittance: String
)

/** QR EPC / Girocode (Belgique) — texte brut non chiffré. */
object EpcQrParser {
    fun parse(raw: String): EpcPayment? {
        val lines = raw.replace("\r\n", "\n").replace('\r', '\n').lines().map { it.trimEnd() }
        if (lines.isEmpty() || !lines[0].equals("BCD", ignoreCase = true) || lines.size < 7) return null
        if (lines.getOrNull(3)?.uppercase() != "SCT") return null
        val name = lines.getOrNull(5)?.trim().orEmpty()
        val iban = lines.getOrNull(6)?.trim().orEmpty().replace(" ", "")
        if (name.isEmpty() || iban.isEmpty()) return null
        val amountCents = parseAmountCents(lines.getOrNull(7).orEmpty())
        val remittance = lines.getOrNull(9)?.trim().orEmpty()
            .ifEmpty { lines.getOrNull(10)?.trim().orEmpty() }
        return EpcPayment(name, iban, amountCents, remittance)
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
