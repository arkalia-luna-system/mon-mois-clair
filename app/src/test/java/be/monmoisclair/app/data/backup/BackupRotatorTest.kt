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

    @Test
    fun firstWrite_createsOnlyV1() {
        val tree = tree()
        rotator.writeRotated(tree, content("A"))
        assertThat(tree.exists(BackupFileNames.V1)).isTrue()
        assertThat(tree.exists(BackupFileNames.V2)).isFalse()
        assertThat(tree.exists(BackupFileNames.V3)).isFalse()
        assertThat(tree.exists(BackupFileNames.TMP)).isFalse()
        assertThat(tree.readUtf8(BackupFileNames.V1)).contains("\"marker\":\"A\"")
    }

    @Test
    fun threeWrites_rotateNewestInV1_oldestInV3() {
        val tree = tree()
        rotator.writeRotated(tree, content("A"))
        rotator.writeRotated(tree, content("B"))
        rotator.writeRotated(tree, content("C"))

        assertThat(tree.readUtf8(BackupFileNames.V1)).contains("\"marker\":\"C\"")
        assertThat(tree.readUtf8(BackupFileNames.V2)).contains("\"marker\":\"B\"")
        assertThat(tree.readUtf8(BackupFileNames.V3)).contains("\"marker\":\"A\"")
    }

    @Test
    fun fourthWrite_dropsOldest_keepsThree() {
        val tree = tree()
        rotator.writeRotated(tree, content("A"))
        rotator.writeRotated(tree, content("B"))
        rotator.writeRotated(tree, content("C"))
        rotator.writeRotated(tree, content("D"))

        assertThat(tree.readUtf8(BackupFileNames.V1)).contains("\"marker\":\"D\"")
        assertThat(tree.readUtf8(BackupFileNames.V2)).contains("\"marker\":\"C\"")
        assertThat(tree.readUtf8(BackupFileNames.V3)).contains("\"marker\":\"B\"")
        // A a disparu
        val all = listOf(BackupFileNames.V1, BackupFileNames.V2, BackupFileNames.V3)
            .joinToString { tree.readUtf8(it) }
        assertThat(all).doesNotContain("\"marker\":\"A\"")
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
        assertThat(tree.readUtf8(BackupFileNames.V1)).contains("\"marker\":\"C\"")
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
