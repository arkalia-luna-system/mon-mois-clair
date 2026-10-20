# Journal de bord — Mon Mois Clair

Règle : aucune étape n’est considérée comme terminée tant que ce fichier n’est pas mis à jour.  
Règle push : README / `versionName` / badges = journal + tests Gradle `--rerun-tasks`.

---

## État global

| Champ | Valeur |
|-------|--------|
| Dernière étape terminée | **6.22** (Menu outils + restore photos honnête) |
| Étape en cours | *(aucune)* |
| Prochaine sous-étape | Smoke Galaxy (P3-Q04 / S05) · campagne profils |
| Audit premium | Clos côté code · archivés dans `docs/archive/` · smoke ouvert |

---

## Journal

### 2026-10-20 — 6.22 Stabilisation (absence device)

**Problème :** Menu → Outils trop maigre (doublons) ; restore ZIP pouvait réussir sans remettre les photos ; plafonds mémoire ZIP fragiles ; audits premium encore « vivants » alors que le code est tenu.

**Codé :** Menu outils (Santé / Comment ça marche / Restaurer / Réglages) · `RestoreResult.Success` compte photos écrites + message honnête · sanitization chemins `receipts/` · plafond 45 Mo à l’empaquetage / lecture · tests archive + restore messages · audits déplacés vers `docs/archive/` + `docs/AUDIT_STATUS.md` · suppression `ic_mark` mort.

**Différé :** smoke Galaxy au doigt.

**Tests :** `./gradlew :app:testDebugUnitTest --rerun-tasks` → **152/152** (42 fichiers, 0 échec)  
**Version :** `0.1.0-6.22` · `versionCode` 27

### 2026-10-20 — 6.21 Archive photos (S04)

**Problème :** partage Mail/WhatsApp n’emportait que le JSON ; les photos restaient hors message.

**Codé :** `BackupSecurityArchive` (ZIP JSON + `receipts/*.webp`, plafond 45 Mo) · `ShareBackupUseCase` envoie l’archive · restore URI ZIP → mois + photos best-effort · textes Réglages / Comment ça marche · `BackupSecurityArchiveTest` · suppression doublon `MarkDoneReceiver`.

**Différé :** smoke Galaxy P3-Q04 / S05 (pas rejoué au doigt dans cette étape).

**Tests :** `./gradlew :app:testDebugUnitTest --rerun-tasks` → **148/148** (42 fichiers, 0 échec)  
**Version :** `0.1.0-6.21` · `versionCode` 26

### 2026-10-20 — 6.20 Passe 3 audit + polices

**Problème :** `PASSE-3.md` — ancre modèle qui saute l’année (F01) ; rappel cassé par un jour 31 (R01) ; scroll notif + recherche (F02) ; divers trop retirés (F03) ; lecture Home (V01–V04, Q02) ; restore technique (R02) ; notif sans FAIT (Q01) ; police système (Q03).

**Codé :** `resolveTemplateAnchor` · clamp `ReminderTiming` + boucle isolée + garde exact alarms · titres « Dans 3 jours : … » · action notif FAIT (`ReminderReceiver.ACTION_MARK_DONE` + `markDoneIfTodo`) · Divers `ReducedToZero` · Home (Après divers atténué, 1 teal, recherche vide/phrase, highlight filtré) · restore phrase humaine · Source Serif 4 / Source Sans 3 (OFL).

**Différé alors :** smoke Galaxy P3-Q04 · S04 archive (clos en 6.21).

**Tests :** `./gradlew :app:testDebugUnitTest --rerun-tasks` → **148/148** (42 fichiers, 0 échec)  
**Version :** `0.1.0-6.20` · `versionCode` 25

### 2026-10-20 — 6.19 Suite audit (fin → début)

**Problème (suite) :** photo via app externe (S01) ; snackbar Copié chasse Annuler FAIT (S02) ; Divers sans libellé ouvert/fermé (S06).

**Codé :** aperçu facture **dans** un Dialog Compose (accueil + fiche édition, plus d’`ACTION_VIEW`) · `faitUndoActive` protège le bandeau Annuler · « Noter des courses » / « Fermer » visibles.

**Différé :** S03 polices TTF · S04 archive photos · S05 smoke Galaxy.

**Tests :** `./gradlew :app:testDebugUnitTest --rerun-tasks` → **141/141** (41 fichiers, 0 échec)  
**Version :** `0.1.0-6.19` · `versionCode` 24

### 2026-10-20 — 6.18 Audit premium (A–D)

**Problème :** points restants de `docs/AUDIT_PREMIUM_2026-10-20.md` (confiance, système, objet).

**Codé :** F01 mois affiché → ajout · F05 libellés Reste/À payer/Après divers/Déficit · F02 Voir facture + chip Facture · F04 textes honnêtes (copie sans photos) · F06 alerte → Réglages · F07 `EditTemplate` · V03 statut crème · V04 typo ≥18 sp · V05 dialogues A11y · V08 cartes Réglages · V10 filet or · V11 M verrou · V12 QR non-EPC · V13 grammaire · V20 Copier · V22 Divers repliable · V23 sauvegarde hors Santé · `AddEntryUseCaseTest`.

**Déjà OK (6.16–6.17) :** V01, V02 (reste), V06, V07, V09, V14, V15, V17–V19, V21, F03.

**Différé :** V16 polices embarquées (TTF).

**Tests :** `./gradlew :app:testDebugUnitTest --rerun-tasks` → **141/141** (41 fichiers, 0 échec)  
**Version :** `0.1.0-6.18` · `versionCode` 23

### 2026-10-20 — 6.17 Confiance reste / mois / photo

**Problème :** gros chiffre ambigu ; « Après divers » enfoui ; `+ Ajouter` ignorait le mois affiché ; photo non rouverte / trop petite.

**Codé (sans écraser le lot 6.16 collaboratif) :** libellé « Reste après factures » · lignes séparées « À payer » / « Après divers » (titre) · `onAdd(YearMonth)` + `AddEntryUseCase(targetYearMonth)` · aperçu photo via `openUri` (Fit 280–480 dp) · compress 2048/q90 · Settings échéances → `EditTemplate` · tests F01.

**Formule `remainingCents` :** revenus − toutes les factures (réservées) ; divers **hors** formule (indicatif) — inchangé, documenté.

**Tests :** `./gradlew :app:testDebugUnitTest --rerun-tasks` → **141/141** (41 fichiers, 0 échec)  
**Version :** `0.1.0-6.17` · `versionCode` 22

### 2026-10-20 — 6.16 Lacunes premium

**Problème :** photos/notes invisibles ; FAIT primary partout ; Divers sur mois vide ; empty/loading pauvres ; coins 10 dp ; € incohérent ; fréquence non éditable ; scan sans cadre ; réglages sans À propos.

**Codé :** FAIT **secondaire** + haptic + Annuler · notes + chip Photo · Divers masqué si mois vide · empty CTA · loading spinner · 12 dp · titre mois + chevrons icônes · € BE · négatif AccentDanger · recherche · fréquence modèle éditable · TakePicture plein cadre + aperçu · splash A12 · ripple · predictive back · Settings À propos · scan cadre+flash · menu 24 mois chips courts · barre bas 24 dp.

**Différé :** polices custom (TTF), animations riches partout.

**Tests :** `./gradlew :app:testDebugUnitTest --rerun-tasks` → **132/132** (39 fichiers, 0 échec)  
**Version :** `0.1.0-6.16` · `versionCode` 22 *(alignée ensuite en 6.17)*

### 2026-10-20 — 6.15 Marque (complétion)

**Problème :** triangle noir encore perçu ; barre de statut transparente (pas de contraste net) ; trait d’or du mois absent sous le M ; notif à resserrer.

**Codé :** foreground M ivoire + trait `#D99B26` · monochrome Android 13 (M + trait) · `ic_stat_reminder` blanc · `statusBarColor` blanc + `SystemBarStyle.light` · nav crème · `windowBackground` papier · `roundIcon`.

**Tests :** `./gradlew :app:testDebugUnitTest --rerun-tasks` → **131/131** (39 fichiers, 0 échec)  
**Version :** `0.1.0-6.15` · `versionCode` 21

### 2026-10-20 — 6.14 Formes & bordures

**Problème :** coins encore échelonnés (8–20 dp) ; contour de champ trop sombre ; sheets / dialogues pas alignés sur 12 dp.

**Codé :** `AppShapes` à **12 dp** (champs, dialogues, sheets) · `FieldLine` teinte intermédiaire `#8A8278` · `ModalBottomSheet` / `AlertDialog` / `OutlinedTextField` en `shapes.medium` · Permissions/Santé déjà en secondaire · `imePadding` onboarding via `PinnedActionScaffold` · charte inchangée.

**Tests :** `./gradlew :app:testDebugUnitTest --rerun-tasks` → **130/130** (39 fichiers, 0 échec)  
**Version :** `0.1.0-6.14` · `versionCode` 20

### 2026-10-20 — 6.13 Marque

**Problème :** l’icône était un triangle noir. Les rappels réutilisaient ce dessin (illisible dans la barre d’état). L’ouverture flashait le blanc Material.

**Codé :** M ivoire sur teal `#1B4D4E`, version monochrome (Android 13), icône de rappel blanche, fond de fenêtre papier `#F7F4EB`. Le gros chiffre est un peu plus serré.

**Tests :** `./gradlew :app:testDebugUnitTest --rerun-tasks` → **130/130** (39 fichiers, 0 échec)  
**Version :** `0.1.0-6.13` · `versionCode` 19

### 2026-10-20 — 6.12 Parcours fini

**Audit :** le métier est là (mois, reste, FAIT, rappels, sauvegarde, facture papier). Ce qui faisait encore « chantier » : plusieurs boutons teal sur le même écran, Continuer perdu dans le scroll d’installation, échéances hors mois en texte nu, champs au contour trop dur, README figé à ~95 % / étape 5.4.

**Codé :** `PinnedActionScaffold` (Continuer / Choisir dossier épinglés) · Permissions + Santé = corrections en **secondaire** · shapes Material 8–20 dp · Home « Chargement… » · Scan `imePadding` · échéances hors mois en cartes · README sans barre 95 %.

**Tests :** `./gradlew :app:testDebugUnitTest --rerun-tasks` → **130/130 OK**  
**Version :** `0.1.0-6.12` · `versionCode` 18  
**Reste device :** smoke S25 des écrans 6.10–6.12 + **S26 maman encore ouvert**.

### 2026-10-20 — 6.11 Rythme visuel

**Audit :** serif sur tous les titres (effet brochure), pile de boutons teal, mois « Oct. », pastille « À régler » trop étroite, menu qui annonçait des fonctions futures, verrou qui parlait d’un bouton vert, restauration qui citait un nom de fichier.

**Codé :** serif seulement pour le montant. Boutons secondaires au trait encre, coins 12 dp. Mois en toutes lettres. Cartes sans carré de jour. Aide aérée. Scan aux coins arrondis. Textes plus courts (réglages, menu, restauration, verrou).

**Tests :** `./gradlew :app:testDebugUnitTest --rerun-tasks` → **130/130 OK**  
**Version :** `0.1.0-6.11` · `versionCode` 17

### 2026-10-20 — 6.10 Calme visuel

**Problème :** l’accueil empilait une plaque (nom, titre, chiffre, ligne, mois, alerte). À la saisie, plusieurs gros boutons restaient au-dessus du clavier et le champ disparaissait.

**Codé :** en-tête = mois + un chiffre + une ligne « Reste · à payer ». Marque hors de l’accueil. Barre du bas et Annuler/Supprimer cachés tant que le clavier est ouvert. Le champ actif remonte. Nom et montant en premier. Boutons calés sur 48 dp.

**Tests :** `./gradlew :app:testDebugUnitTest --rerun-tasks` → **130/130 OK**  
**Version :** `0.1.0-6.10` · `versionCode` 16

### 2026-10-20 — 6.9.1 Validation device + prompts profils

**Smoke S25 Ultra (release) :** Reste/À payer · factures avant divers · FAIT le JJ/MM · Remettre/FAIT · Menu calendrier · grille jours 1–31 · Aide « Reste du mois passé » · versionName.

**Bug corrigé :** clavier sur « Montant divers » masquait +/− Divers → `imePadding()` + `adjustResize` + boutons **au-dessus** du champ.

**Docs :** `docs/PROFILS_TESTEURS.md` (Chiant / Null / Perf / Hostile-doux / Fidélité / Senior) + prompts mis à jour.

**Tests :** à recompter Gradle `--rerun-tasks`  
**Version :** `0.1.0-6.9.1` · `versionCode` 15

### 2026-10-20 — 6.9 Étude 2026 relue

**Verdict étude :** la leçon (deux questions, check-off, mois indépendants, pas de graphiques) est bonne. Le « 75 % / 60 jours » n’a pas de source. Le S25 n’est pas la cible (S26 6,3″). L’ocre `#D99B26` en texte est illisible (~2,2:1). Le russet `#B84A27` ne fait pas 7:1.

**Codé :** pastilles FAIT / EN RETARD / Auto à contraste ≥ 7:1 · russet `#8E3418` · grille jours 1–31 · factures avant Courses/divers · flèches mois 48 dp · Menu « Alertes » seulement si notifications ou alarmes exactes manquent · aide « Reste du mois passé » + montant de ce mois seul.

**Refusé :** 3ᵉ bouton Réglages, fiche Modifier/Supprimer/Fermer, bouton libellé « À PAYER », report auto, or en couleur de lettres.

**Tests :** `./gradlew :app:testDebugUnitTest --rerun-tasks` → **130/130 OK**  
**Version :** `0.1.0-6.9` · `versionCode` 14

### 2026-10-20 — 6.8 Fidélité outil (étude 2026)

**Charte §8 :** adopter Safe-to-Spend + check-off + FAIT daté + zone pouce + mois indépendants + palette carnet ; refuser graphiques / banque / report auto / rouge sang.

**Codé :** `DoneDateFormat` · barre basse Menu/+ Ajouter · cartes espacées · FAIT à droite · palette A11yTokens · libellé édition « ce mois-ci seul ».

**Docs testeurs :** quinté (correcteur / visuel / produit / senior / validation) + campagne 6.8.

**Smoke S25 :** FAIT le 20/10 · barre Menu/+ Ajouter · versionName 6.8  
**Tests :** `./gradlew :app:testDebugUnitTest --rerun-tasks` → **127/127 OK**  
**Version :** `0.1.0-6.8` · `versionCode` 13

### 2026-10-20 — 6.7 Home ledger + Menu

**Problème :** sticky « Mon Mois Clair » trop dominant ; Santé / Réglages / tips en bas = fourre-tout, liste noyée.

**Décision charte :** Accueil = ledger uniquement ; **Menu** (haut droite) = calendrier mois + outils + futures fonctions.

**Codé :** sticky léger (marque légende, montant plus grand) · `HomeMenuSheet` · `goToMonth` · plus de boutons Santé/Réglages en bas de liste.

**Tests :** `./gradlew :app:testDebugUnitTest --rerun-tasks` → **125/125 OK**  
**Version :** `0.1.0-6.7` · `versionCode` 12

### 2026-10-20 — 6.6 Réglages + usage année

**Audit hub Réglages :** biométrie (déjà OK, non retestée) · Santé · échéances hors mois · sauvegarde = indispensables ; **Device Care doublé** = redondant → retiré (renvoi vers Santé).

**Scénario année :** Home « Revenir au mois en cours » ; échéances toujours affichées (état vide inclus) ; lignes multi-lignes ; Aide « Sur une année ».

**Charte :** §7 inventaire + frictions année.

**Tests :** `./gradlew :app:testDebugUnitTest --rerun-tasks` → **122/122 OK**  
**Version :** `0.1.0-6.6` · `versionCode` 11

### 2026-10-20 — 6.5 UX après smoke device (hors biométrie)

**Testé device :** Home FAIT/EN RETARD · Ajout · Réglages · Santé · Aide.  
**Amélioré :**
- `PinnedBackScaffold` — **Retour toujours visible** (Santé / Réglages / Aide / Restaurer)
- Formulaire : clavier **Suivant/OK** entre champs · fréquences en `FlowRow`
- Home : couleurs reste / encore à payer · astuce si pas de revenu

**Tests :** **120/120 OK**  
**Version :** `0.1.0-6.5` · `versionCode` 10

### 2026-10-20 — 6.4.1 polish (bugs mineurs)

**Corrigé :** « Aucune communication » en rouge (plus vert) · scan caméra libérée à la sortie · titre lock adapté compact.  
**Tests :** **120/120 OK**  
**Version :** `0.1.0-6.4.1` · `versionCode` 9

### 2026-10-20 — Audit affichages (post-6.4)

**Corrigé :** message « Copié » en vert (plus en rouge erreur) · mois d’échéance en `FlowRow` · SAF/Permissions titres adaptés compact · nav mois ‹ › sur écran court.  
**Vérifié :** zone boutons système toujours via MainActivity ; **120/120** tests.

### 2026-10-20 — 6.4 affichage multi-Galaxy (priorité S26 maman)

**Recherche :** specs Samsung S26 6,3″ FHD+ / S25 Ultra 6,9″ QHD+ ; One UI marges ≥24 dp ; Window Compact &lt;600 dp.

**Règles charte §6 :** zone boutons système **obligatoire** (safeDrawing + tappable + `bottomSystemClearance` 28 dp) **une seule fois** dans MainActivity ; `DisplayFit` compact si hauteur ≤800 dp.

**Codé :** sticky Home adapté ; plus de double marge bas dans LazyColumn ; bandeau L contrasté.

**Tests :** `./gradlew :app:testDebugUnitTest --rerun-tasks` → **120/120 OK**  
**Version :** `0.1.0-6.4` · `versionCode` 8  
**Zone système :** `bottomSystemClearance` 28 dp **conservée** (MainActivity uniquement).

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
