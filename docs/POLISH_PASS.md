# Passe finition (agent polish) — hors lots produit

Date : 2026-10-21.  
Rôle : sécurité / bugs / polish **sans** écraser le WIP lot B/C.  
**Pas de Gradle en parallèle** (Mac) : corrections code seules jusqu’à commit groupé.

## Corrigé

| Sévérité | Sujet | Fichiers |
|----------|--------|----------|
| P0 | Verrou bio synchrone au background | `MainViewModel.kt` |
| P0 | Lecture ZIP/URI/photos bornée (anti bomb / OOM) | `BackupSecurityArchive`, `BackupRestorer`, `SafDocumentBackupTree`, `ReceiptPhotoStore` |
| P1 | Restore photos : purge + réécriture locale | `BackupRestorer.kt` |
| P1 | FileProvider : `share/` + `camera/` (plus tout le cache) | `file_paths.xml`, `ShareBackupUseCase`, `EntryFormScreen` (2 lignes path) |
| P1 | Sweep JPEG caméra orphelins au démarrage | `MonMoisClairApp.kt` |
| P1 | `SCHEDULE_EXACT_ALARM` + `USE_EXACT_ALARM` (Santé ↔ Manifest) | `AndroidManifest.xml` |
| P2 | BootReceiver : debounce 2 min sur `TIME_SET` | `BootReceiver.kt` |
| P2 | Copy biométrie / onboarding / scan a11y / MIME restore | Settings, Permissions, Scan, RestoreBackupScreen |
| P2 | Messages restore sans jargon technique en UI | `BackupRestorer`, `RestoreBackupViewModel` |
| P2 | Alertes : ligne santé TalkBack + chemin Samsung | `HealthStatusRow`, `ReminderHealthScreen` |
| P2 | Verrou : message d’échec en live region assertive | `BiometricLockScreen` |

## Encore ouvert (plus tard)

- Smoke Galaxy (notif multi-factures, trimestriel)
- Lint soft → findings utiles un par un
- Detekt (phase 2 suite)

## Règle collab

- Jamais `checkout` / `restore` / `reset` sur le WIP des autres
- **Un seul** `./gradlew` à la fois sur la machine
