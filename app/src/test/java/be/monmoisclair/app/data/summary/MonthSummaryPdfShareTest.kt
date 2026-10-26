package be.monmoisclair.app.data.summary

import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class MonthSummaryPdfShareTest {

    @get:Rule
    val tmp = TemporaryFolder()

    @Test
    fun prepareTarget_usesShareSubdir_andClearsPrior() {
        val cache = tmp.newFolder("cache")
        val prior = MonthSummaryPdfShare.targetFile(cache)
        prior.parentFile!!.mkdirs()
        prior.writeText("old")
        assertThat(prior.exists()).isTrue()

        val out = MonthSummaryPdfShare.prepareTarget(cache)
        assertThat(out.name).isEqualTo(MonthSummaryPdfWriter.FILE_NAME)
        assertThat(out.parentFile!!.name).isEqualTo(MonthSummaryPdfShare.SHARE_SUBDIR)
        assertThat(out.exists()).isFalse()
        assertThat(out.absolutePath).startsWith(cache.absolutePath)
    }

    @Test
    fun wipeSharePdfs_removesOnlyPdfs() {
        val cache = tmp.newFolder("cache")
        val dir = MonthSummaryPdfShare.shareDir(cache).also { it.mkdirs() }
        File(dir, "mon-mois-clair-resume.pdf").writeText("pdf")
        File(dir, "backup.mmc").writeText("mmc")
        File(dir, "note.txt").writeText("txt")

        val wiped = MonthSummaryPdfShare.wipeSharePdfs(cache)
        assertThat(wiped).isEqualTo(1)
        assertThat(File(dir, "mon-mois-clair-resume.pdf").exists()).isFalse()
        assertThat(File(dir, "backup.mmc").exists()).isTrue()
        assertThat(File(dir, "note.txt").exists()).isTrue()
    }

    @Test
    fun purgeStale_keepsFreshPdf() {
        val cache = tmp.newFolder("cache")
        val dir = MonthSummaryPdfShare.shareDir(cache).also { it.mkdirs() }
        val fresh = File(dir, "fresh.pdf").also { it.writeText("x") }
        val stale = File(dir, "stale.pdf").also { it.writeText("y") }
        val now = 1_700_000_000_000L
        fresh.setLastModified(now)
        stale.setLastModified(now - MonthSummaryPdfShare.SHARE_TTL_MS - 1)

        val n = MonthSummaryPdfShare.purgeStale(dir, nowEpochMs = now)
        assertThat(n).isEqualTo(1)
        assertThat(fresh.exists()).isTrue()
        assertThat(stale.exists()).isFalse()
    }

    @Test
    fun fileName_matchesWriter() {
        assertThat(MonthSummaryPdfShare.FILE_NAME).isEqualTo(MonthSummaryPdfWriter.FILE_NAME)
    }
}
