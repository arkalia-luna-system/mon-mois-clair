package be.monmoisclair.app.ui.clipboard

/**
 * Texte d’aide — presse-papiers IBAN / communication (aligné [SensitiveClipboard]).
 */
object ClipboardSecurityCopy {

    /** Secondes avant effacement automatique (doit rester aligné sur [SensitiveClipboard.CLEAR_AFTER_MS]). */
    const val CLEAR_SECONDS = 30

    const val HOW_IT_WORKS_BODY =
        "« Copier l’IBAN » ou « Copier la communication » : le texte va dans le presse-papiers " +
            "pour coller dans votre banque. Il disparaît tout seul après environ $CLEAR_SECONDS secondes, " +
            "ou dès que vous quittez l’app. Android peut aussi masquer ce contenu aux autres apps."
}
