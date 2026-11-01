> **ARCHIVÉ** 2026-11-01 — historique figé. Vivant = `docs/audits/2026-11-01-premium-*` + journal.

# Audit DA final — affichage (sticky · sheets · fiche · widget · titres · densités)

| | |
|--|--|
| **Date** | 2026-10-26 · final-01 |
| **Profil** | Directrice artistique senior — carnet papier / bill reminder 2026–27 |
| **Périmètre** | Accueil sticky + liste · ModalBottomSheets · fiche entrée · widget RemoteViews · matrice titres · densités `DisplayFit` |
| **État code** | `0.1.0-6.82` · `versionCode` 87 · **332/332** unit (journal) |
| **Pairs** | Flying Bills · Billiary · AllPaid — **barème papier**, pas fintech |
| **Précédent** | `nuit-01-da-visuel` (6.75) · synthèse Top15 (6.78) · polish widget/carnet **6.79–6.82** |
| **Mode** | **AUDIT ONLY** — zéro édition produit · **pas de commit** |

---

## Verdict

**8,2 / 10** (code) · **~5–6 / 10 « dans la main »** (smoke S26 encore ouvert)

Direction **tenue** et craft **nettement remonté** depuis nuit-01 : sticky crème unifiée, mois muted, cartes filet seul, widget serif système + tokens, Annuler QuietLink, Menu un cran d’air.

Ce qui manque encore pour « carnet premium terminé » n’est plus une fracture palette / purple / logo sticky — c’est le **dernier millimètre de quieting** : sticky encore outil, cartes encore bavardes, sheets encore Material, widget encore « cousin » de l’app, densités encore sandwich.

> Pas un claim « 9/10 dans la main ». Crypto 6.81 et polish 6.82 **ne ferment pas** l’affichage.

| Surface | Note DA 6.75 | Note DA **6.82** | Lecture |
|---------|-------------:|-----------------:|---------|
| Système (tokens, typo, formes) | 8,5 | **8,7** | Matrice PageTitle/Hub stable ; coins différenciés |
| Accueil sticky + liste | 7 | **7,8** | Hero OK ; chrome mois + lignes sous-Reste encore concurrents |
| Hub / sheets | 8 | **8,0** | Crème + clearance OK ; poignée / rayon encore M3 stock |
| Fiche | ~7,5 | **7,6** | FORM01 + QuietLink Annuler ; scroll long, boîtes warm |
| Widget | 6,5 | **7,8** | Serif + crème OK ; ≠ Source Serif 4 ; nuit non inversée |
| Titres | — | **8,5** | Tokens clairs ; › HubRow un cran trop fort |
| Densités | — | **7,5** | Compact/confort utiles ; sticky serré + socle warm |

---

## Ce qui est déjà premium (ne pas rouvrir)

| Item | Preuve 6.82 | Verrou |
|------|-------------|--------|
| Palette crème / un teal / FAIT forêt | `A11yTokens` | Anti-fintech |
| Serif **uniquement** Reste in-app | `displayLarge` + `BrandSerif` | Pas titres, pas boutons |
| Sticky **sans** logo / wordmark | `HomeScreen` commentaire + empty H08 | Ledger |
| Sticky = **même** `PaperCream` que la liste | 6.78+ (plus sandwich warm sticky) | Une feuille |
| Mois sticky **InkMuted** `titleLarge` | Quiet sticky | Reste commande |
| Cartes **filet seul** (pas ombre+bordure) | `ExpenseRow` / `IncomeRow` | Papier imprimé |
| Widget : `fontFamily="serif"` + **pas** `@font/` + **pas** `<View` | contrat `CarnetWidgetLayoutContractTest` · fix One UI 6.79 | Host Samsung |
| Widget muted `#4A453C` = InkMuted | `colors.xml` | Aligné nuit-01 P1-W2 |
| Sheets crème + `SheetBottomClearance` | Menu / Courses / Épargne / mois | One UI edge-to-edge |
| PageTitle InkMuted · HubRow Ink · QuietLink Link | `CarnetTitles` / charte | Matrice 6.72–6.74 |
| Annuler fiche = QuietLink centré | C03 · 6.82 | Pas 2ᵉ Primary |
| FAIT ≥ 96 dp | Primary `widthIn(min = 96)` | Charte pouce |
| Pastilles max 2 | `ExpenseStatusLine` | Pas 3ᵉ chroma UI |

---

## P0 — encore pas premium (bloque le « carnet terminé »)

Plus de P0 **marque** type « widget sans serif » (fermé 6.79). Les P0 ci-dessous sont **affichage premier regard** : sans eux, l’app reste « très bonne » mais pas « objet fini ».

| Id | Surface | Constat (code 6.82) | Pourquoi P0 DA |
|----|---------|---------------------|----------------|
| **P0-S1** | Sticky | Rangée mois = **4 cibles** (‹ · libellé · calendrier · ›) en `titleLarge` muted, puis libellé Reste, chiffre, À payer, Courses, parfois Autres infos (+ aperçu or) | Le hero serif est **codé** ; le viewport reste **bandeau outil**. Pairs premium = 1 chiffre qui commande avant toute nav. |
| **P0-C1** | Cartes échéance | Sous le nom : cause · cadence · montant `titleLarge` · bande pastilles · Copier/Photo · FAIT 96 | Premier regard encore **riche** vs Flying Bills « jour + nom + montant + FAIT ». Smoke S25 a corrigé collisions, pas la **hiérarchie**. |
| **P0-D1** | Densité Accueil | `sectionGap` sticky **4/6 dp** ; socle barre `PaperWarm` + `shadowElevation = 2` sous liste crème | Sandwich **crème / warm + ombre** = Material stock. Premium papier = feuille + **socle discret sans relief** (ou filet seul). |

Pas de P0 palette / purple / logo sticky / widget inflate : direction et host One UI tiennent.

---

## P1 — craft final (gain premium net)

| Id | Surface | Constat | Intention DA |
|----|---------|---------|--------------|
| **P1-S2** | Sticky | « Autres infos » + chevron 20 dp = chrome disclosure UI | Libellé plus carnet (« Détails ») ou filet discret ; garder accessibilité épargne compact |
| **P1-S3** | Sticky | Courses / épargne = `StickyLinkRow` body + › | OK fonctionnel ; vérifier ellipsis + Danger courses ne **rivalisent** pas avec le Reste (couleur / poids) |
| **P1-H1** | Sheets | Coins **16 dp** top alors que `Shapes.extraLarge` = **28 dp** | Harmoniser feuilles à **28** (ou 24) — look carnet, pas carte M3 |
| **P1-H2** | Sheets | Poignée M3 **par défaut** (non stylée dans Courses/Menu/…) | Poignée `PaperBorder` fine / ou masquer si geste clair — moins « Google sheet » |
| **P1-H3** | Menu sheet | HubRow › en `headlineMedium` **24 sp** Link | › trop présent vs label `titleLarge` ; descendre à `titleLarge` / InkMuted |
| **P1-F1** | Fiche | Long scroll ; blocs `PaperWarm` erreurs / tips empilés ; Paiement & preuve + Accord repliés | Plus d’**air entre sections** ; tips = filet ou texte muted, pas boîte warm systématique |
| **P1-F2** | Fiche | Socle bas Primary + QuietLink Annuler + Danger | Rythme C03 tenu ; vérifier compact IME (seul Enregistrer) ne **casse** pas la respiration au doigt |
| **P1-W1** | Widget | Montant **serif système** 32 sp ≠ Source Serif 4 in-app 34/40 | Accepté One UI (WDG02) ; compenser : tracking / taille / filet pour que le bureau **sente** le même carnet |
| **P1-W2** | Widget | Coins **18 dp** vs cartes Compose **16** | Harmoniser **16** ou documenter 18 = host launcher — éviter dérive « quasi » |
| **P1-W3** | Widget | Nuit : couleurs **fixes crème/encre** (`values-night` ne retouche pas widget_*) | OK lisibilité launcher sombre ; smoke doigt nuit + bio masqué |
| **P1-T1** | Titres | `titleLarge` PageTitle **23 sp** SemiBold partout (sheets courtes incluses) | Sur sheet 1 champ, PageTitle un cran **titleMedium** ou spacer plus généreux sous titre |
| **P1-N1** | Lien nuit | `#9AD8D8` (cyan soft) | Surveiller vs teal jour — ne pas dériver purple ; option : teal désaturé plus « encre » |

---

## P2 — polish / cohérence

| Id | Note |
|----|------|
| **P2-1** | Snackbar FAIT compact : vérifier nuit (PaperCard sur crème nuit) — déjà noté nuit-01 |
| **P2-2** | Pastille Auto or + forêt FAIT + brique + teal CTA = 4 chromas — **max 2 pastilles** reste sacré |
| **P2-3** | Empty mois sans BrandMark : ne pas réintroduire illustration |
| **P2-4** | Wordmark hors Accueil (Lock / onboarding / flash load) : OK ; garder hors sticky |
| **P2-5** | Notif silhouette 24 dp : micro-pass (hors scope affichage Accueil, mais même marque) |
| **P2-6** | `HomeScreen` god-file : dette eng — n’aide pas le polish DA (risque régression densités) |
| **P2-7** | LetterSpacing widget titre `0.1` : bon ledger ; ne pas l’étendre aux titres Compose |

---

## Densités — lecture senior

| Mode | Déclencheur | Effet | Risque restant |
|------|-------------|-------|----------------|
| Confort | hauteur > 800 dp | Reste ~40 sp, paddings 12, épargne visible | Socle warm + ombre (P0-D1) |
| Compact | ≤ 800 dp (S26 / texte agrandi) | Reste 34 sp, épargne derrière Autres infos, gaps 4 | Sticky **mange** encore la 1ʳᵉ facture si Plus ouvert + snackbar |
| fontScale ≥ 1,25 | `rememberDisplayFit` | Plafond hero pré-compensé | Smoke **doigt** obligatoire — code ≠ sensation |

**Verdict densités :** le système est **juste** (pas de branche modèle Samsung). Le premium manquant = **respiration** sticky/liste/socle, pas un 3ᵉ breakpoint.

---

## Titres — matrice (état 6.82)

| Rôle | Composant | Style | Couleur | Écart restant |
|------|-----------|-------|---------|---------------|
| Page | `CarnetPageTitle` | `titleLarge` | InkMuted | Sheets courtes : peut-être trop fort (P1-T1) |
| Section | `CarnetSectionTitle` | `titleMedium` | InkMuted | OK fiche |
| Hub tapppable | `CarnetHubRow` | `titleLarge` + › `headlineMedium` | Ink + Link | › trop fort (P1-H3) |
| Disclosure fiche | titre + ▾ | `titleLarge` Ink | — | ≠ PageTitle — OK charte |
| QuietLink | `QuietLinkA11y` | `bodyLarge` | Link | Annuler OK |
| Hero Reste | sticky | `displayLarge` serif | Ink / Danger | Concurrent chrome (P0-S1) |

---

## Tâches finales (ordre DA)

Ordre **impact premium / risque charte** — à traiter comme lot **affichage final**, pas comme crypto :

### Lot A — Accueil « un chiffre commande » (P0)

1. **Quieting sticky (P0-S1)** — alléger la rangée mois (regrouper calendrier / réduire poids visuel des 4 cibles) ; garder ‹ › accessibles ≥ 48 dp.
2. **Cartes premier regard (P0-C1)** — montant + FAIT dominants ; cause / cadence / note en 2ᵉ souffle (muted, 1 ligne max visible).
3. **Socle barre (P0-D1)** — retirer `shadowElevation` ; filet seul ou warm plus discret ; une feuille crème dominante.

### Lot B — Sheets & titres (P1)

4. Rayon feuilles **28 dp** + poignée carnet (P1-H1, P1-H2).
5. › HubRow plus discret (P1-H3) ; PageTitle sheets courtes (P1-T1).
6. Menu : garder air 6.82 ; ne pas réintroduire Aide/Restore.

### Lot C — Fiche & widget (P1)

7. Rythme vertical fiche : moins de `PaperWarm` boîtes (P1-F1) ; smoke IME Annuler (P1-F2).
8. Widget : micro-pass tracking/taille/coins (P1-W1, P1-W2) **sans** `@font/` ni `<View`.
9. Smoke widget nuit + bio (P1-W3).

### Lot D — Gate « dans la main » (hors code, obligatoire)

10. **Smoke S26** (checklist synthèse §3) : sticky · fontScale · FAIT/snackbar · photo · widget · notif · QuietLink Menu.
11. Ne **pas** monter `versionName` / badges « 9/10 » tant que S26 non coché.

### Hors scope volontaire

- Redesign nuit wallpaper · 2ᵉ accent · glass/blur · logo sticky · camembert hero · police custom widget · découpe `HomeScreen` (sauf si bloque A/B).

---

## Ne pas toucher (verrous)

| Verrou | Raison |
|--------|--------|
| Crème / encre / teal `#1B4D4E` | Identité |
| Serif **uniquement** Reste (Compose) | Signature |
| Pas `@font/` / `<View` widget | One UI host |
| Pas logo sticky | Ledger |
| FAIT forêt ≠ teal « + Ajouter » | Deux rôles |
| Pastille Auto or **fond** seulement | Contraste |
| Brique `#8E3418` (pas rouge sang) | Anxiété |
| Pas purple / indigo / glass / glow / dégradé teal | Hors pairs papier |
| Un seul hero Accueil | Un chiffre |
| PageTitle InkMuted · HubRow Ink | Matrice titres |
| Cibles ≥ 48 · corps ≥ 18 · contraste ≥ 7:1 | Famille Galaxy |

---

## Synthèse une phrase

**Le carnet Compose est directionnellement premium ; le dernier cran = faire taire sticky et cartes, humaniser les sheets, et fumer S26 — le widget et la crypto ne suffisent pas à dire « terminé ».**

---

*Audit final-01 affichage · `0.1.0-6.82` · AUDIT ONLY · pas de commit.*
