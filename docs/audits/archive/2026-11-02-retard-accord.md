# Audit — Retard / Accord

| Champ | Valeur |
|-------|--------|
| Date | 2026-11-02 |
| Branche | `develop` @ ~`0.1.0-7.02` |
| Domaine | Pastille « En retard » · section « Retard ou accord » · Mes dossiers |
| Déclencheur | Smoke Athalia — impression légère, débutante, fouillis |
| Nature | Inspection code + dump UI device (SM-S938B) · **pas** smoke doigt Athalia |

---

## 1. Parcours UI exact

| Entrée | Chemin | Ce que voit l’utilisatrice |
|--------|--------|----------------------------|
| **Accueil** | Ligne facture TODO, jour passé | Pastille brique **« En retard »** + lavis ; si accord : pastille **« Accord »** (confort) ou chip titre (compact) |
| **Accueil sticky** | Détails / bandeau | « N courrier(s) sans plan · Voir » → Dossiers |
| **Menu** | si N>0 accord ou courrier | « Dossiers (N) » |
| **Fiche** | Ajout / édition dépense | Bloc repliable **« Retard ou accord »** après Paiement ; hint « Huissier, hôpital… » |
| **Mes dossiers** | Liste reste dû | En cours / Soldés ; courriers sans plan ; résumé |
| **Aide** | Réglages → Comment ça marche | Sections séparées « En retard » et « Retard ou accord » |
| **Widget** | Ligne basse | « En retard · {libellé} · € » via `NextUnpaidLine` |
| **Notif** | OVERDUE 10 h | « En retard : {libellé} » |

Deux notions distinctes (souvent confondues à l’oreille) :

1. **En retard** = date d’échéance passée + pas FAIT (`ExpenseOverdue`).
2. **Accord** = plan de remboursement avec reste dû (`openingRemainingCents != null`).

---

## 2. Code (carte)

| Pièce | Fichier | Rôle |
|-------|---------|------|
| Règle retard | `ExpenseOverdue` | TODO + mois ≤ courant + `today > due` |
| Accueil pastilles | `HomeTimelineRows.ExpenseStatusLine` | max 2 chips ; compact = retard/FAIT seulement |
| Timeline | `HomeViewModel` | `isOverdue` + `isAgreement` par ligne |
| Widget | `NextUnpaidLine` + `CarnetWidgetText` | 1ʳᵉ TODO du mois |
| Rappels | `ReminderSchedulerImpl` | OVERDUE si `ExpenseOverdue` |
| Reste dû | `DossierRemaining` | départ + frais − FAIT ; `pacePhrase` |
| Liste | `ListDossiersUseCase` + `DossiersScreen` | pas sur Accueil (euros) |
| Fiche bloc | `EntryFormDossierSection` + bandeau reste dans `EntryFormScaffold` | conversion / IBAN / frais |
| Aide | `HowItWorksCopy` | 2 sections |

---

## 3. Problèmes — preuves & priorités

### P0 — « Mettre de côté » affiche « En retard »

| | |
|--|--|
| **Hypothèse** | L’épargne volontaire est une dépense Room comme une facture → même règle overdue + même pastille brique. |
| **Preuve code** | `HomeViewModel` L164–168 : `ExpenseOverdue.isOverdue(...)` sans exclusion savings ; `ConfigureSavingsUseCase.LABEL = "Mettre de côté"` ; `ReminderSchedulerImpl` schedule OVERDUE pour **toutes** les TODO ; `NextUnpaidLine.pick` peut élire l’épargne. |
| **Device (VÉRIFIÉ)** | SM-S938B, APK `0.1.0-7.02` (vc 107), dump UI 2026-11-02 : ligne « Mettre de côté » / −20 € / bouton FAIT ✓ / pastille **« En retard »** (jour 1, aujourd’hui le 2). |
| **Impact** | Cri brique sur un geste doux → fouillis + angoisse débutante ; widget / notif « En retard : Mettre de côté » possibles. |

### P1 — Copy « Retard ou accord » ≠ pastille « En retard »

| | |
|--|--|
| **Preuve** | Titre fiche `EntryFormDossierSection` = « Retard ou accord » ; pastille Accueil = « En retard ». Aide a 2 sections mais la fiche hint « Huissier, hôpital, plan de paiement… » mélange intimidation + jargon. |
| **Impact** | 60+ croit que le bloc sert à marquer un retard de paiement (déjà fait par la pastille). |

### P1 — Trois libellés pour le même geste courrier

| | |
|--|--|
| **Preuve** | Fiche QuietLink : « Garder le courrier (sans montant) » ; empty Dossiers : « Ranger sans plan » ; bouton : « Ranger un courrier » ; erreur `CREDITOR_WITHOUT_OPENING_HINT` : « Ranger sans plan ». |
| **Impact** | Parcours découverte incohérent (déjà noté nuit-02 UX). |

### P1 — Densité fiche conversion (fouillis)

| | |
|--|--|
| **Preuve** | Bloc ouvert : cause + reste dû + tip + lien courrier + IBAN + e-mail d’un coup ; reste/rythme aussi au-dessus de la grille **si** déjà accord (OK) ; sous `!isAgreement` un affichage reste/Soldé **mort** (remaining seulement si déjà dossier → `isAgreement`). |
| **Impact** | Première ouverture = mur de champs ; code mort entretient la confusion. |

### P2 — Aide / pacePhrase

| | |
|--|--|
| **Aide** | Section « Retard ou accord » ne dit pas explicitement « ce n’est pas la pastille En retard ». |
| **pacePhrase** | Longue mais honnête (« Encore environ… ») — déjà ellipsis Dossiers ; pas P0. |
| **Pastilles** | En retard + Accord = 2 chips max (tenu) ; compact masque Accord en file (chip titre) — OK charte. |

---

## 4. Hors scope volontaire

- Redesign nuit / nouvelles pastilles chromatiques.
- Refonte Mes dossiers KPI.
- Changer le nom produit « Retard ou accord » (idiome déjà en Aide / tests / empty state).

---

## 5. Plan fix (P0 → P1)

1. **P0** — Exclure « Mettre de côté » de `isOverdue` Accueil, OVERDUE scheduler, et overdue widget (`NextUnpaidLine`).
2. **P1** — Hint fiche + Aide : plan de remboursement ≠ pastille En retard ; unifier copy « Garder le courrier… ».
3. **P1** — Alléger section : retirer affichage reste mort ; tip plus court ; IBAN/e-mail inchangés (pas redesign).
4. Tests unitaires + bump `0.1.0-7.03`.

---

## 6. Fixes appliqués (7.03)

| Priorité | Fix | Preuve |
|----------|-----|--------|
| P0 | `ExpenseOverdue` ignore « Mettre de côté » (Accueil / OVERDUE / widget) | `ExpenseOverdueTest` + `CarnetWidgetTextTest` |
| P1 | Hint fiche + Aide : plan ≠ pastille En retard | `EntryFormDossierSection` · `HowItWorksCopy` |
| P1 | Copy unifiée « Garder le courrier… » | `EntryFormCore` · `DossiersScreen` |
| P1 | Retrait affichage reste mort dans le bloc conversion | `EntryFormDossierSection` |

---

## Limites

Inspection + dump UI pré-fix. **NON VÉRIFIÉ** : notif OVERDUE réelle ; smoke doigt Athalia post-`0.1.0-7.03` ; présence d’un vrai accord sur l’appareil (pas dans le dump Accueil du jour).
