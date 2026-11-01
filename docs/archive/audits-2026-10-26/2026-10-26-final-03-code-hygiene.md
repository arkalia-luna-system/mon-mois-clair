> **ARCHIVÉ** 2026-11-01 — historique figé. Vivant = `docs/audits/2026-11-01-premium-*` + journal.

# Audit hygiène code — Mon Mois Clair (final-03)

| | |
|--|--|
| **Date** | 2026-10-26 |
| **Profil** | Cleanup / maintenabilité — code mort, doublons, god-files, imports, stubs, trous de tests |
| **Portée** | `app/src/main` + `app/src/test` · docs drift · AppleDouble T7 · pas de refonte produit |
| **Mode** | **AUDIT ONLY** — ce fichier seul créé ; **pas de commit** ; Gradle **non** relancé ici |
| **Baseline code** | branche `develop` · `0.1.0-6.82` · `versionCode` 87 · SHA doc `b15fffb` / HEAD local vu `0fad7e5` · claim journal **332/332** (79 XML) |
| **Complète** | [`2026-10-23-nuit-05-ingenierie-android.md`](2026-10-23-nuit-05-ingenierie-android.md) (architecture / perf) — ici focus **hygiène & tâches finales cleanup** |

**Verdict court :** pas de stubs `TODO` / `NotImplemented` dangereux, pas de classes vraiment orphelines. La dette est **structurelle** (2 god-files Compose + miroir Add/Edit) + **opérationnelle** (~112 `._*` hors git) + **filets manquants** sur chemins crypto/partage/À venir. Imports inutiles = petit lot P2 (9 en main). Note hygiène : **6 / 10**.

---

## 1. Inventaire rapide

| Métrique | Valeur |
|----------|-------:|
| Sources `.kt` main (hors `._*`) | **192** |
| Fichiers `*Test.kt` | **79** |
| Ratio fichiers test / main | ~41 % |
| Plus gros fichier | `EntryFormScreen.kt` **1794** L |
| 2ᵉ plus gros | `HomeScreen.kt` **1552** L |
| `androidTest` | **0** |
| Plugins detekt / ktlint / spotless | **Absents** |
| AppleDouble tracked git | **0** (`.gitignore` a `**/._*`) |
| AppleDouble disque (hors `.git`/`.gradle`/`build`) | **~112** (`app/` ~82–84 · `docs/` ~22 · racine ~8) |

---

## 2. God-files (état 6.82)

| Fichier | Lignes | Symptôme | Priorité cleanup |
|---------|-------:|----------|------------------|
| `ui/entry/EntryFormScreen.kt` | **1794** | `AddEntryScreen` + `EditEntryScreen` + `EntryFormScaffold` (~**85** params) + photo/PDF/OCR | **P0** découpe mécanique |
| `ui/home/HomeScreen.kt` | **1552** | Sticky + LazyColumn + rows + chips + highlight | **P0/P1** |
| `ui/entry/EditEntryViewModel.kt` | **790** | État fiche + OCR + IBAN + frais + épargne | **P1** |
| `ui/settings/SettingsPages.kt` | **643** | Plusieurs écrans Réglages dans un fichier | **P2** |
| `ui/entry/AddEntryViewModel.kt` | **605** | Miroir partiel d’Edit (~**21** méthodes homonymes) | **P1** |
| `ui/home/HomeViewModel.kt` | **576** | Timeline + divers/épargne/mail + DAO | **P1** |
| `ui/settings/SettingsViewModel.kt` | **508** | Hub + backup + thème + bio | **P2** |
| Test monolithe `DossierAgreementUseCaseTest.kt` | **696** | OK pour filet ; pas un god-file produit | — |

**Déjà extrait (positif, à ne pas fusionner) :** `HomeCoursesSheet` / `HomeSavingsSheet` / `HomeMenuSheet` / `HomeMonthPickerSheet` · `EntryFormChrome` (`FormSectionToggle`) · formats UI séparés.

**Cible découpe (zéro redesign) :**

1. `EntryFormScaffold.kt` + `entry/sections/{Photo,Payment,Dossier,Recurrence}.kt`
2. `HomeTimelineRows.kt` (`ExpenseRow` / `IncomeRow` / chips)
3. Plus tard : `ReceiptDraftController` partagé Add/Edit (JVM testable)

Benchmark / plan produit listent encore « scission god-files » comme **Pas fait** — aligné.

---

## 3. Code mort / stubs

### 3.1 Ce qui n’est **pas** du code mort

- Aucun `TODO` / `FIXME` / `NotImplementedError` de chantier (les hits `TODO` = enum `ExpenseStatus.TODO`).
- Aucune déclaration top-level réellement orpheline (Hilt / Manifest / receivers / data classes UI « 0 ref externe » = faux positifs).
- `MoneyFormat.formatCents` → délégué `EuroFormat` : **couche volontaire** (domaine vs UI), pas doublon inutile.
- `DoneDateFormat` vs `FeeEventDateFormat` : wrappers fins intentionnels (préfixe « FAIT le » vs date courte).

### 3.2 Surfaces « mortes » / API morte

| Id | Élément | Preuve | Action |
|----|---------|--------|--------|
| **H-DEAD-1** | `DossierRemaining.pacePhrase(…, todoAmounts)` | `@Suppress("UNUSED_PARAMETER")` — param jamais lu | Retirer le param **ou** l’utiliser ; ajuster call sites + tests |
| **H-DEAD-2** | `BiometricAuthGatewayImpl` `negativeButton` | `@Suppress("UNUSED_PARAMETER")` | Documenter contrat API ou supprimer du signature si safe |
| **H-DEAD-3** | Imports Compose orphelins (voir §5) | Nom simple absent du corps | Supprimer (P2, PR triviale) |

### 3.3 Faux « stubs »

Callbacks `onAuthenticationFailed() = Unit` (bio) = pattern Android, **pas** un stub produit.

---

## 4. Doublons / miroirs

| Zone | Nature | Gravité | Cleanup |
|------|--------|---------|---------|
| `AddEntryViewModel` ↔ `EditEntryViewModel` | ~21 méthodes partagées (`applyEpc*`, photo/PDF, `set*`, `save`, OCR, copie note) ; états `EntryFormState` / `EditEntryState` qui se chevauchent | **Élevée** pour merge | Extraire contrôleur brouillon + reducer pur |
| `RecurrenceChoice` dans `AddEntryViewModel.kt` | Utilisé aussi par Edit (couplage fichier) | Faible | Déplacer enum dans `RecurrenceChoice.kt` ou `entry/model` |
| `EuroFormat` / `MoneyFormat` | Délégation claire | Faible | Garder ; éviter d’appeler `EuroFormat` depuis UI (quelques call sites domaine OK) |
| Copy messages (`ShareBackupMessages`, `SafFolderMessages`, `RestoreMessages`, …) | Objets string isolés + tests | Positif | Modèle à réutiliser, pas à fusionner |

Pas de second moteur de cycle / overdue / dossiers détecté — le domaine reste **single source**.

---

## 5. Imports inutiles (heuristique, hors `getValue`/`setValue`)

Vérifié : le symbole n’apparaît **que** sur la ligne `import` (faux positifs Compose delegates exclus).

### Main (9)

| Fichier | Imports à retirer |
|---------|-------------------|
| `ui/home/HomeScreen.kt` | `fadeIn`, `fadeOut`, `Spacer` |
| `ui/settings/SettingsScreen.kt` | `Spacer`, `height` |
| `ui/settings/SettingsPages.kt` | `Spacer`, `height` |
| `ui/restore/RestoreBackupScreen.kt` | `Column` |
| `ui/scan/ScanEpcScreen.kt` | `Color` |

### Test (6)

| Fichier | Imports à retirer |
|---------|-------------------|
| `domain/MonthCycleEngineTest.kt` | `java.time.Instant` |
| `domain/usecase/PrepareMonthSummaryUseCaseTest.kt` | `BackupScheduler`, `ReminderScheduler`, `MutatingRepository` |
| `data/reminder/ReminderTimingTest.kt` | `Instant`, `ZonedDateTime` |

**Pas de detekt/ktlint** → ces imports ne sont pas captés en CI. Tâche finale : PR « chore: unused imports » **ou** ajouter Spotless/ktlint (optionnel, hors scope famille).

---

## 6. AppleDouble & hygiene disque (T7)

| Fait | Détail |
|------|--------|
| `.gitignore` | `**/._*` + `.DS_Store` — **OK** (0 tracked) |
| `scripts/git-safe.sh` | `COPYFILE_DISABLE=1` + purge `._*` **dans `.git` seulement** |
| Working tree | **~112** sidecars (`._EntryFormScreen.kt`, `._CI.md`, `._README.md`, …) |
| Risque | Bruit IDE / outils ; journal nuit-05 évoquait compilation si sources non exclues — Gradle ignore en pratique si non listés, mais le disque reste sale |

**Tâche finale H-OPS-1 :**  
`find app docs . -maxdepth 3 -name '._*' -type f -delete` (hors `.git`) + éventuellement étendre `git-safe.sh` à `app/`/`docs/` avant commit. README a déjà `export COPYFILE_DISABLE=1`.

---

## 7. Tests manquants **critiques** (hygiène filet)

Journal claim **332/332** — **non rejoué** dans cet audit. Domaine / backup / toggles restent solides. Trous ci-dessous = **chemins famille ou crypto** sans filet nommé.

### 7.1 P0 / P1 (prioriser avant grosse découpe UI)

| Id | Zone | État | Pourquoi critique |
|----|------|------|-------------------|
| **H-TEST-1** | `ListUpcomingMonthsUseCase` | **0** référence test | Écran « À venir » lecture seule ; régression silencieuse post-refactor templates |
| **H-TEST-2** | `ShareBackupUseCase` (enveloppe `.mmc`) | Messages testés ; **pas** le use case | P1 crypto 6.81 — partage WhatsApp = surface confiance famille |
| **H-TEST-3** | `KeystoreAesKey` / `DbPassphraseVault` / `ShareSecretVault` | Couverture **indirecte** (`EncryptedDbMigratorTest`, restorer) | Pas de tests unitaires dédiés wrap/unwrap / rotation secret |
| **H-TEST-4** | `ReminderReceiver` (FAIT + UNDO) | Non testé | Action notif sans ouvrir l’app — race / double toggle |
| **H-TEST-5** | Migrations Room **1→8** | Seulement **7→8** Robolectric custom (`RoomMigrationTest`) | Sideload upgrade long historique |
| **H-TEST-6** | `androidTest` / Compose | **0** | God-files UI sans filet ; smoke = humain seul |

### 7.2 P2 (utile, moins bloquant cadeau)

| Id | Zone | Note |
|----|------|------|
| **H-TEST-7** | `UpdateExpenseTemplateUseCase` | Couvert surtout via `UpdateDeleteEntryUseCaseTest` / dossiers — pas de classe dédiée |
| **H-TEST-8** | `AgreementFuturesSync` | Indirect via `DossierAgreementUseCaseTest` — OK partiel ; extraire cas purs si on touche accords |
| **H-TEST-9** | `ReceiptCommunicationOcr` | OCR appareil ; mockable partiel seulement |
| **H-TEST-10** | ViewModels Accueil / Fiche | 0 test — attendre extraction `TimelineBuilder` / reducer |

### 7.3 Bien couvert (ne pas casser)

Domaine (cycle, overdue, dossiers, EPC, `+++`, épargne, formats, contraste) · `MutatingRepository` · backup codec/restorer/rotator/security/envelope/AES · `LocalFileCipher` · `ReminderSchedulerImpl` · widget **texte** + contrat layout · pending letters (fichier unique).

---

## 8. Drift docs (hygiène claims)

| Surface | Écart | Action cleanup |
|---------|-------|----------------|
| Badge README `tests-329/329` + `version-0.1.0--6.81` | Tableau statut dit **332/332** · `0.1.0-6.82` | Aligner badges (P0-DOC) |
| `BENCHMARK_QUALITE_2026.md` « état code 6.80 » | Code à **6.82** | Bump date/état quand on retouche le benchmark |
| Audit nuit-05 lignes ~1894 / 1544 | Désormais **1794** / **1552** | Normal (polish) ; ce fichier = nouvelle référence |

---

## 9. Tâches finales cleanup (backlog ordonné)

Critère : **comportement inchangé** pour maman ; tests unitaires verts après chaque PR.

### Vague A — sans risque produit (1–2 h)

| # | Tâche | Effort |
|---|-------|--------|
| A1 | Supprimer les **15** imports morts (§5) | XS |
| A2 | Purger AppleDouble working tree + étendre `git-safe.sh` | XS |
| A3 | Aligner badges README **332** / **6.82** | XS |
| A4 | Retirer `todoAmounts` mort dans `pacePhrase` (+ tests) | S |

### Vague B — filets critiques (½–1 j)

| # | Tâche | Effort |
|---|-------|--------|
| B1 | Tests `ListUpcomingMonthsUseCase` (revenus / intervalle / accords) | S |
| B2 | Tests `ShareBackupUseCase` (dossier manquant · pack · magic MMC) avec fakes vault/tree | M |
| B3 | Tests vault passphrase / share secret (Robolectric + fallback software Keystore) | M |
| B4 | MigrationTest **1→8** (ou chaîne 1→2…→8) en plus de 7→8 | M |

### Vague C — structure (1–3 j, après smoke doigt)

| # | Tâche | Effort |
|---|-------|--------|
| C1 | Découpe mécanique `EntryFormScreen` (move-only) | L |
| C2 | `HomeTimelineRows.kt` | M |
| C3 | `ReceiptDraftController` / reducer Add↔Edit | L |
| C4 | Optionnel : Spotless ou ktlint unused-import en CI | S |

### Explicitement **hors** cleanup final

- Redesign UI / Glance widget / multi-module Gradle.
- Instrumentation Compose massive avant C1–C2 (sinon flaky sur API monstre).
- Rescan Dependabot (autre piste ; pas hygiène source).

---

## 10. Matrice priorité hygiène

| Id | Sujet | P | Impact |
|----|-------|:-:|--------|
| **H-P0-1** | God-file `EntryFormScreen` | P0 | Merge / régression fiche |
| **H-P0-2** | Drift badges README 329/6.81 | P0-DOC | Claims faux en vitrine |
| **H-P1-1** | God-file `HomeScreen` + VM | P1 | Accueil = surface critique |
| **H-P1-2** | Miroir Add/Edit VM | P1 | Double maintenance |
| **H-P1-3** | Tests Share / Upcoming / vaults | P1 | Crypto + À venir |
| **H-P1-4** | AppleDouble disque | P1-OPS | Bruit T7 |
| **H-P1-5** | Migration 1→8 + receiver notif | P1 | Sideload / FAIT notif |
| **H-P2-1** | Imports inutiles | P2 | CI hygiene |
| **H-P2-2** | Param `todoAmounts` mort | P2 | API claire |
| **H-P2-3** | `SettingsPages` découpe | P2 | Quand on y retouche |

---

## 11. Note hygiène : **6 / 10**

| Critère | Note | Commentaire |
|---------|-----:|-------------|
| Code mort / stubs | 8 | Quasi aucun ; params morts mineurs |
| Doublons | 5 | Add/Edit = vrai coût |
| God-files | 3,5 | Toujours le risque n°1 vélocité |
| Imports / lint auto | 5 | Petit lot ; pas de linter |
| Tests critiques | 6,5 | Domaine fort ; trous crypto-partage / À venir / UI |
| Ops disque (T7) | 5 | gitignore OK ; working tree sale |

**Pourquoi pas plus haut :** god-files + miroir VM + filets crypto/partage incomplets.  
**Pourquoi pas plus bas :** pas de cimetière de stubs ; ignore AppleDouble correct ; domaine et backup déjà disciplinés ; `git-safe.sh` existe.

---

## 12. Méthode (reproductible)

- Comptage lignes `find app/src -name '*.kt' ! -name '._*' | xargs wc -l`
- Heuristique imports : symbole absent du corps après strip commentaires (délégués Compose exclus)
- Références use cases / crypto croisées `app/src/test`
- AppleDouble : `find` hors `.git` / `.gradle` / `build` ; `git ls-files '**/._*'` → 0
- Aucune exécution Gradle dans cette passe (règle audit-only)

---

*Fin audit hygiène — aucune modification hors ce fichier ; pas de commit.*
