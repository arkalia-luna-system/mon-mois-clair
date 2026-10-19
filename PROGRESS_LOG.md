# Journal de bord — Mon Mois Clair

Règle : aucune étape n’est considérée comme terminée tant que ce fichier n’est pas mis à jour.  
Règle push : README / `versionName` / badges = journal + tests Gradle `--rerun-tasks`.

---

## État global

| Champ | Valeur |
|-------|--------|
| Dernière étape terminée | **6.3.1** (durcissement fiabilité lot 6) |
| Étape en cours | *(aucune)* |
| Prochaine sous-étape | Travail quotidien sur `develop` · smoke S25 (scan/photo/bandeau) |

---

## Journal

### 2026-10-19 — 6.3.1 fiabilité (avant gel `main`)

**Corrigé / complété :**
- Restore : accepte sauvegardes schema **1..3** (plus seulement 3) → meta remontée à 3
- Ajout : bouton **Copier la communication** (manquait, présent seulement en édition)
- Aide « Comment ça marche » : section **Facture papier**
- Bandeau mois prochain : contraste PaperWarm + AccentWarn

**Tests :** `./gradlew :app:testDebugUnitTest --rerun-tasks` → **116/116 OK**  
**Version :** `0.1.0-6.3.1` · `versionCode` 7  
**Branches :** snapshot fiable → `main` ; suite du travail sur `develop`.

### 2026-10-19 — Lot 6.0–6.3 (assistant papiers BE)

**Persona :** factures papier + paiement via app bancaire sur le même S25.

**Codé (sans casser 5.4.1) :**
- **J / T10** : `EpcQrParser` + écran `ScanEpc` (CameraX + ML Kit offline) → préremplit Ajout
- **K / T9** : `paymentNote` + Copier ; photo WebP ≤800px via `ReceiptPhotoStore` sous SAF `receipts/` (chemin relatif seul dans JSON)
- **L** : bandeau Home « À prévoir le mois prochain » (`ListNextMonthPeriodicsUseCase`, intervalle > 1)
- Schema Room / backup **3** (`MIGRATION_2_3`)

**Fix prudence :** typo `EditEntryViewModel` (`when (val result = …)`).

**Tests :** `./gradlew :app:testDebugUnitTest --rerun-tasks` → **115/115 OK**  
**Version :** `0.1.0-6.3` · `versionCode` 6  
**Pas encore :** smoke device lot 6 (caméra / photo / bandeau réel).

### 2026-10-19 — Branches : `develop` actif, `main` gelée

**Règle charte §5 :** travail quotidien sur **`develop`** ; **`main`** seulement sur demande explicite après vérif.  
Branche jetable `wip/6.x-assistant-papiers` remplacée par `develop` (lot 6.3 + 115 tests).  
**CI :** triggers sur `develop` + `main` ; plus de push quotidien sur `main`.

### 2026-10-19 — CI GitHub Actions (phase 1, léger)

**Fait :** workflow `.github/workflows/ci.yml` (assembleDebug + testDebugUnitTest) · Dependabot Actions · `docs/CI.md` (feuille de route phases 2–4) · badge CI README.  
**Vérifié local :** `./gradlew :app:testDebugUnitTest --rerun-tasks` → **107/107** (WIP scan/receipt volontairement hors de ce commit).  
**Fix CI :** `android-actions/setup-android` sans package obsolète `tools` (échec sdkmanager).  
**Pas encore :** lint bloquant, detekt, scan CVE (volontairement pour éviter un mur de corrections).

### 2026-10-19 — Polish familial « parfait maman » (sur 5.4.1)

**UI / confiance :**
- Divers : message d’erreur si montant vide ; champ vidé seulement si OK
- Édition : fréquence affichée + conseil pour la changer
- Lock bio : consignes doigt/code + « Déverrouiller maintenant »
- État vide Home plus guidé
- Aide « Comment ça marche » (déjà 5.4.1)

**Build :** `ksp.incremental=false` (T7) ; WIP receipt/scan incomplets retirés du tree.  
**Tests :** **107/107** · APK release : `dist/MonMoisClair-release.apk`

### 2026-10-19 — Vitrine GitHub + hygiene (privé → public-ready)

**Fait :** licence MIT · `SECURITY.md` · description/topics GitHub · WIP scan QR exclu du tree (cassait le build).  
**Vérifié :** `0.1.0-5.4.1` · **107/107** (HEAD `c11e7a1` + polish docs).

### 2026-10-19 — Validation expert 5.4.1 + push

**Confirmé 5.4 :** F–I + T1–T8 présents ; **104** puis **107** tests après aide.  
**Ajouté :** Réglages → « Comment ça marche » ; `RecurrenceLabel` ; erreurs divers visibles ; info récurrence en édition ; copy lock biométrie plus claire.  
**Claims :** `0.1.0-5.4.1` / `versionCode` 4 / **107/107**.

### 2026-10-19 — Audit propreté + hygiene git

**OK :** claims **5.4** alignés ; Gradle **104/104** ; secrets hors git ; Long cents ; pas de catch vides.

**Nettoyé :**
- AppleDouble `._*` (T7) purgés.
- `FileSystemBackupTree` → `src/test` (runtime = SAF seulement).
- ~215 dumps `dist/verify` **retirés du suivi git** (xml/json/txt/rapports) ; seul `RESET_TESTS_FROM_ZERO.md` versionné.
- CHARTE T1 : libellé Manifest `TIME_SET` clarifié.

### 2026-10-19 — Audit post-5.4 (corrections mineures)

**Trouvé / corrigé :**
- Édition : case **Prélèvement automatique** manquante → sync instance (+ template si « mois suivants »)
- Partage T8 : `ClipData` + grant URI (WhatsApp/Mail)
- Hint Home raccourci (freeze sticky)
- Checklist S25 pointée release `0.1.0-5.4`

**Tests :** `./gradlew :app:testDebugUnitTest --rerun-tasks` → **104/104 OK**

### 2026-10-19 — Étapes 5.0–5.4 terminées (failles 4.x + survie données)

**Freeze produit :** plus aucune nouvelle option sur l’écran d’accueil.

**Codé :**
- **T5** codec JSON manuel + `-keep` backup + test noms de champs (`label`, `amountCents`, …)
- **T6** sélecteur « Prochaine échéance » (mois) si 3 mois / annuel ; instance seulement si matérialisable ; liste Réglages « Échéances hors ce mois »
- **T7** `ReminderReceiver` → notif puis `reconcileAll()` (chaîne OVERDUE one-shot)
- **T8** Réglages : « Envoyer une copie de sécurité (WhatsApp / Mail) » (`ACTION_SEND` + FileProvider)

**Tests validés :** `./gradlew :app:testDebugUnitTest --rerun-tasks` → **104/104 OK**

**Release :** `dist/MonMoisClair-release.apk` · `versionName` `0.1.0-5.4` · `versionCode` 3

**Vigilance device :** ouvrir le JSON backup depuis l’APK **release** (clés lisibles) + restore + partage WhatsApp/Mail.

### 2026-10-19 — Polish Visuel + Produit + Senior (post-audits)

**Aligné avec les 3 rapports testeurs (sans bump version) :**
- Visuel : sticky allégé, IME form, dialogs crème (`surfaceContainer*` + `containerColor`)
- Produit : statut `BackupHealth` live dans Réglages ; légende « Mois en cours » ; hints reste/backup/rappels/SAF/prélèvement
- Senior : Device Care 1-2-3 ; « Vérifier les alertes » ; « Remettre à payer » ; warning Restore ; tip FAIT

**Tests :** `./gradlew :app:testDebugUnitTest --rerun-tasks` → **104/104 OK**  
**Rapports campagne :** supprimés après intégration (prompts `docs/PROMPT_TESTEUR_*` conservés).

### 2026-10-19 — Étapes 4.0–4.6 terminées (essentiels + failles Android)

**Codé :**
- **F** EN RETARD (badge) + alarme `OVERDUE` quotidienne 10h + deep link notif → ligne mise en évidence
- **G** libellé « Reste après factures » + Courses / divers (+/−) sur le ledger
- **H** case « Prélèvement automatique » + textes notif adaptés + pastille Auto
- **I** fréquence Une fois / Mensuel / 3 mois / 1× an (`intervalMonths` + `anchorYearMonth`)
- **T1** `MY_PACKAGE_REPLACED` + `TIME_SET` + `TIMEZONE_CHANGED` → `reconcileAll()`
- **T2** keystore local (`keystore.properties` + `.jks` gitignorés) + `assembleRelease` R8
- **T3** `FLAG_SECURE` si biométrie ON
- **T4** confirmation FAIT sur mois passé uniquement
- Migration Room **v2** + backup schema 1|2

**Tests validés (à la clôture 4.6) :** **95/95** · suite courante : voir entrée polish ci-dessus (**104/104**)

**Release :** `dist/MonMoisClair-release.apk` (signé, minify) · `versionName` `0.1.0-4.6` · `versionCode` 2

**Vigilance :** sauvegarder le `.jks` hors git ; ne plus installer un debug.apk sur le téléphone famille (signature incompatible).

**Prochaine sous-étape :** smoke device (retard / divers / prélèvement / update APK sans reboot).

### 2026-10-19 — Validation expert (push 3.4–3.5.1)

**Confirmé :** rapport testeur hub/bio/wrap Suiv. ; overlay bio ; **décision E**.  
**Bug calendrier/mois (retour humain) :** Préc. grisé sans ledger passé → fix `MonthNavigation` + `ensureMonthExists` au tap ; titres FR testés.  
**Tests :** **88/88** · device Oct→Sept→Oct OK · `versionName` `0.1.0-3.5.1`.  
**Réserve acceptée :** unlock empreinte bout-en-bout (prompt OK, confirm humain).

### 2026-10-19 — Audit global + patch 3.5.1

**Confirmé device + code :** Home / Santé / hub / édition / marges ; overlay bio NavHost.  
**Rapport :** `dist/verify/RAPPORT_TEST_2026-10-19_AUDIT_3.5.md` (si présent) + `RAPPORT_TEST_2026-10-19.md`.

### 2026-10-19 — Testeur-correcteur 3.4+3.5 (code + visuel)

Hub Réglages, Home allégé, prompt bio, wrap Suiv. · **84/84** à ce stade.  
**Rapport :** `dist/verify/RAPPORT_TEST_2026-10-19.md`

### 2026-10-19 — Étape 3.5 terminée (hub Réglages)

**Codé :**
- Home allégé : **Ajouter** · **Santé des rappels** · **Réglages** (SAF / Restore sortis du Home).
- Réglages = hub : Confidentialité (biométrie) · Rappels (+ Device Care Samsung + intents batterie / fiche app) · Sauvegarde (dossier + restaurer).
- `DeviceCareCopy` + intents `ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS` / fiche application.

**Tests validés :** `./gradlew :app:testDebugUnitTest --rerun-tasks` → **84/84 OK** (recomptés testeur).

**Device (SM-S938B) :** Home 3 boutons ; Réglages affiche sections Confidentialité / Rappels / Device Care / Sauvegarde.

**Vigilance :** Device Care « Mise en veille » reste une action manuelle One UI (pas d’API publique fiable).

**Prochaine sous-étape :** validation / push.

---

### 2026-10-19 — Étape 3.4 terminée (biométrie optionnelle)

**Codé :** gateway biométrie, Réglages toggle, verrouillage ON_STOP, **82/82** à ce stade.

---

## Modèle d’entrée

```markdown
### YYYY-MM-DD — Étape X.Y terminée
**Codé :** …
**Tests validés :** `./gradlew :app:testDebugUnitTest --rerun-tasks` → N/N OK
**Vigilance :** …
**Prochaine sous-étape :** …
```
