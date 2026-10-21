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
| P2 | Accueil compact : padding cartes + pastille Accord contour | `HomeScreen` |
| P2 | StatusChip TalkBack « Statut : … » | `HomeScreen` |
| P1 | SAF `readUtf8` + copyThenDelete lectures bornées | `SafDocumentBackupTree` |
| P2 | Messages backup/restore sans jargon SAF / URI | `SafDocumentBackupTree`, `SafBackupStore`, `BackupRotator`, `BackupRestorer` |
| P2 | Cibles tactile cartes hors-mois (Réglages) | `SettingsScreen` |
| P2 | TalkBack chargement app / mois / photo | `MainActivity`, `HomeScreen` |
| P2 | Live regions messages Réglages / Restaurer | `SettingsScreen`, `RestoreBackupScreen` |
| P2 | Test `DisplayFit` compact ≤ 800 dp | `DisplayFitTest` |
| P1 | Lot C : voir / enlever courriers en plus + aide | `EntryFormScreen`, `EditEntryViewModel`, `UpdateInstanceUseCase` |
| P2 | CI : `cancel-in-progress: true` (plus de faux « cancelled » en file) | `.github/workflows/ci.yml`, `docs/CI.md` |

## Encore ouvert (plus tard)

- Smoke Galaxy (notif multi-factures, trimestriel, « Écrire »)
- Lint soft → findings utiles un par un
- Detekt (phase 2 suite)
- Lots D / E / F si besoin (plan produit)

## Règle collab

- Jamais `checkout` / `restore` / `reset` sur le WIP des autres
- **Un seul** `./gradlew` à la fois sur la machine
