# Audit — Samsung Device Care « défaillances » + icône notif + #54 → 6.91

| Champ | Valeur |
|-------|--------|
| Date | 2026-11-01 |
| Branche | `develop` (= `main` ff-only après ship) |
| Version | `0.1.0-6.91` · `versionCode` 96 |
| SHA | `e91f4a5` (`develop` = `main`, ff-only) |
| Device | SM-S938B (sans wipe) |
| Tests | **374 exécutés** / 375 (1 skipped), 89 XML, 0 failure |

## A — Samsung « 1 application avec défaillances » / batterie

### Preuves device (VÉRIFIÉ)

- Package installé avant fix : `0.1.0-6.90` / versionCode 95 (depuis 2026-10-26).
- Buffer crash `adb logcat -b crash` : **seul** FATAL MMC =
  `2026-10-29 11:20:03` —
  `Unable to create application` →
  `FileNotFoundException: …/files/crypto/db_passphrase.wrapped` (**ENOENT**).
- Crash AesGcmBox IV (`Caller-provided IV not permitted`) : déjà corrigé en **6.90** ;
  **absent** du buffer crash actuel.
- Alarmes : quelques `REMINDER_FIRE` exacts (J−3 / Jour J / overdue) — usage normal
  `USE_EXACT_ALARM`, pas de wake lock app agressif, WorkManager = one-shot backup.
- Capture Device Care « 1 problème de batterie » : **intermittente** (autre scan =
  « Aucune utilisation élevée de la batterie ») — vraisemblablement historique /
  wakeups liés aux crash loops Direct Boot, pas un JobScheduler agressif.

### Cause réelle (défaillances)

`BootReceiver` était **`directBootAware="true"`** + écoutait
`LOCKED_BOOT_COMPLETED`. Au boot verrouillé :

1. le process démarre,
2. Hilt injecte `CarnetWidgetRefresher` → Room → `DbPassphraseVault`,
3. écriture CE (`filesDir/crypto/…`) **indisponible** → ENOENT → FATAL,
4. Samsung Device Care compte l’app « avec défaillances ».

### Fixes

- Retrait `directBootAware` + `LOCKED_BOOT_COMPLETED` (manifest).
- Garde `UserManager.isUserUnlocked` dans `BootReceiver`.
- Vault : `mkdirs` strict + écriture atomique `.tmp` + refuse CE verrouillé.
- Debounce 15 s sur `CONFIGURATION_CHANGED` → refresh widget (rafales Samsung).

### Device Care après fix (à faire Athalia)

Device Care **conserve un historique** de crashes / score batterie. Après install
6.91 : rouvrir **Maintenance de l’appareil** → **Continuer / Optimiser**, puis
rescanner. L’indicateur peut rester tant que la fenêtre d’historique n’est pas
écoulée — ce n’est **pas** un « mask » magique.

## B — Icône notification reconnaissable

| Avant | Après |
|-------|--------|
| `smallIcon` seul = silhouette blanche `ic_notification` (barre d’état) ; bannière collapsed Samsung = pastille monochrome peu lisible | `smallIcon` silhouette blanche renforcée + **`largeIcon`** bitmap coloré `ic_notification_large.png` (livre+coche, dérivé `book_launcher`) |

Fichiers : `PaymentNotificationHelper` (`setLargeIcon`),
`drawable/ic_notification.xml`, `drawable/ic_notification_large.png`.

## C — Dependabot #54 kotlin-gradle-plugin

- CVE : **CVE-2026-53914** / GHSA-r937-wjx7-w2jp.
- Patch min Dependabot : **`2.4.20-Beta1`**.
- Rescan 2026-11-01 : **aucun backport** 2.0.x / 2.1.x (OSV liste tout `&lt; 2.4.20-Beta1`).
- Bump AGP 9 + Kotlin 2.4 **non tenté** dans ce ship (Hilt/KSP/Room cascade).
- **Pin honnête** : Kotlin / KSP / Compose plugin **2.0.21** — doc `docs/CI.md`.
- Risque = classpath **build**, pas APK runtime.

## D — Ship

- `./gradlew :app:testDebugUnitTest --rerun-tasks` → **374/375** (1 `@Ignore`), 89 XML.
- `assembleRelease` OK · `adb install -r` OK · `MainActivity` resumed · 0 FATAL post-install.
- Preuve version device : `versionName=0.1.0-6.91` · `versionCode=96`.
- SHA ship : **`e91f4a5`** — `develop` = `main` (ff-only, pas de force-push).
