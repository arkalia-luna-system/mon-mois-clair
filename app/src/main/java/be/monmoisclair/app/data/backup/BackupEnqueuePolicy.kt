package be.monmoisclair.app.data.backup

/**
 * Ne pas [ExistingWorkPolicy.REPLACE] un backup déjà en cours : reporter un second passage.
 */
object BackupEnqueuePolicy {
    fun shouldDeferWhileWorkerRunning(workerRunning: Boolean): Boolean = workerRunning

    /** Y compris après échec du worker : les mutations coalescées doivent encore être reflétées. */
    fun needsFollowUpAfterRun(coalescePending: Boolean): Boolean = coalescePending
}
