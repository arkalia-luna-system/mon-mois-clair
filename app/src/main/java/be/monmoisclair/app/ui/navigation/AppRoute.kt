package be.monmoisclair.app.ui.navigation

object AppRoute {
    const val SafSetup = "onboarding/saf"
    const val PermissionsSetup = "onboarding/permissions"
    const val Home = "home"
    const val ReminderHealth = "reminder_health"
    const val RestoreBackup = "restore_backup"
    const val Settings = "settings"
    const val AddEntry = "entry/add"
    const val EditEntry = "entry/edit/{id}/{isExpense}"

    fun editEntry(id: String, isExpense: Boolean): String =
        "entry/edit/$id/${if (isExpense) "true" else "false"}"
}
