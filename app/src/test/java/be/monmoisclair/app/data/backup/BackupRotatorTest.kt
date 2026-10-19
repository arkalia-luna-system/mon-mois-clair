package be.monmoisclair.app.data.backup

import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class BackupRotatorTest {

    @get:Rule
    val tmp: TemporaryFolder = TemporaryFolder()

    private val rotator = BackupRotator()
    private val codec = BackupJsonCodec()

    private fun tree(): FileSystemBackupTree = FileSystemBackupTree(tmp.root)

    private fun content(marker: String): String {
        val payload = BackupPayload(
            ledgers = emptyList(),
            incomeTemplates = emptyList(),
            expenseTemplates = emptyList(),
            incomeInstances = emptyList(),
            expenseInstances = emptyList(),
            appMeta = listOf(
                be.monmoisclair.app.data.db.entity.AppMetaEntity("marker", marker)
            )
        )
        val doc = codec.buildDocument(1, 1L, "Europe/Brussels", payload)
        return codec.encodeDocument(doc)
    }

    private fun markerOf(json: String): String =
        codec.decodeAndVerify(json).payload.appMeta.single { it.key == "marker" }.value

    @Test
    fun firstWrite_createsOnlyV1() {
        val tree = tree()
        rotator.writeRotated(tree, content("A"))
        assertThat(tree.exists(BackupFileNames.V1)).isTrue()
        assertThat(tree.exists(BackupFileNames.V2)).isFalse()
        assertThat(tree.exists(BackupFileNames.V3)).isFalse()
        assertThat(tree.exists(BackupFileNames.TMP)).isFalse()
        assertThat(markerOf(tree.readUtf8(BackupFileNames.V1))).isEqualTo("A")
    }

    @Test
    fun threeWrites_rotateNewestInV1_oldestInV3() {
        val tree = tree()
        rotator.writeRotated(tree, content("A"))
        rotator.writeRotated(tree, content("B"))
        rotator.writeRotated(tree, content("C"))

        assertThat(markerOf(tree.readUtf8(BackupFileNames.V1))).isEqualTo("C")
        assertThat(markerOf(tree.readUtf8(BackupFileNames.V2))).isEqualTo("B")
        assertThat(markerOf(tree.readUtf8(BackupFileNames.V3))).isEqualTo("A")
    }

    @Test
    fun fourthWrite_dropsOldest_keepsThree() {
        val tree = tree()
        rotator.writeRotated(tree, content("A"))
        rotator.writeRotated(tree, content("B"))
        rotator.writeRotated(tree, content("C"))
        rotator.writeRotated(tree, content("D"))

        assertThat(markerOf(tree.readUtf8(BackupFileNames.V1))).isEqualTo("D")
        assertThat(markerOf(tree.readUtf8(BackupFileNames.V2))).isEqualTo("C")
        assertThat(markerOf(tree.readUtf8(BackupFileNames.V3))).isEqualTo("B")
        val markers = listOf(BackupFileNames.V1, BackupFileNames.V2, BackupFileNames.V3)
            .map { markerOf(tree.readUtf8(it)) }
        assertThat(markers).doesNotContain("A")
    }

    @Test
    fun overwriteExistingTargets_doesNotDuplicateFiles() {
        val tree = tree()
        rotator.writeRotated(tree, content("A"))
        rotator.writeRotated(tree, content("B"))
        // Simule un fichier 3 orphelin / collision de nom
        tree.writeUtf8Sync(BackupFileNames.V3, content("OLD3"))
        rotator.writeRotated(tree, content("C"))

        val names = tmp.root.list()?.sorted().orEmpty()
        assertThat(names).containsExactly(
            BackupFileNames.V1,
            BackupFileNames.V2,
            BackupFileNames.V3
        )
        assertThat(markerOf(tree.readUtf8(BackupFileNames.V1))).isEqualTo("C")
    }

    @Test
    fun afterFailedPartialTmp_previousV1_stillReadable() {
        val tree = tree()
        rotator.writeRotated(tree, content("SAFE"))
        // Écriture tmp corrompue sans rotation complète
        tree.writeUtf8Sync(BackupFileNames.TMP, "{not-json")
        assertThat(tree.exists(BackupFileNames.V1)).isTrue()
        val recovered = codec.decodeAndVerify(tree.readUtf8(BackupFileNames.V1))
        assertThat(recovered.payload.appMeta.single().value).isEqualTo("SAFE")
    }
}
