package be.monmoisclair.app.data.backup

import be.monmoisclair.app.data.db.entity.AppMetaEntity
import be.monmoisclair.app.data.db.entity.ExpenseInstanceEntity
import be.monmoisclair.app.data.db.entity.ExpenseTemplateEntity
import be.monmoisclair.app.data.db.entity.IncomeInstanceEntity
import be.monmoisclair.app.data.db.entity.IncomeTemplateEntity
import be.monmoisclair.app.data.db.entity.MonthLedgerEntity
import be.monmoisclair.app.domain.model.ExpenseStatus
import org.json.JSONArray
import org.json.JSONObject

/**
 * Codec JSON déterministe (clés triées) pour garantir un SHA-256 stable.
 */
class BackupJsonCodec {

    fun encodeDocument(document: BackupDocument): String {
        val root = JSONObject()
        putSorted(
            root,
            linkedMapOf(
                "schemaVersion" to document.schemaVersion,
                "exportedAtEpochMs" to document.exportedAtEpochMs,
                "zoneId" to document.zoneId,
                "checksumSha256" to document.checksumSha256,
                "payload" to payloadToJson(document.payload)
            )
        )
        return root.toString()
    }

    fun encodePayloadCanonical(payload: BackupPayload): String {
        return payloadToJson(payload).toString()
    }

    fun buildDocument(
        schemaVersion: Int,
        exportedAtEpochMs: Long,
        zoneId: String,
        payload: BackupPayload
    ): BackupDocument {
        val checksum = BackupChecksum.sha256Hex(encodePayloadCanonical(payload))
        return BackupDocument(
            schemaVersion = schemaVersion,
            exportedAtEpochMs = exportedAtEpochMs,
            zoneId = zoneId,
            checksumSha256 = checksum,
            payload = payload
        )
    }

    fun decodeAndVerify(json: String): BackupDocument {
        val root = JSONObject(json)
        val schemaVersion = root.getInt("schemaVersion")
        // v1 = historique ; v2 = diversSpent + isAutoDebit (CURRENT).
        if (schemaVersion != 1 && schemaVersion != 2) {
            throw BackupFailure.UnsupportedSchema(schemaVersion)
        }
        val payload = jsonToPayload(root.getJSONObject("payload"))
        // Recanonisation : org.json ne garantit pas l'ordre des clés après parse.
        val canonicalPayload = encodePayloadCanonical(payload)
        val expected = root.getString("checksumSha256")
        if (!BackupChecksum.matches(expected, canonicalPayload)) {
            throw BackupFailure.Integrity(
                "Checksum SHA-256 invalide (attendu=$expected, calculé=${BackupChecksum.sha256Hex(canonicalPayload)})"
            )
        }
        return BackupDocument(
            schemaVersion = schemaVersion,
            exportedAtEpochMs = root.getLong("exportedAtEpochMs"),
            zoneId = root.getString("zoneId"),
            checksumSha256 = expected,
            payload = payload
        )
    }

    private fun payloadToJson(payload: BackupPayload): JSONObject {
        val obj = JSONObject()
        putSorted(
            obj,
            linkedMapOf(
                "appMeta" to JSONArray(payload.appMeta.sortedBy { it.key }.map { metaToJson(it) }),
                "expenseInstances" to JSONArray(
                    payload.expenseInstances.sortedBy { it.id }.map { expenseInstanceToJson(it) }
                ),
                "expenseTemplates" to JSONArray(
                    payload.expenseTemplates.sortedBy { it.id }.map { expenseTemplateToJson(it) }
                ),
                "incomeInstances" to JSONArray(
                    payload.incomeInstances.sortedBy { it.id }.map { incomeInstanceToJson(it) }
                ),
                "incomeTemplates" to JSONArray(
                    payload.incomeTemplates.sortedBy { it.id }.map { incomeTemplateToJson(it) }
                ),
                "ledgers" to JSONArray(payload.ledgers.sortedBy { it.id }.map { ledgerToJson(it) })
            )
        )
        return obj
    }

    private fun jsonToPayload(obj: JSONObject): BackupPayload {
        return BackupPayload(
            ledgers = obj.getJSONArray("ledgers").mapObjects { ledgerFromJson(it) },
            incomeTemplates = obj.getJSONArray("incomeTemplates").mapObjects { incomeTemplateFromJson(it) },
            expenseTemplates = obj.getJSONArray("expenseTemplates").mapObjects { expenseTemplateFromJson(it) },
            incomeInstances = obj.getJSONArray("incomeInstances").mapObjects { incomeInstanceFromJson(it) },
            expenseInstances = obj.getJSONArray("expenseInstances").mapObjects { expenseInstanceFromJson(it) },
            appMeta = obj.getJSONArray("appMeta").mapObjects { metaFromJson(it) }
        )
    }

    private fun ledgerToJson(e: MonthLedgerEntity): JSONObject =
        sortedJson(
            "createdAtEpochMs" to e.createdAtEpochMs,
            "diversSpentCents" to e.diversSpentCents,
            "id" to e.id,
            "yearMonth" to e.yearMonth
        )

    private fun ledgerFromJson(o: JSONObject) = MonthLedgerEntity(
        id = o.getString("id"),
        yearMonth = o.getString("yearMonth"),
        diversSpentCents = o.optLong("diversSpentCents", 0L),
        createdAtEpochMs = o.getLong("createdAtEpochMs")
    )

    private fun incomeTemplateToJson(e: IncomeTemplateEntity): JSONObject =
        sortedJson(
            "active" to e.active,
            "amountCents" to e.amountCents,
            "archivedAtEpochMs" to e.archivedAtEpochMs,
            "createdAtEpochMs" to e.createdAtEpochMs,
            "dayOfMonth" to e.dayOfMonth,
            "id" to e.id,
            "label" to e.label
        )

    private fun incomeTemplateFromJson(o: JSONObject) = IncomeTemplateEntity(
        id = o.getString("id"),
        label = o.getString("label"),
        amountCents = o.getLong("amountCents"),
        dayOfMonth = o.getInt("dayOfMonth"),
        active = o.getBoolean("active"),
        createdAtEpochMs = o.getLong("createdAtEpochMs"),
        archivedAtEpochMs = o.nullableLong("archivedAtEpochMs")
    )

    private fun expenseTemplateToJson(e: ExpenseTemplateEntity): JSONObject =
        sortedJson(
            "active" to e.active,
            "amountCents" to e.amountCents,
            "anchorYearMonth" to e.anchorYearMonth,
            "archivedAtEpochMs" to e.archivedAtEpochMs,
            "createdAtEpochMs" to e.createdAtEpochMs,
            "dayOfMonth" to e.dayOfMonth,
            "id" to e.id,
            "intervalMonths" to e.intervalMonths,
            "isAutoDebit" to e.isAutoDebit,
            "label" to e.label,
            "remindJ3" to e.remindJ3,
            "remindJd" to e.remindJd
        )

    private fun expenseTemplateFromJson(o: JSONObject) = ExpenseTemplateEntity(
        id = o.getString("id"),
        label = o.getString("label"),
        amountCents = o.getLong("amountCents"),
        dayOfMonth = o.getInt("dayOfMonth"),
        active = o.getBoolean("active"),
        remindJ3 = o.getBoolean("remindJ3"),
        remindJd = o.getBoolean("remindJd"),
        isAutoDebit = o.optBoolean("isAutoDebit", false),
        intervalMonths = o.optInt("intervalMonths", 1).coerceAtLeast(1),
        anchorYearMonth = o.optString("anchorYearMonth", ""),
        createdAtEpochMs = o.getLong("createdAtEpochMs"),
        archivedAtEpochMs = o.nullableLong("archivedAtEpochMs")
    )

    private fun incomeInstanceToJson(e: IncomeInstanceEntity): JSONObject =
        sortedJson(
            "amountCents" to e.amountCents,
            "createdAtEpochMs" to e.createdAtEpochMs,
            "dayOfMonth" to e.dayOfMonth,
            "id" to e.id,
            "label" to e.label,
            "ledgerId" to e.ledgerId,
            "templateId" to e.templateId
        )

    private fun incomeInstanceFromJson(o: JSONObject) = IncomeInstanceEntity(
        id = o.getString("id"),
        ledgerId = o.getString("ledgerId"),
        templateId = o.nullableString("templateId"),
        label = o.getString("label"),
        amountCents = o.getLong("amountCents"),
        dayOfMonth = o.getInt("dayOfMonth"),
        createdAtEpochMs = o.getLong("createdAtEpochMs")
    )

    private fun expenseInstanceToJson(e: ExpenseInstanceEntity): JSONObject =
        sortedJson(
            "alarmJ3RequestCode" to e.alarmJ3RequestCode,
            "alarmJdRequestCode" to e.alarmJdRequestCode,
            "amountCents" to e.amountCents,
            "createdAtEpochMs" to e.createdAtEpochMs,
            "dayOfMonth" to e.dayOfMonth,
            "doneAtEpochMs" to e.doneAtEpochMs,
            "id" to e.id,
            "isAutoDebit" to e.isAutoDebit,
            "label" to e.label,
            "ledgerId" to e.ledgerId,
            "remindJ3" to e.remindJ3,
            "remindJd" to e.remindJd,
            "status" to e.status.name,
            "templateId" to e.templateId
        )

    private fun expenseInstanceFromJson(o: JSONObject) = ExpenseInstanceEntity(
        id = o.getString("id"),
        ledgerId = o.getString("ledgerId"),
        templateId = o.nullableString("templateId"),
        label = o.getString("label"),
        amountCents = o.getLong("amountCents"),
        dayOfMonth = o.getInt("dayOfMonth"),
        status = ExpenseStatus.valueOf(o.getString("status")),
        doneAtEpochMs = o.nullableLong("doneAtEpochMs"),
        remindJ3 = o.getBoolean("remindJ3"),
        remindJd = o.getBoolean("remindJd"),
        isAutoDebit = o.optBoolean("isAutoDebit", false),
        alarmJ3RequestCode = o.nullableInt("alarmJ3RequestCode"),
        alarmJdRequestCode = o.nullableInt("alarmJdRequestCode"),
        createdAtEpochMs = o.getLong("createdAtEpochMs")
    )

    private fun metaToJson(e: AppMetaEntity): JSONObject =
        sortedJson("key" to e.key, "value" to e.value)

    private fun metaFromJson(o: JSONObject) = AppMetaEntity(
        key = o.getString("key"),
        value = o.getString("value")
    )

    private fun sortedJson(vararg pairs: Pair<String, Any?>): JSONObject {
        val map = linkedMapOf<String, Any?>()
        pairs.sortedBy { it.first }.forEach { (k, v) -> map[k] = v }
        val obj = JSONObject()
        putSorted(obj, map)
        return obj
    }

    private fun putSorted(target: JSONObject, values: Map<String, Any?>) {
        values.keys.sorted().forEach { key ->
            when (val value = values[key]) {
                null -> target.put(key, JSONObject.NULL)
                else -> target.put(key, value)
            }
        }
    }

    private fun JSONObject.nullableLong(key: String): Long? =
        if (isNull(key)) null else getLong(key)

    private fun JSONObject.nullableInt(key: String): Int? =
        if (isNull(key)) null else getInt(key)

    private fun JSONObject.nullableString(key: String): String? =
        if (isNull(key)) null else getString(key)

    private fun <T> JSONArray.mapObjects(transform: (JSONObject) -> T): List<T> {
        val out = ArrayList<T>(length())
        for (i in 0 until length()) {
            out.add(transform(getJSONObject(i)))
        }
        return out
    }
}
