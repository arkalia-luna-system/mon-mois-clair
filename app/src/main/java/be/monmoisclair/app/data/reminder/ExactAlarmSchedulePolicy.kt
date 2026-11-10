package be.monmoisclair.app.data.reminder

/** Décisions pures pour ne pas garder une alarme périmée. */
object ExactAlarmSchedulePolicy {
    fun shouldCancelWhenExactPermissionMissing(canScheduleExactAlarms: Boolean): Boolean =
        !canScheduleExactAlarms

    fun shouldCancelAfterScheduleSecurityFailure(): Boolean = true
}
