# Journal de bord — Mon Mois Clair

Règle : aucune étape n’est considérée comme terminée tant que ce fichier n’est pas mis à jour.

---

## État global

| Champ | Valeur |
|-------|--------|
| Dernière étape terminée | **1.3** |
| Étape en cours | *(aucune)* |
| Prochaine sous-étape | **2.1** — `MonthCycleEngine.ensureCurrentMonthExists` + clamp + pas de doublon |

---

## Journal

### 2026-10-19 — Initialisation projet

- Charte + journal ; rectifications EditEntry / SAF / `goAsync`.

### 2026-10-19 — Étape 1.1 terminée

Room + enums + DAOs + `MonthDayClamp` + `MutatingRepository`. Tests 14/14.

### 2026-10-19 — Étape 1.2 terminée

SAF auto-backup, rotation 3, SHA-256, WorkManager, `backupHealth`. Tests 29/29.

### 2026-10-19 — Étape 1.3 terminée

**Codé :**
- `BackupRestorer` : `restoreFromLatest()`, `restoreFromUri()`, `restoreFromDocument()`.
- Validation **avant** mutation : `schemaVersion` + SHA-256 (rejet → DB inchangée).
- Remplacement atomique Room (`withTransaction` : clear enfants→parents→meta, puis insert).
- **Préservation de l’URI SAF locale** du téléphone (ignore `safTreeUri` du fichier backup).
- Succès → `backupHealth=OK`, meta checksum/date, `reminderScheduler.reconcileAll()`, `backupScheduler.enqueueImmediate()`.
- `RestoreResult` scellé : Success / InvalidChecksum / UnsupportedSchema / IoError / NotConfigured.
- DAOs : `clearAll*` + `insertAll` / `upsertAll` pour le restore.

**Tests validés :** `./gradlew :app:testDebugUnitTest` → **34/34 OK**
- `BackupRestorerTest` (5) :
  - replace data + conserve SAF local + reconcile/backup
  - checksum invalide → DB intacte
  - schema 99 → DB intacte
  - restoreFromUri fichier local
  - restoreFromLatest sans SAF → NotConfigured
- + régression 1.1 / 1.2 (29)

**Vigilance sécurité / fiabilité :**
- Jamais d’import « best-effort » partiel (une transaction ou rien).
- URI SAF d’un autre appareil **non réappliquée** (évite backupHealth cassé après changement de téléphone).
- Échec transaction → `BROKEN` + message I/O explicite.

**Prochaine sous-étape :** 2.1 — moteur de cycle mensuel (`MonthCycleEngine`) : génération lazy Template→Instance, clamp fin de mois, idempotence.

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
