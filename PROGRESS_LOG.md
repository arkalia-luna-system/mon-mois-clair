# Journal de bord — Mon Mois Clair

Règle : aucune étape n’est considérée comme terminée tant que ce fichier n’est pas mis à jour.

---

## État global

| Champ | Valeur |
|-------|--------|
| Dernière étape terminée | **1.1** |
| Étape en cours | *(aucune)* |
| Prochaine sous-étape | **1.2** — SAF Auto-Backup (`SafBackupStore`, rotation 3, `flush`/`fd.sync()`, checksum SHA-256, `backupHealth`) |

---

## Journal

### 2026-10-19 — Initialisation projet

- Création de `CHARTE_ET_ARCHITECTURE.md` (vision, A–E, schéma, règles métier, SAF, rappels, qualité).
- Création de ce journal.
- Rectifications intégrées dans la charte :
  - Édition / suppression (`EditEntry`, `UpdateInstanceUseCase`, `DeleteInstanceUseCase`).
  - SAF : `delete()` explicite avant rename + `flush`/`fd.sync()`.
  - Receivers : `goAsync()` + `finally { finish() }`.

### 2026-10-19 — Étape 1.1 terminée

**Codé :**
- Scaffold Android (Kotlin, Room, Hilt, Robolectric) — package `be.monmoisclair.app`.
- Enums stricts : `ExpenseStatus`, `BackupHealth` + `Converters` Room.
- Entités : `MonthLedger`, `Income/ExpenseTemplate`, `Income/ExpenseInstance`, `AppMeta`.
- DAOs : `MonthLedgerDao`, `TemplateDao`, `InstanceDao` (dont requêtes template → mois suivants pour édition/suppression), `AppMetaDao`.
- `AppDatabase` v1 + schéma exporté `app/schemas/.../1.json`.
- `MonthDayClamp` (borne `dayOfMonth` au dernier jour réel du mois).
- `MutatingRepository` (ordre : transaction → backup enqueue → reconcile).
- Interfaces placeholder : `BackupScheduler` / `ReminderScheduler` (+ NoOp Hilt jusqu’aux étapes 1.2 / 2.x).
- Stub `MainActivity` sans Compose (UI à l’étape 3).
- Build : artefacts Gradle redirigés hors T7 (`~/.cache/mon-mois-clair/`) + tâche `purgeAppleDouble` (fichiers `._*` qui cassaient la compilation).

**Tests validés :** `./gradlew :app:testDebugUnitTest` → **14/14 OK**
- `MonthDayClampTest` (7) — fév. 28/29, avril 30, janvier 31, rejet hors plage.
- `ConvertersTest` (3) — round-trip enums + rejet string inconnue.
- `MutatingRepositoryTest` (4) — persistance + clamp 31→28, rollback sans backup/reconcile, skip reconcile, `ExpenseStatus` via Room.

**Vigilance sécurité / fiabilité :**
- Aucun chiffrement Keystore sur l’export (conforme B).
- `backupHealth` initialisable (`NOT_CONFIGURED`) via callback Room `onCreate`.
- Mutations uniquement via `MutatingRepository` (pas d’écriture DAO « sauvage » dans les use cases futurs).
- Volume T7 : toujours `COPYFILE_DISABLE=1` ; ne pas committer de `._*`.

**Prochaine sous-étape :** 1.2 — `SafBackupStore.writeRotatedBackup` (delete cible avant rename, sync disque), `BackupDocument` + SHA-256, `BackupWorker`, remontée `BackupHealth.BROKEN`/`OK`.

---

## Modèle d’entrée (à dupliquer à chaque fin d’étape)

```markdown
### YYYY-MM-DD — Étape X.Y terminée

**Codé :**
- …

**Tests validés :**
- …

**Vigilance sécurité / fiabilité :**
- …

**Prochaine sous-étape :** …
```
