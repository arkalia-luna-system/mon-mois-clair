# Charte & Architecture — Mon Mois Clair

Document de référence. Toute évolution technique doit rester conforme à ce fichier.  
Mise à jour : 2026-10-19.

---

## 1. Vision

Application **locale, privée, hors-ligne** pour gérer les paiements mensuels de façon plus précise qu’un bloc-notes, sur **Samsung Galaxy S25 (One UI)** uniquement.

- Zéro connexion bancaire, zéro serveur tiers.
- Saisie manuelle uniquement.
- Distribution : **APK sideload familial** (hors Play Store).
- iPad / PWA / frameworks hybrides : **abandonnés définitivement**.

### Stack figée

| Couche | Choix |
|--------|--------|
| Langage / UI | Kotlin + Jetpack Compose |
| Persistance | Room (SQLite) |
| Async | Coroutines + Flow |
| DI | Hilt |
| Fuseau | `Europe/Brussels` |
| Package | `be.monmoisclair.app` |

---

## 2. Décisions verrouillées (A–E)

| ID | Décision |
|----|----------|
| **A** | Kotlin natif + Room + Jetpack Compose. Aucune surcouche hybride. |
| **B** | SQLite source de vérité + auto-backup JSON clair vers dossier Documents (SAF) à chaque mutation. |
| **C** | Rappels via `AlarmManager` exact (`USE_EXACT_ALARM`) + `BootReceiver` + exemption batterie + écran Santé. Fuseau `Europe/Brussels`. |
| **D** | Sideload APK familial. |
| **E** | Ouverture directe sans code ; biométrie **optionnelle** dans les réglages. |

---

## 3. Spécifications fonctionnelles & ergonomie

- **Reste-à-vivre** (header sticky) : `total revenus − total sorties prévues` + montant encore à payer (`status = TODO`).
- **Liste chronologique unique** (1 → dernier jour du mois) : revenus (+) et dépenses (−).
- **FAIT** : la ligne reste en place, passe en vert doux, badge « FAIT ✓ ». Pas de disparition / réordonnancement.
- **Double rappel** (dépenses non FAIT) : J−3 à 10:00 et Jour J à 09:00 (`Europe/Brussels`).
- **Accessibilité** : thème clair fort contraste (fond crème papier), texte ≥ 18 sp, zones tactiles ≥ 48 dp.
- **Droit à l’erreur** : toucher une ligne → **modifier** ou **supprimer**. Si l’instance a un `templateId`, proposer « Appliquer aussi aux mois suivants ? ».

---

## 4. Persistance & sauvegarde (B)

### Source de vérité
- Room / SQLite, écriture dans une **transaction** à chaque mutation via `MutatingRepository`.

### Export JSON (clair)
- **Pas** de chiffrement Keystore (clé perdue si téléphone cassé).
- **Pas** de mot de passe (oubli + données non bancaires).
- Fichier JSON avec `schemaVersion` + hash **SHA-256** du payload (intégrité à l’import).

### Auto-backup SAF
1. Au premier lancement (configuration) : `ACTION_OPEN_DOCUMENT_TREE` + `takePersistableUriPermission()`.
2. Après **chaque** transaction Room réussie : mise à jour du backup dans ce dossier (WorkManager / IO).
3. Rotation de **3 versions** : `mon-mois-clair-backup-1.json` (récent) … `-3.json`.
4. Écriture **atomique** :
   - Écrire `mon-mois-clair-backup.tmp`
   - `flush()` + `fd.sync()` avant fermeture du flux
   - Rotation : pour chaque étape (`2→3`, `1→2`, `tmp→1`) : **`findFile(target)?.delete()`** puis rename/copy (SAF : `DocumentFile.renameTo()` ne remplace pas proprement un fichier existant).
5. Échec I/O → remonter dans `AppMeta.backupHealth` (jamais de `catch` silencieux). Le dossier app-private seul est **insuffisant**.

---

## 5. Rappels (C)

- Manifest : `USE_EXACT_ALARM`, `POST_NOTIFICATIONS`, `RECEIVE_BOOT_COMPLETED`, `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`.
- Moteur : `AlarmManager.setExactAndAllowWhileIdle` + reprogrammation au boot.
- `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` **ne remplace pas** Device Care Samsung → écran « Santé des rappels » obligatoire.
- **BroadcastReceiver** (`BootReceiver`, `ReminderReceiver`) :
  ```kotlin
  val pendingResult = goAsync()
  CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
      try { /* Room / reconcile */ }
      finally { pendingResult.finish() }
  }
  ```

---

## 6. Modèle de données

### Principe
**Templates** (modèles récurrents) ≠ **Instances** (occurrences mensuelles).  
Modifier un template ne réécrit **jamais** l’historique des mois déjà générés.

### Enums stricts (pas de String libres)

```text
ExpenseStatus : TODO | DONE
BackupHealth  : OK | BROKEN | NOT_CONFIGURED
ReminderKind  : J3 | JD
```

Stockés en Room via `TypeConverter`.

### Tables

- `month_ledgers` — id = `"yyyy-MM"`
- `income_templates` / `expense_templates`
- `income_instances` / `expense_instances` (copie figée label/montant/jour à la génération)
- `app_meta` — `schemaVersion`, `safTreeUri`, `onboardingDone`, `biometricEnabled`, `lastBackupAtEpochMs`, `lastBackupChecksumSha256`, `backupHealth`

### Règles métier

1. **Génération de mois** (lazy à l’ouverture) : créer le ledger courant s’il manque ; pour chaque template `active`, créer des instances ; **ne jamais régénérer** un mois déjà créé.
2. **Mois passés** : pas de lecture seule stricte — toggle `TODO` ↔ `DONE` autorisé (oubli de cocher le 28).
3. **Clamp fin de mois** : `dayOfMonth` instance = `min(template.dayOfMonth, lengthOfMonth)` (ex. 31 → 28 en février non bissextile).
4. **Montants** : centimes entiers (`Long`), jamais de `Float`/`Double`.
5. **Ponctuel** : `templateId = null`, non reconduit.
6. **Édition / suppression** :
   - Instance seule, ou
   - Instance + template + (option) instances des mois suivants encore `TODO` / non encore générés selon le dialogue.

### Calculs

- `remainingCents = sum(incomes) − sum(expenses)`
- `stillToPayCents = sum(expenses where status == TODO)`

---

## 7. Navigation UI (contrats)

```text
OnboardingSaf
OnboardingPermissions
Home
Month(ledgerId)
ReminderHealth
Settings
AddEntry
EditEntry(id, isExpense)    ← droit à l’erreur
RestoreBackup
```

Use cases associés : `UpdateInstanceUseCase`, `DeleteInstanceUseCase` (option « Appliquer aussi aux mois suivants ? » si `templateId != null`).

---

## 8. Qualité & sécurité du code

- Typage fort (`enum` + `TypeConverter`), pas de status en String libre.
- Aucun `catch (e: Exception) {}` silencieux.
- Erreurs SAF → `backupHealth` + UI.
- Chaque brique logique livrée avec **tests JUnit**.
- Point d’entrée unique des mutations : `MutatingRepository.mutate`  
  Ordre : **transaction Room → backup enqueue → reconcile rappels**.

---

## 9. Pre-Mortem (risques retenus)

1. URI SAF révoqué → banner Santé + re-sélection dossier.
2. Device Care Samsung malgré exemption batterie → hint permanent.
3. Écriture backup non atomique / `renameTo` SAF → delete explicite + `sync()` + rotation 3.

---

## 10. Plan d’implémentation (rappel)

| Étape | Contenu |
|-------|---------|
| **1.1** | Room + TypeConverters + DAOs + DB + `MonthDayClamp` + `MutatingRepository` + tests ✅ |
| **1.2** | SAF persist + rotation 3 + checksum + sync disque ✅ |
| **1.3** | Restore depuis backup ✅ |
| **2.1** | Cycle mensuel (`MonthCycleEngine`) ✅ |
| **2.2** | Toggle FAIT (mois courant + précédent) ✅ |
| **2.3** | AlarmManager + BootReceiver + Santé rappels (API) ✅ |
| **2.4** | Édition / suppression instances (+ option mois suivants) ✅ |
| **3.1** | Onboarding SAF + permissions + écran Santé ✅ |
| **3.2** | UI Home liste chronologique + reste-à-vivre ✅ |
| **3.x** | Settings biométrie optionnelle / polish |

Aucune étape n’est « terminée » tant que `PROGRESS_LOG.md` n’est pas à jour.

---

## 11. Build sur volume T7

Le disque externe peut générer des fichiers AppleDouble `._*` qui font échouer AGP/Kotlin.  
Règles opératoires : variable d’environnement `COPYFILE_DISABLE=1`, `buildDir` local (`~/.cache/mon-mois-clair/`), tâche Gradle `purgeAppleDouble` avant compile/ksp.
