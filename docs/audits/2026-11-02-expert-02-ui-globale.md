# Audit expert-02 — UI globale (DA senior)

| Champ | Valeur |
|-------|--------|
| Date | 2026-11-02 |
| Branche / version | `develop` · `0.1.0-7.06` · `versionCode` 111 |
| Domaine | **Toutes surfaces UI** — Accueil, fiche, sheets, Réglages, Dossiers, Aide, widget |
| Mode | **AUDIT ONLY** · inspection code Compose / XML / charte · **pas** de correction · **pas** smoke doigt Athalia dans cet appel |
| Référents | `CHARTE_ET_ARCHITECTURE.md` § Accueil / affichage / hub · audits 2026-11-02 (sticky, hub, aide, retard) · premium-00 / clean-10 (historique) |

---

## Verdict DA

| | Note | Lecture |
|--|-----:|---------|
| **Craft carnet (code)** | **7,5 / 10** | Système papier + rôles typo + ledger Accueil = sérieux ; densités fiche / Aide + ombre Dossiers + widget RemoteViews empêchent le 8,5+ |
| **Premium boutique / cadeau dans la main** | **NON VÉRIFIÉ** | Re-smoke Accueil 7.06 + S26 + fontScale = Athalia (journal) — **pas** de claim 9/10 ni GO cadeau |

**Phrase unique :** le carnet belge se lit dans le code (crème, filets, Reste serif, FAIT forêt, hub rangé) ; l’expérience débutante 60+ reste freinée par **fiche longue**, **Aide longue**, et **preuves doigt ouvertes** — premium = craft + main, pas craft seul.

---

## 1. Carte des surfaces (état 7.06)

| Surface | Fichiers pivots | Impression DA (inspection) |
|---------|-----------------|----------------------------|
| **Accueil sticky** | `HomeStickyHeader` · `HomeStickyChrome` · `DisplayFit` | Post-7.06 : mois · Reste hero · statut · **1 rangée** Courses\|épargne · Détails. Objectif liste visible — **hauteur réelle NON VÉRIFIÉE** au doigt |
| **Accueil liste** | `HomeTimelineRows` · `HomeScreen` | Cartes filet seul (pas d’ombre) · FAIT 96 · pastilles max 2 · compact allège meta — quiet ledger |
| **Socle** | `HomeBottomBar` | Crème unie, 0 elevation · Menu + Ajouter (ou Mois en cours) — aligné charte |
| **Sheets** | Menu · Courses · Savings · MonthPicker | `PaperCream` · handle carnet · PageTitle InkMuted · sheets courtes = premium ; Menu allégé OK |
| **Fiche** | `EntryFormScaffold` (~563 L) · sections Payment / Dossier / Chrome | Qui → montant → jour → sections repliables — **bon rythme**, **trop de matière** pour une 1ʳᵉ ouverture |
| **Réglages** | `SettingsScreen` · `SettingsHubCopy` | 3 groupes + raccourci « Code et copie » · Échéances sous Données (7.04) — hub **propre** |
| **Dossiers** | `DossiersScreen` | Pas de KPI ; En cours / Soldés ; **ombre 2 dp** = dérive Material vs Accueil filet |
| **Aide** | `HowItWorksCopy` (12 sections) | Couverture métier bonne ; **mur de prose** pour 60+ |
| **Widget** | `widget_carnet.xml` · `CarnetWidgetRefresher` | Reste + ligne · jour/nuit · serif **système** (contrainte) — esprit carnet, pas Source Serif embarquée |

---

## 2. Forces (ce qui tire vers le premium carnet)

1. **Système de design verrouillé** — `A11yTokens` Day/Night, Source Serif **uniquement** chiffre Reste / wordmark, Source Sans ≥ 18 sp, pastilles `StampShape`, filets papier, teal `#1B4D4E`, FAIT forêt, retard brique contrasté (charte § couleurs).
2. **Rôles typo** — PageTitle InkMuted ≠ HubRow Ink+› ≠ QuietLink (Menu, sheets, hub) : `CarnetTitles` / `CarnetHubRow` = discipline rare chez un MVP.
3. **Accueil = ledger** — pas de logo sticky, pas Aide/Restore dans Menu, socle 2 boutons, anti-dashboard (charte).
4. **7.06 sticky** — `StickyToolsRow` (2 cellules ≥ 48) remplace 2× `StickyLinkRow` empilées ; paddings/gaps resserrés ; Courses compact sans « il reste » (sheet / TalkBack).
5. **Hub Réglages 7.04** — Échéances hors Confort ; « Jour du Mettre de côté » vs montant Accueil ; Aide directe « Comment ça marche ».
6. **Retard / épargne** — `ExpenseOverdue.appliesToLabel` exclut « Mettre de côté » (plus de pastille brique sur geste volontaire — fix post-audit retard).
7. **Widget** — fond crème/nuit aligné tokens ; montant serif système ; ligne overdue en danger ; pas de chrome Copilot.

---

## 3. Faiblesses débutant vs premium — P0 / P1 / P2

### P0 — bloquants perception « cadeau / pro »

| Id | Faiblesse | Preuve (inspection) | Impact débutante |
|----|-----------|---------------------|------------------|
| **P0-MAIN** | Accueil 7.06 **non revalidé au doigt** (liste visible ? Courses\|épargne lisibles ?) | `PROGRESS_LOG` : smoke Athalia ouvert ; cet audit = code seul | Sans preuve main, sticky « premium » reste une promesse |
| **P0-FICHE** | Fiche ajout/édition = **parcours dense** dès l’ouverture (identité + montant + grille + Paiement & preuve + Retard ou accord + récurrence…) | `EntryFormScaffold` scroll long ; sections repliables mais **hints + champs** toujours présents en esprit ; `EntryFormScreen` ~793 L | Débutante se sent « app de pro » / panique ; premium carnet = 1 geste clair à la fois |
| **P0-AIDE** | Aide = **12 blocs** de corps utile mais empilés | `HowItWorksCopy.sections` | 60+ abandonne avant « Facture papier » / « Résumé » — premium = 5 portes max + détail à la demande |

> **Hors P0 UI pure (rappel croisé, non re-audité ici) :** restore `.mmc` + S26 D1–D7 + QuietLink mail = toujours Athalia (premium-00 / clean-10).

### P1 — gain net craft / cohérence carnet

| Id | Faiblesse | Preuve | Fix DA (conseil, **non appliqué**) |
|----|-----------|--------|-------------------------------------|
| **P1-SHADOW** | Dossiers : `.shadow(2.dp)` + bordure ; Accueil : **filet seul** « imprimé » | `DossiersScreen` L338–346 / L415–423 vs `HomeTimelineRows` Surface+border | Aligner Dossiers (et Upcoming si carte) sur filet 0 elevation |
| **P1-FORM-HIÉR** | `FormSectionToggle` = `titleLarge` + **Ink** (poids HubRow) sur fond crème | `EntryFormChrome` L40–43 | SectionToggle → titleMedium ou InkMuted ; réserver Ink aux actions |
| **P1-COMPACT** | Sur ≤800 dp : meta cause/cadence **cachée** ; Accord chip en titre ; tip prélèvement OK | `ExpenseRow` compact branches | Sur S26, carte peut paraître « incomplète » vs Ultra — accepter ou 1 ligne meta ellipsis |
| **P1-WIDGET-SP** | Titre 14 sp / ligne 16 sp &lt; plancher in-app 18 sp | `widget_carnet.xml` | RemoteViews limité ; viser ≥15–16 titre et ≥16–17 ligne si host tient ; documenter écart |
| **P1-STICKY-ELLIPSIS** | Rangée outil demi-largeur : libellés longs → ellipsis | `StickyToolCell` maxLines 1 + weight | Tester « Mettre de côté 120,00 € » sur ~360 dp large |
| **P1-RÉSUMÉ-2P** | Résumé = Réglages Données **et** Dossiers | Hub + `DossiersScreen` boutons texte/PDF | Pas faux ; clarifier 1 chemin privilégié dans Aide (déjà « ou ») |
| **P1-DEPTH** | Hub → groupe → sujet = 2 niveaux OK, mais **Code et copie** + **Sauvegarde** = 2 portes | `SettingsScreen` + `SettingsAccountGroupScreen` | Volontaire 60+ ; garder, mais tip pied plus court si encore « fouillis » oral |

### P2 — polish / dette DA

| Id | Faiblesse | Note |
|----|-----------|------|
| **P2-HELP-ROUTE** | `SettingsHelpScreen` quasi orphelin (hub bypasse) | Cosmétique nav |
| **P2-NIGHT-LINK** | Lien nuit teal soft `#93D2CB` (P1-N1) — OK contraste ; risque « un cran digital » vs encre | Surveiller sur PaperWarm |
| **P2-SHEET-GAP** | Gaps Menu 10–14 vs sticky 4–6 : volontaire ; pas d’unifier aveuglément | |
| **P2-WORDMARK** | Wordmark seulement pied Réglages — correctement hors Accueil | Ne pas ramener sur sticky |
| **P2-ALERT-BADGE** | « ! » Menu si alertes bloquées — utile ; pastille warn sur Secondary peut crier | Smoke S26 |

---

## 4. Notes par surface (/10 craft code)

| Surface | /10 | 1 ligne |
|---------|----:|---------|
| Accueil sticky (7.06 code) | **8,0** | Hero + outils 1 ligne = bon carnet ; doigt ouvert |
| Accueil liste + socle | **8,2** | Filets, FAIT, QuietLink 2ᵉ rang — quasi premium ledger |
| Sheets (Menu / Courses / épargne / mois) | **8,0** | Courtes, crème, PageTitle ; Menu charte |
| Fiche | **6,5** | Rythme FORM01 OK ; densité = fail débutante |
| Réglages hub | **8,0** | Post-7.04 clair ; double porte code assumée |
| Dossiers | **7,0** | Info juste ; ombre Material = faux pas DA |
| Aide | **6,8** | Juste et longue ; pas encore « mode carnet court » |
| Widget | **7,5** | Esprit Reste + ligne ; sp + police système = plafond |

**Moyenne indicative pondérée (craft) : ~7,5 / 10.**

---

## 5. Grille « débutant 60+ » vs « premium carnet »

| Critère | Débutant (risque) | Premium carnet (cible) | Écart 7.06 |
|---------|-------------------|------------------------|------------|
| 1ʳᵉ ouverture Accueil | Voir **plusieurs** factures | Liste commande sous Reste | Code **visé** ; doigt **NON VÉRIFIÉ** |
| Vocabulaire | Peu de tiroirs | 3 groupes max + 1 raccourci vital | Hub **OK** |
| Fiche | 1 intention / écran | Sections rares, repliées, hints 1 ligne | **Écart fort** |
| Aide | 5 questions | Accordion ou top-5 | **Écart fort** |
| Relief | Filet papier | Pas d’ombre Material flottante | Dossiers **écart** |
| Widget | Reste lisible | Même famille typo in-app | **Écart soft** (contrainte OS) |
| Cohérence retard | Pas de cri sur l’épargne | Pastille brique = vraie dette | **Code OK** (exclusion label) |

---

## 6. Priorisation produit (ordre DA, sans implémenter)

1. **Athalia** : re-smoke Accueil 7.06 (sticky + liste + Courses\|épargne + Menu) sur device cadeau / S26 si possible → ferme ou rouvre **P0-MAIN**.
2. **Fiche** : réduire charge visuelle 1ʳᵉ visite (Paiement / Accord vraiment fermés + moins de chrome Ink ; éventuellement regrouper) → **P0-FICHE**.
3. **Aide** : top-5 + « Voir plus » ou sections secondaires repliées → **P0-AIDE**.
4. **Dossiers** : retirer shadow, filet comme Accueil → **P1-SHADOW**.
5. Widget / ellipsis sticky : polish **P1** après smoke.

---

## 7. Limites de cet audit

| Limite | Statut |
|--------|--------|
| Smoke doigt / dump UI device 7.06 | **NON VÉRIFIÉ** |
| TalkBack / fontScale ≥ 1,3 / Device Care | **NON VÉRIFIÉ** |
| Tests Gradle relancés ici | **non exécuté** (audit DA, pas vérificateur) |
| Domaines sécu / archi / packaging | **non audités** (un seul domaine : UI globale) |
| Audits du jour (sticky, hub, aide, retard) | **DÉCLARÉS** comme contexte ; relecture croisée code **VÉRIFIÉE** ci-dessus |

**Domaine couvert :** UI globale (DA senior) · surfaces Accueil / fiche / sheets / Réglages / Dossiers / Aide / widget.  
**aucune modification faite** *(hors rédaction de ce fichier d’audit demandé par Athalia)*.
