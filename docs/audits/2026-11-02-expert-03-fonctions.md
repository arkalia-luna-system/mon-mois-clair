# Audit expert-03 — Fonctions produit / UX (toutes)

| Champ | Valeur |
|-------|--------|
| Date | 2026-11-02 |
| Branche | `develop` @ **`0.1.0-7.06`** · `versionCode` **111** |
| Domaine | Product / UX — inventaire **toutes** fonctions carnet |
| Mode | **AUDIT ONLY** · inspection code + docs vivantes · **pas** de correction · **pas** smoke doigt Athalia dans cet appel |
| Périmètre | FAIT · Reste · Courses · épargne · retard/accord · résumé · sauvegarde · échéances · QR/EPC · notifs |
| Hors périmètre | DA pure · crypto SQLCipher détail · archi twin VM · packaging Drive |

**Chemin projet :** `/Volumes/T7/Mon Mois Clair` — **VÉRIFIÉ** (`pwd` + `versionName`).

---

## Verdict

| | |
|--|--|
| **Note fonctions (craft / couverture)** | **7,4 / 10** |
| **« Dans la main » famille** | **ouvert** — re-smoke Athalia listé dans `PROGRESS_LOG` (Accueil sticky, Aide, hub, retard/accord, résumé, S26, restore) |
| **Lecture** | Socle métier **complet** pour le carnet familial belge ; frictions 60+ encore sur **profondeur**, **double vocabulaire**, **Samsung notifs**, et **preuves doigt**. Pas un claim 9/10 boutique. |

**Phrase unique :** chaque fonction promise par README/charte **existe** et a été raffinée en 7.02–7.06 ; le produit n’est pas « manquant de features », il est **encore à prouver au doigt** et à **aplanir les parcours secondaires**.

---

## 1. Carte fonctions (état 7.06)

Légende statut : **Solide** = parcours principal clair en code · **Fragile** = friction ou dépendance appareil · **Trou** = manqué vs promesse / découverte.

| Fonction | Entrée principale | Statut | Gains récents (code/doc) | Friction / manque |
|----------|-------------------|--------|--------------------------|-------------------|
| **Reste** | Sticky Accueil (serif hero) | Solide | Sticky **basse** 7.06 ; Factures ouvertes ; Détails = Après FAIT | Confusion solde banque ≠ Reste (Aide le dit — découverte = lecture) |
| **FAIT ✓** | Carte liste (≥96 dp forêt) | Solide | Snackbar « Noté FAIT » + Annuler ; tip prélèvement carte ; pastille JJ/MM | Mois passé = dialogue ; pont banque→FAIT = snackbar seulement |
| **Courses** | Sticky rangée | Solide | Cellule compacte 7.06 ; sheet unique ; Reste inchangé | Sticky = libellé court ; « il reste » seulement sheet / TalkBack |
| **Mettre de côté** | Sticky + Réglages « Jour… » | Solide | Plus jamais **En retard** (7.03) ; hub « Jour du… » (7.04) | Split montant (Accueil) / jour (Réglages) = 2 taps ; FAIT = ligne ledger |
| **En retard** | Pastille + notif OVERDUE | Solide* | Exclusion épargne **VÉRIFIÉE** (`ExpenseOverdue`) | *Samsung exact alarm / Device Care = Fragile doigt |
| **Accord / Dossiers** | Fiche « Retard ou accord » · Menu Dossiers | Fragile UX | Copy hint ≠ pastille ; « Garder le courrier… » unifié (7.03) | Bloc ouvert encore dense ; vocabulaire double (retard vs accord) |
| **Résumé** | Réglages Données + Dossiers | Solide* | Boutons branchés ; Reste/À payer/FAIT ; purge TTL share (7.02) | *PDF live = re-smoke Athalia ; mois = calendrier ≠ mois Accueil |
| **Sauvegarde** | Hub « Code et copie » · Données | Solide* | 3 étapes 60+ ; code papier ; danger Restaurer à part | *Restore E2E ouvert ; ops « code ≠ même fil que `.mmc` » |
| **Échéances hors mois** | Données (plus Confort) | Solide | Déplacé Données 7.04 ; tip Accueil Détails | Empty « Aucune… » faible ; chemin 3 taps |
| **QR / EPC / +++** | Fiche « Scanner… » · Copier · OCR photo | Solide* | DraftController EPC/OCR ; `COPIED_OK` banque→FAIT ; OCR jamais montant | *Caméra / One UI ; scan hors Accueil ; OCR rate NON VÉRIFIÉ |
| **Notifs / Alertes** | J-3 / J / OVERDUE · hub Confort | Fragile | Canal + largeIcon ; mask bio ; santé rappels | Exact alarms + POST_NOTIFICATIONS + Device Care = P0 doigt |

---

## 2. Détail par fonction

### 2.1 Reste après factures

| | |
|--|--|
| **Rôle** | Signal n°1 : revenus − **toutes** factures du mois (FAIT ou non). |
| **Preuve** | `HomeStickyHeader` + charte § Accueil ; Aide « Le chiffre en haut ». |
| **Gains** | Hero dominant ; sticky moins haute (7.06) → liste visible = Reste **utile**. |
| **Friction** | Utilisatrice peut lire « solde » ; report auto volontairement absent. |
| **Statut preuve** | **VÉRIFIÉ** (inspection UI) · comportement doigt **NON VÉRIFIÉ** (re-smoke Accueil). |

### 2.2 FAIT ✓

| | |
|--|--|
| **Rôle** | Check-off : À payer baisse ; pastille « FAIT le JJ/MM ». |
| **Preuve** | `HomeTimelineRows` bouton forêt ; snackbar Annuler ; tip prélèvement muted. |
| **Gains** | Geste fort, cohérent charte ; copy Aide claire. |
| **Friction** | Après Copier communication → snackbar guide banque→FAIT, **pas** de deep-link banque (volontaire) ; mois passé = friction cognitive OK mais un tap de plus. |
| **Priorité restante** | P2 polish snackbar durée / concurrence Copié vs Annuler (déjà partiellement géré). |

### 2.3 Courses

| | |
|--|--|
| **Rôle** | Divers du mois ; n’altère pas le gros Reste. |
| **Preuve** | `StickyToolsRow` + `HomeCoursesSheet` ; Aide alignée 7.06. |
| **Gains** | Une rangée avec épargne = moins de scroll perdu. |
| **Friction / manque** | Sur sticky, plus de « Courses X € · il reste Y » en clair (compact) → risque « je ne vois pas mon solde courses » sans ouvrir ›. |
| **Priorité** | **P1** découverte « il reste » (micro-copy sticky ou première ligne sheet toujours visible). |

### 2.4 Épargne (« Mettre de côté »)

| | |
|--|--|
| **Rôle** | Forfait mensuel = virement volontaire, pas cagnotte in-app. |
| **Preuve** | `ConfigureSavingsUseCase` ; `ExpenseOverdue.VOLUNTARY_SAVINGS_LABEL` ; hub `SAVINGS_ROW`. |
| **Gains** | P0 angoisse « En retard » **fermé en code** (7.03). |
| **Friction** | Toujours une **facture** Room (FAIT = virement) — correct métier, surprenant si on attend une tirelire. Split Accueil/Réglages mitigé par libellé « Jour du… ». |
| **Priorité** | **P1** smoke : pastille absente + notif absente ; **P2** empty / retrait UX. |

### 2.5 Retard vs Accord

| | |
|--|--|
| **Rôle** | Retard = échéance passée + TODO. Accord = plan reste dû (Dossiers). |
| **Preuve** | Audits `2026-11-02-retard-accord.md` + fixes 7.03 ; fiche hint **VÉRIFIÉ**. |
| **Gains** | Séparation copy Aide + fiche. |
| **Friction** | Titre section « Retard **ou** accord » mélange encore les deux mots ; accordion densité (IBAN / mail / frais / courrier). Empty Dossiers / conversion : parcours découverte long. |
| **Priorité** | **P1** densité 1ʳᵉ ouverture ; **P2** titre section (« Plan de remboursement » ?). |

### 2.6 Résumé (texte / PDF)

| | |
|--|--|
| **Rôle** | Médiation créancier / conseiller — clair volontaire ≠ `.mmc`. |
| **Preuve** | `SettingsSummaryScreen` boutons + `SettingsShareEffect` ; `PrepareMonthSummaryUseCase` Reste/À payer/FAIT ; `purgeStale` ON_STOP. |
| **Gains** | Cul-de-sac Réglages + wipe FileProvider **corrigés en code** (7.02) — audit résumé antérieur = historique. |
| **Friction** | Mois = `AppClock.currentYearMonth()` (**ignore** mois sélectionné Accueil) ; TTL 2 min ; PDF clair = éducation confiance. |
| **Priorité** | **P0** re-smoke Athalia PDF/texte Dossiers+Réglages ; **P1** aligner mois résumé ↔ Accueil si besoin métier ; **P2** watermark / bio pendant share. |

### 2.7 Sauvegarde

| | |
|--|--|
| **Rôle** | SAF + code papier + partage `.mmc` ; restore danger. |
| **Preuve** | `SettingsBackupPage` 3 étapes ; raccourci hub « Code et copie ». |
| **Gains** | Parcours 60+ structuré ; Aide Sauvegarde en tête. |
| **Friction / manque** | Restore E2E **ouvert** (PROGRESS / clean-10) ; risque humain code+fichier même message = **P0 ops** (pas bug UI). Profondeur Menu→Réglages→… encore 2–3 taps. |
| **Priorité** | **P0** restore doigt ; **P1** brief oral NOTICE. |

### 2.8 Échéances hors ce mois

| | |
|--|--|
| **Rôle** | Assurances / taxes hors mois courant ; réapparaissent le bon mois. |
| **Preuve** | `SettingsDeadlinesScreen` sous Données (7.04) ; tip Détails Accueil. |
| **Gains** | Plus sous Confort (fouillis débutante). |
| **Friction** | Empty faible ; pas d’ajout depuis cet écran (édition template seulement) ; découverte via Aide / Détails. |
| **Priorité** | **P1** empty + pourquoi ; **P2** raccourci depuis « À venir ». |

### 2.9 QR EPC / communication / OCR

| | |
|--|--|
| **Rôle** | Remplir IBAN / montant / +++ sans inventer un euro OCR. |
| **Preuve** | `ScanEpcScreen` ; `EntryFormPaymentSection` ; `BelgianStructuredCommunication` ; OCR photo `+++` only. |
| **Gains** | Pont Copier → « collez à la banque, puis FAIT » ; politique dossiers/prélèvements. |
| **Friction** | Scan **dans** fiche (pas Accueil) ; permission caméra One UI ; échec OCR = message, pas magic ; PDF/photo lecteur externe pour PDF. |
| **Priorité** | **P1** smoke scan+copie S25/S26 ; **P2** rate OCR terrain. |

### 2.10 Notifications / Alertes

| | |
|--|--|
| **Rôle** | J-3 10 h · jour 9 h · OVERDUE 10 h ; hub Santé. |
| **Preuve** | `ReminderSchedulerImpl` ; `ReminderNotifCopy` ; `ReminderHealthChecker` ; Menu badge `!`. |
| **Gains** | Masquage montant/libellé si bio ; icône notif soignée (historique 6.91). |
| **Friction** | Exact alarms refusées = silence total ; Device Care Samsung **NON VÉRIFIÉ** ce tour ; OVERDUE quotidien peut fatiguer. |
| **Priorité** | **P0** rescan Device Care + permissions live ; **P1** copy Aide déjà OK — tenir le hub Alertes visible. |

---

## 3. Priorités consolidées (P0 / P1 / P2)

### P0 — bloquants « cadeau / dans la main » (pas des features manquantes)

| Id | Constat | Nature |
|----|---------|--------|
| **P0-SMOKE-ACCUEIL** | Re-smoke sticky 7.06 (liste visible · Courses\|épargne · Menu) | DÉCLARÉ journal · doigt **NON VÉRIFIÉ** ici |
| **P0-SMOKE-RESUME** | PDF/texte Dossiers + Réglages réellement reçus | Code OK (inspection) · doigt **NON VÉRIFIÉ** |
| **P0-RESTORE** | Restore `.mmc` + code papier bon/mauvais | Ouvert docs · **NON VÉRIFIÉ** |
| **P0-NOTIF-OEM** | Exact alarm + notifs + Device Care S25/S26 | Checker code **VÉRIFIÉ** · OEM **NON VÉRIFIÉ** |
| **P0-S26** | Grille jour 31 · sticky compact · fontScale | Cible charte · **NON VÉRIFIÉ** |

### P1 — gains UX nets (après smoke)

| Id | Constat |
|----|---------|
| **P1-COURSES-RESTE** | Solde « il reste » trop caché en sticky compact |
| **P1-ACCORD-DENSITE** | Première ouverture « Retard ou accord » encore mur de champs |
| **P1-RESUME-MOIS** | Résumé = mois calendaire ≠ mois affiché Accueil |
| **P1-ECHEANCES-EMPTY** | Empty + découverte encore faibles |
| **P1-EPC-DOIGT** | Scan QR + Copier + FAIT enchaînés sur device réel |
| **P1-AIDE-SMOKE** | Balayage Aide (Résumé / Sauvegarde / Retard·Accord) Athalia |

### P2 — polish / dette douce

| Id | Constat |
|----|---------|
| **P2-TITRE-ACCORD** | « Retard ou accord » vs pastille « En retard » — encore homonymie |
| **P2-HELP-ROUTE** | `SettingsHelpScreen` quasi mort (hub → HowItWorks direct) |
| **P2-BIO-NOTIF** | Libellé « Paiement » si bio — utilité ↓ |
| **P2-WIDGET** | Limites hors process documentées — pas enrichir |
| **P2-OCR-RATE** | Taux échec +++ terrain inconnu |

---

## 4. Manques vs hors scope volontaire

| Attendu marché / débutante | Dans MMC 7.06 ? | Décision |
|----------------------------|-----------------|----------|
| Solde bancaire live | Non | Hors scope (charte) |
| Catégories / graphiques | Non | Hors scope |
| Synchro cloud | Non | Local-first |
| Tirelire visuelle épargne | Non — ligne FAIT | Volontaire |
| Scan QR depuis Accueil | Non — via fiche | OK métier ; friction découverte |
| Deep-link app banque | Non | Volontaire |
| Widget multi-lignes / notif rich actions | Minimal | Volontaire |

**Manque produit réel (pas hors scope) :** preuves doigt + restore + OEM notifs — pas une 12ᵉ feature.

---

## 5. Gains à préserver (ne pas casser)

1. Sticky **basse** + rangée Courses\|épargne (7.06).
2. FAIT forêt + Annuler + tip prélèvement.
3. Épargne **hors** En retard (pastille / widget / OVERDUE).
4. Résumé branché Réglages + contenu Reste/À payer/FAIT + purge TTL (pas wipe total share).
5. Hub Données : échéances métier · « Jour du Mettre de côté » · Code et copie.
6. Copier → message banque puis FAIT ; OCR jamais montant.
7. Aide ordonnée FAIT → Sauvegarde → … → Résumé.

---

## 6. Note /10 — grille honnête

| Axe | Note | Commentaire |
|-----|-----:|-------------|
| Couverture fonctions promises | **8,5** | README/charte : tout présent en code |
| Clarté 60+ / vocabulaire | **7,0** | Retard/accord + split épargne encore coûteux |
| Parcours critiques (FAIT, Reste, Copier) | **8,0** | Forts ; secondaires denses |
| Fiabilité perçue (sauvegarde, PDF, notifs) | **6,5** | Code soigné ; doigt/OEM ouverts |
| Découvrabilité (Aide, hub, sticky) | **7,5** | 7.04–7.06 ont beaucoup aidé |
| **Synthèse fonctions** | **7,4** | Moyenne pondérée craft — **pas** note « main » |

Plafond volontaire : sans smoke Athalia P0, **pas** de note ≥ 8,5 globale « produit dans la main ».

---

## 7. Preuves & limites de cet audit

| Constat type | Statut | Nature | Preuve |
|--------------|--------|--------|--------|
| `versionName` 0.1.0-7.06 / vc 111 | **VÉRIFIÉ** | inspection | `app/build.gradle.kts` |
| StickyToolsRow Courses\|épargne | **VÉRIFIÉ** | inspection | `HomeStickyChrome.kt`, `HomeStickyHeader.kt` |
| Exclusion overdue épargne | **VÉRIFIÉ** | inspection | `ExpenseOverdue.kt` |
| Résumé UI Réglages branchée | **VÉRIFIÉ** | inspection | `SettingsComfortPages.kt` `SettingsSummaryScreen` |
| Contenu résumé Reste/À payer/FAIT | **VÉRIFIÉ** | inspection | `PrepareMonthSummaryUseCase.kt` |
| Sauvegarde 3 étapes | **VÉRIFIÉ** | inspection | `SettingsBackupPage.kt` |
| Échéances sous Données | **VÉRIFIÉ** | inspection | `SettingsScreen.kt` + `SettingsHubCopy` |
| Copier→banque→FAIT | **VÉRIFIÉ** | inspection | `BelgianStructuredCommunication.COPIED_OK` |
| Fixes 7.02–7.06 « smoke Athalia » | **DÉCLARÉ** | inspection journal | `PROGRESS_LOG.md` · audits 2026-11-02-* |
| Comportement réel device / PDF / notifs / restore | **NON VÉRIFIÉ** | — | non exécuté (pas de smoke / pas de tests UI ce tour) |
| Gradle tests 411/412 | **DÉCLARÉ** | inspection README/journal | non réexécuté ici |

**Domaine couvert :** Product / UX — inventaire fonctions listées.  
**Non audité :** DA tokens, sécurité crypto, architecture god-files, packaging distribution.  
**Limites :** inspection ≠ validation fonctionnelle ; audits du jour = historiques pour les fixes déjà mergés.

aucune modification faite *(hors rédaction de ce fichier d’audit demandé par OUI Athalia)*
