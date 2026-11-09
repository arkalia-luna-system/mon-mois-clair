# Plan dettes / clarté / photo / mail — synthèse audits

| | |
|---|---|
| **Date** | 2026-11-09 |
| **Branche** | `develop` |
| **Nature** | Synthèse d’inspections lecture seule (3 audits) |
| **Smoke device** | Non — runtime **NON VÉRIFIÉ** |
| **Modifications code** | Implémentées en **7.35** (Athalia ok) — voir matrice `2026-11-09-matrice-audits-7.35.md` |

**Sources :**

- A — Accords / mensualités / retards — agent `f56802c3`
- B — Photo facture + Outlook — agent `ae0bb075`
- C — Données / signaux — agent `a93fd624`

---

## A. Accords / mensualités / retards

### Métier déjà là (VÉRIFIÉ — inspection)

- `pacePhrase` : « Encore environ N mois… » + « Vers {mois année} » + reste €.
- Surfaces : **fiche** + **Mes dossiers** (+ résumé partage).
- **Volontairement PAS Accueil** (charte / PROGRESS 6.37 : rythme jamais sur l’accueil ; tip « Reste dû des accords — pas sur l’accueil »).
- Accueil = pastille **Accord** (+ cause en confort) ; sticky = reste du **mois** + courriers sans plan.

Deux notions distinctes :

1. **En retard** = échéance du mois passée, pas FAIT.
2. **Accord** = dette étalée avec reste dû.

### Problème réel

Pas un bug de calcul de poche → **découverte + copy** (Retard vs Accord) + surface Accueil.

### Findings → amélioration proposée

| ID | Sévérité | Finding | Amélioration proposée |
|----|----------|---------|----------------------|
| A-P0-1 | P0 | Mensualités / dernière échéance invisibles sur Accueil | Micro-ligne sous pastille Accord **ou** sticky « N accords · Voir le rythme » (sans forcément les € si charte) |
| A-P0-2 | P0 | Menu → Dossiers = 2 taps ; sticky n’ouvre Dossiers que pour courriers | Entrée Accueil permanente dès ≥1 accord ouvert |
| A-P1-3 | P1 | Pas de KPI multi-dettes ; `monthTotalCents` non affiché | Ligne douce en tête Dossiers (« 3 accords · X € ce mois ») |
| A-P1-4 | P1 | `dueThisMonth` calculé mais mort sur cartes Dossiers | Pastille « Ce mois » / « Pas ce mois » |
| A-P1-5 | P1 | Pas d’agrégat « dossiers en retard » | Compter accords dont la ligne du mois est overdue |
| A-P1-6 | P1 | Titre « Retard ou accord » collisionne avec « En retard » | Renommer → « Plan de remboursement » / « Accord de paiement » (fiche + Aide) |
| A-P1-7 | P1 | « Encore environ N mois » / « Vers… » trop flou | « Environ N mensualités restantes » + « Dernière estimée : mois année » |
| A-P2-8 | P2 | Compact : cause masquée → pastille Accord seule | Chip courte « encore ~N » même en compact |
| A-P2-9 | P2 | `pacePhrase` ellipsis 3 lignes sur Dossiers | 2 lignes prioritaires (N + dernière) |

### « Athalia croyait que c’était fait »

| Claim | Verdict |
|-------|---------|
| Calcul reste + dernière tranche | **Fait domaine** |
| Phrase « encore N » + mois de fin | **Fait** depuis 6.37 |
| Visible Accueil sans chercher | **Pas fait** — exclu par design |
| Retard ≠ Accord | **Partiel** — hints OK ; titre ambigu |
| Vue multi-dossiers évidente | **Partiel** — liste oui ; totaux / sticky non |

→ **Fait domaine / pas visible Accueil.**

---

## B. Photo facture + Outlook

### Constats (VÉRIFIÉ — inspection)

| Sujet | Constat |
|-------|---------|
| Photo facture | `TakePicture` via FileProvider — **sans** `RequestPermission`, **sans** message si `ok == false` |
| Scan QR | Demande `CAMERA`, rationale, refus permanent → réglages — **modèle à suivre** |
| Courrier sans plan | Même famille TakePicture silencieuse ; message seulement si décodage échoue |
| Onboarding | Notifications / alarmes / batterie — **pas** caméra |
| Support « Un souci » | `mailto` + `ACTION_SENDTO` + `createChooser` |
| Manifest | **Aucun** `<queries>` ; `targetSdk = 36` → risque chooser vide |
| Fallback mail | Pas d’`ACTION_SEND`, pas de copie adresse, pas de ciblage Outlook |

### Améliorations proposées

1. Avant `TakePicture` (facture + courrier) : aligner sur `ScanEpcScreen` — check + `RequestPermission` + rationale + deep-link réglages si refus permanent.
2. Si `ok == false` après permission : message court (« Caméra non ouverte. Vérifiez l’autorisation. »).
3. Manifest : `<queries>` pour `SENDTO` + scheme `mailto` (évent. `SEND` + `message/rfc822`).
4. Échec mail : message actionnable + **copier** `arkalia.luna.system@gmail.com` + fallback `ACTION_SEND`.
5. Ne pas hardcoder Outlook — chooser système après `<queries>`.
6. Demander la caméra **au geste** photo/scan (pas forcément à l’onboarding).

**Classement :** trous **app** (UX permission + package visibility) ; déclencheur possible **OS/OEM** — runtime **NON VÉRIFIÉ**.

---

## C. Données / signaux

### Garde-fous présents (VÉRIFIÉ — inspection)

- Room v8 ; migrations 1→8 ; **pas** de `fallbackToDestructiveMigration`.
- SQL migrations = `ALTER` / `CREATE` / backfill — pas de `DROP TABLE` utilisateur.
- Restore : validation schema + checksum **avant** mutation ; échec → DB inchangée.
- Restore réussie = remplacement **transactionnel** volontaire (`clearAllTables` + insert).
- Reste dû **calculé** (jamais stocké comme solde secondaire).

### Signaux manquants (soft)

| Info | Aujourd’hui |
|------|-------------|
| Cause (`causeLabel`) | Optionnelle — silencieux |
| Photo facture | Optionnelle — silencieux |
| IBAN / e-mail | Optionnels — e-mail bloqué seulement à l’action « Écrire » |
| Erreurs fiche | Bannière globale, pas `isError` par champ |
| Homonymes créanciers | Aucun signal collision |

### Principes non négociables (futures fixes)

1. **Ne pas wipe** données déjà saisies pour « nettoyer » l’UX.
2. Migrations **additives** seulement (pas de destructive fallback).
3. Restore = seul remplacement total voulu ; mauvais `.mmc` → DB intacte.
4. Info manquante → **signaler** (soft-warn), pas supprimer.
5. Pas inventer un second solde stocké qui divergerait de `DossierRemaining`.
6. Si un jour « Personne » : migration additive + IDs stables dans `.mmc`.

---

## D. Plan priorisé

| ID | Domaine | Action | Impact données | Niveau suivant (amélioration) |
|----|---------|--------|----------------|-------------------------------|
| P0-A1 | Accords UX | Exposer rythme Accueil (chip ou sticky « Voir le rythme ») | Aucun | Charte : rester sans reste € Accueil si souhaité |
| P0-A2 | Accords UX | Raccourci Accueil → Dossiers si ≥1 accord | Aucun | — |
| P0-B1 | Photo | `RequestPermission` + rationale + message échec (facture/courrier) | Aucun | Soft-warn « photo manquante » (P1) |
| P0-B2 | Mail | `<queries>` mailto/SENDTO + fallback + copier adresse | Aucun | Align message créancier / support |
| P1-A3 | Accords UX | KPI douce Mes dossiers + pastille « Ce mois » | Aucun | Agrégat retards accords |
| P1-A6 | Copy | Renommer « Retard ou accord » | Aucun | Aide / empty states |
| P1-A7 | Copy | `pacePhrase` plus direct (mensualités + dernière estimée) | Aucun | Compact chip ~N |
| P1-C1 | Signaux | Soft-warn cause / photo / IBAN-mail manquants | Aucun (signal seulement) | Option hard-block Athalia |
| P1-C2 | Signaux | Ancrer erreurs au champ (`isError`) | Aucun | — |
| P2-A8 | Densité | Chip « encore ~N » en compact Accueil | Aucun | — |
| P2-A9 | Densité | Hiérarchie 2 lignes `pacePhrase` Dossiers | Aucun | — |
| P2-C3 | Données | Couverture tests migration 1→N (process) | Aucun wipe | Avant toute v9 |
| P2-C4 | Modèle | Table Personne (si besoin multi-liens) | Migration additive + `.mmc` | Décision Athalia d’abord |

---

## E. Décisions Athalia

Athalia **ok** 2026-11-09 → fixes **7.35**. Cases :

- [x] Afficher le rythme (`pacePhrase` / « encore ~N ») sur **Accueil** ? → chip + sticky (sans €)
- [x] Afficher le **reste €** sur Accueil, ou seulement le rythme (charte actuelle) ? → **rythme seul**
- [x] Entrée permanente Accueil → **Mes dossiers** dès ≥1 accord ? → sticky Détails
- [x] Renommer **« Retard ou accord »** → « Plan de remboursement » / « Accord de paiement » ?
- [x] Reformuler `pacePhrase` (« N mensualités » + « Dernière estimée ») ?
- [x] KPI douce en tête de **Mes dossiers** (DOS02 aujourd’hui = non) ?
- [x] Photo facture : **soft-warn** si absente ? ou hard-block ? → **soft-warn**
- [x] Cause : soft-warn si accord sans cause ?
- [x] IBAN / e-mail : soft-warn sur fiche accord ?
- [x] Ajouter `<queries>` Manifest pour **mailto** + fallback envoi ?
- [x] Caméra : permission au geste (recommandé) vs onboarding ? → **au geste**
- [ ] Entité **Personne** (multi-dossiers liés) — maintenant / plus tard / non ? → **plus tard**

---

## F. Limites

- Nature = **inspection code/docs** uniquement.
- **Pas de smoke** téléphone / Samsung / Outlook réel.
- Runtime comportement OEM = **NON VÉRIFIÉ**.
- Tests unitaires lus = **DÉCLARÉ** (fichiers), non réexécutés dans ces audits.
- Aucune correction code dans cette mission — **MD seul**.

---

*Livrable docs — 2026-11-09. Fixes code : attendre « oui » explicite Athalia.*
