> **ARCHIVÉ** 2026-11-01 (7.01) — brut premium craft. Vivant = `docs/audits/2026-11-01-premium-00-synthese.md` + `premium-clean-10.md`. Ne plus lire pour l’état code.

# Audit DA premium — affichage (sticky · cartes · sheets · fiche · widget · titres · densités)

| | |
|--|--|
| **Date** | 2026-11-01 · premium-01 |
| **Profil** | Directrice artistique senior — carnet papier belge / bill reminder 2026–27 |
| **Périmètre** | Accueil sticky + liste · ModalBottomSheets · fiche entrée · widget RemoteViews · matrice titres · densités `DisplayFit` |
| **État code** | `0.1.0-6.91` · README badge · journal 6.91 (Samsung Direct Boot / notif — **hors** DA) |
| **Pairs** | Flying Bills · Billiary · AllPaid — **barème papier**, pas fintech |
| **Précédent** | `2026-10-26-final-01-affichage.md` (6.82) · quiet UI 6.83–6.87 |
| **Mode** | **AUDIT ONLY** — zéro fix produit · preuves fichiers · pas de smoke doigt dans cet appel |

---

## Verdict

**8,5 / 10 (code / craft Compose)** · **~5–6 / 10 « dans la main »** (smoke S26 / doigt **NON VÉRIFIÉ** ici)

Direction carnet **tenue**. Depuis final-01, les P0 « marque » et la plupart des P1 craft (mois quiet, Détails, sheets 28 + poignée, › Hub/sticky, shortSheet, coins widget 16, socle sans ombre) sont **visibles dans le code**.

Ce qui bloque encore un vrai **premium objet fini** n’est plus purple / logo sticky / inflate One UI : c’est le **dernier millimètre de quieting + respiration**, surtout en compact (~S26), et la **preuve doigt** absente.

> **Pas de claim 9/10.** Pas de claim « premium vendable terminé ». Crypto / ship / Samsung 6.91 **ne ferment pas** l’affichage.

| Surface | Note DA 6.82 | Note DA **6.91 (code)** | Lecture |
|---------|-------------:|------------------------:|---------|
| Système (tokens, typo, formes) | 8,7 | **8,8** | Matrice titres + shapes stables |
| Accueil sticky + liste | 7,8 | **8,2** | Mois allégé ; sticky encore dense (2 dp) |
| Hub / sheets | 8,0 | **8,4** | 28 dp + `CarnetSheetHandle` + shortSheet |
| Fiche | 7,6 | **7,7** | FORM01 / QuietLink OK ; boîtes warm + scroll |
| Widget | 7,8 | **8,0** | Serif + 16 dp + tracking ; ≠ Source Serif 4 |
| Titres | 8,5 | **8,7** | shortSheet + Hub › muted |
| Densités | 7,5 | **7,6** | Compact juste ; respiration sticky faible |

---

## Méthode & limites

| Étape | Statut | Nature |
|-------|--------|--------|
| `pwd` = `/Volumes/T7/Mon Mois Clair` | VÉRIFIÉ | commande |
| Version `0.1.0-6.91` README / PROGRESS | DÉCLARÉ | inspection docs |
| Surfaces UI listées ci-dessous | VÉRIFIÉ | inspection sources |
| Sensation pouce / S26 / fontScale / widget bureau / nuit | **NON VÉRIFIÉ** | smoke manuel **non exécuté** |
| Tests Gradle / screenshots | **NON VÉRIFIÉ** | hors périmètre DA de cet appel |

---

## Ce qui est déjà premium (ne pas rouvrir)

| Item | Preuve 6.91 | Statut |
|------|-------------|--------|
| Palette crème / un teal / FAIT forêt / brique mesurée | `A11yTokens` · CHARTE palette | VÉRIFIÉ (inspection) |
| Serif **uniquement** Reste in-app (`displayLarge` + `BrandSerif`) | `MonMoisClairTheme` · sticky | VÉRIFIÉ |
| Sticky **sans** logo / wordmark | `HomeStickyHeader` commentaire + code | VÉRIFIÉ |
| Sticky = `PaperCream` (même feuille que liste) | `HomeStickyHeader` `.background(PaperCream)` | VÉRIFIÉ |
| Mois sticky **InkMuted** `titleMedium` (plus titleLarge outil) | `HomeStickyHeader` L75–79 | VÉRIFIÉ |
| Rangée mois = **3** cibles (‹ · libellé tapppable · ›) | `HomeStickyHeader` L64–97 | VÉRIFIÉ |
| « Détails / Masquer » (ex Autres infos) | `HomeStickyHeader` L220–222 | VÉRIFIÉ |
| › Courses/épargne `titleMedium` InkMuted | `HomeStickyChrome` L55–60 | VÉRIFIÉ |
| Cartes **filet seul** (pas ombre+bordure) | `ExpenseRow` / `IncomeRow` | VÉRIFIÉ |
| Montant **avant** cause/cadence muted | `ExpenseRow` L231–248 | VÉRIFIÉ |
| Sheets `extraLarge` 28 dp + `CarnetSheetHandle` | Menu/Courses/Épargne/Mois | VÉRIFIÉ |
| PageTitle InkMuted · HubRow Ink · › Hub muted | `CarnetTitles` · `CarnetHubRow` | VÉRIFIÉ |
| shortSheet → `titleMedium` (Courses / épargne) | `CarnetPageTitle` · sheets | VÉRIFIÉ |
| Socle barre `shadowElevation = 0` + filet | `HomeBottomBar` L33–38 | VÉRIFIÉ |
| Widget serif système + coins **16** + pas `@font/` / View nu | `widget_carnet.xml` · `widget_carnet_bg.xml` | VÉRIFIÉ |
| Écart cartes liste **12 dp** (charte) | `A11yTokens.cardGap` · `HomeScreen` | VÉRIFIÉ |
| Annuler fiche = QuietLink ; FAIT ≥ 96 dp | `EntryFormScaffold` · `ExpenseRow` | VÉRIFIÉ |
| Pastilles max 2 | `ExpenseStatusLine` `.take(2)` | VÉRIFIÉ |

---

## Écart CHARTE ↔ code (constat)

| Sujet | CHARTE (DÉCLARÉ) | Code (VÉRIFIÉ) |
|-------|------------------|----------------|
| Nav mois Accueil | « ‹ mois › + **icône calendrier** » (`CHARTE_ET_ARCHITECTURE.md` §Accueil) | Calendrier = **tap sur le libellé mois** ; plus d’icône 4ᵉ cible (`HomeStickyHeader`) |

Le quieting DA est **cohérent** avec le code ; la phrase CHARTE est **en retard**. Comportement réel doigt = NON VÉRIFIÉ.

---

## P0 — encore pas « carnet premium terminé »

Plus de P0 marque (widget sans serif, logo sticky, purple). Les P0 ci-dessous sont **premier regard Accueil** : sans eux l’app reste « très bonne appli Material carnet », pas « objet papier fini ».

| Id | Surface | Constat (code 6.91) | Pourquoi P0 DA | Preuve |
|----|---------|---------------------|----------------|--------|
| **P0-S1** | Sticky | Colonne sticky `spacedBy(2.dp)` ; sous le hero : statut À payer + Courses (+ épargne confort) + parfois Détails/aperçu | Le serif **commande en typo** ; le viewport reste une **pile d’outils**. Premium papier = air autour du chiffre avant toute 2ᵉ ligne | `HomeStickyHeader` L62–63, L127–188 |
| **P0-C1** | Cartes | Même carte : DayBadge · nom · montant `titleLarge` · meta · bande FAIT 96 · pastilles · Copier/Photo | Hiérarchie **mieux** qu’en 6.82 (montant avant meta) ; premier regard encore **riche** vs Flying Bills « jour + nom + montant + FAIT » | `HomeTimelineRows.kt` `ExpenseRow` |
| **P0-D1** | Densité Accueil | Compact : `sectionGap` 4 dp, sticky pad vertical 8 ; socle bas **`PaperWarm`** sous liste crème (ombre retirée) | Ombre fermée ✅ ; sandwich **crème / warm** + sticky **serré** restent la sensation « chrome » sur ~780–800 dp | `DisplayFit` · `HomeBottomBar` · sticky |

---

## P1 — craft final (gain premium net)

| Id | Surface | Constat | Intention DA | Preuve |
|----|---------|---------|--------------|--------|
| **P1-S2** | Sticky | « Détails » + chevron 20 dp OK libellé ; disclosure reste chrome UI | Filet discret / moins de poids ; garder a11y épargne compact | `HomeStickyHeader` L219–255 |
| **P1-S3** | Sticky | Courses DangerInk si après négatif | Ne **pas** laisser la brique rivaliser avec le Reste (poids / proximité) — smoke doigt | `StickyLinkRow` + couleurs Courses |
| **P1-F1** | Fiche | Scroll long ; tips/infos/erreurs en **boîtes** `PaperWarm` / soft ; socle bas warm | Plus d’air entre sections ; tips courants = texte muted / filet (CHARTE : boîtes = erreur/ok/warn seulement) | `EntryFormScaffold` L459–517 |
| **P1-F2** | Fiche | IME : seul Enregistrer ; Annuler QuietLink hors clavier | Vérifier respiration compact + clavier One UI (doigt) | `EntryFormScaffold` L520–531 |
| **P1-W1** | Widget | Montant **serif système** 34 sp ≠ Source Serif 4 in-app 34/40 | Accepté host One UI ; micro-pass tracking/filet déjà là — smoke bureau | `widget_carnet.xml` |
| **P1-W3** | Widget | Fonds crème/encre **fixes** (même si téléphone nuit) | OK lisibilité launcher ; smoke nuit + bio masqué | `widget_carnet_bg.xml` commentaire |
| **P1-N1** | Lien nuit | `#9AD8D8` cyan soft | Surveiller vs teal jour — ne pas dériver purple | `A11yTokens` Night `link` |
| **P1-H4** | Sheets courtes | Courses : 2 Secondary côte à côte (« J’ai dépensé » / « Corriger ») | Rythme encore **outil** ; un Primary carnet + QuietLink corriger serait plus ledger | `HomeCoursesSheet` L122–137 |

Sheets 28 / poignée / Hub › / shortSheet (ex-P1-H1…T1) : **fermés en code** — ne pas rouvrir sauf régression.

---

## P2 — polish / cohérence

| Id | Note | Preuve |
|----|------|--------|
| **P2-1** | Snackbar FAIT = `PaperCard` + filet sous sticky — vérifier nuit doigt | `HomeScreen` L366–382 |
| **P2-2** | Widget crème `#F7F4EC` ≠ app `PaperCream` `#F6F3E8` — micro-dérive feuille | `widget_carnet_bg.xml` vs `A11yTokens` / `colors.xml` |
| **P2-3** | Pastille Auto or + forêt FAIT + brique + teal CTA = 4 chromas écran — **max 2 pastilles** reste sacré | `ExpenseStatusLine` |
| **P2-4** | Empty / BrandMark hors sticky : ne pas réintroduire illustration Accueil | CHARTE marque |
| **P2-5** | Wordmark Lock / onboarding OK ; garder hors sticky | composant `AppWordmark` |
| **P2-6** | Notif / Direct Boot 6.91 = hors DA affichage Accueil | journal |
| **P2-7** | LetterSpacing widget titre `0.1` : bon ledger ; ne pas l’étendre aux titres Compose | `widget_carnet.xml` |
| **P2-8** | CHARTE « icône calendrier » à aligner sur tap libellé | `CHARTE_ET_ARCHITECTURE.md` L29 |

---

## Densités — lecture senior

| Mode | Déclencheur | Effet code | Risque restant |
|------|-------------|------------|----------------|
| Confort | hauteur > 800 dp | Reste ~40 sp, pad sticky 12, épargne visible | Socle warm (P0-D1) |
| Compact | ≤ 800 dp | Reste 34 sp, épargne derrière Détails, gaps 4 | Sticky **2 dp** + Plus ouvert + snackbar = 1ʳᵉ facture mangée (doigt) |
| fontScale ≥ 1,25 | `rememberDisplayFit` | Plafond hero pré-compensé | Smoke **doigt** obligatoire |

**Verdict densités :** système **juste** (pas de branche modèle Samsung). Premium manquant = **respiration** sticky/liste/socle, pas un 3ᵉ breakpoint.

---

## Titres — matrice (état 6.91)

| Rôle | Composant | Style | Couleur | Écart restant |
|------|-----------|-------|---------|---------------|
| Page | `CarnetPageTitle` | `titleLarge` | InkMuted | OK |
| Page sheet courte | idem `shortSheet` | `titleMedium` | InkMuted | Fermé (ex-P1-T1) |
| Section | `CarnetSectionTitle` | `titleMedium` | InkMuted | OK fiche |
| Hub tappable | `CarnetHubRow` | `titleLarge` + › `titleLarge` muted | Ink + InkMuted | Fermé (ex-P1-H3) |
| QuietLink | `QuietLinkA11y` | body | Link | Annuler OK |
| Hero Reste | sticky | `displayLarge` serif | Ink / Danger | Concurrent pile sticky (P0-S1) |

---

## Gate « dans la main » (obligatoire, hors code)

Checklist **ouverte** (PROGRESS : S26 / restore doigt encore à faire Athalia) :

1. Sticky compact S26 : Reste lisible **avant** Courses ; Détails plié par défaut.
2. fontScale 1,3–1,5 : hero plafonné, 1ʳᵉ carte encore atteignable.
3. FAIT → snackbar Annuler : ne pas masquer la 1ʳᵉ échéance.
4. Sheet Courses / épargne / Menu : poignée + clearance pouce edge-to-edge.
5. Fiche IME : un Primary ; Annuler revient.
6. Widget bureau clair + launcher sombre + bio masqué.
7. Ne **pas** monter un claim « 9/10 » / badge marketing tant que cette liste n’est pas cochée au doigt.

---

## Synthèse une phrase

**Le craft 6.91 a fermé le lot « quiet UI » de final-01 en code ; le premium restant = faire respirer sticky/cartes/socle et fumer S26 — pas un nouveau redesign de marque.**

---

## Domaine couvert / limites

- **Domaine couvert :** DA affichage Accueil (sticky, cartes, densités), sheets hub, fiche, widget, titres — lecture senior carnet papier.
- **Non audité :** sécurité crypto 6.91, notif icons, CI, restore, a11y TalkBack exhaustif, nuit wallpaper.
- **Limites :** pas de smoke doigt / screenshot device dans cet appel ; notes « dans la main » = estimation honnête, pas mesure.
- **aucune modification faite** au produit (uniquement ce rapport d’audit demandé).

*Audit premium-01 DA affichage · `0.1.0-6.91` · AUDIT ONLY.*
