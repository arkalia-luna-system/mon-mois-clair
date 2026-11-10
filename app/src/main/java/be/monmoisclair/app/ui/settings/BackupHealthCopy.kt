package be.monmoisclair.app.ui.settings

import be.monmoisclair.app.domain.AppClock
import be.monmoisclair.app.domain.model.BackupHealth
import java.time.Instant

/** Libellés statut sauvegarde (Réglages) — testables hors Compose. */
object BackupHealthCopy {
    fun statusLine(health: BackupHealth, lastBackupAtEpochMs: Long? = null): String {
        val base = when (health) {
            BackupHealth.OK -> "La copie dans Documents est à jour."
            BackupHealth.BROKEN -> "La copie ne s’écrit plus. Choisissez à nouveau le dossier."
            BackupHealth.NOT_CONFIGURED -> "Aucun dossier Documents choisi."
        }
        if (health != BackupHealth.OK) return base
        val epoch = lastBackupAtEpochMs?.takeIf { it > 0L } ?: return base
        val z = Instant.ofEpochMilli(epoch).atZone(AppClock.zone)
        val date = "%02d/%02d/%02d".format(z.dayOfMonth, z.monthValue, z.year % 100)
        return "$base Dernière copie : $date."
    }

    fun isOk(health: BackupHealth): Boolean = health == BackupHealth.OK
}
