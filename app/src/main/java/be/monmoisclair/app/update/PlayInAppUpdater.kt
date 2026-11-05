package be.monmoisclair.app.update

import android.app.Activity
import android.content.Context
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Mises à jour Play **In-App Updates** (type **Immediate**) via le Play Store (IPC),
 * sans permission `INTERNET` dans l’app.
 *
 * ## Comportement (install Play Internal)
 * 1. Cold start, **après** déverrouillage / Accueil prêt (`MainActivity`).
 * 2. Si une version plus récente est disponible sur la piste Internal :
 *    Play Core ouvre un **écran plein** de mise à jour (téléchargement + install),
 *    puis **relance** l’app — pas besoin d’ouvrir l’appli Play Store manuellement.
 * 3. Si le flux était en cours et l’utilisatrice revient au premier plan :
 *    reprise via `onResumeContinueIfNeeded`.
 *
 * ## Limites honnêtes
 * - Install **via Play Internal** + compte **testeur** opt-in requis.
 * - Sideload / Option B (signature ≠ Play) / pas de Play → **no-op** silencieux.
 * - Immediate = écran Play bloquant (pas un bandeau Flexible en arrière-plan).
 * - Secours : ouvrir Play Store → Mettre à jour (MAJ auto Play).
 */
@Singleton
class PlayInAppUpdater @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val manager by lazy { AppUpdateManagerFactory.create(context.applicationContext) }

    @Volatile
    private var coldStartChecked = false

    /** Une fois par process, après déverrouillage / Accueil prêt. */
    fun maybeCheckImmediateOnColdStart(activity: Activity) {
        if (coldStartChecked) return
        coldStartChecked = true
        runCatching {
            manager.appUpdateInfo
                .addOnSuccessListener { info ->
                    startImmediateIfAllowed(activity, info)
                }
                .addOnFailureListener {
                    // Pas de Play / sideload / API indisponible.
                }
        }
    }

    /**
     * Reprend une MAJ Immediate déjà en cours (retour au premier plan).
     * Sans cela, un flux interrompu peut rester bloqué.
     */
    fun onResumeContinueIfNeeded(activity: Activity) {
        runCatching {
            manager.appUpdateInfo.addOnSuccessListener { info ->
                if (info.updateAvailability() ==
                    UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS
                ) {
                    startImmediateFlow(activity, info)
                }
            }
        }
    }

    private fun startImmediateIfAllowed(activity: Activity, info: AppUpdateInfo) {
        val available = info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
        val immediateOk = info.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
        if (!available || !immediateOk) return
        startImmediateFlow(activity, info)
    }

    private fun startImmediateFlow(activity: Activity, info: AppUpdateInfo) {
        runCatching {
            manager.startUpdateFlowForResult(
                info,
                activity,
                AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build(),
                REQUEST_CODE
            )
        }
    }

    companion object {
        const val REQUEST_CODE = 7819
    }
}
