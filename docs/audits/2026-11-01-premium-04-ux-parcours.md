# Audit UX — parcours Accueil → FAIT → paiement → sauvegarde → Aide

| | |
|--|--|
| **Date** | 2026-11-01 · premium-04 |
| **Profil** | UX senior — public **maman 60+ Belgique** (factures BE, FAIT, Reste, restore code) |
| **Périmètre** | Parcours **Accueil → FAIT → paiement (Copier / banque) → sauvegarde / restore code → Aide** · copy · charge cognitive · One UI |
| **État code** | `versionName` **0.1.0-6.91** (`app/build.gradle.kts`) · journal **6.91** (`PROGRESS_LOG.md`) |
| **Mode** | **AUDIT ONLY** — zéro édition produit · **pas de commit** |
| **Workspace** | `/Volumes/T7/Mon Mois Clair` (chemin confirmé) |

---

## Verdict

**7,4 / 10** (parcours **lu en code**) · **~5–6 / 10 « dans la main »** (smoke S26 / restore doigt encore ouverts — journal)

Le socle métier belge tient : **Reste** hero, **FAIT ✓** forêt ≥ 96 dp, **Copier** → communication structurée, **code de copie** gros + papier, Aide en accordéon familial. Pour une maman 60+ sur Galaxy One UI, ce n’est plus « fintech confuse » — c’est un carnet **presque** prêt.

Ce qui empêche le premium parcours : **profondeur Réglages** (Sauvegarde / Aide trop loin), **cartes échéance encore riches**, **jargon restore** (« chiffrée », ZIP/JSON), et **Aide** encore dense pour un premier recours. Pas un claim « 9/10 dans la main ».

| Étape | Note UX code | Lecture maman 60+ |
|-------|-------------:|-------------------|
| Accueil (sticky + liste) | **7,6** | Reste clair ; chrome sticky + carte encore concurrent |
| FAIT | **8,2** | Geste fort ; snackbar / mois passé / aperçu OK |
| Paiement (Copier → banque) | **7,5** | Copy +++ solide ; pas de pont in-app vers la banque |
| Sauvegarde / restore code | **6,8** | Contenu bon ; chemin long + jargon residual |
| Aide | **7,0** | Ton familial ; 11 sections + profondeur hub |

---

## Méthode & limites

| | |
|--|--|
| **Méthode** | Inspection sources Compose / copy figée / charte / notice famille |
| **Non fait** | Smoke doigt S26 · restore `.mmc` réel · connected androidTest · captures |
| **Nature dominante** | inspection (contenu lu ≠ validation fonctionnelle) |

Claims journal « smoke / GO cadeau ouverts » = **DÉCLARÉ** (historique), pas rejoués ici.

---

## Parcours étape par étape

### 1. Accueil — Reste, liste, One UI

**Constat A1 — Hero Reste prioritaire (mois quiet).**  
‹ mois › = 3 cibles (`MonthStep` + libellé calendrier) en `titleMedium` InkMuted ; chiffre `displayLarge` serif.  
- **Statut :** VÉRIFIÉ  
- **Nature :** inspection  
- **Preuve :** `HomeStickyHeader.kt` — « Quiet sticky : titleMedium muted — le Reste serif commande » ; libellés `Reste après factures` / déficit / aperçu.

**Constat A2 — Sous-Reste encore multi-signaux.**  
« À payer » / « Tout est payé » / Courses › / parfois épargne / « Détails » (Après les FAIT, courriers, tip revenu). Compact S26 : épargne derrière « Détails ».  
- **Statut :** VÉRIFIÉ  
- **Nature :** inspection  
- **Preuve :** `HomeStickyHeader.kt` + `StickyStillToPayLabel.kt` (`Après les FAIT…`).

**Faiblesse UX :** pour 60+, « Reste » vs « À payer » vs « Après les FAIT » = **3 euros proches**. Aide le dit ; l’écran ne le séquence pas assez.

**Constat A3 — Carte facture riche (charge avant FAIT).**  
Jour · nom · montant `titleLarge` · meta cause/cadence · FAIT 96 · pastilles (max 2) · rangée Copier/Photo.  
- **Statut :** VÉRIFIÉ  
- **Nature :** inspection  
- **Preuve :** `HomeTimelineRows.kt` (`ExpenseRow` / `ExpenseStatusLine` / QuietLink « Copier »).

**Constat A4 — Zone pouce One UI tenue en code.**  
Socle `Menu` · `+ Ajouter` ; filet ; `shadowElevation = 0` ; badge `!` Alertes.  
- **Statut :** VÉRIFIÉ  
- **Nature :** inspection  
- **Preuve :** `HomeBottomBar.kt` ; charte §8 zone pouce. Sensation S26 = **NON VÉRIFIÉ** (pas de smoke).

**Constat A5 — Menu allégé : pas Aide / Restore.**  
Contact QuietLink bas ; Aide/Restore = Réglages (charte).  
- **Statut :** VÉRIFIÉ  
- **Nature :** inspection  
- **Preuve :** `HomeMenuSheet.kt` ; `CHARTE_ET_ARCHITECTURE.md` §7–8 ; `AppNavHost.kt`.

---

### 2. FAIT — check-off

**Constat F1 — Action claire, vocabulaire fort.**  
« FAIT ✓ » Primary forêt ; « Remettre » Secondary ; a11y « Marquer FAIT » / « Remettre à payer » ; min 96 dp.  
- **Statut :** VÉRIFIÉ  
- **Nature :** inspection  
- **Preuve :** `HomeTimelineRows.kt` ; charte check-off.

**Constat F2 — Feedback immédiat + Annuler.**  
Mois courant : haptique + snackbar « Noté FAIT » / « Remis à payer » / dossier ; `Annuler` ; durée Long ; bandeau sous sticky.  
- **Statut :** VÉRIFIÉ  
- **Nature :** inspection  
- **Preuve :** `HomeScreen.kt` `onFaitToggle`. Comportement device = **NON VÉRIFIÉ**.

**Constat F3 — Garde-fous mois passé / aperçu.**  
Passé → dialogue ; futur → « Aperçu seulement… ».  
- **Statut :** VÉRIFIÉ  
- **Nature :** inspection  
- **Preuve :** `HomeScreen.kt` ; `HomeDialogs.kt` ; `HomeViewModel.kt`.

**Constat F4 — Preuve visuelle post-FAIT.**  
Fond `AccentOkSoft` + pastille `FAIT le …`.  
- **Statut :** VÉRIFIÉ  
- **Nature :** inspection  
- **Preuve :** `HomeTimelineRows.kt` `ExpenseStatusLine`.

**Faiblesse UX :** sur prélèvement, le bon moment est « quand le débit apparaît » (Aide) — **invisible sur la carte** (pastille Prélèvement sans tip inline).

---

### 3. Paiement — Copier → banque (BE)

**Constat P1 — Copier = communication, pas magie banque.**  
QuietLink « Copier » si `paymentNote` ; `BelgianStructuredCommunication.prepareCopy` → 12 chiffres si +++ valide ; message « Copié — collez dans votre application bancaire. »  
- **Statut :** VÉRIFIÉ  
- **Nature :** inspection  
- **Preuve :** `HomeTimelineRows.kt` ; `HomeViewModel.copyPaymentNote` ; `BelgianStructuredCommunication.kt` (`COPIED_OK`, `VERIFY_ON_MAIL`).

**Constat P2 — Pas de deep-link / intent banque.**  
Parcours = copier → quitter app → coller (Belfius / BNP / etc.). Attendu local-first ; friction réelle pour 60+.  
- **Statut :** VÉRIFIÉ (absence d’intent)  
- **Nature :** inspection  
- **Preuve :** presse-papiers seul ; charte « Zéro banque ».

**Constat P3 — Fiche « Paiement & preuve » repliée.**  
Section fermée par défaut ; hint « QR, +++ , photo, PDF » ; Scanner QR / Prélèvement / Copier.  
- **Statut :** VÉRIFIÉ  
- **Nature :** inspection  
- **Preuve :** `EntryFormPaymentSection.kt` FORM01.

**Constat P4 — IBAN hors Accueil.**  
« Copier l’IBAN » = dossier / accord, pas carte Accueil.  
- **Statut :** VÉRIFIÉ  
- **Nature :** inspection  
- **Preuve :** `EntryFormDossierSection.kt`.

**Faiblesse UX :** ordre mental « payer puis FAIT » n’est pas guidé sur la carte (Copier sous FAIT = bon craft, pas un script). Invalid +++ → « Vérifiez sur le courrier. » = fort, peu de next step.

---

### 4. Sauvegarde — code de copie / restore

**Constat S1 — Contenu Sauvegarde familial (quand on y est).**  
Code `headlineMedium` ; « Notez-le une fois… » ; 3 copies Documents ; Envoyer / Dossier / Restaurer (Danger).  
- **Statut :** VÉRIFIÉ  
- **Nature :** inspection  
- **Preuve :** `SettingsPages.kt` `SettingsBackupScreen` ; `BackupHealthCopy.kt`.

**Constat S2 — Profondeur trop longue pour 60+.**  
Accueil → Menu → Réglages → **Données & sauvegarde** → **Sauvegarde** (+ restore encore un cran).  
- **Statut :** VÉRIFIÉ  
- **Nature :** inspection  
- **Preuve :** `AppNavHost.kt` ; `SettingsScreen` / `SettingsAccountGroupScreen` ; charte « Aide / restore → Réglages ».

**Constat S3 — Restore : confirmation + code, jargon residual.**  
Intro claire ; champ « Code de copie » 8 chiffres ; confirm destructif ; opt-in « Ancien format (ZIP/JSON clair) ». Messages `RestoreMessages` : « copie **chiffrée** », ZIP/JSON.  
- **Statut :** VÉRIFIÉ  
- **Nature :** inspection  
- **Preuve :** `RestoreBackupScreen.kt` ; `RestoreMessages.kt` (`CLEARTEXT_REJECTED`, `CLEARTEXT_GRACE_WARNING`).  
**Désaccord copy :** journal / notice « sans jargon chiffré » (**DÉCLARÉ**) vs restore encore « chiffrée » (**VÉRIFIÉ**).

**Constat S4 — Notice famille aligne l’oral, pas l’in-app court.**  
Code papier ; jamais code + `.mmc` même message ; ZIP à jeter.  
- **Statut :** VÉRIFIÉ (doc)  
- **Nature :** inspection  
- **Preuve :** `docs/NOTICE_FAMILLE_2MIN.md`. Restore doigt = **NON VÉRIFIÉ**.

**Faiblesse UX :** P0 parcours = **trouver** Sauvegarde + **comprendre** `.mmc` / ZIP sans aide orale.

---

### 5. Aide — Comment ça marche

**Constat H1 — Hub Aide = raccourci direct HowItWorks.**  
`onOpenHelp` → `HowItWorks` (pas `SettingsHelp` intermédiaire).  
- **Statut :** VÉRIFIÉ  
- **Nature :** inspection  
- **Preuve :** `AppNavHost.kt` L147–148. `SettingsHelpScreen` existe mais n’est pas la porte hub.

**Constat H2 — Accordéon 11 sections, ton familial.**  
Reste / Courses / FAIT / retard / alertes / facture papier / accord / épargne / année / sauvegarde / empreinte.  
- **Statut :** VÉRIFIÉ  
- **Nature :** inspection  
- **Preuve :** `HowItWorksCopy.kt` ; `HowItWorksScreen.kt` (« Touchez un titre pour lire. »).

**Constat H3 — Section Sauvegarde Aide encore technique.**  
« .mmc », « ZIP claires », chemin Réglages → Sauvegarde. Correct, dense pour 1ʳᵉ panique.  
- **Statut :** VÉRIFIÉ  
- **Nature :** inspection  
- **Preuve :** `HowItWorksCopy` section « Sauvegarde ».

**Faiblesse UX :** Aide absente du Menu (volontaire) → **3 taps** minimum Accueil→Aide. Contact ≠ Aide (Menu QuietLink) = bien, mais « je ne comprends pas » n’a pas de porte Accueil.

---

## Charge cognitive (synthèse)

| Moment | Charge | Pourquoi |
|--------|--------|----------|
| Ouvrir Accueil mois courant | Moyenne | Reste OK ; lignes sticky + cartes riches |
| FAIT une facture simple | Basse | Un bouton + snackbar |
| Payer via +++ puis FAIT | Moyenne–haute | Copier → banque externe → revenir → FAIT |
| Noter le code de copie | Haute | Profondeur + enjeu perte |
| Restore nouveau téléphone | Haute | Code + fichier + confirm + legacy |
| Comprendre Reste vs Après les FAIT | Moyenne | 3 montants ; détail replié |

One UI (cibles ≥ 48, FAIT ≥ 96, sheets crème, socle sans ombre) : **tenu en code**. Sensation police / safe area S26 : **NON VÉRIFIÉ**.

---

## Ce qui est déjà premium (ne pas casser)

| Item | Preuve |
|------|--------|
| Reste serif = signal n°1 | `HomeStickyHeader` + charte |
| FAIT forêt / Remettre / Annuler snackbar | `HomeTimelineRows` / `HomeScreen` |
| Copier +++ → 12 chiffres banque | `BelgianStructuredCommunication` |
| Menu sans Aide/Restore ; contact QuietLink | `HomeMenuSheet` / charte |
| Code de copie gros + « notez une fois » | `SettingsBackupScreen` |
| Aide accordéon testable | `HowItWorksCopy` + tests dédiés (existence **DÉCLARÉE** via arborescence) |
| Restore confirm Danger + succès « Voir le mois » | `RestoreBackupScreen` |

---

## P0 — bloque le parcours maman (premium)

| Id | Zone | Constat | Amélioration premium (intention, pas patch) |
|----|------|---------|-----------------------------------------------|
| **P0-U1** | Sauvegarde | 4 niveaux Accueil→…→Sauvegarde ; code vital hors vue | Raccourci « Votre code / copie » plus proche (Réglages hub 1ʳᵉ ligne ou 1er onboarding post-install) — **sans** remettre Restore dans Menu Accueil |
| **P0-U2** | Restore copy | « chiffrée », ZIP/JSON, `.mmc` | Remplacer par « copie protégée / ancienne copie » + une phrase « notez le code à part » ; zéro jargon crypto |
| **P0-U3** | Accueil cartes | Trop d’info avant le geste payer/FAIT | Hiérarchie Flying-Bills-like : jour · nom · montant · FAIT ; Copier reste 2ᵉ rangée ; meta plus discrète |

Pas de P0 sur le **mot** FAIT ni sur le Reste hero : direction tenue.

---

## P1 — gain premium net

| Id | Zone | Constat | Intention |
|----|------|---------|-----------|
| **P1-U1** | Sticky | Reste / À payer / Après les FAIT | Micro-copy ou ordre : un seul « second chiffre » visible ; Après les FAIT seulement dans Détails (déjà partiel) — vérifier confort non-compact |
| **P1-U2** | Prélèvement | Tip Aide seulement | Une ligne muted sous pastille Prélèvement : « Cochez FAIT quand le débit apparaît » |
| **P1-U3** | Paiement | Pas de script Copier→banque→FAIT | Après Copié : snackbar qui rappelle « Collez à la banque, puis FAIT ici » (une fois / court) |
| **P1-U4** | Aide | 11 sections ; Sauvegarde dense | Mettre **Sauvegarde** + **FAIT** en tête ; raccourcir corps Sauvegarde au niveau notice 2 min |
| **P1-U5** | Aide accès | 3 taps | Garder hors Menu ledger, mais hub Réglages : libellé « Aide — Comment ça marche » plus explicite |
| **P1-U6** | +++ invalide | « Vérifiez sur le courrier » | Ajouter next step soft : « ou Scanner le QR » |

---

## P2 — polish / cohérence

| Id | Note |
|----|------|
| **P2-U1** | `SettingsHelpScreen` quasi mort si hub → HowItWorks direct — clarifier nav / éviter écran fantôme |
| **P2-U2** | Aligner Aide « jamais code + .mmc » avec écran Sauvegarde (phrase unique, même ordre) |
| **P2-U3** | Snackbar FAIT vs snackbar Copié : priorité « Annuler FAIT » déjà codée — vérifier doigt |
| **P2-U4** | Compact : Détails ouvert + snackbar = risque de manger la 1ʳᵉ carte (DA final-01) — smoke S26 |
| **P2-U5** | Wordmark Aide OK ; ne pas le ramener sur sticky |

---

## Priorisation recommandée (ordre UX)

1. **P0-U2** copy restore / grace (risque panique + perte)  
2. **P0-U1** découvrir le code de copie sans notice orale  
3. **P0-U3** quieting carte Accueil  
4. **P1-U3** / **P1-U2** pont paiement → FAIT  
5. **P1-U4** Aide scannable  
6. P2 nav / smoke densités  

---

## Preuves croisées (documents)

| Document | Claim | Statut ici |
|----------|-------|------------|
| `PROGRESS_LOG.md` | 6.91 ; smoke S26 / restore doigt ouverts ; pas « 9/10 dans la main » | DÉCLARÉ (inspection journal) |
| `NOTICE_FAMILLE_2MIN.md` | Brief oral code / `.mmc` / contact Menu | VÉRIFIÉ (doc lue) |
| `CHARTE_ET_ARCHITECTURE.md` | Parcours sticky / FAIT / Aide hors Menu | VÉRIFIÉ (extraits lus) |
| `docs/audits/2026-10-26-final-01-affichage.md` | DA sticky/cartes P0 | DÉCLARÉ (audit pair, non rejoué DA) |

---

## Note /10 — détail

| Critère | Note | Commentaire |
|---------|-----:|-------------|
| Clarté Accueil (Reste) | 8,0 | Hero OK ; sous-lignes concurrentes |
| Geste FAIT | 8,5 | Meilleur moment du parcours |
| Paiement BE (+++ / QR) | 7,5 | Solide local ; friction banque externe |
| Sauvegarde / code | 6,5 | Contenu oui ; trouvabilité + jargon |
| Aide / récupération erreur | 7,0 | Bon ton ; trop long / trop loin |
| Charge cognitive globale | 6,5 | OK facture simple ; pic restore |
| One UI / pouce (code) | 8,0 | Contrats tenus ; doigt ouvert |
| **Moyenne pondérée parcours** | **7,4** | |

---

## Domaine couvert

**UX parcours** Accueil → FAIT → paiement (Copier) → sauvegarde/restore code → Aide — copy, charge cognitive, One UI (inspection code 6.91).

## Limites (reste NON VÉRIFIÉ)

- Smoke manuel S26 (sticky, FAIT, Copier, sheets, densités, fontScale)  
- Restore `.mmc` + mauvais code + grace cleartext **au doigt**  
- Intent partage WhatsApp / Mail réel  
- QuietLink support mail réel  
- Validation fonctionnelle de tout use case (toggle, clipboard, SAF)  
- Domaines non audités : DA pur, sécu crypto, CI, widget host, notifs Device Care  

**Aucune modification produit / code.** Seul livrable : ce fichier d’audit (création MD autorisée par Athalia).
