package be.monmoisclair.app.data.backup

/**
 * Rotation atomique 3 versions sur un [BackupTree].
 *
 * Pour chaque étape (2→3, 1→2, tmp→1) : delete explicite de la cible, puis rename.
 * L'écriture de tmp force un sync disque via [BackupTree.writeUtf8Sync].
 */
class BackupRotator {

    fun writeRotated(tree: BackupTree, utf8Content: String) {
        tree.writeUtf8Sync(BackupFileNames.TMP, utf8Content)
        rotateStep(tree, from = BackupFileNames.V2, to = BackupFileNames.V3)
        rotateStep(tree, from = BackupFileNames.V1, to = BackupFileNames.V2)
        rotateStep(tree, from = BackupFileNames.TMP, to = BackupFileNames.V1)
        if (tree.exists(BackupFileNames.TMP)) {
            tree.delete(BackupFileNames.TMP)
        }
    }

    private fun rotateStep(tree: BackupTree, from: String, to: String) {
        if (!tree.exists(from)) return
        if (tree.exists(to)) {
            val deleted = tree.delete(to)
            if (!deleted && tree.exists(to)) {
                throw BackupFailure.Io("Impossible de supprimer la cible avant rename: $to")
            }
        }
        tree.rename(from, to)
        if (!tree.exists(to)) {
            throw BackupFailure.Io("Rename échoué: $from → $to")
        }
        if (tree.exists(from)) {
            throw BackupFailure.Io("Source encore présente après rename: $from")
        }
    }
}
