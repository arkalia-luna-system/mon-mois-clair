# Audit — « Préparer un résumé » (PDF / partage mois)

| Champ | Valeur |
|-------|--------|
| Date | 2026-11-02 |
| Branche | `develop` @ ~`0.1.0-7.01` |
| Domaine | Résumé mois (texte + PDF share) |
| Déclencheur | Smoke Athalia — impression « pas assez performant / ne marche pas assez bien » |
| Nature | Inspection code + logcat device (SM-S938B) · **pas** smoke doigt Athalia |

---

## 1. Parcours UI exact

| Entrée | Chemin | Action réelle |
|--------|--------|----------------|
| **Mes dossiers** | Accueil → Menu / Dossiers → bas de page « Résumé à montrer » | Boutons **Préparer un résumé** (texte) + **PDF du résumé** → sheet système |
| **Réglages** | Menu → Réglages → Données & sauvegarde → **Préparer un résumé** (si déficit **ou** ≥1 dossier) | Écran `SettingsSummaryScreen` : **texte + QuietLink « Ouvrir Dossiers » seulement** — **aucun** bouton partage |
| Accueil | — | Pas de CTA résumé |

**VÉRIFIÉ (inspection)** : `SettingsViewModel.shareMonthSummary` / `shareMonthSummaryPdf` existent mais **ne sont branchés nulle part** dans Compose. Seul `DossiersScreen` appelle les shares.

---

## 2. Code (carte)

| Pièce | Fichier | Rôle |
|-------|---------|------|
| Texte | `PrepareMonthSummaryUseCase` | Mois calendaire (`AppClock.currentYearMonth`), revenus/factures/dossiers/épargne |
| PDF | `MonthSummaryPdfWriter` | A4 18/22 pt, watermark « données sensibles », clair volontaire |
| Cache | `MonthSummaryPdfShare` | `cache/share/mon-mois-clair-resume.pdf`, TTL **2 min**, `wipeShareCache` |
| Share intents | `ShareFileIntents.pdf` + ClipData + `FLAG_GRANT_READ_URI_PERMISSION` | FileProvider `.fileprovider` / `cache-path share/` |
| VM | `DossiersViewModel`, `SettingsViewModel` | PDF write sur `Dispatchers.IO` ; **prepare** sur scope Main |
| Wipe | `MainViewModel.onAppBackgrounded` ← `MainActivity` `ON_STOP` | `wipeShareCache` **immédiat** + clipboard + `view/` |

---

## 3. Problèmes — preuves & priorités

### P0 — Share PDF cassé par wipe ON_STOP

| | |
|--|--|
| **Hypothèse** | Au `startActivity(chooser)`, l’activité passe `ON_STOP` → `wipeShareCache` **supprime** le PDF (et `.mmc`) **pendant** que le sheet / l’app cible lit l’URI FileProvider. |
| **Preuve** | `MainActivity` L152–155 → `MainViewModel.onAppBackgrounded` L141–143 `MonthSummaryPdfShare.wipeShareCache` ; PRIVACY/SECURITY documentent ce wipe immédiat. Commentaires VM disent encore « wipe au lock » (désalignés). |
| **Device** | SM-S938B connecté ; app installée 7.01 ; logcat sans crash dump sur ce flux (échec = fichier manquant côté cible, souvent silencieux). **NON VÉRIFIÉ** doigt Athalia. |
| **Impact** | « PDF du résumé » / copie `.mmc` **semblent** lents ou « ne marchent pas ». |

### P0 — Réglages « Préparer un résumé » = cul-de-sac

| | |
|--|--|
| **Preuve** | `SettingsComfortPages.SettingsSummaryScreen` : titre + copy + QuietLink Dossiers ; **pas** `shareMonthSummary` / PDF ; **pas** `SettingsShareEffect`. |
| **Impact** | 60+ : tap hub → écran qui **ne prépare rien**. Impression « ça ne marche pas ». |

### P1 — Contenu résumé incomplet vs charte Accueil

| | |
|--|--|
| **Preuve** | `PrepareMonthSummaryUseCase` : « Reste après factures » **seulement** si déficit ; **pas** de ligne « À payer » / total FAIT ; pas de total revenus. Charte sticky = Reste + À payer (+ épargne à part — déjà présent). |
| **Impact** | PDF « vide d’utilité » pour médiation même si techniquement non vide. |

### P1 — Perf / main-thread / gros mois

| | |
|--|--|
| **Preuve** | `getAllIncomes()` + `getAllExpenses()` puis `filter { ledgerId }` (pas de query ledger suspend) ; `prepareMonthSummary()` aussi à **chaque** `SettingsViewModel.refresh()` (ON_RESUME) juste pour `isDeficit` ; Dossiers/Settings : prepare hors `withContext(IO)` (Room suspend aide, mais charge + `listDossiers` sur le chemin critique share). |
| **Impact** | Sensation de lenteur / ANR risk sur historique multi-mois. |

### P1 — Erreurs Dossiers silencieuses

| | |
|--|--|
| **Preuve** | `DossiersViewModel.shareSummary*` : `try/finally` **sans** `catch` ni message UI (Settings a flash). `startActivity` Dossiers = `runCatching` avalé. |

### P2 — Mois courant vs sélection Accueil

| | |
|--|--|
| **Preuve** | Use case défaut = `AppClock.currentYearMonth()` ; Accueil a `selectedMonth` indépendant. Résumé **ignore** le mois affiché si ≠ calendrier. |

### P2 — Copy / bio / watermark

| Point | Statut |
|-------|--------|
| Watermark + note « pas .mmc » | **OK** (writer + `PDF_SUMMARY_SHARE_NOTE`) |
| FileProvider `cache/share/` | **OK** (`file_paths.xml`) |
| Bio lock pendant share | Sheet système → ON_STOP → lock si bio ON (attendu) ; wipe P0 aggrave |
| UX copy 60+ Réglages | Cul-de-sac P0 ; Dossiers copy correcte |

---

## 4. Hors scope / limites

- Pas de smoke doigt complet Athalia dans cet audit.
- Pas de rejeu instrumented PDF sur device ici.
- Crypto SQLCipher / MMC1 **hors** périmètre (PDF clair volontaire + TTL).

---

## 5. Plan fix (étape 2)

1. **P0** : ON_STOP → `purgeStale` sur `share/` (TTL 2 min) au lieu de wipe total ; wipe total réservé aux tests / prochain `prepareTarget`. Aligner PRIVACY/SECURITY.
2. **P0** : brancher boutons texte/PDF + `SettingsShareEffect` + busy/erreur sur `SettingsSummaryScreen`.
3. **P1** : en-tête résumé = Reste + À payer (+ Déficit si besoin) ; queries ledger ; prepare sur `Dispatchers.IO` ; catch + message Dossiers.
4. Bump `0.1.0-7.02` · tests · ship `develop`.

---

## 6. Correctifs appliqués (7.02)

| Ticket | Statut | Preuve |
|--------|--------|--------|
| P0 share wipe | **Fait** | `MainViewModel.onAppBackgrounded` → `purgeStale` ; PRIVACY/SECURITY |
| P0 Settings cul-de-sac | **Fait** | `SettingsSummaryScreen` boutons + `SettingsShareEffect` |
| P1 contenu | **Fait** | `PrepareMonthSummaryUseCase` Reste / À payer / FAIT / Total revenus |
| P1 perf | **Fait** | `getIncomesForLedger` / `getExpensesForLedger` ; IO dans VMs + refresh Settings |
| P1 erreurs Dossiers | **Fait** | catch + `summaryMessage` + `reportShareIntentFailed` |
| P2 mois sélectionné | **Non** (volontaire) | Toujours mois calendaire — documenté |

---

## 7. Re-smoke Athalia (résumé)

1. Dossiers → **PDF du résumé** → sheet s’ouvre → envoyer Drive/Mail → fichier **lisible** (Reste, À payer, factures).
2. Même flux **Préparer un résumé** (texte).
3. Réglages → Données & sauvegarde → **Préparer un résumé** → boutons présents (plus seulement « Ouvrir Dossiers »).
4. Gros mois : préparation sans gel ressenti ; retour app après sheet OK.
