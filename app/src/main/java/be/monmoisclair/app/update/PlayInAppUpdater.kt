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
 * Mises à jour Play **In-App Updates** (type **Flexible**) via le Play Store (IPC),
 * sans permission `INTERNET` dans l’app.
 *
 * ## Comportement (install Play Internal)
 * 1. Cold start, **après** déverrouillage / Accueil prêt (`MainActivity`).
 * 2. Si une version plus récente est disponible sur la piste Internal :
 *    Play Core ouvre le flux Flexible **dans** l’app (pas besoin d’ouvrir
 *    l’appli Play Store manuellement).
 * 3. Téléchargement en arrière-plan → `completeUpdate()` quand `DOWNLOADED`
 *    (listener + reprise `onResume`).
 *
 * ## Limites honnêtes
 * - Install **via Play Internal** + compte **testeur** opt-in requis.
 * - Sideload / Option B (signature ≠ Play) / pas de Play → **no-op** silencieux.
 * - Flexible peut demander un **accord** utilisateur (UI Play Core) ; ce n’est
 *   pas un téléchargement 100 % invisible.
 * - Secours : ouvrir Play Store → Mettre à jour (MAJ auto Play).
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
