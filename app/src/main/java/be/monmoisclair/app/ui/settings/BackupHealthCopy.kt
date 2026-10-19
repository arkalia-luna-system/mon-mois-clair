package be.monmoisclair.app.ui.settings

import be.monmoisclair.app.domain.model.BackupHealth

/** Libellés statut sauvegarde (Réglages) — testables hors Compose. */
object BackupHealthCopy {
    fun statusLine(health: BackupHealth): String = when (health) {
        BackupHealth.OK -> "État : sauvegarde OK."
        BackupHealth.BROKEN -> "État : sauvegarde à régler (écriture impossible)."
        BackupHealth.NOT_CONFIGURED -> "État : aucun dossier Documents choisi."
    }

    fun isOk(health: BackupHealth): Boolean = health == BackupHealth.OK
}
