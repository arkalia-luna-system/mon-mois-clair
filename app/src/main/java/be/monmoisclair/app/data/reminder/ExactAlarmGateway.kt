package be.monmoisclair.app.data.reminder

interface ExactAlarmGateway {
    fun canScheduleExactAlarms(): Boolean
    fun schedule(requestCode: Int, triggerAtEpochMs: Long, payload: ReminderPayload)
    fun cancel(requestCode: Int)
}
