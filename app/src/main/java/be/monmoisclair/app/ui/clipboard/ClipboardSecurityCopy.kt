package be.monmoisclair.app.ui.clipboard

/**
 * Texte d’aide — presse-papiers IBAN / communication (aligné [SensitiveClipboard]).
 */
object ClipboardSecurityCopy {

    /** Secondes avant effacement automatique (doit rester aligné sur [SensitiveClipboard.CLEAR_AFTER_MS]). */
    const val CLEAR_SECONDS = 30

    const val HOW_IT_WORKS_BODY =
        "Touchez « Copier l’IBAN » ou « Copier la communication ». " +
            "Ouvrez votre banque. " +
            "Collez aussitôt. " +
            "Le presse-papiers s’efface seul après environ $CLEAR_SECONDS secondes. " +
            "Il s’efface aussi quand vous quittez l’app."
}
