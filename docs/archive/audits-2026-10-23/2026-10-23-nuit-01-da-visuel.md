> **ARCHIVÉ / FIGÉ** (2026-10-26) — ne plus lire pour l’état code. Vérité = `PROGRESS_LOG.md` + PRIVACY/SECURITY + audits `final-0*`.

# Audit DA — Mon Mois Clair (visuel)

| | |
|--|--|
| **Date** | 2026-10-23 · nuit-01 |
| **Profil** | Directrice artistique senior — apps mobiles carnet / papier 2026–27 |
| **Périmètre** | Compose (tous écrans) · widget home · small icon notif · launcher |
| **État code** | `0.1.0-6.75` |
| **Pairs** | Bill reminder premium (Smart Bill Reminder, Billiary, Flying Bills, AllPaid) — **barème papier**, pas fintech (Monarch / Copilot / Origin) |
| **Charte figée** | Crème · **un** teal `#1B4D4E` · serif = **Reste seulement** · **pas** de logo sticky |
| **Mode** | AUDIT ONLY — zéro édition produit |

---

## Verdict

**7,5 / 10**

Direction **tenue** : objet papier crédible, hors purple / glass / fluo.  
Craft **pas encore « carnet terminé »** : le Reste commande dans l’app, mais le **premier contact hors-app** (widget) et la **densité Accueil** freinent le sentiment premium 2026–27.

> Pas un claim « 9/10 dans la main » — smoke doigt encore requis (aligné journal 6.75).

| Surface | Note DA | Lecture |
|---------|---------|---------|
| Système de design (tokens, typo, formes) | **8,5** | Palette calme, hiérarchie PageTitle/Hub, coins différenciés |
| Accueil (sticky + liste) | **7** | Hero Reste OK ; sticky encore bavard ; cartes riches |
| Hub / sheets / fiches | **8** | Tokens 6.74 cohérents ; formulaire long mais teal maîtrisé |
| Widget | **6,5** | Mise en page ledger bonne ; **fracture serif** (sans système) |
| Notif (icône + teal) | **7,5** | Silhouette claire + canal teal ; pas de signature visuelle riche |
| Launcher / splash | **8** | M / carnet ivoire sur crème — juste |

---

## Ce qui est déjà « pair premium papier »

- **Fond crème** `#F6F3E8`, cartes `#FFFDF9`, encre `#1E1B18`, socle `PaperWarm` — pas de blanc clinique ni de dark Copilot.
- **Teal unique** boutons / liens jour / notif `setColor` / launcher — pas de second accent marketing.
- **Serif (Source Serif 4)** réservée au chiffre Reste (`displayLarge` + `DisplayFit` 34/40 sp) — mois, libellés, montants de ligne en sans.
- **Sticky sans wordmark** — mois + Reste ; `BrandMark` seulement flash chargement (pas empty).
- **Grammaire papier** : pastilles capsule ≠ boutons 12 dp ; FAIT **forêt** (pas 2ᵉ teal barre) ; ombre courte encre ~8–10 % ; pas de dégradé teal, pas de blur glass.
- **Nuit** = encre inversée, **même** teal bouton — pas teinte wallpaper.
- Widget : libellé discret · montant · filet · prochaine ligne — bon schéma « carnet ouvert sur le bureau ».
- Notif : `ic_notification` blanc opaque + teal `#1B4D4E` — lisible type WhatsApp, aligné marque.

---

## P0 — fracture marque / première impression

| Id | Constat | Pourquoi c’est P0 |
|----|---------|-------------------|
| **P0-W1** | Widget Reste en `sans-serif-medium` 36 sp (RemoteViews) alors que l’app promet **serif = Reste** | Le widget est le logo vivant sur One UI. Les pairs premium soignent ce chiffre ; ici la marque **se contredit** hors process. |
| **P0-H1** | Sticky Accueil : mois en `headlineMedium` **Ink** + nav 4 cibles + lignes secondaires (À payer, Courses, Plus…) au-dessus / autour du hero | Le Reste est codé hero, mais **visuellement dilué** — look « bandeau outil » plus que « page de carnet ». Premium bill = un chiffre qui **commande le viewport**. |

Pas de P0 palette / purple / logo sticky : la charte directionnelle tient.

---

## P1 — craft demain (gain premium net)

| Id | Surface | Constat | Intention DA |
|----|---------|---------|--------------|
| **P1-H2** | Accueil | Sandwich `PaperWarm` sticky + crème liste + `PaperWarm` barre = trois bandes | Une **feuille** dominante : sticky un cran plus « page » (ou filet seul), barre = socle discret — pas deux plages chaudes égales. |
| **P1-H3** | Cartes échéance | Label · cause · cadence · montant `titleLarge` · pastilles · Copier/Photo · FAIT | Trop de chrome vs Flying Bills / Billiary « une ligne, une action ». Hiérarchie : **jour + nom + montant + FAIT** ; le reste en 2ᵉ souffle. |
| **P1-H4** | Mois sticky | Titre mois trop fort (Ink + 24 sp) face au Reste | Mois = **InkMuted** / un cran plus petit — le serif Reste redevient le seul pic. |
| **P1-W2** | Widget | Muted jour `#3F3A33` ≠ `InkMuted` `#4A453C` ; filet `#C9C0B0` ≠ `PaperBorder` | Aligner **exactement** les tokens Day/Night — zéro dérive « quasi crème ». |
| **P1-N1** | Notif | Small icon carnet+coche lisible, mais à 24 dp = « notes générique » ; pas de large/bitmap marque | Affiner la silhouette (filet or / coche plus nette à petite taille) ; garder teal canal. Pas de glass. |
| **P1-C1** | Pastilles | Or Auto + forêt FAIT + brique retard + teal CTA | Quatre chromas actifs. Tenir le système, mais **max 2 pastilles** (déjà codé) doit rester sacré — éviter toute 3ᵉ couleur UI. |
| **P1-F1** | Fiche / Entry | Long scroll ; TypeChip teal sélection OK | Rythme vertical : plus d’air entre blocs, moins de `PaperWarm` boîtes empilées — même langage Accueil. |

---

## P2 — polish / cohérence

| Id | Note |
|----|------|
| **P2-1** | Lien nuit `#9AD8D8` : nécessaire contraste, dérive « cyan soft » — surveiller vs teal jour (pas le remplacer par purple). |
| **P2-2** | Ombre **+** bordure sur chaque carte : un peu Material stock ; tester filet seul sur crème (relief papier plus « imprimé »). |
| **P2-3** | Wordmark Réglages / Aide / Lock / onboarding : OK identité ; garder **hors** Accueil sticky (déjà). |
| **P2-4** | Canal notif : pas de `lightColor` canal — optionnel ; `setColor` notif suffit. |
| **P2-5** | Widget coins 14 dp vs cartes Compose 16 dp — harmoniser 16 si One UI le permet. |
| **P2-6** | Snackbar FAIT compact bordée : bon ; vérifier nuit (PaperCard sur crème nuit). |
| **P2-7** | Empty mois sans BrandMark : correct ; ne pas réintroduire illustration marketing. |

---

## Top chantiers demain

Ordre DA (impact / risque charte) :

1. **Widget = même Reste que l’app** — serif (ou équivalent RemoteViews) + tokens muted/rule alignés `A11yTokens` (**P0-W1**, **P1-W2**).
2. **Accueil : quieting sticky** — mois muted ; réduire concurrence visuelle avec Reste ; garder Courses accessible (**P0-H1**, **P1-H4**).
3. **Cartes : alléger le premier regard** — montant + FAIT dominants ; secondary en retrait (**P1-H3**).
4. **Unifier le plan papier Accueil** — sticky / page / socle (**P1-H2**).
5. **Notif silhouette** — micro-pass lisibilité 24 dp (**P1-N1**), smoke shade One UI.

Hors scope demain (volontaire) : mode sombre redesign, 2ᵉ accent, glass, graphiques, logo sticky.

---

## Ne pas toucher

Verrous directionnels — toute « amélioration » ici **casse** le positionnement vs pairs papier :

| Verrou | Raison |
|--------|--------|
| Palette crème / encre / teal `#1B4D4E` | Identité anti-fintech |
| Serif **uniquement** pour le Reste (in-app) | Signature carnet ; pas titres, pas boutons |
| Pas de logo / wordmark sur sticky Accueil | Ledger, pas landing |
| FAIT forêt `#1E5631` ≠ teal barre « + Ajouter » | Un primary barre, une action ligne |
| Pastille Auto or **fond** seulement (jamais texte or) | Contraste + tampon papier |
| Brique `#8E3418` (pas rouge sang `#FF0000`) | Anxiété AMOLED |
| Pas de purple, indigo, glass, blur, glow, dégradé teal | Hors pairs bill reminder papier |
| Pas de 2ᵉ hero Accueil (camembert, solde banque, logo) | Un chiffre commande |
| Nuit = même teal bouton, fonds encre fixes | Pas wallpaper / Copilot |
| PageTitle / SectionTitle **InkMuted** ; HubRow **Ink** | Matrice 6.72–6.74 |
| Cibles ≥ 48 dp, corps ≥ 18 sp, contraste ≥ 7:1 | Famille Galaxy, non négociable |
| Empty Accueil sans BrandMark | H08 |

---

## Synthèse une phrase

**Bon carnet en Compose ; premium 2026–27 = faire mentir le widget et faire taire le sticky pour que le Reste serif soit le seul héros — partout, y compris sur l’écran d’accueil du téléphone.**
