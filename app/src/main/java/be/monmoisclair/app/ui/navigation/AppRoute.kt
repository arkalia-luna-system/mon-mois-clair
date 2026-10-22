package be.monmoisclair.app.ui.navigation

object AppRoute {
    const val SafSetup = "onboarding/saf"
    const val PermissionsSetup = "onboarding/permissions"
    const val Home = "home"
    const val ReminderHealth = "reminder_health"
    const val RestoreBackup = "restore_backup"
    const val Settings = "settings"
    const val HowItWorks = "how_it_works"
    const val Dossiers = "dossiers"
    const val Upcoming = "upcoming"
    const val PendingLetterForm = "dossiers/pending/new"
    const val ConvertPendingLetter = "dossiers/pending/convert/{letterId}"
    const val AddEntry = "entry/add/{ym}"
    const val ScanEpc = "entry/scan_epc"
    const val EditEntry = "entry/edit/{id}/{isExpense}"
    const val EditTemplate = "template/edit/{id}"

    fun addEntry(ym: java.time.YearMonth): String = "entry/add/$ym"

    fun editEntry(id: String, isExpense: Boolean): String =
        "entry/edit/$id/${if (isExpense) "true" else "false"}"

    fun editTemplate(id: String): String = "template/edit/$id"

    fun convertPendingLetter(letterId: String): String =
        "dossiers/pending/convert/$letterId"
}
