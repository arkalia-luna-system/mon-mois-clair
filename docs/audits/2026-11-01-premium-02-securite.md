# Audit premium sécurité — Mon Mois Clair (~6.91)

| | |
|--|--|
| **Date** | 2026-11-01 · premium-02 |
| **Branche / version** | `develop` · `0.1.0-6.91` (`versionCode` 96) — déclaré journal ; `versionName` VÉRIFIÉ dans `app/build.gradle.kts` |
| **Périmètre** | Crypto réelle vs claims `PRIVACY.md` / `SECURITY.md` ; trous Direct Boot, ZIP/MMC, PDF, cache, widget, notifs, Keystore ; confiance premium famille offline |
| **Hors périmètre** | Pentest root/malware, rewrite, CVE Dependabot jar-par-jar, Play Store, smoke doigt restore 2ᵉ device (non rejoué ici) |
| **Code livré dans cette passe** | **Aucun** — audit only |
| **Prédécesseur** | [`2026-10-26-final-02-securite.md`](2026-10-26-final-02-securite.md) (post-6.82) · fix Direct Boot [`2026-11-01-samsung-defaillances-notif-icon.md`](2026-11-01-samsung-defaillances-notif-icon.md) |

**Verdict court :** à **6.91**, le socle crypto (SQLCipher + wrap Keystore, `MMC1` / code 8 chiffres, `MRF1`, refuse cleartext par défaut, TTL 15 min `view/`+`share/`, Direct Boot retiré) est **aligné** avec les claims PRIVACY/SECURITY et **plus mature** qu’en 6.82. Ce n’est **pas** un coffre bancaire. Les résidus de confiance premium sont surtout **surfaces hors process** (widget/notifs, PDF résumé clair, cache aperçu), **grace opt-in**, **entropie code**, et **discipline humaine** (code ≠ même message que `.mmc`). **Note indicative : 7,5 / 10** pour sideload famille discipliné.

---

## 1. Synthèse exécutive

| Axe | État 6.91 | Commentaire |
|-----|-----------|-------------|
| Réseau | Fort | Aucune permission `INTERNET` |
| Au repos (device OK) | Fort | SQLCipher + `MRF1` + wrap `mmc_db_wrap_v1` |
| Copies partagées | Bon sous discipline | `.mmc` / `MMC1B64` ; grace claire = opt-in |
| Surfaces hors app | Moyen | Widget, shade notif, PDF share, URI `view/` |
| Alignement docs ↔ code | Bon | Claims crypto tenus ; limites documentées |
| Direct Boot / CE | Fermé (6.91) | Plus de `directBootAware` / `LOCKED_BOOT` |
| Confiance « premium » | Conditionnelle | Smoke restore + brief code encore ouverts (journal) |

**Delta vs final-02 (6.82) :** Direct Boot corrigé ; TTL aperçu **1 h → 15 min** (aligné docs) ; restore claire **refusée par défaut** (déjà claimé, confirmé code) ; crash Keystore IV (6.90) derrière nous.

---

## 2. Claims PRIVACY / SECURITY vs code

| Claim | Statut | Nature | Preuve |
|-------|--------|--------|--------|
| Pas d’`INTERNET` | **VÉRIFIÉ** | inspection | `AndroidManifest.xml` : permissions exact alarm / notifs / boot / bio / camera uniquement — pas `INTERNET` |
| `allowBackup="false"` | **VÉRIFIÉ** | inspection | `android:allowBackup="false"` |
| Room SQLCipher ; passphrase wrap Keystore `mmc_db_wrap_v1` | **VÉRIFIÉ** (structure) | inspection | `DatabaseModule` + `DbPassphraseVault` + `KeystoreAesKey.WRAP_ALIAS` ; comportement runtime device = **NON VÉRIFIÉ** cet appel |
| Migration Failed → ouverture claire temporaire | **VÉRIFIÉ** | inspection | `DatabaseModule` : `Failed` + marqueur absent → `factory = null` |
| Backups SAF `MMC1B64` + code | **VÉRIFIÉ** (structure) | inspection | `BackupEnvelope.TEXT_PREFIX`, `ShareSecretVault` 8 chiffres, PBKDF2 120k (`AesGcmBox`) |
| Partage `.mmc` ; texte **sans** le code | **VÉRIFIÉ** | inspection | `ShareBackupUseCase` : seal ZIP → `.mmc` ; `EXTRA_TEXT` rappelle le code sans l’inclure |
| Anciennes ZIP/JSON refusées sauf opt-in « Ancien format » | **VÉRIFIÉ** | inspection | `BackupRestorer` + `RestoreBackupScreen` checkbox ; tests unit/instrumented présents (non réexécutés) |
| Photos/PDF `MRF1` ; aperçu `cache/view/` TTL 15 min + wipe lock | **VÉRIFIÉ** | inspection | `LocalFileCipher`, `ReceiptPhotoStore.VIEW_CACHE_TTL_MS = 15 min`, `MainViewModel` purge |
| PDF résumé clair éphémère `cache/share/` TTL 15 min + wipe | **VÉRIFIÉ** | inspection | `MonthSummaryPdfShare.SHARE_TTL_MS`, watermark documenté dans writer |
| Bio : porte UI + CryptoObject `mmc_unlock_v1` | **VÉRIFIÉ** (structure) | inspection | `KeystoreAesKey.unlockCipherForAuth` ; CryptoObject **ne wrappe pas** SQLCipher (aligné docs) |
| Widget / notifs : montants masqués si bio ON | **VÉRIFIÉ** | inspection | `CarnetWidgetText.compose(locked=…)` ; `ReminderNotifCopy` + `BIOMETRIC_ENABLED` |
| `FLAG_SECURE` si verrou ON | **VÉRIFIÉ** | inspection | `MainActivity` + `BiometricFlagCache` |
| Clipboard sensible + clear ~1 min | **VÉRIFIÉ** | inspection | `SensitiveClipboard` `CLEAR_AFTER_MS = 60_000` |
| FileProvider non exporté | **VÉRIFIÉ** | inspection | `exported="false"` ; paths `share/` `view/` `camera/` `receipts/` |
| Keystore signature hors git | **VÉRIFIÉ** (ignore) | contrôle Git | `git check-ignore` → `keystore.properties` ; `git ls-files` jks vide |
| « Pas un coffre parfait » / RAM / root | **DÉCLARÉ** | inspection docs | `PRIVACY` § Honnêteté + `SECURITY` § Limites — cohérent avec architecture |

**Conclusion claims :** pas de sur-promesse crypto majeure détectée à l’inspection. Risque principal = **compréhension famille** + résidus documentés, pas un mensonge MD↔code.

---

## 3. Surfaces à risque

### 3.1 Direct Boot / Credential Encrypted storage

| Point | Constat | Priorité |
|-------|---------|----------|
| `directBootAware` / `LOCKED_BOOT` | **Absents** du manifeste 6.91 ; commentaire explicite CE | Fermé |
| `BootReceiver` | `exported=true` (requis boot) ; garde `UserManager.isUserUnlocked` | OK |
| Vault | `DbPassphraseVault.requireCredentialStorage()` refuse CE verrouillé | OK |
| Historique | Crash ENOENT passphrase en Direct Boot (pré-6.91) — **DÉCLARÉ** journal / audit Samsung | Clos en code |

**Statut Direct Boot :** trou **fermé** à l’inspection. Comportement boot réel Samsung = **NON VÉRIFIÉ** ici (smoke Device Care ouvert dans le journal).

### 3.2 Keystore / SQLCipher

| Point | Constat | Priorité |
|-------|---------|----------|
| Wrap sans `userAuthenticationRequired` | Volontaire : widget / alarmes ouvrent Room | Produit (P2 confiance) |
| Unlock `mmc_unlock_v1` AUTH_STRONG\|CREDENTIAL | Preuve UI ; pas de binding passphrase DB | Aligné |
| Fallback logiciel Keystore | `KeystoreAesKey` → clé RAM si Keystore KO (tests) | P2 device exotique |
| Migration Failed → DB claire | Conservatoire données, affaiblit au repos | **P1** |
| IV GCM | `AesGcmBox.encrypt` sans caller IV (fix 6.90) | OK structure |

### 3.3 ZIP / `.mmc` / SAF restore

| Point | Constat | Priorité |
|-------|---------|----------|
| Écritures neuves | MMC1 / MMC1B64 | OK |
| Zip-slip / bomb | `sanitizeReceiptPath` (pas `..`, 2 segments) + `MAX_ZIP_BYTES` + `readBounded` | OK structure |
| Grace claire | Opt-in UI + warning | **P1** résidu |
| Entropie code 8 chiffres | ~10⁸ + PBKDF2 120k — curieux OK ; crack offline motivé non | **P1**/P2 selon audience |
| Code + `.mmc` même chat | App n’envoie pas le code ; utilisatrice peut coller | **P0 ops** (humain) |
| Authenticité canal | SHA payload interne ; pas de signature éditeur | P2 |

### 3.4 PDF / cache

| Point | Constat | Priorité |
|-------|---------|----------|
| PDF résumé | Clair volontaire (`cache/share/`) | **P1** (partage = fuite contenu) |
| Aperçu pièces | Clair `cache/view/` 15 min + wipe lock | **P1** fenêtre |
| Caméra JPEG orphelins | Sweep 1 h racine/`camera/` | P2 |
| FileProvider `receipts/` | Blobs = MRF1 au repos | Faible |

### 3.5 Widget / notifications / FAIT

| Surface | Bio ON | Bio OFF | Priorité |
|---------|--------|---------|----------|
| Widget | Pas de montant ; « Touchez pour ouvrir » | Reste + libellé + montant | Produit |
| Notifs | Montant → « Montant masqué » ; **libellé** dans le titre | Libellé + montant | **P2** |
| FAIT ✓ / Annuler | `ReminderReceiver` non exporté ; PendingIntent `IMMUTABLE` ; **sans** porte bio | Produit | **P2** |
| Texte shade | Non chiffré (limite Android) | — | Documenté |

### 3.6 Autres sains

- Receivers : `ReminderReceiver` `exported=false` ; widget + boot exportés (requis).
- R8 minify release (claim / build — minify **DÉCLARÉ** SECURITY, non re-prouvé ici).
- Pas de cleartext traffic config (pas de réseau app).

### 3.7 Scénarios famille (post-6.91)

| Scénario | Fuite ? | Commentaire |
|----------|---------|-------------|
| Téléphone éteint / sandbox dump sans clé Keystore | Room / receipts **illisibles** | Gain majeur vs pré-6.81 |
| WhatsApp : `.mmc` **sans** code | Illisible | Gain majeur |
| WhatsApp : `.mmc` + code dans le même message | **Fuite totale** pour le destinataire | Éducation utilisatrice — P0 ops |
| Ancien ZIP/JSON clair encore sur Drive | Clair si restauré avec opt-in | Grace ; supprimer / remplacer |
| Documents SAF sync cloud | Blobs `MMC1B64` / `MRF1` — illisibles sans code / wrap appareil | Photos SAF = clé **appareil** |
| Lockscreen + bio ON | Libellé facture visible, pas le montant | Acceptable |
| Bio OFF + widget | Montants sur l’accueil | Attendu |
| Boot avant unlock (Direct Boot) | Plus d’accès vault CE via receiver | Fermé 6.91 |
| Perte téléphone + oubli code + pas d’autre copie | **Perte données** | Documenté ; pas de recovery cloud |

---

## 4. Faiblesses & améliorations « premium confiance »

*(Recommandations d’audit — **aucune correction code appliquée**.)*

### P0
1. **Ops famille :** ne jamais coller le **code de copie** dans le même fil que le `.mmc` — brief oral / fiche 5 lignes (toujours le risque n°1 post-chiffrement).
2. **Aucun P0 crypto runtime bloquant** identifié à l’inspection code 6.91 pour un sideload familial discipliné.

### P1
1. Surveiller / durcir le chemin **Migration SQLCipher Failed → Room claire** (télémétrie locale, retry, message utilisatrice).
2. Réduire la fenêtre **grace cleartext** (date butoir une fois toute la famille ≥ 6.81).
3. Renforcer le **code de copie** (longueur / alphabet) si diffusion hors cercle fermé.
4. Éducation / UX autour du **PDF résumé clair** (ce n’est pas une copie `.mmc`).
5. Smoke **restore 2ᵉ device** + mauvais code (encore ouvert dans `PROGRESS_LOG`).

### P2
1. Option masquer aussi le **libellé** notif si bio ON (trade-off utilité).
2. Revoir **FAIT depuis notif** vs verrou (convenience vs porte stricte).
3. Purge `view/`/`share/` plus agressive (TTL plus court, wipe onPause).
4. Documenter explicitement dans SECURITY le **choix wrap sans bio** (widget/rappels).
5. Hygiène Dependabot (bruit AGP) — pas runtime APK.

---

## 5. Note /10 (confiance premium offline)

| Critère | Note | Motif |
|---------|------|-------|
| Privacy réseau | 9,5 | Zéro `INTERNET` |
| Confidentialité au repos | 8 | SQLCipher + MRF1 + Keystore ; fallback migration |
| Copies partagées | 7,5 | MMC solide ; grace + entropie + erreur humaine |
| Surfaces hors app | 6,5 | Widget/notifs/PDF/cache |
| Alignement docs | 8,5 | Claims honnêtes |
| Durcissement récent (Direct Boot, IV, TTL) | 8 | 6.90–6.91 |

**Note globale indicative : 7,5 / 10**  
Interprétation : **bon produit famille local-first**, pas « vault premium bancaire ». GO cadeau = conditionnel aux smoke restore / Device Care encore listés au journal.

---

## 6. Limites de cet audit

- Aucun test Gradle / androidTest / smoke device **réexécuté** dans cet appel.
- Résultats 374 tests / install SM-S938B = **DÉCLARÉ** (`PROGRESS_LOG`), pas rejoués.
- Pas d’attaque offline sur un `.mmc` réel, pas de dump CE/DE, pas de Frida.
- Inspection ≠ validation fonctionnelle.
- Comportement runtime (migration OK, restore 2ᵉ device, Device Care post-fix) reste **NON VÉRIFIÉ** ici.

---

## 7. Fichiers de référence

| Zone | Chemins |
|------|---------|
| Manifeste / Direct Boot | `app/src/main/AndroidManifest.xml`, `BootReceiver.kt`, `DbPassphraseVault.kt` |
| DB / Keystore | `di/DatabaseModule.kt`, `crypto/DbPassphraseVault.kt`, `KeystoreAesKey.kt`, `AesGcmBox.kt`, `EncryptedDbMigrator.kt` |
| Backup / ZIP | `BackupEnvelope.kt`, `ShareSecretVault.kt`, `BackupSecurityArchive.kt`, `BackupRestorer.kt`, `ShareBackupUseCase.kt` |
| Photos / PDF / cache | `LocalFileCipher.kt`, `ReceiptPhotoStore.kt`, `MonthSummaryPdfShare.kt`, `MonMoisClairApp.kt` |
| Bio / UI | `BiometricAuthGatewayImpl.kt`, `MainActivity.kt`, `MainViewModel.kt` |
| Widget / notifs | `CarnetWidgetText.kt`, `ReminderSchedulerImpl.kt`, `ReminderNotifCopy.kt`, `ReminderReceiver.kt` |
| Claims | `PRIVACY.md`, `SECURITY.md` |

---

*Fin audit premium-02 sécurité · `0.1.0-6.91` · AUDIT ONLY.*
