# Journal de bord — Mon Mois Clair

Règle : aucune étape n’est considérée comme terminée tant que ce fichier n’est pas mis à jour.  
Règle push : README / `versionName` / badges = journal + tests Gradle `--rerun-tasks`.

---

## État global

| Champ | Valeur |
|-------|--------|
| Dernière étape terminée | **6.60** (passe premium densité / typo / rythme crème) |
| Étape en cours | — |
| Prochaine sous-étape | Smoke doigt S25 Ultra + S26 (checklist retour) |
| Audit premium | Matrice H/MEN/SET/FORM/… vérifiée code · **284/284** |
| Règle agents | **Un seul Gradle** à la fois (sinon `R` / merge resources cassés). Prefer `./scripts/git-safe.sh` pour push T7 |

---

## Journal

### 2026-10-22 — 6.60 Passe premium densité / typo / rythme

**Vu :** Paiement & preuve ouvert = mur de Secondary ; CheckRow / À venir / empty dossiers encore « boîtes » ; titres et Secondary au même poids ; tips busy en cartes.

**Codé (visuel seulement) :**
- FORM : sections `FormSectionToggle` (titleLarge) ; Secondary = QR / photo / PDF seulement ; Copier / Voir / Enlever / OCR / IBAN = `QuietLinkA11y` ; CheckRow sans carte
- Secondary global : contour PaperBorder + poids Medium (plus discret que titres 23 sp)
- À venir / Santé / empty dossiers / chargements : filets ou typo InkMuted (plus de tip-box hors états)
- Dossiers lignes : ombre 2 dp papier ; pas de grain overlay
- Anti-régression audit : sticky H01, Courses uniques, DOS sans KPI, Restaurer Danger, Fermer photo Secondary, StampShape, insets MainActivity inchangés

**Tests :** `./gradlew :app:testDebugUnitTest --rerun-tasks` → **284/284** (67 fichiers).  
**Version :** `0.1.0-6.60` · `versionCode` 65.  
**Pas un claim « premium terminé »** : smoke doigt / S26 restent ouverts.

### 2026-10-22 — 6.59 FORM01 + SET01

**Vu :** FORM01 PARTIEL (6.58) — page fiche encore plate ; Réglages = 8 cartes clones PaperCard.

**Codé (visuel seulement) :**
- FORM01 : rythme objet papier — page qui/combien/jour · « Paiement & preuve » repliable (QR / +++ / photo-PDF) · « Retard ou accord » après, déjà expansible ; CTA photo/PDF d’ajout seulement si absents, sinon via le bloc pièce ; tips InkMuted ; 1 Primary Enregistrer
- SET01 : Réglages en filets + texture page (plus de Surface clone par section) ; navigation inchangée

**Tests :** `./gradlew :app:testDebugUnitTest --rerun-tasks` → **284/284** (67 fichiers).  
**Assemble :** `:app:assembleDebug` OK.  
**Version :** `0.1.0-6.59` · `versionCode` 64.  
**Pas un claim « premium terminé »** : smoke doigt / S26 restent ouverts.

### 2026-10-22 — 6.58 Audit exhaustif + last tip-boxes

**Vu :** claims 6.56/6.57 OK sur Accueil/Menu/Scan/Widget ; restaient tip-boxes hors états (édition échéance, dossiers courriers, empty fiche) et « Fermer » photo encore Primary ; dialogs PaperWarm vs PaperCream.

**Codé (visuel seulement) :**
- SYS01 : tips EditTemplate / empty missing / Courriers sans plan / Convert missing → InkMuted sans boîte
- Dialogs photo fiche : Fermer = Secondary ; PaperCream (restore, settings, fiche, modèle)
- WDG02 **ACCEPTÉ** : RemoteViews = police système (pas Source Sans)
- FORM01 **PARTIEL** : sans boîte-dans-boîte OK ; regroupement profond différé

**Tests :** `./gradlew :app:testDebugUnitTest --rerun-tasks` → **284/284** (67 fichiers).  
**Version :** `0.1.0-6.58` · `versionCode` 63.  
**Pas un claim « premium terminé »** : smoke doigt / S26 / FORM01 profond restent ouverts.

### 2026-10-22 — 6.57 Suite audit (Accueil / Réglages / micro)

**Vu :** quick wins 6.56 OK ; restaient tips tip-box, SCAN calme, SET radio/case, H11/H12, LOCK/LOAD, widget, DOS KPI.

**Codé :**
- H05/H06 : FAIT à droite ; Copier/Photo = liens 2ᵉ rangée ; max 2 pastilles (critique → Auto → Accord)
- H08–H12/H14 : empty Accueil sans carte ; tip recherche sans boîte ; highlight 2 dp ; sticky + socle plus d’air ; snackbar PaperCard ; dialog mois passé PaperCream
- Tips courants → InkMuted (onboarding SAF/Alertes, Alertes santé, À venir, HowItWorks, courrier/versement) ; boîtes = erreur/ok/warn seulement
- SET : Radio + Checkbox teal ; tip Mettre de côté 1 phrase ; Restaurer Danger ; wordmark pied petit
- LOCK01 / LOAD01 : verrou une composition ; chargement brand+texte (pas spinner/carte)
- SCAN01 : cadre ivoire plus calme ; flash crème 0,28 ; deny permanent = warn box
- DOS02 : plus de bandeau KPI dossiers
- WDG01 : padding 20 ; titre/ligne 16 sp, montant 32 sp — **limite RemoteViews** : police système seulement
- SYS03 : outline champs = PaperBorder
- DisplayFit : stickyPaddingVertical 8/14 dp

**Tests :** `./gradlew :app:testDebugUnitTest --rerun-tasks` → **284/284** (67 fichiers).  
**Version :** `0.1.0-6.57` · `versionCode` 62.  
**Pas un claim « premium terminé »** : polish ; smoke doigt / S26 / FORM01 restent ouverts.

### 2026-10-22 — 6.56 Mois du menu + capsule gauche

**Vu :** puces mois du menu coupées par la barre (feuille dialogue : insets Compose = 0, barre ~48 dp). Capsule grise fixe à gauche (~120 dp) sur écrans scrollables : thumb framework Samsung One UI et/ou ombre sticky 4 côtés ; poignée Edge (`CocktailBar`) possible en plus hors app.

**Codé :**
- `SheetBottomClearance` / `rememberSheetBottomClearance` — max(Compose, framework, 48 dp) + 28 dp ; `contentWindowInsets = 0` ; padding **hors** scroll (`HomeMenuSheet`)
- `DisableFrameworkScrollbars` dans le thème
- Sticky accueil : Reste + 1 statut (+ Après courses si > 0) ; détails derrière « Plus / Moins » ; plus de lien teal « Noter des courses » ; une seule UI Courses en liste (« J’ai dépensé » / « Corriger »)
- Tips courants sans boîte (Accueil sticky, Menu, Réglages, fiche) ; boîtes réservées erreur/ok/warn
- Menu : titre seul (pas wordmark) ; Mois sans carte-titre ; puces `StampShape`
- Dossiers : plus de `LinearProgressIndicator` — typo « Déjà versé · reste »
- Restaurer : CTA `Danger` (hors Primary teal) ; dialog photo « Fermer » = Secondary
- Pastilles Prêt/À régler + chips exclusifs = `StampShape` ; cartes compact moins écrasées
- FORM02/04/06 + GRID01 : `TypeChip` / mois ancre / jours grille → `StampShape` ; tips Facture papier sans boîte imbriquée ; « Retard ou accord » court ; miniature sans tip « agrandir »
- MEN03 : coins sheet Menu 16 dp ; padding bas hors scroll
- SCAN01/02 : cadre ivoire sans croix ; flash crème ; status neutre hors carte
- FORM05 : Annuler = lien ; 1 Primary + Danger delete
- FORM07 : Checkbox teal carnet

**Tests :** `./gradlew :app:testDebugUnitTest --rerun-tasks` → **284/284** (67 fichiers).  
**Version :** `0.1.0-6.56` · `versionCode` 61.  
**Pas un claim « premium terminé »** : polish visuel audit ; smoke doigt / S26 restent ouverts.

### 2026-10-22 — 6.55 Premier frame, empreinte, OCR, widget

**Vu :** le splash n’était pas le papier du carnet (`#F8F1DE`). Le fond de fenêtre suivait encore la nuit du système avant Compose. `FLAG_SECURE` attendait Room. L’OCR collait des chiffres séparés en une fausse `+++`. TalkBack pouvait encore lire FAIT sous le verrou. Le widget ne se rafraîchissait pas si l’app n’était pas ouverte au changement de nuit.

**Codé :**
- Splash et fond de fenêtre = `#F6F3E8` / encre selon le cache d’apparence
- `BiometricFlagCache` posé avant le premier frame ; `MainViewModel` init depuis le cache (sinon `LaunchedEffect` effaçait `FLAG_SECURE`)
- OCR : forme `+++`/`***`, ou 12 chiffres isolés **avec** contexte (réf / OGM…) — plus de fenêtre glissante
- Verrou : `pointerInput` consomme les gestes ; sous le verrou, `clearAndSetSemantics` sur le NavHost
- Processus vivant : `CONFIGURATION_CHANGED` dynamique (pas le manifeste — broadcast protégé) → refresh widget
- Partage résumé texte **et** PDF : `try/finally` sur `summaryBusy`

**Inspection visuelle (S25, accueil jour) :** un seul chiffre, un seul teal (« + Ajouter »), FAIT forêt, retard brique, cartes papier. Pas de page Téléchargements ni de page Mise à jour dans l’app : le dossier est « Dossier de sauvegarde », les alertes sont « Alertes fiables ». Même papier, mêmes boutons. Rien à redessiner.

**Limite assumée :** widget nuit hors process mort (Android bloque `CONFIGURATION_CHANGED` au manifeste).

**Tests :** `./gradlew :app:testDebugUnitTest --rerun-tasks` → **281/281** (66 fichiers) après retrait use case mort.  
**Version :** `0.1.0-6.55` · `versionCode` 60.

**Docs (même lot) :** plan détaillé A→F → `docs/archive/` ; plan vivant court ; charte §4 condensée ; index anti-doublon ; `DeviceCareCopy.BODY` retiré ; purge locale `dist/verify/smoke-*`.  
**Suite rangement :** `DeletePendingLetterUseCase` retiré (jamais branché UI ; convert utilise déjà `deleteById`).

### 2026-10-22 — 6.54 Correctifs P0 audit (photo / verrou / PDF)

**Vu (audits agents) :** « Voir la photo » masqué si `webp\n…pdf` (endsWith sur toute la chaîne — cas loyer3) ; taps passant sous l’overlay biométrie ; `summaryBusy` PDF sans `finally` ; VIEW sans ClipData ; widget stale au bascule nuit.

**Codé :**
- `hasReceipt` via `ReceiptPathList.primaryPhotoOrFirst`
- Overlay verrou consomme les clics
- `try/finally` PDF résumé ; `viewIntent` + ClipData
- Refresh widget quand `night` change dans `MainActivity`

**Tests :** `./gradlew :app:testDebugUnitTest --rerun-tasks` → **281/281** (66 fichiers).  
**Version :** `0.1.0-6.54` · `versionCode` 59.

### 2026-10-22 — 6.53 Copy premium (Réglages / Aide / Alertes)

**Vu :** trop de tip boxes qui répétaient le titre ou le bouton (Apparence, Aide, Alertes, biometrie « Disponible… », sauvegarde longue, aide « Guide court… », Alertes en 2 blocs, restauration en 2 phrases). Smoke : photo loyer3 « Impossible d’ouvrir ».

**Codé :**
- Textes raccourcis / retirés là où le contrôle suffit ; Aide plus dense ; ton aligné Mes dossiers / aperçu / Menu / restauration
- Photo : décodage **local d’abord** (puis Documents) ; si échec in-app → « Ouvrir à part »
- Sécurité revue OK (pas d’INTERNET, allowBackup false, ClipData, zip-slip, FLAG_SECURE, clipboard sensible)
- **P0 feedback sauvegarde :** `backupMessage` sous Sauvegarde (plus dans Confidentialité) ; `refresh()` ne wipe plus ; tip « 3 copies… » seulement si dossier choisi ; HowItWorks Facture papier / Retard / Sur une année coupés
- **P1/P2 :** déficit clavier non doublé ; tip Apparence/Aide/Alertes retirés ; messages VM courts ; empty échéances « Aucune… » ; un seul « Préparation… » résumé

**Tests :** `./gradlew :app:testDebugUnitTest --rerun-tasks` → **281/281** (66 fichiers).  
**Version :** `0.1.0-6.53` · `versionCode` 58.

### 2026-10-22 — 6.52 Finitions chasse d’erreurs + rangement docs

**Vu :** PDF en échec à l’ajout non retriable (bytes vidés) ; flash thème au cold start ; ZIP de sauvegarde hors `ShareFileIntents` ; couleurs widget inversées en `values-night` ; audits historiques encore « actifs » ; AppleDouble T7.

**Codé :**
- Ajout : retry photo **et** PDF (`retryAttachments`) ; garder bitmap/PDF si échec ; revenu vide aussi la photo (+ aperçu Compose) ; édition : photo seulement si dépense (comme PDF)
- `ThemePreferenceCache` + peek boot `MainActivity` + `MainViewModel` init depuis le cache — moins de flash Clair/Sombre
- `ShareFileIntents.zip` branché sauvegarde ; strip `widget_*` nuit (crème/encre fixes)
- `MutatingRepository` / `BackupRestorer` : plus de défaut `Noop` (tests passent Noop explicitement)
- Decode photo : flux non rewindable (WebP Documents) via `decodeImageStream` / `decodeImageBytes`
- Aide KDoc Menu ; charte Menu = Réglages · À venir · Mes dossiers · calendrier
- Archive `AUDIT_ALEX` + `FEEDBACK` ; index docs / liens

**Tests :** `./gradlew :app:testDebugUnitTest --rerun-tasks` → **280/280** (66 fichiers).  
**Version :** `0.1.0-6.52` · `versionCode` 57.

**Smoke S25 (`SM-S938B`), même jour, sans cocher FAIT ni enregistrer :** widget posé sur la page 4 (Reste + ligne en retard, tap ouvre le carnet, fond encre). « Lire +++ sur la photo » sur une facture du 31 : photo ouverte, aucune communication valide. « Joindre un PDF » : message « à côté de la photo », courrier retiré, Annuler. Dossier `receipts/` : seulement des webp. Photo de loyer3 : boutons présents, image impossible à ouvrir.

### 2026-10-22 — 6.51 PDF résumé à gros caractères

**Vu :** le PDF résumé était en 11/14 pt, polices système, fond blanc — illisible pour maman / impression.

**Codé :**
- `MonthSummaryPdfWriter` : papier `#F6F3E8`, encre, Source Sans 3 / Serif 4 via `ResourcesCompat.getFont`
- Corps **18 pt**, titre doc **22 pt**, pied 14 pt (« Mon Mois Clair » + n° page) — pas tout le texte à 18
- Rubriques (Revenus, mois, Déficit…) en semibold ; puces / versements indentés exclus
- Aide : « gros caractères ».

**Smoke S25 Ultra (`SM-S938B`), 2026-10-22, APK release `versionCode` 56 installé par-dessus 6.44 (données gardées) :** accueil nuit (Apparence = comme le téléphone), FAIT et « En retard » visibles, Réglages affiche l’étape **6.51**, « PDF du résumé » ouvre le partage de `mon-mois-clair-resume.pdf` (rien n’a été envoyé). Pas coché FAIT, pas ajouté de ligne. Widget enregistré dans l’app, pas posé sur l’écran d’accueil. OCR `+++`, PDF sur une facture, et S26 : pas faits dans ce passage.

**Finition chasse d’erreurs :**
- Aide : widget suit Apparence (plus « One UI seul »)
- Ajout : passer en revenu vide le PDF en attente ; pas de succès trompeur si le PDF n’est lié à rien
- Fond widget crème fixe (déjà 6.50) + comment PDF ajout

**Tests :** `./gradlew :app:testDebugUnitTest --rerun-tasks` → **276/276** (66 fichiers).  
**Version :** `0.1.0-6.51` · `versionCode` 56.

### 2026-10-22 — 6.50 Partage PDF résumé + fond widget

**Vu :** le PDF résumé partait sans `ClipData` (permission URI fragile). Un fond widget branché sur la couleur de nuit du téléphone rendait Clair illisible.

**Codé :**
- `ShareFileIntents.pdf` (même pattern que la sauvegarde ZIP) branché Mes dossiers / Réglages
- Fond widget : crème `#F6F3E8` et encre `#1E1B18` **fixes**, choisis selon Apparence (un `@color` nuit du téléphone inversait Clair)

**Tests :** `./gradlew :app:testDebugUnitTest --rerun-tasks` → **274/274** (66 fichiers).  
**Version :** `0.1.0-6.50` · `versionCode` 55.

### 2026-10-22 — 6.49 Widget + PDF dès l’ajout

**Vu :** widget agents presque prêt (WIP) ; « Joindre un PDF » manquait à la **création** (seulement en édition) — claim 6.44 incomplet.

**Codé :**
- Widget accueil : Reste + prochaine ligne non FAIT ; refresh après mutate / restore ; montants masqués si biométrie
- Le widget suit Apparence (Clair, Sombre, ou comme le téléphone)
- « Joindre un PDF » aussi sur **Ajouter** (mêmes règles que l’édition)
- Doc / charte / benchmark alignés ; charte OCR « plus tard » corrigée

**Tests :** `./gradlew :app:testDebugUnitTest --rerun-tasks` → **273/273** (65 fichiers).  
**Version :** `0.1.0-6.49` · `versionCode` 54.

### 2026-10-22 — 6.48 PDF du résumé

**Vu :** le texte « Préparer un résumé » existe ; pour imprimer / joindre il manquait une feuille.

**Codé :**
- `MonthSummaryPdfWriter` (A4, même texte que `PrepareMonthSummaryUseCase`)
- Bouton « PDF du résumé » à côté du partage texte (Mes dossiers + Réglages)
- FileProvider `cache/share/` · Aide à jour. **Aucun fichier widget touché.**

**Tests :** `./gradlew :app:testDebugUnitTest --rerun-tasks` → **273/273** (65 fichiers).  
**Version :** `0.1.0-6.48` · `versionCode` 53.

### 2026-10-22 — 6.47 OCR communication +++ sur la photo

**Vu :** resaisir `+++…+++` depuis une photo est pénible ; un OCR de montant fausserait le Reste.

**Codé :**
- `findInOcrText` (mod 97 seulement) + tests
- ML Kit text-recognition emballé · `ReceiptCommunicationOcr`
- Fiche ajout/édition : « Lire +++ sur la photo » — remplit la communication, **jamais** le montant
- Aide à jour. **Aucun fichier widget touché.**

**Tests :** `./gradlew :app:testDebugUnitTest --rerun-tasks` → **269/269** (64 fichiers).  
**Version :** `0.1.0-6.47` · `versionCode` 52.

### 2026-10-22 — 6.46 Apparence Clair / Sombre (Réglages)

**Vu :** la nuit suivait seulement One UI ; pas de choix dans l’app.

**Codé :**
- Réglages → Apparence : Comme le téléphone / Clair / Sombre
- Préférence en `app_meta` (suit la sauvegarde), défaut = système
- Thème et barres système appliqués tout de suite depuis `MainActivity`

**Tests :** `./gradlew :app:testDebugUnitTest --rerun-tasks` → **269/269** (64 fichiers).  
**Version :** `0.1.0-6.46` · `versionCode` 51.

### 2026-10-22 — 6.45 Polish sticky + FAIT (benchmark V01 / V07)

**Vu :** le Reste n’avait pas d’ombre (V01 réservait le relief au sticky) ; cocher FAIT changeait la ligne d’un coup (V07).

**Codé :**
- Ombre courte sous le bloc sticky (Reste)
- Fond de ligne FAIT / overdue animé ~150 ms
- Pastilles un cran plus serrées (V03)
- Doc : `BENCHMARK_QUALITE_2026.md` + charte alignés (nuit et PDF déjà faits en 6.44 — plus de fausse promesse « pas encore »)

**Tests :** `./gradlew :app:testDebugUnitTest --rerun-tasks` → **259/259** (63 fichiers). Nuit : texte, lien, brique et vert ≥ 7:1 sur la page, la carte et le socle.  
**Version :** `0.1.0-6.45` · `versionCode` 50.  
**Pas de smoke device** pour 6.43–6.45.

### 2026-10-22 — 6.44 PDF à côté de la photo

**Vu :** un courrier reçu par mail est souvent un PDF. Le chemin commencé le mettait à la place de la photo, et le bouton n’était pas fiable.

**Codé :**
- « Joindre un PDF » sur la fiche de **toute** facture (mensuelle, une fois, accord…) — pas l’accueil
- Le fichier va dans `receipts/…pdf`, à côté des WebP, et dans l’archive
- Il ne remplace pas la photo. « Voir le courrier » ouvre le lecteur du téléphone
- Facture « une fois » : photo + PDF dans le même champ instance ; facture qui revient : PDF dans les courriers du modèle

**Finition (derrière agents) :** compile cassée — `A11yTokens.*` @Composable dans `Canvas` (scan QR) + tests contraste hors Composition. Couleurs lues **avant** le DrawScope ; `WcagContrastTest` sur `Day`/`Night` (palette agents **conservée**).  
**Tests :** `./gradlew :app:testDebugUnitTest --rerun-tasks` → **251/251** à la livraison ; suite **259/259** après 6.45.  
**Version :** `0.1.0-6.44` · `versionCode` 49.

### 2026-10-22 — 6.43 Forme de l’objet (benchmark 2026)

**Vu :** les cartes se confondaient avec la page, pastilles et boutons avaient le même coin, « FAIT ✓ » avait le même trait que « Copier », les montants n’avaient pas la même chasse.

**Codé :**
- Chiffres `tnum` sur les styles de l’app
- Fond `#F6F3E8` (brique toujours ≥ 7:1). Cartes 16 dp, ombre courte. Pastilles en capsule. Boutons 12 dp
- Barre Menu / + Ajouter sur `PaperWarm`
- « FAIT ✓ » plein forêt ; « Remettre » reste en trait
- Fondu 180 ms de la liste au changement de mois
- Décisions dans la charte et `docs/BENCHMARK_QUALITE_2026.md`. Pas encore : PDF, nuit, widget, OCR
- Aperçu : Mes dossiers n’ouvre plus une ligne future ; AddEntry avertit si `ym` futur ; fiche édition lecture seule ; undo FAIT feedback

**Tests :** `./gradlew :app:testDebugUnitTest --rerun-tasks` → **247/247** (61 fichiers).  
**Version :** `0.1.0-6.43` · `versionCode` 48.  
**Pas de smoke device** pour ce lot.

### 2026-10-22 — 6.42 Sync mois futurs + Soldés + dist

**Vu :** ledger futur déjà ouvert n’accueillait pas une nouvelle facture ; Soldés toujours dépliés ; doc famille mince ; Dependabot Security rouge ≠ CI.

**Codé :**
- `MonthCycleEngine` : sync templates manquants sur `AlreadyExists` (montants/FAIT inchangés)
- Mes dossiers : « Soldés » **replié** par défaut
- Aide : fréquences + aperçu futur ; `docs/DISTRIBUTION_FAMILLE.md` enrichi
- CI/SECURITY : procédure Dependabot Security (pas de faux fix yml)
- Aperçu 6.41 durci : sticky « Prévu », sheet Courses fermée, pas d’« En retard » futur, gardes Update/Delete, highlight borné

**Tests :** `./gradlew :app:testDebugUnitTest --rerun-tasks` → **247/247** (61 fichiers).  
**Version :** `0.1.0-6.42` · `versionCode` 47.

### 2026-10-22 — 6.41 Aperçu mois futurs (feedback §4)

**Vu :** « À venir » listait bien Nov+… mais le **calendrier / flèche ›** refusaient encore tout mois > courant — Athalia n’avait pas l’accès demandé (factures + rentrées dans le mois futur).

**Codé :**
- `MonthNavigation` : jusqu’à **M+3** sélectionnable ; `isFuturePreview`
- Menu : mois à venir en tête ; Home : `ensureMonthExists` + bandeau aperçu ; pas de FAIT / + Ajouter / courses
- Aide + tests navigation mis à jour
- Doc CI : bruit Dependabot security ≠ échec `ci.yml`

**Suite (trous aperçu) :** voir durcissement listé sous **6.42** (même passe).

**Tests :** `./gradlew :app:testDebugUnitTest --rerun-tasks` → **240/240** à la livraison nav ; **247/247** après gardes (journal 6.42).  
**Version :** `0.1.0-6.41` · `versionCode` 46.

### 2026-10-22 — 6.40 Polish sticky (smoke 6.39)

**Vu (doigt Ultra) :** sticky un peu chargé ; « Après courses −340 € » trop discret ; gros bouton Courses en double avec le sheet.

**Codé :**
- « Après courses » en **AccentDanger** si négatif
- Courses = **lien compact** teal (« Courses · X » / « Noter des courses »), cible ≥ 48 dp — plus de `SecondaryA11yButton` plein
- `pacePhrase` Mes dossiers : max 3 lignes + ellipsis
- Aide à jour (lien Courses + brique si négatif)

**Tests :** `./gradlew :app:testDebugUnitTest --rerun-tasks` → **239/239** (61 fichiers).  
**Version :** `0.1.0-6.40` · `versionCode` 45.

### 2026-10-22 — 6.39 Feedback famille (test 21 oct.)

**Demandes :** mail Athalia · soldés · date « Vers… » + recalcul · à venir · épargne sticky · rythme Home · courses accessibles · doc APK sans USB.

**Codé :**
- Réglages → « Un souci avec l’app ? » (`SupportMailComposer` → `arkalia.luna.system@gmail.com`)
- Mes dossiers : sections **En cours** / **Soldés**
- `pacePhrase` : date « Vers mois année » + recalcul si FAIT ≠ forfait
- Menu → **À venir** (aperçu 3 mois lecture seule, revenus + dépenses)
- Sticky : Mis de côté + bouton **Courses** (sheet) ; cadence discrète si ≠ mensuel
- Retrait épargne : dialogue d’accroche ; `docs/DISTRIBUTION_FAMILLE.md`
- Aide à jour

**Tests :** `./gradlew :app:testDebugUnitTest --rerun-tasks` → **239/239** (61 fichiers).  
**Version :** `0.1.0-6.39` · `versionCode` 44.

### 2026-10-21 — Mes dossiers sans sursaut + gradlew exécutable (CI)

**Vu :** micro-saut Mes dossiers ; job GitHub `submit-gradle` en échec.  
**Cause dossiers :** `refresh()` à chaque `ON_RESUME` vidait la liste (`loading=true`).  
**Cause CI :** `gradlew` indexé en `100644` → `Permission denied` sur Automatic Dependency Submission.  
**Codé :** refresh doux ; `git update-index --chmod=+x gradlew`.  
**Tests :** `./gradlew :app:testDebugUnitTest --rerun-tasks` → **236/236** (60 fichiers). Version inchangée `0.1.0-6.38`.

### 2026-10-21 — Garde-fou montants + Mettre de côté (clavier)

**Vu :** saisie absurde (ex. 99999) possible ; tests adb avaient tapé sur le tel (fantôme). Plus d’automation input.  
**Codé :** plafond ligne **50 000 €** / reste dû **2 000 000 €** (`MoneyFormat`) sur ajout/édition/épargne/courses/conversion ; Réglages : jour puis Activer, **Activer épinglé** si clavier ouvert.  
**Tests :** `./gradlew :app:testDebugUnitTest --rerun-tasks` → **236/236** (60 fichiers). Version inchangée `0.1.0-6.38`.

### 2026-10-21 — Fin de journée : main = develop + APK 6.38

**Vérif :** **235/235**. Fix E* partiel. `main` = `develop`. APK 6.38 sur SM-S938B.  
**Reporté :** S26 maman · `androidTest` · pas de re-smoke adb (doigt humain seulement).

### 2026-10-21 — Campagne suite_pe : P2 / D2 / D11

**Vu :** conversion courrier — grille mangeait « Créer l’accord » · reste/cadence trop bas sous les 31 jours.  
**Codé :** `PinnedActionScaffold` sur conversion · bandeau reste + cadence + rythme **au-dessus** de la grille (fiche accord).  
**Faux positifs automation :** E* (Activer introuvable), S1/S2/S9 (pas scroll / notif).  
**Dumps :** `suite_pe` purgé.  
**Tests :** **235/235**. Version inchangée `0.1.0-6.38`.

### 2026-10-21 — 6.38 Historique daté des frais

**Problème :** trois lettres de frais = une somme + un seul motif (le dernier disparaissait).

**Codé :**
- Table `expense_fee_events` (schéma Room/backup **8**) + migration 7→8 (backfill cumul lot C)
- Ajout frais → événement daté + cumul `addedFeesCents` (reste dû inchangé)
- Fiche accord : liste date · montant · motif ; aide à jour
- Backup/restore : `feeEvents` ; sauvegardes ≤7 synthétisent un événement

**Tests :** `./gradlew :app:testDebugUnitTest --rerun-tasks` → **235/235** (60 fichiers)  
**Version :** `0.1.0-6.38` · `versionCode` 43

### 2026-10-21 — Audit §8 : rien d’ouvert à coder

**Relu §8 :** lots A–F + 6.37 tenus · sécurité déjà dans `SECURITY.md` · écarts acceptés (zéro pixel, motif frais = dernier, lint soft).  
**Seul geste UI :** hint sous le champ motif frais (« remplace l’ancien »), pour coller à la décision écrite.  
**Pas rouvert :** liste de frais datée, S26 réel, `androidTest`.  
**Tests :** **233/233** (`--rerun-tasks`, 59 fichiers). Version inchangée `0.1.0-6.37`.

### 2026-10-21 — Nettoyage caches / strings mortes

**Fait :** caches locaux (`.gradle` jetable, `~/.cache/mon-mois-clair`, Gradle **8.9** obsolète) · dumps `suite_pe` · 3 `@string` jamais référencés retirés (`onboarding_*_title`, `health_title` — textes déjà en dur UI).  
**Audit dossiers :** `data/`, `domain/`, `ui/`, `res/`, `docs/`, `dist/`, `gradle/` vs `.gradle/` — pas de code produit mort à fusionner.  
**Suite :** `DeviceCareCopy` (orphelin) rebranché via `HINT` (santé alertes + onboarding + aide) — plus de textes Samsung en triple.  
**Tests :** **233/233** (`--rerun-tasks`, 59 fichiers). Version inchangée `0.1.0-6.37`.

### 2026-10-21 — APK release allégée (ABI ARM only)

**Problème :** l’APK sideload emmenait les libs **x86/x86_64** (émulateur), surtout ML Kit barcode (~12 Mo inutiles sur téléphone).

**Codé :** `ndk.abiFilters` release = `armeabi-v7a` + `arm64-v8a` seulement. Debug inchangé (émulateurs OK).

**Résultat :** APK **~25 Mo → ~14 Mo**. Scan QR / caméra inchangés sur Galaxy.  
**Version :** inchangée `0.1.0-6.37`. Pas de suite tests (config packaging).

### 2026-10-21 — Campagne : sticky Senior + snackbar FAIT

**Vu :** « Hors à payer » opaque (Senior/Produit) ; doute « FAIT efface la ligne ? ».

**Codé :** sticky → **Après les FAIT** · aide + charte · snackbar « Noté FAIT — la ligne reste ».  
**Tests :** **232/232** (`--rerun-tasks`). Version inchangée `0.1.0-6.37`.  
**Suite :** dumps campagne allégés (rapports seuls).

### 2026-10-21 — Rangement fin de journée (docs / dist)

**Fait :** AppleDouble `._*` retirés · checklist S25 → `docs/archive/` · rapports 2026-10-20 archivés · dumps `dist/verify` (audit, from_zero, p3*, visuel…) effacés · debug APK retiré · `.gitignore` ignore campagnes/py/log · index `docs/` + `dist/verify/README` à jour.  
**Préservé :** `campagne-2026-10-21/` (agents en cours) · release APK locale · RESET.  
**Pas de bump version.**

### 2026-10-21 — Campagne suite : « Après les FAIT » + hint jours

**Vu :** Senior ne comprenait pas « Hors à payer » ; hint grille sous le pli.

**Codé :** sticky « Après les FAIT » (+ aide / charte) · hint jour **au-dessus** de la grille.

**Tests :** `./gradlew :app:testDebugUnitTest --rerun-tasks` → **232/232**  
**Version :** inchangée `0.1.0-6.37` · device SM-S938B vérifié

### 2026-10-21 — Campagne device (correctifs P0/P1, pas de bump)

**Vu sur SM-S938B** (release `0.1.0-6.37`) : grille jours qui mangeait Enregistrer ; recherche `zzzz` sans message au-dessus du clavier ; déplier « retard ou accord » = faux dirty ; hero Mes dossiers lu 2× en TalkBack.

**Codé :** cases jour en `size(48.dp)` + hint défiler · message vide recherche collé sous le champ · dirty AddEntry sans `dossierExpanded` seul · a11y hero sans contentDescription doublon · spacer bas formulaire.

**Preuves :** `dist/verify/campagne-2026-10-21/` · `RAPPORT_CAMPAGNE.md`  
**Tests :** `./gradlew :app:testDebugUnitTest --rerun-tasks` → **232/232** (59 fichiers, 0 échec)  
**Version :** inchangée `0.1.0-6.37`

### 2026-10-21 — Marque (pas une étape)

Le logo est le carnet ouvert du fichier dans Téléchargements (encre, filet or, coche), décliné en icône, splash, notification, et dans les écrans qui présentent l’app. Un petit carnet est posé sur les bandeaux du mois (revenu manquant, échéance, courrier, mois vide). La liste des factures ne porte pas le logo en grand. `versionName` reste `0.1.0-6.37`. Pas de nouvelle suite (dernière close : **231/231**).

### 2026-10-21 — 6.37 Phrase rythme dossier

**Problème :** sans estimation honnête, Alex invente une date de fin ; avec une date certaine, on ment dès qu’un mois diverge.

**Codé :**
- `DossierRemaining.pacePhrase` — « Encore environ N mois si le versement reste de X » (mensuel) ou « N versements » (autre rythme) ; toujours « environ » ; jamais « fin prévue »
- Masquée si soldé, forfait ≤ 0, ou si un versement **FAIT** ≠ forfait ; les TODO plafonnées (dernier mois partiel) ne masquent pas
- Reste < forfait : « Encore environ 1 mois : le prochain versement sera le reste, X €. »
- Affichée sous le reste sur la **fiche** et **Mes dossiers** (+ ligne dans le résumé texte) ; **jamais** sur l’accueil
- `cadenceLabel` : `50 € / mois|trimestre|an` (forfait, pas le montant partiel du mois)

**Tests :** `./gradlew :app:testDebugUnitTest --rerun-tasks` → **231/231** (59 fichiers, 0 échec)  
**Version :** `0.1.0-6.37` · `versionCode` 42  
**Pas encore :** S26 maman en main · tests instrumentés

### 2026-10-21 — Correctifs après 6.36 (absorbés dans 6.37)

IBAN checksum, e-mail, anti double-tap, photos, frais, Mettre de côté, total dossiers « ce mois », densités compact — voir arbre 6.37.

### 2026-10-21 — Smoke hauteur courte type S26 (sim. Ultra)

**Geste :** release réinstallée (polish après 6.36). Densité appareil **640** → **360×780 dp** (`compactHomeChrome`). Police système **1,3** le temps du geste (remise à **1,15** après).

**Vu :** pastille **Accord** à droite du nom ; « pour quoi » puis montant ; FAIT hit **48 dp** aligné en haut ; Menu Réglages / Mes dossiers ; fiche ajout avec bloc « C’est un retard ou un accord ? » replié (▾).

**Pas de bump version** (smoke + install polish). Plan §5.3 / §5.10 : clos sur sim.

### 2026-10-21 — Smoke Ultra : « Écrire » + hauteur Accord

**Geste :** APK `0.1.0-6.36` sur SM-S938B. Accord « Etude Smoke » / cause Hopital2025. E-mail saisi en édition → **Écrire** → Gmail `ComposeActivityGmailExternal` (mailto). Accueil : pastille Accord visible ; cible FAIT ≥ 48 dp — FAIT pas poussé hors carte.

**Pas de bump version** (doc smoke seulement).  
**Suite :** hauteur courte — entrée ci-dessus.

### 2026-10-21 — 6.36 Lot F clos : résumé texte

**Problème :** un mois négatif ou une médiation demande une liste propre, sans faux PDF CPAS.

**Codé :**
- `PrepareMonthSummaryUseCase` — texte brut (mois, revenus, factures, dossiers 4 colonnes, mis de côté à part, photos dans l’app)
- Bouton « Préparer un résumé » dans Mes dossiers · aussi Réglages si déficit ou dossier
- Partage Android (mail / WhatsApp) · phrase « créancier ou conseiller »

**Tests :** `./gradlew :app:testDebugUnitTest --rerun-tasks` → **226/226** (59 fichiers, 0 échec)  
**Version :** `0.1.0-6.36` · `versionCode` 41  
**Smoke Ultra :** clos (entrée ci-dessus)

### 2026-10-21 — 6.35 Lot E clos : Mettre de côté

**Problème :** rien n’était réservé pour eux ; une cagnotte à l’écran les perdrait.

**Codé :**
- Réglages « Mettre de côté » (montant, jour, alerte déficit avant enregistrement, retirer)
- Facture mensuelle ordinaire · entre dans le Reste · FAIT = virement vers un autre compte
- Fiche : « déjà viré — pas disponible ici » · marqueur `app_meta` `savingsTemplateId` (pas de schéma nouveau)
- Aide + charte §8 (une facture admise, pas d’enveloppes multiples)
- `SavingsProjection` + `ConfigureSavingsUseCase` + tests

**Tests :** `./gradlew :app:testDebugUnitTest --rerun-tasks` → **222/222** (58 fichiers, 0 échec)  
**Version :** `0.1.0-6.35` · `versionCode` 40  
**Pas encore :** lot F · smoke « Écrire »

### 2026-10-21 — 6.34 Lot D clos : courriers sans plan

**Problème :** une lettre reçue sans versement n’avait pas de tiroir ; inventer un montant la mettait dans le Reste.

**Codé :**
- Schéma Room/backup **7** · table `pending_letters` · migration 6→7
- « Ranger sans plan » (fiche ajout) + formulaire depuis Mes dossiers
- Section « Courriers sans plan » · conversion → accord (versement + reste)
- Bandeau accueil « N courrier(s) sans plan · Voir » · Menu si dossiers **ou** courriers
- Backup JSON : clé `pendingLetters` seulement si non vide (checksum schéma ≤6 intact)
- Alignement test `ShareBackupMessages` (phrase polish)

**Tests :** `./gradlew :app:testDebugUnitTest --rerun-tasks` → **214/214** (56 fichiers, 0 échec)  
**Version :** `0.1.0-6.34` · `versionCode` 39  
**Pas encore :** phrase « encore N mois si… » · smoke « Écrire » / hauteur Accord · clôture E (code présent, suite non relancée) · F

### 2026-10-21 — Polish premium (cartes papier, sans Gradle)

**Codé :** intros/status en cartes (scan, restore, SAF, permissions, alertes, verrou, aide, formulaire) · hero dossiers · CheckRow / HealthStatusRow · notifs FAIT ✓ · scaffolds gaps · WIP courriers laissé aux autres (écran Dossiers non cassé). Voir `docs/archive/POLISH_PASS.md`.

**Version :** inchangée `0.1.0-6.33` · pas de Gradle ici.

### 2026-10-21 — Polish premium large (sans Gradle)

**Codé :** passe continue UI/a11y/copy — aide, onboarding, chips, alertes Samsung, verrou, Mes dossiers empty, menu mois, scan, restore, partage, version TalkBack. Voir `docs/archive/POLISH_PASS.md`.

**Version :** inchangée `0.1.0-6.33`

### 2026-10-21 — Polish scan / dossiers / restore (copy + couleur)

**Codé :** sous-titre Mes dossiers · statut scan coloré (OK / erreur) · intro restore muted · hero dossiers live region.

**Version :** inchangée `0.1.0-6.33`

### 2026-10-21 — Polish Mes dossiers + jargon backup

**Codé :** cartes dossiers type papier (bordure, gap) · sous-titre Menu si N>0 · hors-mois espacées · messages `BackupFailure` sans « SAF » / schemaVersion brut.

**Version :** inchangée `0.1.0-6.33` · pas de Gradle ici (agent smoke/tests ailleurs).

### 2026-10-21 — Scan QR : handoff mémoire (plus de SavedStateHandle)

**Codé :** `EpcScanHandoff` branché dans `AppNavHost` (offer au scan, take au ON_RESUME ajout/édition) · IBAN/référence ne persistent plus dans le SavedState · test unitaire offer/take.

**Version :** inchangée `0.1.0-6.32`

### 2026-10-21 — Polish UI/a11y + EuroFormat + mailto (sécurisé pause)

**Codé (agents) :** TalkBack (onboarding, Alertes, menu mois, grille jours, erreurs, live regions) · pastilles compactes · empty « Rien encore ce mois » · boutons haptique / FieldLine · notif undo humaine · `EuroFormat` + mailto query · garde IBAN sans reste à l’ajout · sync futures déjà poussée (`6fca55a`).

**Docs :** `docs/archive/POLISH_PASS.md` à jour.

**Tests :** base **198/198** (6.32) + passes ciblées Euro/mail/Money/dossier (autres agents). Pas de bump `versionName` (toujours `0.1.0-6.32`).

### 2026-10-21 — CI : plus de concurrency (vrai fix des « cancelled »)

**Problème :** « Canceling since a higher priority waiting request… » = `concurrency` GitHub (true comme false). Affiché comme « 1 erreur » sans échec de test.

**Codé :** retrait du bloc `concurrency` dans `.github/workflows/ci.yml` · chaque push termine · `docs/CI.md` à jour.

**Version :** inchangée `0.1.0-6.32`

### 2026-10-21 — CI + polish premium (messages / TalkBack)

**Problème :** badge « 1 erreur » = runs CI **cancelled** ; messages SAF/URI trop techniques ; chargements sans sémantique TalkBack.

**Codé :** messages Documents en français · live regions Réglages/Restaurer · contentDescription chargements · docs rangés (`archive/`, `testeurs/`). (La tentative `cancel-in-progress: true` ne suffisait pas — corrigée juste au-dessus.)

**Tests :** suite déjà **198/198** (52 fichiers) au 6.32 — pas de nouveau test métier ici.  
**Version :** inchangée `0.1.0-6.32`

### 2026-10-21 — Smoke Galaxy retour (6.33 installé)

**Device :** SM-S938B · APK release `0.1.0-6.33` (vCode 38) installé (était encore en 6.30).

| Check | Résultat |
|-------|----------|
| Cold start / accueil octobre | ✅ |
| EuroFormat (espace fine milliers sur Reste / À payer) | ✅ |
| Menu · Réglages · version **6.33** | ✅ |
| Menu sans « Mes dossiers » si 0 accord | ✅ (zéro pixel tenu) |
| FATAL / crash logcat | ✅ aucun |
| « Écrire » (mailto) | ⏳ pas d’accord avec e-mail sur l’appareil pour le geste |
| Ligne Accord hauteur courte | ⏳ pas d’accord affiché ce mois |

### 2026-10-21 — 6.33 Mes dossiers (liste) + EuroFormat / mailto / polish

**Contexte :** pause téléphone — suite code sans smoke device (smoke ajouté au retour, entrée ci-dessus).

**Codé :**
- Lot D **partiel** : Menu « Mes dossiers (N) » si N>0 · écran liste (barre, reste, hero € mois) · `ListDossiersUseCase`
- `EuroFormat` + `mailtoUri` + garde IBAN à l’ajout
- Polish a11y multi-agents (live regions, haptique, copy empty mois, pastilles compactes…)

**Pas encore (D) :** courriers sans plan, ligne d’accueil.

**Tests :** `./gradlew :app:testDebugUnitTest --rerun-tasks` → **208/208** (55 fichiers, 0 échec)  
**Version :** `0.1.0-6.33` · `versionCode` 38

### 2026-10-21 — Pause : push sécurité polish + EuroFormat + mailto

**Contexte :** premier push sécurité pendant l’absence (commit `a15f7e4`).

### 2026-10-21 — Fix sync futures accord (édition montant / frais)

**Problème (audit 7.4) :** après un FAIT qui effaçait le mois suivant, baisser le montant ou ajouter des frais (ligne encore DONE) ne recréait pas la suite.

**Codé :** `AgreementFuturesSync` partagé — `afterToggle` (Toggle) · `afterAmountOrFeesChange` (Update, même si DONE) · DONE ne réserve pas le mois courant une 2ᵉ fois.

**Tests :** `DossierAgreementUseCaseTest` → baisse montant + frais (11 tests dossier, 0 échec dans la passe ciblée).

### 2026-10-21 — Docs : archive + index (plus de doublons)

**Fait :** vérif code lots A/B/C OK · `docs/README.md` · archive (`AUDIT_PREMIUM`, `POLISH_PASS`, `RECHERCHE_OUTILS`) · prompts → `docs/testeurs/` · plan §7 + smoke status à jour (6.32). WIP polish autre agent **non** touché.

### 2026-10-21 — 6.32 Lot C : fiche anti-confusion (IBAN, mail, frais, photos)

**Problème :** IBAN perdu après le QR ; pas d’e-mail prêt ; avenant / 2ᵉ lettre sans place ; photo extra invisible.

**Codé :** schéma Room/backup **6** · IBAN + e-mail sur le modèle · « Copier l’IBAN » / « Écrire » · frais cumulés · 2ᵉ photo → `extraReceiptPaths` · UI **voir / enlever** les courriers · libellé « Ajouter un courrier » · aide à jour. Motif frais = dernier saisi (pas une liste datée).

**Tests :** `./gradlew :app:testDebugUnitTest --rerun-tasks` → **198/198** (52 fichiers, 0 échec)  
**Version :** `0.1.0-6.32` · `versionCode` 37  
**Ouvert :** smoke réel `ACTION_SENDTO` sur téléphone ; lots D/E/F.

### 2026-10-21 — Docs alignées (état réel A/B/C)

**Fait :** relecture code sans suite Gradle lourde · audit §7 + plan + journal mis à jour (tests mail/chemins/frais présents ; photos extras encore sans UI) · push docs. Autre agent : tests isolés lot B OK à laisser finir.

### 2026-10-21 — Polish premium (sécurité / restore / a11y) + WIP lot C

**Problème :** zip-bomb / lectures illimitées ; FileProvider trop large ; verrou bio asynchrone ; copy Samsung ; messages restore techniques ; lot C (schéma 6) déjà dans l’arbre sans clôture.

**Codé (polish) :** lectures bornées · restore photos locales · FileProvider `share/`+`camera/` · sweep JPEG · `SCHEDULE_EXACT_ALARM` · debounce `TIME_SET` · bio lock sync · scan/onboarding/settings copy · (note figée : `docs/archive/POLISH_PASS.md`).

**Arbre lot C (non clos)** : Room **6** (IBAN, e-mail, `extraReceiptPaths`, frais) · UI IBAN/mail/frais en édition · 2ᵉ photo stockée mais **pas affichée** · `CreditorMailComposer` + tests unitaires mail/chemins/frais. `versionName` reste **6.31** jusqu’à clôture C.

**Tests :** annoncés au commit polish → **198/198** (52 fichiers) — ne pas recompter sans `--rerun-tasks` après nouveaux fichiers.  
**Version :** inchangée `0.1.0-6.31` · `versionCode` 36  
**Commit :** `60b18af`

### 2026-10-21 — 6.31 Lot B : dossier / accord (replié)

**Problème :** un seul nom ; pas de reste dû ; la ligne continue après la fin de la dette.

**Codé :** schéma **5** (`causeLabel`, `openingRemainingCents` nullable sur `expense_templates`) · reste = départ − FAIT (centimes réels) · arrêt de matérialisation si soldé · fiche : bloc « C’est un retard ou un accord ? » · accueil : ligne 2 « pour quoi » + pastille **Accord** (sans euros de dette) · fiche « Soldé » · pas d’entrée Menu « Mes dossiers » (lot D). 0 dossier = accueil inchangé.

**Tests :** `./gradlew :app:testDebugUnitTest --rerun-tasks` → **183/183** (50 fichiers, 0 échec)  
**Version :** `0.1.0-6.31` · `versionCode` 36

### 2026-10-21 — 6.30 Lot A : mémoire du papier (invisible)

**Problème :** communication et photo collées à l’instance du mois ; le mois suivant était recréé sans elles. Copier collait le `+++…+++` alors que la banque veut souvent les 12 chiffres.

**Codé :** schéma Room/backup **4** (`paymentNote` + `receiptRelativePath` sur `expense_templates`) · migration 3→4 · `MonthCycleEngine` reprend note/photo · enregistrement : note → modèle toujours ; photo → modèle si « Aussi les mois suivants » · `BelgianStructuredCommunication` (mod 97) · aide + phrase sous la case. Aucun libellé nouveau sur l’accueil.

**Tests :** `./gradlew :app:testDebugUnitTest --rerun-tasks` → **173/173** (48 fichiers, 0 échec)  
**Version :** `0.1.0-6.30` · `versionCode` 35

### 2026-10-21 — 6.29 Suite audit premium (Copier, Alertes, scan, libellés)

**Problème (audit code post-6.27) :** Copier encore dans la zone Modifier · Menu·Alertes n’ouvrait pas Alertes · dialogue photo édition en spinner · pas de rescan QR en édition · doublon « Après courses » · « Voir » orphelin · titres « entrée ».

**Codé :** Copier hors zone Modifier · Menu·Alertes → hub Alertes · message photo en édition · scan QR en édition · Enlever la photo · Noté X € sous Courses · Voir dans Réglages · titres clarifiés · Envoyer une copie.

**Différé :** changer Dépense↔Revenu sans supprimer (migration plus lourde).

**Tests :** `./gradlew :app:testDebugUnitTest --rerun-tasks` → **165/165** (47 fichiers, 0 échec)  
**Version :** `0.1.0-6.29` · `versionCode` 34

### 2026-10-21 — 6.28 Premium : photo au doigt + libellé Menu

**Problème :** pastille « Facture » (Text clickable) ne réagissait pas au tap sur Galaxy ; si le fichier manquait, le dialogue se fermait sans explication ; « Menu / Alertes » coupait sur 2 lignes ; photos seulement dans SAF (illisibles si Documents KO).

**Codé :** bouton « Voir la photo » · dialogue avec message d’erreur · « Menu · Alertes » · copie locale + SAF à l’enregistrement / relecture.

**Tests :** `./gradlew :app:testDebugUnitTest --rerun-tasks` → **165/165** (47 fichiers, 0 échec)  
**Version :** `0.1.0-6.28` · `versionCode` 33

### 2026-10-21 — 6.27 Retours manuels maman (photo, fréquence, menu, sticky)

**Confirmé OK (manuel) :** notifications · biométrie.

**Problèmes :** pastille « Facture » ouvrait l’édition au lieu de la photo · fréquence non modifiable sans supprimer · Menu doublonnait Alertes / Aide avec Réglages · sticky peu clair (reste vs futures vs courses) · aide Courses trop bas.

**Codé :** pastille photo hors zone Modifier · décode photo unifié · fréquence Once↔mensuel/3 mois/an sur fiche · Menu = Réglages + mois · sticky « Sans les futures » · Courses en 2ᵉ section d’aide · charte §1/7/8 à jour.

**Tests :** `./gradlew :app:testDebugUnitTest --rerun-tasks` → **165/165** (47 fichiers, 0 échec)  
**Version :** `0.1.0-6.27` · `versionCode` 32

### 2026-10-20 — Fin de journée : rangement + gel `main` = `develop` (6.26)

**Fait :** alignment docs audit → 6.26 / 162 tests · ignore `.gradle-agent-prompts/` · nettoyage AppleDouble dans `.git` · release installée sur SM-S938B · `main` alignée sur `develop` (demande explicite) · retour sur `develop`.

**Pas encore :** smoke notif multi-factures · trimestriel nom seul.

**Tests :** `./gradlew :app:testDebugUnitTest --rerun-tasks` → **162/162** (47 fichiers, 0 échec)  
**Version :** `0.1.0-6.26` · `versionCode` 31

### 2026-10-20 — 6.26 Campagne prompts : mois vide + onboarding

**Problème (Correcteur / Senior sur Galaxy) :** un mois sans facture affichait « À payer 0 € » en vert (fausse réussite) ; l’écran permissions disait encore « pas OK » alors que les pastilles disent « Prêt ».

**Codé :** `stickyStillToPayLabel` (masque la ligne si aucune facture ; « Tout est payé » seulement s’il y en a) · phrase onboarding « pas prêts » · tests unitaires dédiés.

**Tests :** `./gradlew :app:testDebugUnitTest --rerun-tasks` → **162/162** (47 fichiers, 0 échec)  
**Version :** `0.1.0-6.26` · `versionCode` 31

### 2026-10-20 — 6.25 Échéance hors mois, phrases, photo nommée

**Problème :** quitter une échéance hors mois jetait la saisie ; l’erreur restait dans le défilement ; une échéance absente ouvrait une fiche vide ; « OK », « ancre », « intégrité » et « version non supportée » restaient à l’écran ; la photo s’appelait seulement « Facture » ; l’aide ne disait pas que les courses ne changent pas le Reste.

**Codé :** confirmation « Quitter sans enregistrer ? » · erreur au-dessus d’Enregistrer · écran « Cette échéance n’est plus là. » · pastille « Prêt » · restauration en phrases fixes · aide courses + « Copier » · titre de photo = nom de la ligne.

**Tests :** `./gradlew :app:testDebugUnitTest --rerun-tasks` → **159/159** (46 fichiers, 0 échec)  
**Version :** `0.1.0-6.25` · `versionCode` 30

### 2026-10-20 — CI phase 2 soft + phase 3 soft

**Problème :** warning Node 20 sur actions CI ; lint jamais exécuté en CI ; Dependabot Gradle absent.

**Codé :** `setup-android@v4` · `upload-artifact@v7` · step `:app:lintDebug` avec `continue-on-error: true` + artifact rapport · Dependabot Gradle hebdo · correction des **4 erreurs** Lint (`MissingPermission` notif, `NewApi` nav bar v27, `app/lint.xml` opt-in CameraX scan) · silence logs (`keepDebugSymbols` libs natives ; retrait `@file:OptIn` inutile sur scan).

**Pas encore :** lint bloquant · Detekt · scan CVE.

**Tests :** `./gradlew :app:assembleDebug :app:lintDebug :app:testDebugUnitTest --rerun-tasks` → **159/159** (46 fichiers, 0 échec) · lint **0 erreur** / 37 warnings  
**Version :** inchangée `0.1.0-6.25`

### 2026-10-20 — 6.24 Gestes fiche / échéance / photo

**Problème :** Retour jette la saisie (E01) ; échéance hors mois non retirable (E02) ; jpg photo laissé dans le cache (E08) ; montant à 3 décimales tronqué (E07).

**Codé :** `BackHandler` + « Quitter sans enregistrer ? » · `ArchiveExpenseTemplateUseCase` + bouton « Retirer cette échéance » · effacement jpg après capture/annulation · `parseToCents` refuse >2 décimales · scan **Lampe** (D01) + « Ouvrir les réglages » caméra (D02).

**Tests :** `./gradlew :app:testDebugUnitTest --rerun-tasks` → **159/159** (46 fichiers, 0 échec)  
**Version :** `0.1.0-6.24` · `versionCode` 29

### 2026-10-20 — 6.23 Audit premium (A01–C02) + menu Outils + smoke

**Problème :** points ouverts de `docs/AUDIT_PREMIUM.md` ; smoke SM-S938B : boutons « Outils » hors écran sous le calendrier.

**Codé :** Outils en tête + scroll + `navigationBarsPadding` · **A01** mois par année · **A02** « En retard » · **A03** courses · **A04** « Prélèvement » · **B01** « Alertes » · **B02** version sans `0.1.0-` · **C01** phrases fixes dossier/envoi · **C02** « Voir le mois ».

**Smoke :** recherche zzzz ✅ · mois vide ✅ · courses trop retirées ✅ · Menu / Comment / Restaurer ✅ · share ZIP ✅ · notif + trimestriel + relecture A01–C02 ⏸.

**Tests :** `./gradlew :app:testDebugUnitTest --rerun-tasks` → **157/157** (45 fichiers, 0 échec)  
**Version :** `0.1.0-6.23` · `versionCode` 28

### 2026-10-20 — 6.22 Stabilisation (absence device)

**Problème :** Menu → Outils trop maigre (doublons) ; restore ZIP pouvait réussir sans remettre les photos ; plafonds mémoire ZIP fragiles ; audits premium encore « vivants » alors que le code est tenu.

**Codé :** Menu outils (Santé / Comment ça marche / Restaurer / Réglages) · `RestoreResult.Success` compte photos écrites + message honnête · sanitization chemins `receipts/` · plafond 45 Mo à l’empaquetage / lecture · tests archive + restore messages · audits ensuite regroupés dans `docs/AUDIT_PREMIUM.md` · suppression `ic_mark` mort.

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
