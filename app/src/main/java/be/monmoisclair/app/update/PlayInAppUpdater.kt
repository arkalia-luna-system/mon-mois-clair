package be.monmoisclair.app.update

import android.app.Activity
import android.content.Context
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Mises à jour Play **In-App** (Flexible) via le Play Store (IPC),
 * sans permission `INTERNET` dans l’app.
 *
 * Sideload / pas de Play Store → no-op silencieux.
 */
@Singleton
class PlayInAppUpdater @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val manager by lazy { AppUpdateManagerFactory.create(context.applicationContext) }

    @Volatile
    private var coldStartChecked = false

    private var downloadListener: InstallStateUpdatedListener? = null

    /** Une fois par process, après déverrouillage / Accueil prêt. */
    fun maybeCheckFlexibleOnColdStart(activity: Activity) {
        if (coldStartChecked) return
        coldStartChecked = true
        runCatching {
            manager.appUpdateInfo
                .addOnSuccessListener { info ->
                    if (info.installStatus() == InstallStatus.DOWNLOADED) {
                        completeQuietly()
                        return@addOnSuccessListener
                    }
                    val available =
                        info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    val flexibleOk = info.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
                    if (!available || !flexibleOk) return@addOnSuccessListener
                    ensureDownloadListener()
                    runCatching {
                        manager.startUpdateFlowForResult(
                            info,
                            activity,
                            AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build(),
                            REQUEST_CODE
                        )
                    }
                }
                .addOnFailureListener {
                    // Pas de Play / sideload / API indisponible.
                }
        }
    }

    /** Reprend une MAJ Flexible déjà téléchargée (retour au premier plan). */
    fun onResumeCompleteIfReady() {
        runCatching {
            manager.appUpdateInfo.addOnSuccessListener { info ->
                if (info.installStatus() == InstallStatus.DOWNLOADED) {
                    completeQuietly()
                }
            }
        }
    }

    private fun ensureDownloadListener() {
        if (downloadListener != null) return
        val listener = InstallStateUpdatedListener { state ->
            if (state.installStatus() == InstallStatus.DOWNLOADED) {
                completeQuietly()
                clearListener()
            }
        }
        downloadListener = listener
        runCatching { manager.registerListener(listener) }
    }

    private fun clearListener() {
        val listener = downloadListener ?: return
        runCatching { manager.unregisterListener(listener) }
        downloadListener = null
    }

    private fun completeQuietly() {
        runCatching { manager.completeUpdate() }
    }

    companion object {
        const val REQUEST_CODE = 7819
    }
}
