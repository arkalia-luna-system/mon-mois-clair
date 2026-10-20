package be.monmoisclair.app.ui.settings

import be.monmoisclair.app.domain.model.BackupHealth

/** Libellés statut sauvegarde (Réglages) — testables hors Compose. */
object BackupHealthCopy {
    fun statusLine(health: BackupHealth): String = when (health) {
        BackupHealth.OK -> "La copie dans Documents est à jour."
        BackupHealth.BROKEN -> "La copie ne s’écrit plus. Choisissez à nouveau le dossier."
        BackupHealth.NOT_CONFIGURED -> "Aucun dossier Documents choisi."
    }

    fun isOk(health: BackupHealth): Boolean = health == BackupHealth.OK
}
