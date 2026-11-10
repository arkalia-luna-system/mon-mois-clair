# Direct Boot — comportement volontaire

**Résumé :** pas de `directBootAware` · base SQLCipher et vault en stockage **CE** (credential-encrypted) · alarmes et widget **ne touchent pas** Room avant déverrouillage utilisateur.

## Ce qui se passe

| Moment | Comportement |
|--------|----------------|
| Boot, téléphone **verrouillé** | `BootReceiver` / `UserUnlockedReceiver` : skip si `UserManager.isUserUnlocked == false` |
| Ouverture app avant unlock | `MainViewModel` : écran « Déverrouillez le téléphone » (`UserLockedScreen`) — pas d’accès DB |
| Après `ACTION_USER_UNLOCKED` | Reconcile alarmes + `refreshStart()` (activité ouverte) |
| Widget | Coquille seule si CE indisponible |

## Limites connues

- Pas de rappels ni widget à jour **avant** le premier déverrouillage post-boot (acceptable famille).
- Pas de migration vers DE (device-encrypted) : complexité SQLCipher + wipe interdit.
- Smoke Samsung Direct Boot = **NON VÉRIFIÉ** hors device réel.

Code : `CredentialStorageGate`, `DbPassphraseVault`, `BootReceiver`, `UserUnlockedReceiver`, `MainViewModel`.
