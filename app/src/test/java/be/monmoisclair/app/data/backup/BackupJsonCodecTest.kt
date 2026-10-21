package be.monmoisclair.app.data.backup

import be.monmoisclair.app.data.db.entity.AppMetaEntity
import be.monmoisclair.app.data.db.entity.ExpenseInstanceEntity
import be.monmoisclair.app.data.db.entity.ExpenseTemplateEntity
import be.monmoisclair.app.data.db.entity.MonthLedgerEntity
import be.monmoisclair.app.domain.model.AppMetaKeys
import be.monmoisclair.app.domain.model.ExpenseStatus
import com.google.common.truth.Truth.assertThat
import org.junit.Assert.assertThrows
import org.junit.Test

class BackupJsonCodecTest {

    private val codec = BackupJsonCodec()

    private fun samplePayload(): BackupPayload = BackupPayload(
        ledgers = listOf(
            MonthLedgerEntity(id = "2026-02", yearMonth = "2026-02", createdAtEpochMs = 10L)
        ),
        incomeTemplates = emptyList(),
        expenseTemplates = emptyList(),
        incomeInstances = emptyList(),
        expenseInstances = listOf(
            ExpenseInstanceEntity(
                id = "exp-1",
                ledgerId = "2026-02",
                templateId = "tpl",
                label = "Loyer",
                amountCents = 47_000L,
                dayOfMonth = 28,
                status = ExpenseStatus.TODO,
                doneAtEpochMs = null,
                remindJ3 = true,
                remindJd = true,
                alarmJ3RequestCode = null,
                alarmJdRequestCode = null,
                createdAtEpochMs = 11L
            )
        ),
        appMeta = listOf(AppMetaEntity("schemaVersion", "1"))
    )

    @Test
    fun build_encode_decode_roundTrip_preservesDataAndChecksum() {
        val doc = codec.buildDocument(
            schemaVersion = 1,
            exportedAtEpochMs = 1_700_000_000_000L,
            zoneId = "Europe/Brussels",
            payload = samplePayload()
        )
        val json = codec.encodeDocument(doc)
        val decoded = codec.decodeAndVerify(json)

        assertThat(decoded.checksumSha256).isEqualTo(doc.checksumSha256)
        assertThat(decoded.payload.expenseInstances).hasSize(1)
        assertThat(decoded.payload.expenseInstances[0].amountCents).isEqualTo(47_000L)
        assertThat(decoded.payload.expenseInstances[0].status).isEqualTo(ExpenseStatus.TODO)
        assertThat(decoded.zoneId).isEqualTo("Europe/Brussels")
    }

    /** T5 : clés littérales (R8 ne doit pas les obfusquer — codec manuel). */
    @Test
    fun encode_uses_human_readable_field_names() {
        val doc = codec.buildDocument(2, 1L, "Europe/Brussels", samplePayload())
        val json = codec.encodeDocument(doc)
        assertThat(json).contains("\"label\"")
        assertThat(json).contains("\"amountCents\"")
        assertThat(json).contains("\"status\"")
        assertThat(json).contains("\"dayOfMonth\"")
        assertThat(json).contains("\"diversSpentCents\"")
        assertThat(json).contains("\"paymentNote\"")
        assertThat(json).contains("\"receiptRelativePath\"")
        assertThat(json).contains("\"schemaVersion\"")
        assertThat(json).doesNotContain("\"a\":")
    }

    @Test
    fun decode_rejectsTamperedPayload() {
        val doc = codec.buildDocument(1, 1L, "Europe/Brussels", samplePayload())
        val json = codec.encodeDocument(doc).replace("47000", "4700")
        assertThrows(BackupFailure.Integrity::class.java) {
            codec.decodeAndVerify(json)
        }
    }

    @Test
    fun decode_rejectsUnsupportedSchema() {
        val doc = codec.buildDocument(1, 1L, "Europe/Brussels", samplePayload())
        val json = codec.encodeDocument(doc).replace("\"schemaVersion\":1", "\"schemaVersion\":99")
        assertThrows(BackupFailure.UnsupportedSchema::class.java) {
            codec.decodeAndVerify(json)
        }
    }

    @Test
    fun expenseTemplate_openingRemainingCents_roundTrip() {
        val tpl = ExpenseTemplateEntity(
            id = "tpl-acc",
            label = "Étude",
            amountCents = 5_000L,
            dayOfMonth = 12,
            active = true,
            causeLabel = "Hôpital",
            openingRemainingCents = 60_000L,
            createdAtEpochMs = 1L,
            archivedAtEpochMs = null
        )
        val payload = samplePayload().copy(expenseTemplates = listOf(tpl))
        val schema = AppMetaKeys.CURRENT_SCHEMA_VERSION.toInt()
        val doc = codec.buildDocument(schema, 1L, "Europe/Brussels", payload)
        val decoded = codec.decodeAndVerify(codec.encodeDocument(doc))
        val out = decoded.payload.expenseTemplates.single()
        assertThat(out.openingRemainingCents).isEqualTo(60_000L)
        assertThat(out.causeLabel).isEqualTo("Hôpital")
        assertThat(codec.encodeDocument(doc)).contains("\"openingRemainingCents\":60000")
    }

    @Test
    fun expenseTemplate_nullOpening_roundTrip_staysNull() {
        val tpl = ExpenseTemplateEntity(
            id = "tpl-plain",
            label = "Loyer",
            amountCents = 47_000L,
            dayOfMonth = 5,
            active = true,
            openingRemainingCents = null,
            createdAtEpochMs = 1L,
            archivedAtEpochMs = null
        )
        val payload = samplePayload().copy(expenseTemplates = listOf(tpl))
        val schema = AppMetaKeys.CURRENT_SCHEMA_VERSION.toInt()
        val doc = codec.buildDocument(schema, 1L, "Europe/Brussels", payload)
        val decoded = codec.decodeAndVerify(codec.encodeDocument(doc))
        assertThat(decoded.payload.expenseTemplates.single().openingRemainingCents).isNull()
    }

    @Test
    fun canonicalPayload_isStableAcrossBuildOrder() {
        val p1 = samplePayload()
        val p2 = samplePayload().copy(
            appMeta = samplePayload().appMeta.reversed(),
            expenseInstances = samplePayload().expenseInstances.reversed()
        )
        assertThat(codec.encodePayloadCanonical(p1)).isEqualTo(codec.encodePayloadCanonical(p2))
    }
}
