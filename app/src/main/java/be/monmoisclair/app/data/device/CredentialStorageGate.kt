package be.monmoisclair.app.data.device

import android.content.Context
import be.monmoisclair.app.data.reminder.BootReceiver

/**
 * Stockage **CE** (fichiers app, SQLCipher vault) : accessible seulement après déverrouillage
 * utilisateur — pas de Direct Boot sur la DB (charte / Device Care).
 */
object CredentialStorageGate {

  /** Vrai si le stockage chiffré « credential-encrypted » est disponible. */
  fun isAvailable(context: Context): Boolean = BootReceiver.isCredentialStorageAvailable(context)

  fun requireAvailable(context: Context) {
    check(isAvailable(context)) {
      "Stockage CE indisponible (téléphone verrouillé)"
    }
  }
}
