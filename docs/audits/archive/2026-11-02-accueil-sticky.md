# Audit — Accueil sticky (espace liste / densité)

| Champ | Valeur |
|-------|--------|
| Date | 2026-11-02 |
| Branche | `develop` @ ~`0.1.0-7.05` |
| Domaine | Écran Accueil — sticky + liste factures (pas Menu/Réglages) |
| Déclencheur | Smoke Athalia — bannière trop informative, **cache presque toutes les factures** |
| Nature | Inspection code Compose · estimation hauteur · **pas** smoke doigt Athalia |

---

## 1. Inventaire fonctions (avant)

### Sticky (`HomeStickyHeader`)

| Rang | Élément | Rôle | Toujours visible ? |
|------|---------|------|-------------------|
| 1 | ‹ **mois** › (tap libellé → calendrier) | Navigation mois | Oui · cibles 48 dp |
| 2 | Titre « Reste après factures » (+ variantes déficit / aperçu) | Contexte du chiffre | Oui |
| 3 | **Montant Reste** (serif `displayLarge` 34/40 sp) | Signal n°1 | Oui |
| 4 | Spacer 8–12 dp | Air sous hero | Oui |
| 5 | **Factures ouvertes** / Tout est payé / Prévu | Statut 1 ligne | Si factures |
| 6 | **Courses** `StickyLinkRow` (≥48) → sheet | Divers du mois | Hors aperçu futur |
| 7 | **Mettre de côté** `StickyLinkRow` (≥48) → sheet | Forfait mensuel | Hors futur ; **compact ≤800 dp** → derrière **Détails** |
| 8 | Tip aperçu futur (warn) | Contexte lecture seule | Si mois futur |
| 9 | **Détails / Masquer** | Disclosure | Si extras |
| 9a | Après les FAIT | Info secondaire | Derrière Détails |
| 9b | Tip « Ajoutez un revenu » | Déficit sans revenu | Derrière Détails |
| 9c | Warning échéances hors mois → Réglages | Alerte | Derrière Détails |
| 9d | Courriers sans plan → Dossiers | Alerte | Derrière Détails |

Pas de logo / wordmark (charte). Sheets Courses / épargne / calendrier / Menu inchangés dans l’esprit.

### Liste + socle (hors sticky hauteur)

| Zone | Contenu | Note |
|------|---------|------|
| Snackbar sous sticky | FAIT / Annuler | Compact papier |
| LazyColumn cartes | Revenus / factures · FAIT forêt · pastilles · Copier/Photo QuietLink | Déjà quiet — **ne pas regrossir** |
| Socle | Menu · + Ajouter (ou Mois en cours en futur) | Esprit 7.04 / charte OK |

---

## 2. Hauteur approximative (sticky plié, hors Détails ouverts)

Hypothèses : `DisplayFit` tall (900 dp) · `sectionGap` 10 · `stickyPaddingVertical` 16 · Courses + épargne visibles · 1 ligne statut · pas d’extras → pas de bouton Détails.

| Bloc | ~dp |
|------|-----|
| Padding V (16×2) | 32 |
| Rangée mois (min 48) | 48 |
| Gaps `spacedBy` (≈5–6) | 50–60 |
| Titre Reste | ~20 |
| Montant ~40 sp | ~48 |
| Spacer hero | 12 |
| Factures ouvertes | ~24 |
| Courses row | 48 |
| Mettre de côté row | 48 |
| Divider | 1 |
| **Total plié typique** | **~330–360 dp** |

Sur S25 Ultra (~900+ dp utiles) : sticky ≈ **35–40 %** avant socle → **1–2 cartes** visibles.  
Sur S26 / compact (≤800) : paddings un peu plus serrés + épargne derrière Détails → encore **~280–320 dp** si Courses + statut + hero — **P0 confirmé**.

---

## 3. Priorités

### P0 — Espace liste (sticky trop haute)

| | |
|--|--|
| **Preuve** | Empilement vertical : 2× `StickyLinkRow` 48 dp + Spacer + `sectionGap` généreux + padding 16. Smoke : « cache presque toutes les factures ». |
| **Impact** | Accueil = ledger ; si la liste disparaît, le produit rate. |
| **Fix** | Réduire fortement la hauteur pliée : hero Reste dominant ; **Courses + Mettre de côté en une rangée secondaire compacte** (2 cellules ≥48 dp) ; resserrer padding / gaps ; retirer Spacer hero. |

### P1 — Clarté / polish carnet

| | |
|--|--|
| **Preuve** | Courses affiche « · il reste Y € » en sticky (long) ; double hero perçu (Reste + deux lignes outil pleine largeur). |
| **Fix** | Libellés courts en rangée outil ; détail solde dans sheet / TalkBack. Détails = uniquement secondaires (Après FAIT, alertes). Cartes liste **inchangées** (quiet). |

### P2 — Gains possibles (sans mini-dashboard)

| Idée | Garde ? | Pourquoi |
|------|---------|----------|
| Sticky collapsible 1 ligne Reste+mois | Non (défaut) | Perd le hero Reste = signal n°1 charte |
| Détails en **sheet** dédiée | Non ce tour | Disclosure inline suffit ; sheet = geste de plus |
| Rangée secondaire compacte Courses \| épargne | **Oui** | −1 rangée 48 + gaps ; les deux restent ≥48 |
| Épargne toujours dans la rangée (même ≤800) | **Oui** | Plus besoin de la cacher derrière Détails |
| QuietLink « Mois en cours » si mois passé (dans Détails) | **Oui** | Raccourci 60+ discret ; déjà dans Menu / futur=socle |

---

## 4. Contraintes charte (non négociables)

- Carnet papier belge · tokens PageTitle / SectionTitle / HubRow / QuietLink  
- Pas purple · pas logo sur sticky · FAIT forêt · anti-dashboard Copilot/Monarch  
- `A11yTokens` cibles **48 dp** · Reste serif seul chiffre  
- Menu / Réglages esprit 7.04 inchangé  
- Capacités actuelles **préservées** (mois, Reste, statut, Courses sheet, épargne sheet, Détails, alertes, liste, FAIT, socle)

---

## 5. Cible après fix (contrat)

| Sticky plié | Contenu |
|-------------|---------|
| Rangée 1 | ‹ mois › |
| Rangée 2–3 | Titre + **Reste** (hero) |
| Rangée 4 | Factures ouvertes / Tout est payé *(si applicable)* |
| Rangée 5 | **Courses** \| **Mettre de côté** (1 ligne, 2 cibles) |
| Optionnel | Tip aperçu futur · **Détails** (Après FAIT, tips, courriers, Mois en cours si passé) |

Hauteur pliée visée : **~180–220 dp** tall / **~160–200** compact (−≈40 %). Liste : plusieurs cartes dès l’ouverture.

---

## 6. Après fix (7.06) — résumé

| | Avant | Après |
|--|-------|-------|
| Outils | 2× `StickyLinkRow` empilées (+ épargne parfois dans Détails) | **1 rangée** Courses \| Mettre de côté |
| Padding V / gap | 12–16 / 8–10 | **8–10 / 4–6** |
| Spacer hero | 8–12 dp | **retiré** |
| Gain | — | QuietLink **Mois en cours** dans Détails si mois passé |
| Capacités | mois, Reste, statut, Courses, épargne, Détails, alertes | **toutes préservées** (+ raccourci mois) |
